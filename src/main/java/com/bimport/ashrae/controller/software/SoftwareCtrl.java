package com.bimport.ashrae.controller.software;

import com.bimport.ashrae.common.Exception.ConflictException;
import com.bimport.ashrae.common.FileUtil;
import com.bimport.ashrae.common.ImageUtil;
import com.bimport.ashrae.common.ServletUtil;
import com.bimport.ashrae.common.auzreFile.AzureFileUploader;
import com.bimport.ashrae.common.auzreFile.LinkBuilder;
import com.bimport.ashrae.mySQL.software.SoftwareDAO;
import com.bimport.ashrae.mySQL.software.model.*;
import com.bimport.ashrae.mySQL.software.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping("/api")
public class SoftwareCtrl {
    @Autowired
    SoftwareDAO softwareDAO;
    @Autowired
    ServletUtil servletUtil;
    @Autowired
    AzureFileUploader azureFileUploader;

    @Autowired
    FileUtil fileUtil;

    private static String testDir = "cases";
    private static String Lv1Dir = "lv1";
    private static String standardDir = "standard-cases";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int totalTests = 18;
    private String testsRepo = "gbxml-test-file";
    //todo upload image
    @RequestMapping(value = "/AddSoftware", method = RequestMethod.POST)
    public Software addSoftware(HttpServletRequest req, @RequestParam String name, @RequestParam String description, @RequestParam String version){
        String userId = (String) req.getAttribute("userId");
        Software software = new Software(name, description, version);
        software.setUserId(userId);
        return softwareDAO.saveSoftware(software);
    }

    @RequestMapping(value = "/UpdateSoftware", method = RequestMethod.PUT)
    public Software addSoftware(HttpServletRequest req, @RequestParam String name, @RequestParam String version,@RequestParam String description, String id) {
        String userId = (String) req.getAttribute("userId");
        Software old_software = softwareDAO.getSoftwareById(id);
        old_software.setName(name);
        old_software.setVersion(version);
        old_software.setDescription(description);
        return softwareDAO.updateSoftware(old_software);
    }

    @RequestMapping(value = "/GetSoftwares", method = RequestMethod.GET)
    public List<Software> getSoftwares(HttpServletRequest req){
        String userId = (String) req.getAttribute("userId");
        return softwareDAO.getSoftwares(userId);
    }

    @RequestMapping(value = "/GetSoftware", method = RequestMethod.GET)
    public Software getSoftwares(String id) {
        return softwareDAO.getSoftwareById(id);
    }

    @RequestMapping(value = "/GetCertification", method = RequestMethod.GET)
    public Certification getCertification(@RequestParam String id){
        return softwareDAO.getCertificationById(id);
    }

    @RequestMapping(value = "/PassedLevel1", method = RequestMethod.PUT)
    public Certification validateLevel1(@RequestParam String id, @RequestParam Boolean isPassed){
        Certification certification = softwareDAO.getCertificationById(id);
        Software software = softwareDAO.getSoftwareById(id);
        if(isPassed){
            certification.setIsLevel1Passed(true);
            certification.setLevel1Status(CertStatusEnum.PASSED);
            certification.setLevel1CertificationId(UUID.randomUUID().toString());
        }else{
            certification.setIsLevel1Passed(false);
            certification.setLevel1Status(CertStatusEnum.FAILED);
        }

        software.setStatus(StatusEnum.PROCESSING);
        software.setCertificationLevel(CertificationLevel.LEVEL1);
        return softwareDAO.updateCertification(software, certification);
    }
    // todo only accept gbxml how about .xml file
    @RequestMapping(value = "/uploadTestgbXMLFile", method = RequestMethod.POST)
    public String uploadTestgbxmlFile(String id, String testName, HttpServletRequest req){
        logger.info("upload test gbXML file start ...");
        List<File> files = servletUtil.readAllMultiPartFiles(req);
        if(files != null){
            System.out.println(files.size() + " : " + files.get(0).getName() );
            File file = files.get(0);
            String file_name = file.getName();
            if(file_name.contains(".gbxml")){
                azureFileUploader.upload(testsRepo, testDir, file, id + "_" + testName + ".gbxml");
                return "gbxml";
            }else if(file_name.contains(".xml")){
                azureFileUploader.upload(testsRepo, testDir, file, id + "_" + testName + ".xml");
                return "xml";
            }

        }

        return "";
    }
    /*update standard gbxml file*/
    @RequestMapping(value = "/uploadStandardgbXMLFile", method = RequestMethod.POST)
    public String uploadStandardgbxmlFile(HttpServletRequest req) {
        logger.info("upload standard gbXML file start ...");
        List<File> files = servletUtil.readAllMultiPartFiles(req);
        for(int i =0; i< files.size(); i++){
            File file = files.get(i);
            String file_name = file.getName();
            logger.info("uploaded standard test file: " + file_name);
            if (file_name.contains(".gbxml")) {
                azureFileUploader.upload(testsRepo, standardDir, file, file_name);
            }
        }
        return "success";
    }

    @RequestMapping(value = "/uploadLv1gbxmlFile", method = RequestMethod.POST)
    public String uploadLv1gbxmlFile(String id, HttpServletRequest req) {
        logger.info("upload Level test gbXML file start ...");
        List<File> files = servletUtil.readAllMultiPartFiles(req);
        Certification certification = softwareDAO.getCertificationById(id);
        if (files != null) {
            System.out.println(files.size() + " : " + files.get(0).getName());
            File file = files.get(0);
            String file_name = file.getName();
            String file_type = "";
            if (file_name.contains(".gbxml")) {
                azureFileUploader.upload(testsRepo, Lv1Dir, file, id + ".gbxml");
                file_type = "gbxml";
            } else if (file_name.contains(".xml")) {
                azureFileUploader.upload(testsRepo, Lv1Dir, file, id + ".xml");
                file_type = "xml";

            }
            certification.setLv1Type(file_type);
            softwareDAO.updateCertification(certification);
            return file_type;
        }
        return "";
    }

    @RequestMapping(value = "/GetCroppedImage", method = RequestMethod.POST)
    public String GetCroppedImage(HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart("image");
        String filename = part.getSubmittedFileName();
        File file = fileUtil.convertInputStreamToFile(part.getInputStream(), filename);

        if (!ImageUtil.cropImageToSquare(file, 500)) {
            throw new ConflictException("Parse user profile image failed");
        }
        return FileUtil.BASE64_PREFIX + fileUtil.encodeToBase64Str(file);
    }
    @RequestMapping(value = "/UploadProjectImage", method = RequestMethod.POST)
    public String UploadProjectImage(@RequestParam String id, HttpServletRequest req) throws ServletException, IOException {
        Part part = req.getPart("image");
        String filename = part.getSubmittedFileName();
        File file = fileUtil.convertInputStreamToFile(part.getInputStream(), filename);

        if (!ImageUtil.cropImageToSquare(file, 500)) {
            throw new ConflictException("Parse user profile image failed");
        }
        softwareDAO.updateProjectImage(id, file);

        return FileUtil.BASE64_PREFIX + fileUtil.encodeToBase64Str(file);
    }

    @RequestMapping(value = "/getTestgbXMLFile", method = RequestMethod.GET)
    public String getTestgbxmlFile(@RequestParam String id, @RequestParam String testName, @RequestParam String type){
        type = type == null? "gbxml": type;
        String file_name = id + "_" + testName + "." + type;
        return LinkBuilder.buildTestgbXMLLink(file_name);

    }

    @RequestMapping(value = "/getLv1TestgbXMLFile", method = RequestMethod.GET)
    public String getLv1TestgbxmlFile(@RequestParam String id, @RequestParam String type) {
        type = type == null ? "gbxml" : type;
        String file_name = id + "." + type;
        return LinkBuilder.buildLv1TestgbXMLLink(file_name);

    }

    @RequestMapping(value = "/ValidateLevel2", method = RequestMethod.PUT)
    public Certification validateLevel2(@RequestParam String id, String testCaseId, String status, String type) {
        Certification certification = softwareDAO.getCertificationById(id);
        Software software = softwareDAO.getSoftwareById(id);
        Map<String, String> testResult = certification.getTestResult();
        logger.info("file_type: " + type );
        if(testResult == null){
            testResult = new HashMap();
        }
        if (status.equals("success")) {
            testResult.put(testCaseId, "success;" + type);
        } else {
            testResult.put(testCaseId, "failure;" + type);
        }
        certification.setTestResult(testResult);
        int passed_tests = 0;
        for(String key: testResult.keySet()){
            /*
            *  test 19 is optional
            * */
            if(key.equals("test19")){
                continue;
            }
            if(testResult.get(key).split(";")[0].equals("success")){
                passed_tests += 1;
            }
        }
        certification.setPassedTests(passed_tests);
        // todo total tests: 18 put it inside the properties
        if(passed_tests >= totalTests){
            software.setCertificationLevel(CertificationLevel.LEVEL2);
            software.setStatus(StatusEnum.COMPLETED);
            certification.setLevel2CertificationId(UUID.randomUUID().toString());
            certification.setIsLevel2Passed(true);
        }else{
            software.setCertificationLevel(CertificationLevel.LEVEL1);
            software.setStatus(StatusEnum.PROCESSING);
            certification.setIsLevel2Passed(false);
        }


        return softwareDAO.updateCertification(software, certification);


    }
}

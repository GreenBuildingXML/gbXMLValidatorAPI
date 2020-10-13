package com.bimport.asharea.controller.software;

import com.bimport.asharea.common.ServletUtil;
import com.bimport.asharea.common.auzreFile.AzureFileUploader;
import com.bimport.asharea.common.auzreFile.LinkBuilder;
import com.bimport.asharea.mySQL.software.SoftwareDAO;
import com.bimport.asharea.mySQL.software.model.Certification;
import com.bimport.asharea.mySQL.software.model.CertificationLevel;
import com.bimport.asharea.mySQL.software.model.Software;
import com.bimport.asharea.mySQL.software.model.StatusEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
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
    private static String testDir = "cases";
    private static String standardDir = "standard-cases";
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int totalTests = 19;
    private String testsRepo = "gbxml-test-file";
    //todo upload image
    @RequestMapping(value = "/AddSoftware", method = RequestMethod.POST)
    public Software addSoftware(HttpServletRequest req, Software software){
        String userId = (String) req.getAttribute("userId");
        software.setUserId(userId);
        return softwareDAO.saveSoftware(software);
    }

    @RequestMapping(value = "/UpdateSoftware", method = RequestMethod.PUT)
    public Software addSoftware(HttpServletRequest req, Software software, String id) {
        String userId = (String) req.getAttribute("userId");
        Software old_software = softwareDAO.getSoftwareById(id);
        old_software.setName(software.getName());
        old_software.setVersion(software.getVersion());
        old_software.setDescription(software.getDescription());
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
    public Certification validateLevel1(@RequestParam String id){
        Certification certification = softwareDAO.getCertificationById(id);
        Software software = softwareDAO.getSoftwareById(id);
        certification.setIsLevel1Passed(true);
        certification.setLevel1CertificationId(UUID.randomUUID().toString());
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
                return "success: " + id + "_" + testName + ".gbxml";
            }else if(file_name.contains(".xml")){
                azureFileUploader.upload(testsRepo, testDir, file, id + "_" + testName + ".xml");
                return "success: " + id + "_" + testName + ".xml";
            }

        }

        return "failure";

    }

    @RequestMapping(value = "/getTestgbXMLFile", method = RequestMethod.GET)
    public String getTestgbxmlFile(@RequestParam String id, @RequestParam String testName, @RequestParam String type){
        type = type == null? "gbxml": type;
        String file_name = id + "_" + testName + "." + type;
        return LinkBuilder.buildTestgbXMLLink(file_name);

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
            if(testResult.get(key).split(";")[0].equals("success")){
                passed_tests += 1;
            }
        }
        certification.setPassedTests(passed_tests);
        // todo total tests: 19 put it inside the properties
        if(passed_tests >= totalTests){
            software.setCertificationLevel(CertificationLevel.LEVEL2);
            software.setStatus(StatusEnum.COMPLETED);
            certification.setLevel2CertificationId(UUID.randomUUID().toString());
            certification.setIsLevel2Passed(true);
        }


        return softwareDAO.updateCertification(software, certification);


    }
}

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private int totalTests = 5;
    private String testsRepo = "gbxml-test-file";
    //todo upload image
    @RequestMapping(value = "/AddSoftware", method = RequestMethod.POST)
    public Software addSoftware(HttpServletRequest req, Software software){
        String userId = (String) req.getAttribute("userId");
        software.setUserId(userId);
        return softwareDAO.saveSoftware(software);
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
        software.setStatus(StatusEnum.IN_PROCESSING);
        software.setCertificationLevel(CertificationLevel.Lv1);
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
                azureFileUploader.upload(testsRepo, "cases", file, id + "_" + testName + ".gbxml");
                return "success: " + id + "_" + testName + ".gbxml";
            }

        }

        return "failure";

    }

    @RequestMapping(value = "/getTestgbXMLFile", method = RequestMethod.GET)
    public String getTestgbxmlFile(@RequestParam String id, @RequestParam String testName){
        String file_name = id + "_" + testName + ".gbxml";
        return LinkBuilder.buildTestgbXMLLink(file_name);

    }

    @RequestMapping(value = "/ValidateLevel2", method = RequestMethod.PUT)
    public Certification validateLevel2(@RequestParam String id, String testCaseId, String status) {
        Certification certification = softwareDAO.getCertificationById(id);
        Software software = softwareDAO.getSoftwareById(id);
        Map<String, String> testResult = certification.getTestResult();
        if(testResult == null){
            testResult = new HashMap();
        }
        if (status.equals("success")) {
            testResult.put(testCaseId, "success");
        } else {
            testResult.put(testCaseId, "failure");
        }
        certification.setTestResult(testResult);
        int passed_tests = 0;
        for(String key: testResult.keySet()){
            if(testResult.get(key).equals("success")){
                passed_tests += 1;
            }
        }
        certification.setPassedTests(passed_tests);
        if(passed_tests >= 5){
            software.setCertificationLevel(CertificationLevel.Lv2);
            software.setStatus(StatusEnum.COMPLETED);
            certification.setLevel2CertificationId(UUID.randomUUID().toString());
            certification.setIsLevel2Passed(true);
        }


        return softwareDAO.updateCertification(software, certification);


    }
}

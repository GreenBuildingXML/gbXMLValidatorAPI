package com.bimport.asharea.controller.software;

import com.bimport.asharea.mySQL.software.SoftwareDAO;
import com.bimport.asharea.mySQL.software.model.Certification;
import com.bimport.asharea.mySQL.software.model.CertificationLevel;
import com.bimport.asharea.mySQL.software.model.Software;
import com.bimport.asharea.mySQL.software.model.StatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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

    private int totalTests = 5;

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

    @RequestMapping(value = "/ValidateLevel2", method = RequestMethod.PUT)
    public Certification validateLevel2(@RequestParam String id, String testCaseId, String status) {
        Certification certification = softwareDAO.getCertificationById(id);
        Software software = softwareDAO.getSoftwareById(id);
        Map<String, String> testResult = certification.getTestResult();
        if(testResult == null){
            testResult = new HashMap();
        }
        if( testResult.containsKey(testCaseId)){
            String prev_status = testResult.get(testCaseId);
            if(status.equals("success") && !prev_status.equals("success")){
                testResult.put(testCaseId, status);
            }
        }else{
            if(status.equals("success")){
                testResult.put(testCaseId, "success");
            }else{
                testResult.put(testCaseId, "failure");
            }

        }
        certification.setTestResult(testResult);
        int passed_tests = 0;
        for(String key: testResult.keySet()){
            if(testResult.get(key).equals("success")){
                passed_tests += 1;
            }
        }
        if(passed_tests >= 5){
            software.setCertificationLevel(CertificationLevel.Lv2);
            software.setStatus(StatusEnum.COMPLETED);
            certification.setLevel2CertificationId(UUID.randomUUID().toString());
            certification.setIsLevel2Passed(true);
        }else{
            certification.setPassedTests(passed_tests);
        }

        return softwareDAO.updateCertification(software, certification);


    }
}

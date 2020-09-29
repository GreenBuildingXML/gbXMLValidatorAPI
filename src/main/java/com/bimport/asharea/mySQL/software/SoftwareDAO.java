package com.bimport.asharea.mySQL.software;

import com.bimport.asharea.mySQL.software.model.Certification;
import com.bimport.asharea.mySQL.software.model.CertificationRepo;
import com.bimport.asharea.mySQL.software.model.Software;
import com.bimport.asharea.mySQL.software.model.SoftwareRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoftwareDAO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SoftwareRepo softwareRepo;

    @Autowired
    private CertificationRepo certificationRepo;

    public List<Software> getSoftwares(String userId){
        return softwareRepo.findAllByUserId(userId);
    }

    public Software getSoftwareById(String id){
        return softwareRepo.findById(Long.parseLong(id)).get();
    }

    public Software saveSoftware(Software software){
        Certification certification = new Certification();
        certification.setSoftware(software);
        software.setCertification(certification);
        software = softwareRepo.save(software);
        certificationRepo.save(certification);
        return software;
    }

    public Certification updateCertification(Software software, Certification certification) {
        certification.setSoftware(software);
        software.setCertification(certification);
        softwareRepo.save(software);
        return certificationRepo.saveAndFlush(certification);
    }

    public Certification getCertificationById(String id){
        return certificationRepo.findById(Long.parseLong(id)).get();
    }

}

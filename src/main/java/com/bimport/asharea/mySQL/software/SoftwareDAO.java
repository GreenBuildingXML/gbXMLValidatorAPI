package com.bimport.asharea.mySQL.software;

import com.bimport.asharea.common.Base64Compression;
import com.bimport.asharea.mySQL.software.model.Certification;
import com.bimport.asharea.mySQL.software.model.CertificationRepo;
import com.bimport.asharea.mySQL.software.model.Software;
import com.bimport.asharea.mySQL.software.model.SoftwareRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class SoftwareDAO {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SoftwareRepo softwareRepo;
    @Autowired
    Base64Compression base64Compression;

    @Autowired
    private CertificationRepo certificationRepo;

    public List<Software> getSoftwares(String userId){
        return softwareRepo.findAllByUserId(userId);
    }

    public Software getSoftwareById(String id){
        return softwareRepo.findById(Long.parseLong(id)).get();
    }
    public Software updateSoftware(Software software){
        return softwareRepo.saveAndFlush(software);

    }
    public void updateProjectImage(String id, File image){
        byte[] compressed = base64Compression.compressFileBase64(image);
        Software software = getSoftwareById(id);
        software.setPicBlob(compressed);
        softwareRepo.saveAndFlush(software);
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

    public Certification updateCertification(Certification certification) {
        return certificationRepo.saveAndFlush(certification);
    }
    public Certification getCertificationById(String id){
        return certificationRepo.findById(Long.parseLong(id)).get();
    }

}

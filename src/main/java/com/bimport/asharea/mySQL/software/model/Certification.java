package com.bimport.asharea.mySQL.software.model;

import com.bimport.asharea.common.converter.HashMapConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.Map;

@Data
@Entity
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean isLevel1Passed = false;
    private Boolean isLevel2Passed = false;
    private CertStatusEnum Level1Status = CertStatusEnum.NA;
    private String Lv1Type;
    private int passedTests = 0;
    private String level1CertificationId;
    private String level2CertificationId;
    @Convert(converter = HashMapConverter.class)
    @Lob
    private Map<String,String> testResult;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    @JsonIgnore
    private Software software;

    public Certification() {
    }
}

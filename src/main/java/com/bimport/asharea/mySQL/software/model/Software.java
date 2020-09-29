package com.bimport.asharea.mySQL.software.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Data
public class Software {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String image;
    private String userId;
    @Enumerated(value = EnumType.STRING)
    private CertificationLevel certificationLevel;
    private String version;
    @Enumerated(value = EnumType.STRING)
    private StatusEnum status = StatusEnum.START;
    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private Timestamp createdDate;

    @UpdateTimestamp
    private Timestamp modifiedDate;

    @OneToOne(mappedBy = "software", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Certification certification;
    public Software() {
    }
}

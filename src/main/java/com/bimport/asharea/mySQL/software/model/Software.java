package com.bimport.asharea.mySQL.software.model;

import com.bimport.asharea.common.FileUtil;
import com.bimport.asharea.common.StringCompression;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @Lob
    @JsonIgnore
    private byte[] picBlob;
    @Transient
    private String picUrl;
    @UpdateTimestamp
    private Timestamp modifiedDate;

    @OneToOne(mappedBy = "software", cascade = CascadeType.ALL, fetch = FetchType.LAZY, optional = true)
    private Certification certification;
    public Software() {
    }

    public String getPicUrl() {
        if (picBlob != null) {
            return FileUtil.BASE64_PREFIX + StringCompression.decompress(picBlob);
        }
        return null;
    }
}

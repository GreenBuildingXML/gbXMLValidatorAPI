package com.bimport.ashrae.mySQL.user.model;
import com.bimport.ashrae.common.FileUtil;
import com.bimport.ashrae.common.StringCompression;
import com.bimport.ashrae.common.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String email;
    private String phone;
    private String organization;
    private Boolean validated;
    @Lob
    @JsonIgnore
    private byte[] picBlob;
    @Transient
    private String picUrl;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    @JsonIgnore
    private User user;
    public String getFullname(){
        if(!(StringUtil.isNullOrEmpty(firstName) && StringUtil.isNullOrEmpty(lastName))){
            return firstName + " " + lastName;
        }else{
            return email.split("@")[0];
        }

    }
    public UserInfo() {
    }

    public String getPicUrl() {
        if(picBlob != null){
            return FileUtil.BASE64_PREFIX + StringCompression.decompress(picBlob);
        }
        return null;
    }
}

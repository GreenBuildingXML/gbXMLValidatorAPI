package com.bimport.ashrae.common.auzreFile;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class AzureFileUploader {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    AzureFileUtil azureFileUtil;
    public JsonObject upload(String bucketName, String path, File file, String fileName) {
        if (file != null) {
            azureFileUtil.uploadFile(bucketName, path, file, fileName);
        }

        JsonObject ret = new JsonObject();
        ret.addProperty("status", "success");
        return ret;
    }

    public JsonObject delete(String bucketName, String path, String fileName) {
        azureFileUtil.deleteFile(bucketName, path, fileName);
        JsonObject ret = new JsonObject();
        ret.addProperty("status", "success");
        return ret;
    }
}

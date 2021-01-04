package com.bimport.ashrae.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Base64;
@Service
public class Base64Compression {
    private final FileUtil fileUtil;

    @Autowired
    public Base64Compression(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public byte[] compressFileBase64(File image) {
        String base64 = fileUtil.encodeToBase64Str(image);
        return StringCompression.compress(base64);
    }

    public String decompress(String base64Encoded) {
        return StringCompression.decompress(Base64.getDecoder().decode(base64Encoded));
    }
}

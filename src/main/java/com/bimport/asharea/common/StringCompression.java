package com.bimport.asharea.common;

import org.apache.commons.compress.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class StringCompression {
    public static byte[] compress(String raw) {
        if (raw == null) {
            return null;
        }

        byte[] compressed = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(raw.length())) {
            try (GZIPOutputStream gzippedOut = new GZIPOutputStream(bos)) {
                gzippedOut.write(raw.getBytes(StandardCharsets.UTF_8));
                gzippedOut.flush();
            }

            compressed = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return compressed;
    }

    public static String decompress(String base64Encoded) {
        return decompress(Base64.getDecoder().decode(base64Encoded));
    }

    public static String decompress(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        String decompressed = null;
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             GZIPInputStream gzippedIn = new GZIPInputStream(bais)) {
            byte[] content2 = IOUtils.toByteArray(gzippedIn);
            decompressed = new String(content2, StandardCharsets.UTF_8);
        } catch (IOException e) {
        }
        return decompressed;
    }
}

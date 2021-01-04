package com.bimport.ashrae.common;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.*;
import java.net.URLEncoder;
import java.util.*;

@Service
public class ServletUtil {
    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final FileUtil fileUtil;

    @Autowired
    public ServletUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public JsonObject buildParamJson(HttpServletRequest req, Set<String> ignores) {
        Map<String, String[]> map = req.getParameterMap();
        JsonObject jo = new JsonObject();
        for (String key : map.keySet()) {
            if (ignores != null && ignores.contains(key)) {
                continue;
            }

            String[] vals = map.get(key);
            if (vals != null && vals.length > 0) {
                jo.addProperty(key, vals[0]);
            }
        }
        return jo;
    }

    public void returnErrorResult(HttpServletResponse resp, JsonObject res, String errorMsg) {
        res.addProperty("status", "error");
        res.addProperty("error_msg", StringUtil.checkNullAndEmpty(errorMsg, "Unknown Error (String)"));
        returnJsonResult(resp, res);
    }

    public List<File> readAllMultiPartFiles(HttpServletRequest req) {
        List<File> files = new ArrayList<>();

        Collection<Part> parts;
        try {
            parts = req.getParts();
        } catch (IOException | ServletException e) {
            return files;
        }

        for (Part part : parts) {
            String uploadFileName = part.getSubmittedFileName();
            if (StringUtil.isNullOrEmpty(uploadFileName)) {
                continue;
            }

            String fieldName = part.getName();
            String frontEndFileName = req.getParameter(fieldName + "_name");

            if (!StringUtil.isNullOrEmpty(frontEndFileName)) {
                uploadFileName = frontEndFileName;
            }

            uploadFileName = FilenameUtils.normalize(uploadFileName);

            try {
                File file = fileUtil.convertInputStreamToFile(part.getInputStream(), uploadFileName);
                files.add(file);
            } catch (IOException ignored) {
            }
        }

        return files;
    }

    public List<File> readMultiPartFilesToPath(HttpServletRequest req,
                                               HttpServletResponse resp,
                                               String path,
                                               String nameBase) throws IOException, ServletException {
        List<File> files = new ArrayList<>();
        int idx = 1;
        outer:
        while (true) {
            String target = nameBase + idx;
            for (Part part : req.getParts()) {
                String fieldName = part.getName();
                if (part.getSize() > 0 && fieldName.equalsIgnoreCase(target)) {
                    String uploadFileName = part.getSubmittedFileName();
                    files.add(fileUtil.saveInputStreamToPath(part.getInputStream(), path + uploadFileName));
                    idx++;
                    continue outer;
                }
            }
            break;
        }
        return files;
    }

    public File[] readMultiPartFiles(HttpServletRequest req,
                                     HttpServletResponse resp,
                                     String[] fieldNames) throws IOException, ServletException {
        int num = fieldNames.length;

        File[] files = new File[num];

        for (Part part : req.getParts()) {
            String fieldName = part.getName();
            if (part.getSize() > 0) {
                for (int i = 0; i < num; i++) {
                    if (fieldName.equals(fieldNames[i])) {
                        String uploadFileName = part.getSubmittedFileName();
                        files[i] = fileUtil.convertInputStreamToFile(part.getInputStream(), uploadFileName);
                        break;
                    }
                }
            }
        }

        return files;
    }

    public void returnJsonResult(HttpServletResponse resp, JsonElement jo) {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("json");

        try (PrintWriter pw = resp.getWriter()) {
            pw.print(jo);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void returnString(HttpServletResponse resp, String str) {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text");
        try (PrintWriter pw = resp.getWriter()) {
            pw.write(str);
            pw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isRequestedByMobile(HttpServletRequest req) {
        String userAgent = req.getHeader("user-agent");
        if (!StringUtil.isNullOrEmpty(userAgent)) {
            return userAgent.toLowerCase().contains("mobile");
        }
        return false;
    }

    public void allowCrossOriginResourceSharing(HttpServletResponse resp, String methods) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", methods);
    }

    public void returnFile(HttpServletResponse resp, File file, String fileName, boolean isDownload) {
        String mime = "";
        if (!isDownload) {
            mime = fileUtil.getMimeType(file, fileName);
        }
        mime = StringUtil.checkNullAndEmpty(mime, "application/octet-stream");

        try (InputStream is = new FileInputStream(file)) {
            returnInpusStreamAsFile(resp, is, fileName, mime);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void returnInpusStreamAsFile(HttpServletResponse resp, InputStream is, String fileName, String mime) {
        resp.setCharacterEncoding("utf-8");

        mime = StringUtil.checkNullAndEmpty(mime, "application/octet-stream");

        resp.setContentType(mime);

        String disposition = "inline";
        if (mime.equals("application/octet-stream")) {
            disposition = "attachment";
        }

        try {
            resp.setHeader("Content-Disposition", disposition + ";filename=" + URLEncoder.encode(fileName, "utf-8"));
        } catch (UnsupportedEncodingException e) {
            LOG.error(e.getMessage(), e);
        }

        byte[] outputByte = new byte[2048];

        //write binary content to output stream
        int c;
        try (OutputStream os = resp.getOutputStream()) {
            while ((c = is.read(outputByte, 0, 2048)) != -1) {
                os.write(outputByte, 0, c);
            }
            os.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public boolean paramNotEmptyOrNull(String param, String errorMsg, JsonObject jo) {
        if (StringUtil.isNullOrEmpty(param)) {
            jo.addProperty("res", "error");
            jo.addProperty("error_msg", errorMsg);
            return false;
        }

        return true;
    }

    public boolean paramNotNegative(int param, String errorMsg, JsonObject jo) {
        if (param < 0) {
            jo.addProperty("res", "error");
            jo.addProperty("error_msg", errorMsg);
            return false;
        }

        return true;
    }

    public boolean paramNotNegative(double param, String errorMsg, JsonObject jo) {
        if (param < 0) {
            jo.addProperty("res", "error");
            jo.addProperty("error_msg", errorMsg);
            return false;
        }

        return true;
    }
}

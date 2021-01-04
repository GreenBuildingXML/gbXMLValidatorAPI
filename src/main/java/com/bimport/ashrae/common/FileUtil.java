package com.bimport.ashrae.common;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.detect.Detector;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class FileUtil {
    public static final String BASE64_PREFIX = "data:image/png;base64, ";
    private static final Logger LOG = LoggerFactory.getLogger(FileUtil.class);

    @Value("${ashrae.tmp_folder}")
    private String tmpFolder;

    public File createTempFile(String fileName) {
        File res;

        String randomFolderPath = tmpFolder + "ISToFile_" + SecurityUtil.genRandomStr() + "/";

        File randomFolder = new File(randomFolderPath);
        randomFolder.mkdir();

        res = new SelfDestryoFile(randomFolderPath + fileName, true);

        return res;
    }

    File saveInputStreamToPath(InputStream is, String path) {
        File res = new File(path);
        return saveInputStream(is, res);
    }

    public File convertInputStreamToFile(InputStream is, String fileName) {
        File res = createTempFile(fileName);
        return saveInputStream(is, res);
    }

    private File saveInputStream(InputStream is, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            IOUtils.copy(is, out);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return file;
    }

    public File convertStringToFile(String str) {
        if (str == null) {
            str = "";
        }

        File res = createTempFile("StrToFile.tmp");
        try (FileWriter fw = new FileWriter(res);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(str);
            bw.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return res;
    }

    public File convertStringToFile(String str, File parent, String fileName) {
        if (str == null) {
            str = "";
        }

        File res = new File(parent, fileName);
        try (FileWriter fw = new FileWriter(res);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(str);
            bw.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return res;
    }

    public void writeStringToFile(String str, String path) {
        if (str == null) {
            str = "";
        }

        try (FileOutputStream fos = new FileOutputStream(path);
             OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
             BufferedWriter bw = new BufferedWriter(osw)) {
            bw.write(str);
            bw.flush();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public String readStringFromFile(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             InputStreamReader isr = new InputStreamReader(fis, "utf-8");
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
            return sb.toString();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }

    public String getFileHash(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            return DigestUtils.md5Hex(fis);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return null;
    }

    public boolean saveFileToFolder(File file, File dir) {
        try {
            FileUtils.copyFileToDirectory(file, dir);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean simpleCheckIsImage(String fileName) {
        if (StringUtil.isNullOrEmpty(fileName)) {
            return false;
        }

        fileName = fileName.toLowerCase();
        return fileName.endsWith(".png")
                || fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg");
    }

    public boolean saveFileToFile(File file, File dest) {
        try (
                RandomAccessFile raf = new RandomAccessFile(dest, "rw");
                FileChannel fc = raf.getChannel();

                FileInputStream fis = new FileInputStream(file);
                ReadableByteChannel rbc = Channels.newChannel(fis);
        ) {
            fc.transferFrom(rbc, 0, file.length());
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

        return false;
    }

    public boolean saveFileToPath(File file, String path) {
        return saveFileToFile(file, new File(path));
    }

    /**
     * suffix with heading dot
     *
     * @param s
     * @param suffix
     * @return
     */
    public String makeFileName(String s, String suffix) {
        String fileName = s.replaceAll("\\W", "_");
        int suffixLen = suffix.length();
        if (fileName.length() > 255 - suffixLen) {
            fileName = fileName.substring(0, 255 - suffixLen);
        }
        return fileName + suffix;
    }

    public List<File> readZipFile(File zipFile) {
        List<File> res = new ArrayList<>();

        // unzip file
        int bytesRead;
        byte[] dataBuffer = new byte[1024];
        try (FileInputStream zipFis = new FileInputStream(zipFile);
             ZipInputStream zipIs = new ZipInputStream(zipFis)) {
            ZipEntry entry = zipIs.getNextEntry();
            while (entry != null) {
                File tmp = createTempFile(entry.getName());
                OutputStream outputStream = new FileOutputStream(tmp);
                while ((bytesRead = zipIs.read(dataBuffer)) != -1) {
                    outputStream.write(dataBuffer, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();

                res.add(tmp);
                entry = zipIs.getNextEntry();
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        return res;
    }

    public File makeZipFile(File parent, String fileName, File... files) {
        File zipFile = new File(parent, fileName);

        byte[] b = new byte[1024];
        int count;
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream ous = new ZipOutputStream(fos)) {
            for (File file : files) {
                if (file == null) {
                    continue;
                }
                ous.putNextEntry(new ZipEntry(file.getName()));

                try (FileInputStream fis = new FileInputStream(file)) {
                    while ((count = fis.read(b)) > 0) {
                        ous.write(b, 0, count);
                    }
                    ous.flush();
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            zipFile = null;
        }

        return zipFile;
    }

    public File prepareFolder(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    public File prepareFolder(String path, boolean isSelfDestroyed) {
        File folder = isSelfDestroyed ? new SelfDestryoFile(path, true) : new File(path);
        if (folder.exists()) {
            try {
                FileUtils.cleanDirectory(folder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            folder.mkdirs();
        }

        return folder;
    }

    public static String getSuffix(File file) {
        if (file == null) {
            return null;
        }

        return getSuffix(file.getName());
    }

    public static String getSuffix(String fileName) {
        if (fileName == null) {
            return null;
        }

        fileName = fileName.toLowerCase();
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return "NO_TYPE";
        }

        return fileName.substring(lastDot + 1).trim();
    }

    public String getFileNameWithoutSuffix(String fileName) {
        if (fileName == null) {
            return null;
        }

        int lastDot = fileName.lastIndexOf(".");
        if (lastDot == -1) {
            return fileName.trim();
        }
        return fileName.substring(0, lastDot).trim();
    }

    public boolean deleteFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                deleteFile(f);
            }
        } else {
            file.delete();
        }
        return true;
    }

    public File compressFile(String zipFileName, File file) {
        File zipFile = createTempFile(zipFileName);

        byte[] b = new byte[1024];
        int count;
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream ous = new ZipOutputStream(fos)) {
            ous.putNextEntry(new ZipEntry(file.getName()));

            try (FileInputStream fis = new FileInputStream(file)) {
                while ((count = fis.read(b)) > 0) {
                    ous.write(b, 0, count);
                }
                ous.flush();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            zipFile = null;
        }

        return zipFile;
    }

    public String encodeToBase64Str(File file) {
        String encodedfile = null;
        try (FileInputStream fileInputStreamReader = new FileInputStream(file)) {
            byte[] bytes = new byte[(int) file.length()];
            fileInputStreamReader.read(bytes);
            encodedfile = new String(Base64.encodeBase64(bytes), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return encodedfile;
    }

    public String getMimeType(File file, String fileName) {
        try (InputStream is = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(is)) {
            AutoDetectParser parser = new AutoDetectParser();
            Detector detector = parser.getDetector();
            Metadata md = new Metadata();
            md.add(Metadata.RESOURCE_NAME_KEY, fileName);
            MediaType mediaType = detector.detect(bis, md);
            return mediaType.toString();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

    public void saveBytesToFile(byte[] bytes, File file) {
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(bytes);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] getFileBytes(File file) {
        byte[] res = new byte[(int) file.length()];
        try (InputStream is = new FileInputStream(file)) {
            is.read(res);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}

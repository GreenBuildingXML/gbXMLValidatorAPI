package com.bimport.asharea.common.auzreFile;

import com.bimport.asharea.common.FileUtil;
import com.bimport.asharea.common.StringUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;



@Service
public class AzureFileUtil {
    private final Object LOCK = new Object();
    private final int DEFAULT_QUOTA = 1000;
    private final FileUtil fileUtil;
    private Logger LOG = LoggerFactory.getLogger(this.getClass());
    private volatile CloudFileClient fileClient;

    @Value("${azure.fileStorage.accountName}")
    private String azureStorageAccountName;
    @Value("${azure.fileStorage.accountKey}")
    private String azureStorageAccountKey;
    @Value("${azure.fileStorage.suffix}")
    private String suffix;

    @Autowired
    public AzureFileUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    private void init() {
        synchronized (LOCK) {
            if (fileClient == null) {
                String storageConnectionString =
                        "DefaultEndpointsProtocol=https;"
                                + "AccountName=" + azureStorageAccountName + ";"
                                + "AccountKey=" + azureStorageAccountKey + ";"
                                + "EndpointSuffix=core.windows.net";

                try {
                    CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);

                    fileClient = account.createCloudFileClient();
                } catch (InvalidKeyException | URISyntaxException e) {
                    LOG.error("Cannot get Azure file client, " + e.getMessage(), e);
                }
            }
        }
    }

    private CloudFileClient getFileClient() {
        if (fileClient == null) {
            init();
        }

        return fileClient;
    }

    private CloudFileShare getShare(String shareName) {
        CloudFileClient client = getFileClient();
        CloudFileShare share = null;
        try {
            share = client.getShareReference(shareName);

        } catch (URISyntaxException | StorageException e) {
            LOG.error("Create file share failed: " + e.getMessage(), e);
        }
        return share;
    }


    CloudFileDirectory getShareRootDir(String shareName) {
        try {
            CloudFileShare share = getShare(shareName);
            if (share.createIfNotExists()) {
                share.getProperties().setShareQuota(DEFAULT_QUOTA);
            }
            return share.getRootDirectoryReference();
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Get file share failed: " + e.getMessage(), e);
        }
        return null;
    }

    CloudFileDirectory createRootDir(CloudFileShare share, String folderName) {
        try {
            CloudFileDirectory root = share.getRootDirectoryReference();

            HashMap<String, String> meta = genMetaMap(new String[]{"name", "is_default_folder"},
                    new String[]{StringUtil.base64Encode(folderName), "true"});
            CloudFileDirectory dir = root.getDirectoryReference(folderName);
            dir.setMetadata(meta);

            dir.createIfNotExists();
            return dir;
        } catch (StorageException | URISyntaxException e) {
            LOG.error("Create root dir failed: " + e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    boolean isDirExist(String share, String path) {
        CloudFileDirectory dir = getDirOnly(share, path);
        if (dir == null) {
            return false;
        }

        try {
            return dir.exists();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return false;
    }

    CloudFileDirectory getDirCreateIfNotExist(String share, String path) {
        CloudFileDirectory dir = getDirOnly(share, path);

        if (dir != null) {
            try {
                dir.createIfNotExists();
            } catch (StorageException | URISyntaxException e) {
                LOG.error("Dir create if not exist failed: " + share + ", " + path + ", " + e.getMessage(), e);
                return null;
            }
        }

        return dir;
    }

    HashMap<String, String> getFolderMeta(CloudFileDirectory dir) {
        try {
            dir.downloadAttributes();
            return dir.getMetadata();
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    CloudFileDirectory getDirOnly(String share, String path) {
        CloudFileShare fileShare = getShare(share);

        try {
            fileShare.createIfNotExists();
        } catch (StorageException e) {
            LOG.error("Share create if not exist failed: " + share + ", " + e.getMessage(), e);
            return null;
        }

        CloudFileDirectory rootDir;
        try {
            rootDir = fileShare.getRootDirectoryReference();
        } catch (StorageException | URISyntaxException e) {
            LOG.error("Get root directory failed: " + share + ", " + e.getMessage(), e);
            return null;
        }

        if (StringUtil.isNullOrEmpty(path)
                || path.equals(suffix)) {
            return rootDir;
        }

        CloudFileDirectory dir;
        try {
            dir = rootDir.getDirectoryReference(path);
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Get directory failed: " + share + ", " + path + ", " + e.getMessage(), e);
            return null;
        }

        return dir;
    }

    CloudFile getCloudFile(String share, String path, String fileName) {
        CloudFileDirectory dir;
        if (path.equals("/")) {
            dir = getShareRootDir(share);
        } else {
            dir = getDirCreateIfNotExist(share, path);
        }

        CloudFile cloudFile = null;
        try {
            cloudFile = dir.getFileReference(fileName);
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Get cloud file failed: " + share + ", " + path + ", " + fileName + ", " + e.getMessage(), e);
        }

        return cloudFile;
    }

    private CloudFileDirectory createDir(CloudFileDirectory dir, String name) {
        if (dir != null) {
            try {
                HashMap<String, String> meta = genMetaMap(new String[]{"name"},
                        new String[]{StringUtil.base64Encode(name)});
                dir.setMetadata(meta);

                dir.createIfNotExists();
            } catch (StorageException | URISyntaxException e) {
                LOG.error("Dir create if not exist failed: " + e.getMessage(), e);
                return null;
            }
        }
        return dir;
    }

    CloudFileDirectory createDir(String share, String path, String name) {
        CloudFileDirectory dir = getDirOnly(share, path);
        return createDir(dir, name);
    }

    private HashMap<String, String> genMetaMap(String[] keys, String[] values) {
        HashMap<String, String> meta = new HashMap<>();
        for (int i = 0; i < keys.length; i++) {
            meta.put(keys[i], values[i]);
        }
        return meta;
    }


    JsonObject getFolderProperties(CloudFileDirectory dir) {
        try {
            dir.downloadAttributes();
            FileDirectoryProperties props = dir.getProperties();

            JsonObject res = new JsonObject();
            res.addProperty("type", "folder");

            HashMap<String, String> meta = dir.getMetadata();
            if (meta.containsKey("name")) {
                res.addProperty("name", StringUtil.base64Decode(meta.get("name")));
            } else {
                res.addProperty("name", "Unknown");
            }
            String path = dir.getUri().getPath();
            res.addProperty("path", path);
            res.addProperty("path_name", extractName(path)[1]);

            DateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            Date lastModified = props.getLastModified();
            res.addProperty("modify_date", format.format(lastModified));
            return res;
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    JsonObject getFileProperties(CloudFile file) {
        try {
            file.downloadAttributes();
            FileProperties props = file.getProperties();

            JsonObject res = new JsonObject();
            res.addProperty("type", "file");

            HashMap<String, String> meta = file.getMetadata();
            String metaName = meta.get("name");
            String decode = StringUtil.base64Decode(metaName);
            res.addProperty("name", decode);

            String path = file.getUri().getPath();
            res.addProperty("path", path);
            res.addProperty("path_name", extractName(path)[1]);
            res.addProperty("size", props.getLength());

            DateFormat format = new SimpleDateFormat("MMM dd, yyyy");
            Date lastModified = props.getLastModified();
            res.addProperty("modify_date", format.format(lastModified));
            return res;
        } catch (StorageException e) {
            e.printStackTrace();
        }
        return null;
    }

    public File download(CloudFile cloudFile) {
        String fileName = "Download";

        try {
            cloudFile.downloadAttributes();
        } catch (StorageException ignored) {
        }

        HashMap<String, String> meta = cloudFile.getMetadata();
        if (meta.containsKey("name")) {
            fileName = StringUtil.base64Decode(meta.get("name"));
        }

        return download(cloudFile, fileName);
    }

    public File download(CloudFile cloudFile, String fileName) {
        File res = fileUtil.createTempFile(fileName);
        try {
            cloudFile.downloadToFile(res.getAbsolutePath());
        } catch (StorageException | IOException e) {
            LOG.error("Download failed: " + cloudFile.getUri().getPath() + ", " + fileName + ", " + e.getMessage(), e);
            return null;
        }

        return res;
    }

    public File download(String share, String path, String fileName) {
        CloudFile cloudFile = getCloudFile(share, path, fileName);
        if (cloudFile == null) {
            return null;
        }

        return download(cloudFile, fileName);
    }

    public void deleteFile(String share, String path, String fileName) {
        CloudFile cloudFile = getCloudFile(share, path, fileName);
        if (cloudFile != null) {
            deleteFile(cloudFile);
        }
    }

    /**
     * If fileName is null or empty, use file's name
     */
    CloudFile uploadFile(String share, String path, File file, String cloudName) {
        if (StringUtil.isNullOrEmpty(cloudName)) {
            cloudName = file.getName();
        }

        CloudFile cloudFile = getCloudFile(share, path, cloudName);
        if (cloudFile == null) {
            return null;
        }

        HashMap<String, String> meta = genMetaMap(new String[]{"name"},
                new String[]{StringUtil.base64Encode(file.getName())});
        cloudFile.setMetadata(meta);

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            cloudFile.upload(bis, file.length());
        } catch (URISyntaxException | StorageException | IOException e) {
            LOG.error("Upload failed: " + share + ", " + path + ", " + cloudName + ", " + e.getMessage(), e);
            return null;
        }

        return cloudFile;
    }

    public JsonArray listFolderContent(CloudFileDirectory dir) {
        JsonArray ja = new JsonArray();
        try {
            if (dir != null && dir.exists()) {

                for (ListFileItem item : dir.listFilesAndDirectories()) {
                    String path = item.getUri().getPath();
                    String itemName = path.substring(path.lastIndexOf(suffix) + 1);

                    CloudFileDirectory tryDir = dir.getDirectoryReference(itemName);

                    if (tryDir.exists()) {
                        // found new folder
                        ja.add(getFolderProperties(tryDir));
                    } else {
                        // item is file
                        CloudFile file = dir.getFileReference(itemName);
                        ja.add(getFileProperties(file));
                    }
                }
            }
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Browse folder encounters error, " + dir.getName() + ", " + e.getMessage(), e);
        }
        return ja;
    }

    public JsonArray listFolderFiles(CloudFileDirectory dir) {
        JsonArray ja = new JsonArray();
        try {
            if (dir != null && dir.exists()) {

                for (ListFileItem item : dir.listFilesAndDirectories()) {
                    String path = item.getUri().getPath();
                    String itemName = path.substring(path.lastIndexOf(suffix) + 1);

                    CloudFileDirectory tryDir = dir.getDirectoryReference(itemName);

                    if (tryDir.exists()) {
                        // found new folder
                        ja.addAll(listFolderFiles(tryDir));
                    } else {
                        // item is file
                        CloudFile file = dir.getFileReference(itemName);
                        ja.add(getFileProperties(file));
                    }
                }
            }
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Browse folder encounters error, " + dir.getName() + ", " + e.getMessage(), e);
        }
        return ja;
    }

    JsonObject updateFileName(CloudFile file, String newName) {
        JsonObject res = new JsonObject();
        try {
            file.downloadAttributes();
            HashMap<String, String> meta = file.getMetadata();

            meta.put("name", StringUtil.base64Encode(newName));
            file.setMetadata(meta);

            file.uploadMetadata();
        } catch (StorageException | URISyntaxException e) {
            LOG.error("Update file name failed: " + e.getMessage(), e);
            e.printStackTrace();

            res.addProperty("error", e.getMessage());
        }
        return res;
    }

    JsonObject updateFolderName(CloudFileDirectory folder, String newName) {
        JsonObject res = new JsonObject();
        try {
            folder.downloadAttributes();
            HashMap<String, String> meta = folder.getMetadata();

            meta.put("name", StringUtil.base64Encode(newName));
            folder.setMetadata(meta);

            folder.uploadMetadata();
        } catch (StorageException | URISyntaxException e) {
            LOG.error("Update folder name failed: " + e.getMessage(), e);
            e.printStackTrace();

            res.addProperty("error", e.getMessage());
        }
        return res;
    }

    JsonObject deleteFile(CloudFile file) {
        JsonObject res = new JsonObject();
        try {
            file.deleteIfExists();
        } catch (StorageException | URISyntaxException e) {
            LOG.error("Delete file failed: " + e.getMessage(), e);
            e.printStackTrace();

            res.addProperty("error", e.getMessage());
        }
        return res;
    }

    JsonObject deleteFolder(CloudFileDirectory dir) {
        try {
            for (ListFileItem item : dir.listFilesAndDirectories()) {
                String path = item.getUri().getPath();
                String itemName = extractName(path)[1];

                CloudFileDirectory tryDir = dir.getDirectoryReference(itemName);

                if (tryDir.exists()) {
                    // found new folder
                    deleteFolder(tryDir);
                } else {
                    // item is file
                    CloudFile file = dir.getFileReference(itemName);
                    deleteFile(file);
                }
            }

            dir.delete();
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Delete folder failed: " + e.getMessage());
            e.printStackTrace();
        }

        JsonObject res = new JsonObject();
        res.addProperty("status", "success");
        return res;
    }

    JsonObject copyFileTo(CloudFile file, CloudFileDirectory dest) {
        JsonObject res = new JsonObject();
        try {
            CloudFile destFile = dest.getFileReference(file.getName());
            destFile.startCopy(file);
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Copy file failed: " + e.getMessage(), e);
            res.addProperty("error", e.getMessage());
        }
        return res;
    }

    JsonObject copyFolderTo(CloudFileDirectory dir, CloudFileDirectory dest) {
        JsonObject res = new JsonObject();
        try {
            String dirName = extractName(dir.getName())[1];
            CloudFileDirectory target = dest.getDirectoryReference(dirName);
            target.createIfNotExists();
            target.setMetadata(dir.getMetadata());
            target.uploadMetadata();

            Iterable<ListFileItem> items = dir.listFilesAndDirectories();
            for (ListFileItem item : items) {
                String path = item.getUri().getPath();
                String itemName = extractName(path)[1];

                CloudFileDirectory tryDir = dir.getDirectoryReference(itemName);

                if (tryDir.exists()) {
                    // found new folder
                    copyFolderTo(tryDir, target);
                } else {
                    // item is file
                    copyFileTo(dir.getFileReference(itemName), target);
                }
            }
        } catch (URISyntaxException | StorageException e) {
            LOG.error("Copy folder failed: " + e.getMessage(), e);
            res.addProperty("error", e.getMessage());
        }
        return res;
    }

    public String[] extractName(String name) {
        String[] res = new String[2];
        int suffixIdx = name.lastIndexOf(suffix);
        if (suffixIdx > 0) {
            res[0] = name.substring(0, suffixIdx);
            res[1] = name.substring(suffixIdx + 1);
        } else {
            res[0] = "/";
            res[1] = name;
        }
        return res;
    }
}

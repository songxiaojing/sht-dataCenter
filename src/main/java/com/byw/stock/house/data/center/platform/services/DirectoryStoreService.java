package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.data.center.web.utils.UploadFile;
import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.core.utils.Utils;
import com.byw.stock.house.platform.log.PlatformLogger;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.IOUtils;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


/**
 * 
 * 目录管理服务.
 * 
 * 系统文件读写服务.
 * 
 * @title DirectoryStoreService
 * @package com.topsec.tss.platform.service
 * @author baiyanwei
 * @version
 * @date 2014-6-27
 * 
 */
@PlatformServiceInfo(description = "DirectoryStoreService", configurationPath = "application/services/DirectoryStoreService/")
public class DirectoryStoreService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    /**
     * logger.
     */
    final private static PlatformLogger theLogger = PlatformLogger.getLogger(DirectoryStoreService.class);

    /**
     * ZIP文件后缀.
     */
    final public static String ZIP_FILE_STR = ".zip";
    /**
     * 同步数据导出目录.
     */
    final private static String SYNCH_EXPORT_DIRECTORY = "synch_export";
    /**
     * 同步数据导入目录.
     */
    final private static String SYNCH_IMPORT_DIRECTORY = "synch_import";

    /**
     * 资源导入目录.
     */
    final private static String RESOURCE_IMPORT_DIRECTORY = "resource_import";
    /**
     * 资源导出目录.
     */
    //final private static String RESOURCE_EXPORT_DIRECTORY = "resource_export";

    @XmlElement(name = "jmxObjectName", defaultValue = "topsec.tss.core:type=DirectoryStoreService")
    public String _jmxObjectName = "topsec.tss.core:type=DirectoryStoreService";

    /**
     * 系统文件主目录.
     */
    @XmlElement(name = "fileStorageHomeDirectory", type = String.class, defaultValue = "/opt/tss/storage/")
    public String _fileStorageHomeDirectory = "/opt/tss/storage/";

    /**
     * 系统文件临时目录.
     */
    @XmlElement(name = "temporaryStorageDirectory", type = String.class, defaultValue = "/opt/tss/temporary/")
    public String _temporaryStorageDirectory = "/opt/tss/temporary/";

    /**
     * IO操作块大小.
     */
    @XmlElement(name = "ioBuffereSize", type = Integer.class, defaultValue = "2048")
    public int _ioBuffereSize = 2048;

    @Override
    public void start() throws Exception {

        long currentPoint = System.currentTimeMillis();
        this.registerMBean(_jmxObjectName, this);

        theLogger.info("storageHome", _fileStorageHomeDirectory, String.valueOf(System.currentTimeMillis() - currentPoint));
    }

    @Override
    public void stop() throws Exception {

        this.unRegisterMBean(_jmxObjectName);
    }

    /**
     * apache方式压缩文件夹.
     * 
     * @param directoryPath
     * @return
     */
    public File apacheCompressDirectory(String directoryPath, boolean isDelete) {

        if (Assert.isEmptyString(directoryPath) == true) {
            return null;
        }
        File targetFile = new File(directoryPath);
        if (targetFile.exists() == false) {
            return null;
        }
        File zipFile = new File(targetFile.getAbsolutePath() + ZIP_FILE_STR);
        if (this.checkAndCreateFileOrDirectory(zipFile) == false) {
            theLogger.debug("Create the target storage file fault,Path:" + zipFile.getAbsolutePath());
            return null;
        }
        ZipArchiveOutputStream zipOut = null;
        //ZipOutputStream zipOut = null;
        try {
            OutputStream out = new FileOutputStream(zipFile);
            BufferedOutputStream bos = new BufferedOutputStream(out);
            zipOut = new ZipArchiveOutputStream(bos);
            zipOut.setEncoding("GBK");
            this.apacheCompressFile(zipOut, targetFile, "");
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                }
            }
            if (isDelete == true) {
                try {
                    deleteFile(targetFile);
                } catch (Exception e) {

                }
            }
        }

        return zipFile;
    }

    /**
     * apach方式压缩目录或文件.
     * 
     * @param zipOut
     * @param targetFile
     * @param zipEntryName
     * @throws Exception
     */
    private void apacheCompressFile(ZipArchiveOutputStream zipOut, File targetFile, String zipEntryName) throws Exception {

        ByteArrayOutputStream tempbaos = new ByteArrayOutputStream();
        BufferedOutputStream tempbos = new BufferedOutputStream(tempbaos);
        try {
            if (targetFile.isFile() == true) {
                zipOut.putArchiveEntry(new ZipArchiveEntry(zipEntryName + targetFile.getName()));
                IOUtils.copy(new FileInputStream(targetFile.getAbsolutePath()), zipOut);
                zipOut.closeArchiveEntry();
            } else if (targetFile.isDirectory() == true) {
                //返回此绝对路径下的文件
                File[] files = targetFile.listFiles();
                if (files == null || files.length < 1) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    //判断此文件是否是一个文件夹
                    if (files[i].isDirectory()) {
                        apacheCompressFile(zipOut, files[i], zipEntryName + files[i].getName() + File.separator);
                    } else {
                        zipOut.putArchiveEntry(new ZipArchiveEntry(zipEntryName + files[i].getName()));
                        IOUtils.copy(new FileInputStream(files[i].getAbsolutePath()), zipOut);
                        zipOut.closeArchiveEntry();

                    }

                }
            }
        } catch (Exception e1) {
            theLogger.exception(e1);
        } finally {
            if (tempbaos != null) {
                try {
                    tempbaos.flush();
                    tempbaos.close();
                } catch (Exception e) {
                }
            }
            if (tempbos != null) {
                try {
                    tempbos.flush();
                    tempbos.close();
                } catch (Exception e) {
                }
            }
        }

    }

    /**
     * 压缩文件夹.
     * 
     * @param directoryPath
     * @return
     */
    public File compressDirectory(String directoryPath, boolean isDelete) {

        if (Assert.isEmptyString(directoryPath) == true) {
            return null;
        }
        File targetFile = new File(directoryPath);
        if (targetFile.exists() == false) {
            return null;
        }
        File zipFile = new File(targetFile.getAbsolutePath() + ZIP_FILE_STR);
        if (this.checkAndCreateFileOrDirectory(zipFile) == false) {
            theLogger.debug("Create the target storage file fault,Path:" + zipFile.getAbsolutePath());
            return null;
        }
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            this.compressFile(zipOut, targetFile, targetFile.getName());
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                }
            }
            if (isDelete == true) {
                try {
                    deleteFile(targetFile);
                } catch (Exception e) {

                }
            }
        }

        return zipFile;
    }

    /**
     * 压缩目录或文件.
     * 
     * @param zipOut
     * @param targetFile
     * @param zipEntryName
     * @throws Exception
     */
    private void compressFile(ZipOutputStream zipOut, File targetFile, String zipEntryName) throws Exception {

        //压缩目录下的文件或目录 
        if (targetFile.isDirectory() == true) {
            theLogger.debug("compressDirectory", targetFile.getAbsolutePath());
            File[] sonFiles = targetFile.listFiles();
            if (sonFiles == null) {
                return;
            }
            //压缩文件或子目录
            for (int i = 0; i < sonFiles.length; i++) {
                compressFile(zipOut, sonFiles[i], zipEntryName + "/" + sonFiles[i].getName());
            }
            //压缩文件
        } else {
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(targetFile);
                theLogger.debug("compressFile:", targetFile.getAbsolutePath());
                //增加压缩节点
                zipOut.putNextEntry(new ZipEntry(zipEntryName));
                //分段写入压缩文件
                byte[] bytes = new byte[_ioBuffereSize];
                int len = 0;
                while ((len = fileInputStream.read(bytes)) > 0) {
                    zipOut.write(bytes, 0, len);
                }
                //结束压缩节点
                zipOut.closeEntry();
            } catch (Exception e) {
                theLogger.exception(e);
                throw e;
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                    }
                }
            }
        }

    }

    /**
     * 将对象写入文件中.
     * 
     * @param objectList
     * @throws Exception
     */
    public File storeSynchDataObjects(String targetFileName, List<? extends Serializable> objectList) throws Exception {

        if (Assert.isEmptyString(targetFileName) == true || Assert.isEmptyCollection(objectList) == true) {
            return null;
        }
        //创建存储目录
        File storeTargetFile = new File(this._fileStorageHomeDirectory + File.separator + SYNCH_EXPORT_DIRECTORY + File.separator + targetFileName);
        if (this.checkAndCreateFileOrDirectory(storeTargetFile) == false) {
            theLogger.debug("Create the target storage file fault,Path:" + storeTargetFile.getAbsolutePath());
            return null;
        }
        //将同步数据写入文件
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(storeTargetFile));
            oos.writeObject(objectList);
            oos.flush();
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (Exception e) {
                }
            }
        }

        return storeTargetFile;
    }

    /**
     * 取向同步数据文件
     * 
     * @param fileName
     * @return
     */
    public File getExportSynchDataFile(String fileName) {

        if (Assert.isEmptyString(fileName) == true) {
            return null;
        }
        //创建指定文件引用
        return new File(this._fileStorageHomeDirectory + File.separator + SYNCH_EXPORT_DIRECTORY + File.separator + fileName);
    }

    /**
     * 取得同步数据导入文件.
     * 
     * @param fileName
     * @return
     */
    public File getImportSynchDataFile(String fileName) {

        if (Assert.isEmptyString(fileName) == true) {
            return null;
        }
        return new File(this._fileStorageHomeDirectory + File.separator + SYNCH_IMPORT_DIRECTORY + File.separator + fileName);
    }

    /**
     * 复制同步数据上传文件.
     * 
     * @return
     */
    public File copySynchDataFile(File targetFile, String newFileName) {

        if (targetFile == null || targetFile.exists() == false) {
            return null;
        }
        if (Assert.isEmptyString(newFileName) == true) {
            return null;
        }
        File storeTargetFile = new File(this._fileStorageHomeDirectory + File.separator + SYNCH_IMPORT_DIRECTORY + File.separator + newFileName);

        return copyFile(targetFile, storeTargetFile);
    }

    /**
     * 复制导入资源文件到目录中.
     * 
     * @param targetFile
     * @param newFileName
     * @return
     */
    public File copyImportDataFile(File targetFile, String newFileName) {

        if (targetFile == null || targetFile.exists() == false) {
            return null;
        }
        if (Assert.isEmptyString(newFileName) == true) {
            return null;
        }
        File storeTargetFile = new File(this._fileStorageHomeDirectory + File.separator + RESOURCE_IMPORT_DIRECTORY + File.separator + newFileName);

        return copyFile(targetFile, storeTargetFile);
    }

    /**
     * 在临时文件夹中创建一个FILE.
     * 
     * @param newFileName
     * @return
     */
    public File createFileInTemporary(String newFileName) {

        if (Assert.isEmptyString(newFileName) == true) {
            return null;
        }
        //创建文件系统临时目录中
        File storeTargetFile = new File(this._temporaryStorageDirectory + File.separator + newFileName);
        if (this.checkAndCreateFileOrDirectory(storeTargetFile) == false) {
            return null;
        }
        return storeTargetFile;
    }

    /**
     * 在临时文件夹中创建一个Directory.
     * 
     * @return
     */
    public File createDirectoryInTemporary(String newDirectoryName) {

        if (Assert.isEmptyString(newDirectoryName) == true) {
            return null;
        }
        //创建目录到系统临时目录中
        File targetDirectory = new File(this._temporaryStorageDirectory + File.separator + newDirectoryName);
        if (targetDirectory.exists() == false) {
            targetDirectory.mkdirs();
        }
        return targetDirectory;
    }

    /**
     * 复制文件.
     * 
     * @param resourceFile
     * @param targetFile
     * @return
     */
    public File copyFile(File resourceFile, File targetFile) {

        if (resourceFile == null || targetFile == null) {
            return null;
        }
        if (resourceFile.isDirectory() == true || targetFile.isDirectory() == true) {
            return null;
        }
        //如果目标文件存在则删除
        if (targetFile.exists() == true) {
            targetFile.delete();
        }
        //查检源文件或目录是否存在
        if (this.checkAndCreateFileOrDirectory(targetFile) == false) {
            return null;
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutStream = null;
        try {
            fileInputStream = new FileInputStream(resourceFile);
            fileOutStream = new FileOutputStream(targetFile);
            //分段写入压缩文件
            byte[] bytes = new byte[_ioBuffereSize];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) > 0) {
                fileOutStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
            if (fileOutStream != null) {
                try {
                    fileOutStream.close();
                } catch (IOException e) {
                }
            }
        }
        return targetFile;
    }

    /**
     * 读取同步资源对象.
     * 
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public List<Serializable> readObjects(File resourceFile) throws Exception {

        if (resourceFile == null || resourceFile.exists() == false || resourceFile.isDirectory() == true) {
            return null;
        }
        //读取数据文件到对象中
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream(resourceFile));
            Object rowObject = ois.readObject();
            return (List<Serializable>) rowObject;
        } catch (Exception e) {
            theLogger.exception(e);
            throw e;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * 根据事件SN+附件类型+附件文件压缩转存附件.
     * 
     * @param sseEventSn
     * @param className
     * @param uploadFileList
     * @return
     * @throws Exception
     */
    public List<UploadFile> storeSseEventAttachFile(String sseEventSn, String className, List<UploadFile> uploadFileList) throws Exception {

        if (Assert.isEmptyString(sseEventSn) == true) {
            return null;
        }

        if (Assert.isEmptyCollection(uploadFileList) == true) {
            return null;
        }
        theLogger.debug("start to compress SseEvent zip file for " + sseEventSn);
        theLogger.debug("The storage collection :" + uploadFileList.toString());
        //
        SimpleDateFormat YYYYMMDD_FORMATTER = new SimpleDateFormat("yyyyMMdd");
        String uploadDate = YYYYMMDD_FORMATTER.format(new Date());
        //将资源对象的附近按以属性压缩成独立的文件

        for (int i = 0; i < uploadFileList.size(); i++) {

            UploadFile uploadFile = uploadFileList.get(i);
            if (uploadFile == null) {
                continue;
            }

            //压缩存储文件
            String storageFilePath = uploadDate + "/" + sseEventSn + "/" + className + "/" + Utils.getUUID32() + ZIP_FILE_STR;
            File storageFile = new File(_fileStorageHomeDirectory + File.separator + storageFilePath);
            //
            theLogger.debug("confirm compressing " + uploadFile.getParameterName() + " FileName:" + uploadFile.getFileName() + " Property value:" + storageFilePath);
            theLogger.debug("The Target zipFile is " + storageFile.getAbsolutePath());
            //
            List<UploadFile> compressFileList = new ArrayList<UploadFile>();
            compressFileList.add(uploadFile);
            try {
                storeUpLoadFile(storageFile, storageFilePath, compressFileList);
            } catch (Exception e) {
                theLogger.exception(e);
                throw e;
            }
        }

        return uploadFileList;
    }

    /**
     * 压缩存储文件.
     * 
     * @param storeTargetFile
     * @param compressFileList
     * @throws Exception
     */
    private void storeUpLoadFile(File storeTargetFile, String propertyValue, List<UploadFile> compressFileList) throws Exception {

        theLogger.debug("Start to store the target File:" + storeTargetFile.getAbsolutePath());
        theLogger.debug("The compressing collection size:" + compressFileList.size());
        //检查创建文件
        if (this.checkAndCreateFileOrDirectory(storeTargetFile) == false) {
            theLogger.debug("Create the target storage file fault,Path:" + storeTargetFile.getAbsolutePath());
            return;
        }
        //
        theLogger.debug("The Target zipFile is " + storeTargetFile.getAbsolutePath());
        ZipOutputStream zipOut = null;
        try {
            zipOut = new ZipOutputStream(new FileOutputStream(storeTargetFile));
            for (int i = 0; i < compressFileList.size(); i++) {
                //压缩指定文件夹或文件
                this.compressOneLevelFile(zipOut, compressFileList.get(i).getFileName(), compressFileList.get(i).getFile());
            }

        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipOut != null) {
                try {
                    zipOut.close();
                } catch (IOException e) {
                }
            }
        }

    }

    /**
     * 将一个文件压缩到指定的ZIP文件中.
     * 
     * @param zipOut
     * @param targetFiles
     */
    private void compressOneLevelFile(ZipOutputStream zipOut, String fileName, File targetFiles) {

        //不处理文件夹目标的压缩 附件压缩只一层
        if (targetFiles.isDirectory() == true) {
            theLogger.debug("Just accpet file to compress ,pass the directory:" + targetFiles.getAbsolutePath());
            return;
        }
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(targetFiles);
            theLogger.debug("ready to compress the File:" + targetFiles.getName());
            //增加压缩节点
            zipOut.putNextEntry(new ZipEntry(fileName));
            //分段写入压缩文件
            byte[] bytes = new byte[_ioBuffereSize];
            int len = 0;
            while ((len = fileInputStream.read(bytes)) > 0) {
                zipOut.write(bytes, 0, len);
            }
            //结束压缩节点
            zipOut.closeEntry();
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 取得解压文件中的附件文件.
     * 
     * @return
     */
    public File[] readFilesByPath(String[] filePathArray) {

        if (filePathArray == null || filePathArray.length == 0) {
            return null;
        }
        File[] decompressArray = new File[filePathArray.length];
        for (int i = 0; i < filePathArray.length; i++) {
            //
            String filePath = filePathArray[i];
            if (Assert.isEmptyString(filePath) == true) {
                continue;
            }
            //检查目标文件是否存在
            File targetFile = new File(_fileStorageHomeDirectory + File.separator + filePath);
            if (targetFile.exists() == false) {
                theLogger.error("File doesn't exist " + targetFile.getAbsolutePath());
                continue;
            }
            if (targetFile.isFile() == false) {
                theLogger.error("Target is not file " + targetFile.getAbsolutePath());
                continue;
            }
            decompressArray[i] = targetFile;
            /*
            try {
                //压缩指定文件夹或文件
                HashMap<String, File> fileMap = this.decompressFile(targetFile);
                //填入解压文件
                if (fileMap != null && fileMap.isEmpty() == false) {
                    decompressArray[i] = fileMap.values().iterator().next();
                }
            } catch (Exception e) {
                theLogger.exception(e);
            }
            */
        }
        return decompressArray;
    }

    /**
     * 
     * 解压zip文件.
     * 
     * @param zipFile
     * @return
     */
    public HashMap<String, File> apacheDecompressFile(File zipFile) {

        HashMap<String, File> fileMap = new HashMap<String, File>();
        //验证是否存在
        if (zipFile == null || zipFile.exists() == false) {
            return fileMap;
        }
        ZipArchiveInputStream zipInput = null;
        try {
            byte[] buffer = new byte[_ioBuffereSize];
            zipInput = new ZipArchiveInputStream(new BufferedInputStream(new FileInputStream(zipFile), buffer.length));
            ZipArchiveEntry entry = null;
            File destDir = new File(this._temporaryStorageDirectory + File.separator + zipFile.getName());
            while ((entry = zipInput.getNextZipEntry()) != null) {
                BufferedOutputStream fileOutputStream = null;
                try {
                    //创建临时目录
                    if (destDir.exists() == false) {
                        destDir.mkdir();
                    }
                    File file = new File(destDir, entry.getName());
                    //目录
                    if (entry.isDirectory()) {
                        File directory = new File(destDir, entry.getName());
                        if (directory.exists() == false) {
                            directory.mkdirs();
                        }
                        //文件    
                    } else {
                        fileOutputStream = new BufferedOutputStream(
                                new FileOutputStream(file), buffer.length);
                        IOUtils.copy(zipInput, fileOutputStream);
                        fileMap.put(entry.getName(), file);
                    }
                } catch (Exception e) {
                    theLogger.exception(e);
                } finally {
                    if (fileOutputStream != null) {
                        IOUtils.closeQuietly(fileOutputStream);
                    }
                }
            }
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipInput != null) {
                IOUtils.closeQuietly(zipInput);
            }
        }
        return fileMap;
    }

    /**
     * 将一个文件压缩到指定的ZIP文件中.
     * 
     * @throws IOException
     */
    public HashMap<String, File> decompressFile(File zipFile) {

        HashMap<String, File> fileMap = new HashMap<String, File>();
        ZipInputStream zipInput = null;
        try {
            zipInput = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry nextEntry = null;
            while ((nextEntry = zipInput.getNextEntry()) != null) {
                theLogger.debug("ready to decompress the File:" + nextEntry.getName());
                //
                BufferedOutputStream fileOutputStream = null;
                try {
                    File targetFile = new File(this._temporaryStorageDirectory + File.separator + zipFile.getName() + File.separator + nextEntry.getName());

                    if (targetFile.exists() == true) {
                        //文件已经解压过
                        fileMap.put(nextEntry.getName(), targetFile);
                        continue;
                    }
                    //如果是目录创建目录
                    if (nextEntry.isDirectory() == true) {
                        if (targetFile.mkdirs() == true) {
                            fileMap.put(nextEntry.getName(), targetFile);
                            continue;
                        } else {
                            throw new Exception("Can't create the directory:" + targetFile.getAbsolutePath());
                        }
                    }
                    //检查创建文件
                    if (this.checkAndCreateFileOrDirectory(targetFile) == false) {
                        throw new Exception("Can't create the File:" + targetFile.getAbsolutePath());
                    }
                    //如果是目录，已经被创建
                    if (targetFile.isDirectory() == true) {
                        continue;
                    }
                    int size;
                    byte[] buffer = new byte[_ioBuffereSize];

                    fileOutputStream = new BufferedOutputStream(new FileOutputStream(targetFile), buffer.length);
                    //读取压缩流中数据
                    while ((size = zipInput.read(buffer, 0, buffer.length)) != -1) {
                        fileOutputStream.write(buffer, 0, size);
                    }
                    fileOutputStream.flush();
                    //加入返回值
                    fileMap.put(nextEntry.getName(), targetFile);
                } catch (Exception e) {
                    theLogger.exception(e);
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                        }
                    }
                    try {
                        zipInput.closeEntry();
                    } catch (IOException e) {
                    }
                }
            }
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipInput != null) {
                try {
                    zipInput.close();
                } catch (IOException e) {
                }
            }
        }

        return fileMap;

    }

    /**
     * 取得压缩包内文件内容.
     * 
     * @param zipFile
     * @return
     */
    public HashMap<String, String> getCompressFileEntryDetail(File zipFile) {

        if (zipFile == null || zipFile.exists() == false || zipFile.isFile() == false) {
            return null;
        }
        HashMap<String, String> EntryDetailMap = new HashMap<String, String>();
        ZipInputStream zipInput = null;
        try {
            //读取ZIP包的节点信息
            zipInput = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry nextEntry = null;
            while ((nextEntry = zipInput.getNextEntry()) != null) {
                //
                try {
                    EntryDetailMap.put(nextEntry.getName(), String.valueOf(nextEntry.getSize()));
                } catch (Exception e) {
                    theLogger.exception(e);
                }
            }
        } catch (Exception e) {
            theLogger.exception(e);
        } finally {
            if (zipInput != null) {
                try {
                    zipInput.close();
                } catch (IOException e) {
                }
            }
        }

        return EntryDetailMap;

    }

    /**
     * 检查目标文件或目录是否存在，不存在就创建.
     * 
     * @param targetFile
     * @return
     */
    public boolean checkAndCreateFileOrDirectory(File targetFile) {

        if (targetFile == null) {
            return false;
        }
        try {
            //判断是否是目录，如果是目录直接创建
            if (targetFile.isDirectory() == true) {
                if (targetFile.exists() == false) {
                    return targetFile.mkdirs();
                } else {
                    return true;
                }
            }
            //目标是文件
            //判断是否存在上级目录，不存在就创建
            File parentFile = targetFile.getParentFile();
            if (parentFile.exists() == false) {
                if (parentFile.mkdirs() == false) {
                    theLogger.error("Crate new parent directory of target File fault, target file Path:" + targetFile.getAbsolutePath());
                    return false;
                }
            }
            //创建存储文件
            if (targetFile.createNewFile() == false) {
                theLogger.error("Crate new File fault, target file Path:" + targetFile.getAbsolutePath());
                return false;
            }
            return true;
        } catch (IOException e) {
            theLogger.exception(e);
        }
        return false;
    }

    /**
     * 删除指定的文件或目录.
     * 
     * @param deleteFile
     * @return
     */
    public boolean deleteFile(File deleteFile) {

        if (deleteFile == null || deleteFile.exists() == false) {
            return true;
        }

        // 路径为文件且不为空则进行删除  
        if (deleteFile.isFile() == true) {
            return deleteFile.delete();
        } else {
            File[] files = deleteFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                //删除子文件  
                if (deleteFile(files[i]) == false) {
                    return false;
                }
            }
            return deleteFile.delete();
        }

    }

}

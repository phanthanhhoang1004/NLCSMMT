package src.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Filedata implements Serializable {
    private int file_id;
    private String fileName;
    private int uploaderid;
    private Timestamp uploadTime;
    private long fileSize;
    private String uploaderName;
    private int receiverid;
    private String receiverName;
    private Timestamp sharedTime;
    private byte[] rawData;

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public Filedata() {
    }

    public Filedata(int uploaderId, String uploaderName, String fileName, long fileSize) {
        this.uploaderid = uploaderId;
        this.uploaderName = uploaderName;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    // Get
    public int getFileId() {
        return file_id;
    }

    public String getFileName() {
        return fileName;
    }

    public int getUploaderId() {
        return uploaderid;
    }

    public int getReceiverId() {
        return receiverid;
    }

    public Timestamp getUploadDate() {
        return uploadTime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public Timestamp getSharedTime() {
        return sharedTime;
    }

    public Timestamp getTimestamp() {
        return (sharedTime != null) ? sharedTime : uploadTime;
    }

    @Override
    public String toString() {
        return fileName;
    }

    // Set
    public void setFileId(int file_id) {
        this.file_id = file_id;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setUploaderId(int uploaderid) {
        this.uploaderid = uploaderid;
    }

    public void setReceiverId(int receiverid) {
        this.receiverid = receiverid;
    }

    public void setSharedTime(Timestamp sharedTime) {
        this.sharedTime = sharedTime;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
}

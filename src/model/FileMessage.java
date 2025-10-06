package src.model;

import java.io.Serializable;

public class FileMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private Filedata filedata;
    private byte[] fileBytes;
    private byte[] encryptedFileBytes;
    private byte[] encryptedAESKey;

    public FileMessage(Filedata filedata, byte[] encryptedFileBytes, byte[] encryptedAESKey) {
        this.filedata = filedata;
        this.encryptedFileBytes = encryptedFileBytes;
        this.encryptedAESKey = encryptedAESKey;
    }

    public FileMessage(Filedata filedata, byte[] fileBytes) {
        this.filedata = filedata;
        this.fileBytes = fileBytes;
    }

    public Filedata getFiledata() {
        return filedata;
    }

    public byte[] getEncryptedFileBytes() {
        return encryptedFileBytes;
    }

    public byte[] getEncryptedAESKey() {
        return encryptedAESKey;
    }

    public byte[] getFileBytes() {
        return fileBytes;
    }
}

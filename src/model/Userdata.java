package src.model;

import java.io.Serializable;

public class Userdata implements Serializable {
    private static final long serialVersionUID = 1L;
    private int uploaderId;
    private String uploaderName;
    private String password;
    private static Userdata instance = new Userdata();

    public Userdata() {
    }

    public void setUploader(int id, String name, String password) {
        uploaderId = id;
        uploaderName = name;
        this.password = password;
    }

    public static Userdata getInstance() {
        return instance;
    }

    public int getUploaderId() {
        return uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public String getPassword() {
        return password;
    }

    public void setUploaderId(int uploaderId) {
        this.uploaderId = uploaderId;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

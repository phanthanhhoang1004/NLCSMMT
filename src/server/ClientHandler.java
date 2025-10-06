package src.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import javafx.fxml.FXML;
import src.model.FileMessage;
import src.model.Filedata;
import src.model.Userdata;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    // private String username;

    // public String getUsernam() {
    // return username;
    // }

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            ois = new ObjectInputStream(socket.getInputStream());
            while (true) {
                try {
                    String command = ois.readUTF();
                    switch (command) {
                        case "LOGIN":
                            handleLogin();
                            break;
                        case "REGISTER":
                            handleRegister();
                            break;
                        case "UPLOAD":
                            handleUpload();
                            break;
                        case "DOWNLOAD_FILE":
                            handleDownloadFile();
                            break;
                        case "LOAD_SHARED_FILES":
                            handleLoadSharedFiles();
                            break;
                        case "LOAD_FILES":
                            loadAllFiles();
                            break;
                        case "INSERT_SHARED_FILE":
                            sharedFile();
                            break;
                        case "GET_USER_ID":
                            getUserByID();
                            break;
                        case "DELETE_FILE":
                            deleteFile();
                            break;
                        default:
                            System.out.println("Unknown command: " + command);
                    }
                } catch (Exception e) {
                    System.out.println("Dong ket noi voi client: " + socket.getInetAddress());
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                ServerMain.getConnectedClients().remove(this);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    private void loadAllFiles() {
        try {
            int uploader_id = ois.readInt();
            List<Filedata> filelist = ServerDAO.getAllFiles(uploader_id);
            oos.writeObject(filelist);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLogin() {
        try {
            String user = ois.readUTF();
            String pass = ois.readUTF();
            int user_id = ServerDAO.login(user, pass);
            oos.writeInt(user_id);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleRegister() {
        try {
            String user = ois.readUTF();
            String pass = ois.readUTF();
            boolean success = ServerDAO.register(user, pass);
            oos.writeBoolean(success);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleUpload() throws IOException, ClassNotFoundException {
        try {
            Filedata data = (Filedata) ois.readObject();
            long fileSize = ois.readLong();
            // Lưu file
            File uploadsDir = new File("data/uploads");
            if (!uploadsDir.exists())
                uploadsDir.mkdirs();
            File file = new File(uploadsDir, data.getFileName());
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                long totalRead = 0;
                while (totalRead < fileSize) {
                    int read = ois.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalRead));
                    if (read > 0) {

                        fos.write(buffer, 0, read);
                        totalRead += read;
                    }
                    if (read == -1)
                        break;
                }
            }
            ServerDAO.SaveFile(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleLoadSharedFiles() throws IOException {
        try {
            int receiverid = (int) ois.readInt();
            List<Filedata> sharedFiles = ServerDAO.getSharedFiles(receiverid);
            oos.writeObject(sharedFiles);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sharedFile() {
        try {
            Filedata sharedFile = (Filedata) ois.readObject();
            int senderid = (Integer) ois.readInt();
            int receiverid = (Integer) ois.readInt();
            ServerDAO.insertSharedFile(sharedFile, senderid, receiverid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteFile() {
        try {
            Filedata fileToDelete = (Filedata) ois.readObject();
            ServerDAO.deleteFile(fileToDelete);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getUserByID() {
        try {
            String username = (String) ois.readUTF();
            int userId = ServerDAO.getUserIdFromDB(username);
            oos.writeInt(userId);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDownloadFile() throws IOException, ClassNotFoundException {
        try {
            int fileId = ois.readInt();
            Filedata filedata = ServerDAO.getFileById(fileId);
            if (filedata == null) {
                System.out.println("Không tìm thấy file ID: " + fileId);
                return;
            }
            File file = new File("data/uploads", filedata.getFileName());
            if (!file.exists()) {
                System.out.println("File không tồn tại trong thư mục uploads.");
                return;
            }
            oos.writeObject(filedata);
            oos.flush();
            System.out.println("Đã gửi metadata file: " + filedata.getFileName());

            oos.writeLong(file.length());
            oos.flush();

            // Gửi file theo từng đoạn
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    oos.write(buffer, 0, read);
                }
                oos.flush();
            }

            System.out.println("Đã gửi file: " + filedata.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isAlive() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

}

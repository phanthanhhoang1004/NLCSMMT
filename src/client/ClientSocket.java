package src.client;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.stage.FileChooser;
import src.model.FileMessage;
import src.model.Filedata;
import src.model.Userdata;

public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ClientSocket() throws IOException {
        socket = new Socket("localhost", 5000);
        socket.setSoLinger(true, 0);
        oos = new ObjectOutputStream(socket.getOutputStream());
        oos.flush();
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public static ClientSocket getInstance() throws IOException {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public List<Filedata> loadAllFiles() throws IOException, ClassNotFoundException {
        oos.writeUTF("LOAD_FILES");
        oos.flush();
        oos.writeInt(Userdata.getInstance().getUploaderId());
        oos.flush();
        return (List<Filedata>) ois.readObject();
    }

    public void uploadFile(File file, Filedata data) throws IOException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException("File không tồn tại.");
        }

        oos.writeUTF("UPLOAD");
        oos.flush();
        oos.writeObject(data);
        oos.flush();
        oos.writeLong(file.length());

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                oos.write(buffer, 0, bytesRead);
            }
        }
        oos.flush();
    }

    public int sendLogin(String username, String password) throws IOException, ClassNotFoundException {
        oos.writeUTF("LOGIN");
        oos.flush();
        oos.writeUTF(username);
        oos.writeUTF(password);
        oos.flush();
        return ois.readInt();
    }

    public boolean sendregister(String user, String pass) {
        try {
            oos.writeUTF("REGISTER");
            oos.writeUTF(user);
            oos.writeUTF(pass);
            oos.flush();
            return ois.readBoolean();
        } catch (IOException e) {
            return false;
        }
    }

    public void sendSharedFile(Filedata file, int senderid, int receiverid) throws IOException {
        oos.writeUTF("INSERT_SHARED_FILE");
        oos.flush();
        oos.writeObject(file);
        oos.flush();
        oos.writeInt(senderid);
        oos.flush();
        oos.writeInt(receiverid);
        oos.flush();
    }

    public List<Filedata> loadSharedFiles(int receiverid) throws IOException, ClassNotFoundException {
        oos.writeUTF("LOAD_SHARED_FILES");
        oos.flush();
        oos.writeInt(receiverid);
        oos.flush();
        return (List<Filedata>) ois.readObject();
    }

    public void deleteFile(Filedata file) {
        try {
            oos.writeUTF("DELETE_FILE");
            oos.flush();
            oos.writeObject(file);
            oos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getUserIdByUsername(String username) throws IOException {
        oos.writeUTF("GET_USER_ID");
        oos.writeUTF(username);
        oos.flush();
        return ois.readInt();
    }

    public void downloadFileTo(int fileid, File saveFile) {
        try {
            oos.writeUTF("DOWNLOAD_FILE");
            oos.writeInt(fileid);
            oos.flush();

            Filedata metadata = (Filedata) ois.readObject();
            long fileSize = ois.readLong();

            try (FileOutputStream fos = new FileOutputStream(saveFile)) {
                byte[] buffer = new byte[4096];
                long total = 0;
                while (total < fileSize) {
                    int read = ois.read(buffer, 0, (int) Math.min(buffer.length, fileSize - total));
                    if (read == -1)
                        throw new EOFException("Mất kết nối khi tải file.");
                    fos.write(buffer, 0, read);
                    total += read;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

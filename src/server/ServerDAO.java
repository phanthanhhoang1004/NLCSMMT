package src.server;

import java.sql.*;
import java.util.*;
import org.mindrot.jbcrypt.BCrypt;
import src.model.Filedata;

public class ServerDAO {
    public static void insertSharedFile(Filedata file, int senderid, int receiverid) throws Exception {
        String sql = "INSERT INTO file_share (file_id, sender_id, receiver_id) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, file.getFileId());
            ps.setInt(2, senderid);
            ps.setInt(3, receiverid);
            ps.executeUpdate();
            System.out.println("File chia sẻ đã được lưu vào DB: " + file.getFileName());
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi lưu file chia sẻ vào DB: " + e.getMessage());
        }
    }

    public static List<Filedata> getAllFiles(int uploader_id) {
        List<Filedata> list = new ArrayList<>();
        String sql = "SELECT f.id, f.file_name, f.upload_time, f.file_size, u.username FROM files f JOIN users u ON f.uploader_id = u.id WHERE f.uploader_id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uploader_id);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Filedata file = new Filedata();
                file.setFileId(rs.getInt("id"));
                file.setFileName(rs.getString("file_name"));
                file.setUploadTime(rs.getTimestamp("upload_time"));
                file.setFileSize(rs.getLong("file_size"));
                file.setUploaderId(uploader_id);
                file.setUploaderName(rs.getString("username"));
                list.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static List<Filedata> getSharedFiles(int receiverid) {
        List<Filedata> sharedFiles = new ArrayList<>();
        String sql = """
                    SELECT f.file_name, f.file_size, f.upload_time, u1.username AS sender_name, fs.shared_at
                    FROM file_share fs
                    JOIN files f ON fs.file_id = f.id
                    JOIN users u1 ON fs.sender_id = u1.id
                    WHERE fs.receiver_id = ?
                """;
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, receiverid);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Filedata file = new Filedata();
                file.setFileName(rs.getString("file_name"));
                file.setSharedTime(rs.getTimestamp("shared_at"));
                file.setFileSize(rs.getLong("file_size"));
                file.setUploaderName(rs.getString("sender_name"));
                sharedFiles.add(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sharedFiles;
    }

    public static void SaveFile(Filedata data) {
        String sql = "INSERT INTO files (uploader_id, file_name, file_size) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, data.getUploaderId());
            ps.setString(2, data.getFileName());
            ps.setLong(3, data.getFileSize());
            ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("File đã tồn tại.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int login(String username, String password) throws Exception {
        String sql = "SELECT id, password FROM users WHERE username=?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (BCrypt.checkpw(password, hashedPassword)) {
                    return rs.getInt("id");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean register(String username, String password) throws Exception {
        String checksql = "SELECT * FROM users WHERE username = ?";
        try {
            Connection conn = DBConnection.getConnection();
            // Kiểm tra tồn tại
            PreparedStatement check = conn.prepareStatement(checksql);
            check.setString(1, username);
            ResultSet rs = check.executeQuery();
            if (rs.next())
                return false;

            // Thêm mới
            String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
            String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt(12));
            PreparedStatement insert = conn.prepareStatement(sql);
            insert.setString(1, username);
            insert.setString(2, hashPassword);
            insert.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteFile(Filedata file) {
        String sql1 = "DELETE FROM file_share WHERE file_id = ?";
        String sql2 = "DELETE FROM files WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps1 = conn.prepareStatement(sql1);
                PreparedStatement ps2 = conn.prepareStatement(sql2)) {

            // Xóa các bản ghi liên quan trong bảng chia sẻ
            ps1.setInt(1, file.getFileId());
            ps1.executeUpdate();

            // Xóa file chính
            ps2.setInt(1, file.getFileId());
            ps2.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getUserIdFromDB(String username) throws Exception {
        int id = -1;
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    id = rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static Filedata getFileById(int fileId) {
        Filedata filedata = null;
        String sql = "SELECT * FROM files WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                filedata = new Filedata();
                filedata.setFileId(rs.getInt("id"));
                filedata.setUploaderId(rs.getInt("uploader_id"));
                filedata.setFileName(rs.getString("file_name"));
                filedata.setFileSize(rs.getLong("file_size"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return filedata;
    }

}

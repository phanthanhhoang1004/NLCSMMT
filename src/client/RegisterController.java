package src.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.stage.Stage;
import src.client.untils.RSAKeyUtil;
import src.server.ServerDAO;

public class RegisterController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Hyperlink loign;

    @FXML
    private void handleLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ tên và mật khẩu");
            return;
        }

        try {
            ClientSocket clientSocket = ClientSocket.getInstance();
            boolean success = clientSocket.sendregister(username, password);
            if (success) {
                RSAKeyUtil.generateKeyPair(username);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký thành công. Vui lòng đăng nhập.");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
                Parent root = loader.load();
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
            } else {
                showAlert(Alert.AlertType.WARNING, "Thất bại", "Tên người dùng đã tồn tại.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi đăng ký");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

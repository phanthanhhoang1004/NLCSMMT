package src.client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import src.model.Userdata;
import src.server.ServerDAO;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Hyperlink register;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ tên và mật khẩu");
            return;
        }

        try {
            ClientSocket clientSocket = ClientSocket.getInstance();
            int userId = clientSocket.sendLogin(username, password);
            if (userId != -1) {
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đăng nhập thành công");
                Userdata.getInstance().setUploader(userId, username, password);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
                Parent root = loader.load();
                Controller controller = loader.getController();

                Stage stage = (Stage) usernameField.getScene().getWindow();
                controller.initialize(clientSocket, stage);
                stage.setScene(new Scene(root));
                stage.setTitle("Ứng dụng quản lý file");
                stage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Sai tên đăng nhập hoặc mật khẩu");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Register.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng ký");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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

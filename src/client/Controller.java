package src.client;

import javafx.stage.*;
import src.client.untils.FileUntil;
import src.model.FileMessage;
import src.model.Filedata;
import src.model.Userdata;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SimpleTimeZone;

import com.jfoenix.controls.JFXListView;

public class Controller {
    @FXML
    private TableView<Filedata> fileTableView;
    @FXML
    private TableColumn<Filedata, String> nameColumn;
    @FXML
    private TableColumn<Filedata, String> sizeColumn;
    @FXML
    private TableColumn<Filedata, String> uploaderColumn;
    @FXML
    private TableColumn<Filedata, String> timeColumn;
    @FXML
    private TableView<Filedata> sharedFileTableView;
    @FXML
    private TableColumn<Filedata, String> sharedNameColumn;
    @FXML
    private TableColumn<Filedata, String> sharedSizeColumn;
    @FXML
    private TableColumn<Filedata, String> sharedUploaderColumn;
    @FXML
    private TableColumn<Filedata, String> sharedTimeColumn;
    @FXML
    private BorderPane mainPane;
    @FXML
    private TextField uploaderName;
    @FXML
    private Label usernameLabel;
    @FXML
    private Label uploaderNameLabel;
    @FXML
    private TextField receiverField;
    @FXML
    private Label fileName;
    @FXML
    private Label fileSize;
    @FXML
    private Label uploadDate;
    @FXML
    private TextField searchField;
    @FXML
    private VBox fileDetail;
    private List<Filedata> allFiles = new ArrayList<>();
    private List<Filedata> receivedFiles = new ArrayList<>();
    private static TableView<Filedata> staticFileTableView;
    private static TableView<Filedata> staticReceivedTableView;
    private ClientSocket clientSocket;
    private Stage stage;
    private File selectedFile;
    private Userdata userdata = new Userdata();
    private static Controller controllerInstance;

    public Controller() {
        controllerInstance = this;
    }

    public static Controller getControllerInstance() {
        return controllerInstance;
    }

    public void initialize(ClientSocket clientSocket, Stage stage) throws Exception {
        this.clientSocket = clientSocket;
        this.stage = stage;
        usernameLabel.setText(Userdata.getInstance().getUploaderName());
        setupTableColumns(nameColumn, uploaderColumn, sizeColumn, timeColumn, false);
        setupTableColumns(sharedNameColumn, sharedUploaderColumn, sharedSizeColumn, sharedTimeColumn, true);
        loadFile();
        loadSharedFile();
        staticFileTableView = fileTableView;
        staticReceivedTableView = sharedFileTableView;
        RabbitMQReceiver receiver = new RabbitMQReceiver(clientSocket);
        receiver.startReceiving(Userdata.getInstance().getUploaderName());
        mainPane.setRight(null);
    }

    public void setupTableColumns(
            TableColumn<Filedata, String> nameCol,
            TableColumn<Filedata, String> uploaderCol,
            TableColumn<Filedata, String> sizeCol,
            TableColumn<Filedata, String> timeCol,
            boolean isShared) {
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFileName()));
        uploaderCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUploaderName()));
        sizeCol.setCellValueFactory(
                cellData -> new SimpleStringProperty(FileUntil.readableFileSize(cellData.getValue().getFileSize())));
        timeCol.setCellValueFactory(cellData -> {
            Timestamp timestamp = isShared ? cellData.getValue().getSharedTime()
                    : cellData.getValue().getUploadDate();
            String formattedTime = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(timestamp);
            return new ReadOnlyStringWrapper(formattedTime);
        });
    }

    private void loadSharedFile() {
        try {
            List<Filedata> sharedFiles = clientSocket.loadSharedFiles(Userdata.getInstance().getUploaderId());
            receivedFiles.clear();
            receivedFiles.addAll(sharedFiles);
            sharedFileTableView.setItems(FXCollections.observableArrayList(receivedFiles));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addReceivedFileToTable(Filedata filedata) {
        if (staticReceivedTableView != null) {
            staticReceivedTableView.getItems().add(filedata);
            System.out.println("Da nhan file tu nguoi khac: " + filedata.getFileName());
        } else {
            System.err.println("⚠️ staticReceivedTableView chưa được khởi tạo");
        }

    }

    public void loadFile() throws ClassNotFoundException, IOException {
        List<Filedata> Files = clientSocket.loadAllFiles();
        allFiles.clear();
        allFiles.addAll(Files);
        fileTableView.setItems(FXCollections.observableArrayList(allFiles));
    }

    @FXML
    private void showReceivedFiles() {
        fileTableView.setVisible(false);
        sharedFileTableView.setVisible(true);
    }

    @FXML
    private void showMyFiles() {
        fileTableView.setVisible(true);
        sharedFileTableView.setVisible(false);
    }

    @FXML
    private void handleUpload() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn tệp để tải lên");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tất cả các tệp", "*.*"));
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile == null)
            return;
        if (selectedFile != null) {
            try {
                int user_id = Userdata.getInstance().getUploaderId();
                String uploaderName = Userdata.getInstance().getUploaderName();
                String fileName = selectedFile.getName();
                long fizeSize = selectedFile.length();
                Filedata data = new Filedata(user_id, uploaderName, fileName, fizeSize);
                clientSocket.uploadFile(selectedFile, data);
                loadFile();
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Tải lên thành công");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải file");
            }

        }

    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleDocumentClick() {
        Filedata selected = null;
        if (fileTableView.getSelectionModel().getSelectedItem() != null) {
            selected = fileTableView.getSelectionModel().getSelectedItem();
        } else if (sharedFileTableView.getSelectionModel().getSelectedItem() != null) {
            selected = sharedFileTableView.getSelectionModel().getSelectedItem();
        }
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn file");
            return;
        }
        if (selected != null) {
            fileName.setText(selected.getFileName());
            uploadDate.setText(FileUntil.formatUploadTime(selected.getTimestamp()));
            fileSize.setText(FileUntil.readableFileSize(selected.getFileSize()));
            uploaderNameLabel.setText(selected.getUploaderName());
            mainPane.setRight(fileDetail);
        }
    }

    @FXML
    public void shareFile(ActionEvent event) throws IOException {
        Filedata file = fileTableView.getSelectionModel().getSelectedItem();

        if (file == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn file để chia sẻ");
            return;
        }
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Chia sẻ tài liệu");
        dialog.setHeaderText("Nhập tên người nhận:");
        dialog.setContentText("Tên:");
        Stage stage = (Stage) fileTableView.getScene().getWindow();
        dialog.initOwner(stage);
        dialog.showAndWait().ifPresent(receiver -> {
            if (receiver.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "⚠️ Cảnh báo", "Vui lòng nhập tên người nhận.");
                return;
            }

            if (file != null) {
                try {
                    int senderId = Userdata.getInstance().getUploaderId();
                    int receiverId = clientSocket.getUserIdByUsername(receiver);
                    file.setSharedTime(new Timestamp(System.currentTimeMillis()));
                    clientSocket.sendSharedFile(file, senderId, receiverId);

                    File sharedFile = new File("data\\uploads\\" + file.getFileName());
                    byte[] fileBytes = Files.readAllBytes(sharedFile.toPath());
                    RabbitMQSender sender = new RabbitMQSender();
                    sender.send(receiver, file, fileBytes);
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Chia sẻ thành công");
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể chia sẻ file");
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    private void handleDeleteFile() {
        Filedata selectedFile = fileTableView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Vui lòng chọn một file để xóa.");
            return;
        }

        try {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Xác nhận xóa");
            alert.setHeaderText("Bạn có chắc chắn muốn xóa tài liệu này?");
            Optional<ButtonType> result = alert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                clientSocket.deleteFile(selectedFile);
                fileTableView.getItems().remove(selectedFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi khi gửi yêu cầu xóa file.");
        }
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (keyword == null || keyword.isEmpty()) {
            fileTableView.setItems(FXCollections.observableArrayList(allFiles));
            sharedFileTableView.setItems(FXCollections.observableArrayList(receivedFiles));
            return;
        }

        List<Filedata> filteredFiles = allFiles.stream()
                .filter(file -> file.getFileName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        List<Filedata> filteredSharedFiles = receivedFiles.stream()
                .filter(file -> file.getFileName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();

        fileTableView.setItems(FXCollections.observableArrayList(filteredFiles));
        sharedFileTableView.setItems(FXCollections.observableArrayList(filteredSharedFiles));

    }

    @FXML
    private void handleDownload() {
        Filedata selected = null;

        if (fileTableView.getSelectionModel().getSelectedItem() != null) {
            selected = fileTableView.getSelectionModel().getSelectedItem();
        } else if (sharedFileTableView.getSelectionModel().getSelectedItem() != null) {
            selected = sharedFileTableView.getSelectionModel().getSelectedItem();
        }

        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một file để tải xuống.");
            return;
        }
        if (selected != null) {
            try {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Lưu File");
                chooser.setInitialFileName(selected.getFileName());
                File saveFile = chooser.showSaveDialog(null);
                if (saveFile == null)
                    return;
                clientSocket.downloadFileTo(selected.getFileId(), saveFile);
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tải xuống thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải xuống file.");
            }
        }
    }

    @FXML
    private void handleRefresh() throws ClassNotFoundException, IOException {
        loadFile();
        loadSharedFile();
        mainPane.setRight(null);
    }

}

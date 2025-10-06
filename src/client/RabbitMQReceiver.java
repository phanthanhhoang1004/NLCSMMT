package src.client;

import com.rabbitmq.client.*;
import javafx.application.Platform;
import src.client.untils.DecryptionUtil;
import src.client.untils.EncryptionUtil;
import src.client.untils.RSAKeyUtil;
import src.model.FileMessage;
import src.model.Filedata;
import src.model.Userdata;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class RabbitMQReceiver {
    private final ClientSocket clientSocket;

    public RabbitMQReceiver(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private static final String EXCHANGE_NAME = "file_exchange";

    public void startReceiving(String username) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");
        String queueName = "queue_" + username;
        channel.queueDeclare(queueName, true, false, true, null);
        channel.queueBind(queueName, EXCHANGE_NAME, queueName);

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            try (ByteArrayInputStream bis = new ByteArrayInputStream(delivery.getBody());
                    ObjectInputStream ois = new ObjectInputStream(bis)) {

                Object obj = ois.readObject();
                if (obj instanceof FileMessage msg) {
                    SecretKey aesKey = DecryptionUtil.decryptAESKey(msg.getEncryptedAESKey(),
                            RSAKeyUtil.loadPrivateKey(username));
                    byte[] fileBytes = DecryptionUtil.decryptFile(msg.getEncryptedFileBytes(), aesKey);

                    Platform.runLater(() -> {
                        System.out.println("File nhan duoc: " + msg.getFiledata().getFileName());
                        Controller.addReceivedFileToTable(msg.getFiledata());
                    });

                }
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi đọc object RabbitMQ:");
                e.printStackTrace();
            }
        };
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
        });
        System.out.println("📥 Dang lang nghe " + queueName + " voi routing key: " + queueName);

    }
}

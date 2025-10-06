package src.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;

import src.client.untils.EncryptionUtil;
import src.client.untils.RSAKeyUtil;
import src.model.FileMessage;
import src.model.Filedata;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

public class RabbitMQSender {
    private static final String EXCHANGE_NAME = "file_exchange";

    public void send(String receiverUsername, Filedata filedata, byte[] fileBytes)
            throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
                Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            SecretKey aesKey = EncryptionUtil.generateAESKey();
            byte[] encryptedFile = EncryptionUtil.encryptAES(fileBytes, aesKey);
            byte[] encryptedAESKey = EncryptionUtil.encryptRSA(aesKey.getEncoded(),
                    RSAKeyUtil.loadPublicKey(receiverUsername));

            FileMessage message = new FileMessage(filedata, encryptedFile, encryptedAESKey);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(message);
            oos.flush();
            byte[] data = bos.toByteArray();
            String routingKey = "queue_" + receiverUsername;
            channel.basicPublish(EXCHANGE_NAME, routingKey, true, null, data);
            System.out.println("📤 Da gui file'" + message.getFiledata().getFileName()
                    + "' den '" + receiverUsername + "' qua exchange '" + EXCHANGE_NAME + "'");

        }
    }
}

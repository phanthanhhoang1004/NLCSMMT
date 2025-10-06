package src.client.untils;

import java.io.*;
import java.security.*;

public class RSAKeyUtil {
    public static void generateKeyPair(String username) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        File keyDir = new File("keys");
        if (!keyDir.exists()) {
            keyDir.mkdirs();
        }

        try (ObjectOutputStream outPub = new ObjectOutputStream(new FileOutputStream("keys/" + username + "_pub.key"));
                ObjectOutputStream outPriv = new ObjectOutputStream(
                        new FileOutputStream("keys/" + username + "_priv.key"))) {
            outPub.writeObject(pair.getPublic());
            outPriv.writeObject(pair.getPrivate());
        }
    }

    public static PublicKey loadPublicKey(String username) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("keys/" + username + "_pub.key"))) {
            return (PublicKey) in.readObject();
        }
    }

    public static PrivateKey loadPrivateKey(String username) throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("keys/" + username + "_priv.key"))) {
            return (PrivateKey) in.readObject();
        }
    }
}

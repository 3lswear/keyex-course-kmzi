package com.roma;

import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.roma.CryptoUtil.printHex;
//import static com.roma.Server.encrypt;


public class Client {

    private static final String ALGORITHM = "RSA";

    public static byte[] encrypt(byte[] publicKey, byte[] inputData) throws Exception {

        PublicKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        // 512 is keysize
        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }

    public static void main(String args[]) throws Exception {

        Socket s = new Socket("localhost", 31337);
        System.out.println("[+] Connected to 31337");

        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        KeyPair generateKeyPair = generateKeyPair();

        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
        byte[] foreignKey = new byte[0];
        byte[] encryptedMsg = new byte[0];

        System.out.println("[+] Sending pubkey...");
//        System.out.println("my pubkey: " + new String(publicKey));
        dout.writeInt(publicKey.length);
        dout.write(publicKey);

        System.out.println("[+] Recieving pubkey...");
        int fKeyLength = din.readInt();
        if (fKeyLength > 0) {
            foreignKey = new byte[fKeyLength] ;
            din.readFully(foreignKey, 0, fKeyLength); // read the message
        }
//        System.out.println("recieved pubkey ->" + new String(foreignKey));

        String sendstr = "";
        String recstr = "";
        while (!sendstr.equals("stop")) {
            System.out.print("[client] Send a message -> ");
            sendstr = br.readLine();

            encryptedMsg = encrypt(foreignKey, sendstr.getBytes(StandardCharsets.UTF_8));
            System.out.println("[+] Encrypting message: ");
            printHex(encryptedMsg);

            dout.writeInt(encryptedMsg.length);
            dout.write(encryptedMsg);
//            dout.writeUTF(sendstr);
            dout.flush();

            recstr = din.readUTF();
            System.out.println("[+] Server\'s response: " + recstr);
        }

        System.out.println("[+] Connection aborted by user");
        dout.close();
        s.close();
    }
}

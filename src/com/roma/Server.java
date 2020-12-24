package com.roma;

import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.roma.CryptoUtil.printHex;

public class Server {


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
        ServerSocket ss = new ServerSocket(31337);
        System.out.println("[+] Listening on 31337");
        Socket s = ss.accept();
        System.out.println("[+] Connection accepted");

        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        KeyPair generateKeyPair = generateKeyPair();

        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
        byte[] foreignKey;
        byte[] encryptedMsg = new byte[0];

        System.out.println("[+] Recieving pubkey...");
        int fKeyLength = din.readInt();
        if (fKeyLength > 0) {
            foreignKey = new byte[fKeyLength] ;
            din.readFully(foreignKey, 0, fKeyLength); // read the message
        }
//        System.out.println("recieved pubkey ->" + new String(foreignKey));

        System.out.println("[+] Sending pubkey...");
//        System.out.println("my pubkey: " + new String(publicKey));
        dout.writeInt(publicKey.length);
        dout.write(publicKey);

        String str = "", str2 = "";
        while (!str.equals("stop")) {
//            str = din.readUTF();
            int msgLen = din.readInt();
            if (msgLen > 0) {
                encryptedMsg = new byte[msgLen] ;
                din.readFully(encryptedMsg, 0, msgLen); // read the message
            }
            System.out.print("[+] Recieved encrypted message: ");
            printHex(encryptedMsg);
            str = new String(decrypt(privateKey, encryptedMsg));
            System.out.println("[+] Client\'s response: " + str);

            System.out.print("[server] Send a message -> ");
            str2 = br.readLine();
            dout.writeUTF(str2);
            dout.flush();
        }
        System.out.println("[+] Connection aborted by user");
        din.close();
        s.close();
        ss.close();
    }
}

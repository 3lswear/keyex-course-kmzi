package com.roma;

import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.roma.CryptoUtil.*;


public class Client {

    public static void main(String args[]) throws Exception {

        Socket s = new Socket("localhost", port);
        System.out.println("[+] Connected to " + port);

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
        sendBytes(publicKey, dout);

        System.out.println("[+] Recieving pubkey...");
        foreignKey = receiveBytes(din);
//        System.out.println("recieved pubkey ->" + new String(foreignKey));

        String sendstr = "";
        String recstr = "";
        while (!recstr.equals("stop") && !sendstr.equals("stop")) {
            System.out.print("[client] Send a message -> ");
            sendstr = br.readLine();

            encryptedMsg = encrypt(foreignKey, sendstr.getBytes(StandardCharsets.UTF_8));
            System.out.println("[+] Encrypting message: ");
            printHex(encryptedMsg);

            sendBytes(encryptedMsg, dout);

            encryptedMsg = receiveBytes(din);
            System.out.print("[+] Recieved encrypted message: ");
            printHex(encryptedMsg);
            recstr = new String(decrypt(privateKey, encryptedMsg));
            System.out.println("[+] Client\'s response: " + recstr);
        }

        System.out.println("[+] Connection aborted by user");
        dout.close();
        s.close();
    }
}

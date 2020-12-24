package com.roma;

import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import static com.roma.CryptoUtil.*;

public class Server {

    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("[+] Listening on " + port);
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
        foreignKey = receiveBytes(din);
//        System.out.println("recieved pubkey ->" + new String(foreignKey));

        System.out.println("[+] Sending pubkey...");
//        System.out.println("my pubkey: " + new String(publicKey));
        sendBytes(publicKey, dout);

        String recstr = "", sendstr = "";
        while (!recstr.equals("stop") && !sendstr.equals("stop")) {
            encryptedMsg = receiveBytes(din);
            System.out.print("[+] Recieved encrypted message: ");
            printHex(encryptedMsg);
            recstr = new String(decrypt(privateKey, encryptedMsg));
            System.out.println("[+] Client\'s response: " + recstr);

            System.out.print("[server] Send a message -> ");
            sendstr = br.readLine();

            encryptedMsg = encrypt(foreignKey, sendstr.getBytes(StandardCharsets.UTF_8));
            System.out.println("[+] Encrypting message: ");
            printHex(encryptedMsg);

            sendBytes(encryptedMsg, dout);
        }

        System.out.println("[+] Connection aborted by user");
        din.close();
        s.close();
        ss.close();
    }
}

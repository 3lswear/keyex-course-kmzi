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
        System.out.println("[+] Listening on port " + port);
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

        System.out.println("[+] Receiving pubkey...");
        foreignKey = receiveBytes(din);
//        System.out.println("received pubkey ->" + new String(foreignKey));
        if (foreignKey.length > 0)
            System.out.println("[+] Successfully received pubkey!");

        System.out.println("[+] Sending pubkey...");
//        System.out.println("my pubkey: " + new String(publicKey));
        sendBytes(publicKey, dout);

        String recstr = "", sendstr = "";
        while (!recstr.equals("stop") && !sendstr.equals("stop")) {
            encryptedMsg = receiveBytes(din);
            System.out.print("[+] Received encrypted message: ");
            printHex(encryptedMsg);
            recstr = new String(decrypt(privateKey, encryptedMsg));
            System.out.println("[+] Bob\'s response: " + recstr);

            System.out.print("[Alice] Send a message -> ");
            sendstr = br.readLine();
            encryptedMsg = encrypt(foreignKey, sendstr.getBytes(StandardCharsets.UTF_8));

            sendBytes(encryptedMsg, dout);
            System.out.print("[+] Sent encrypted message: ");
            printHex(encryptedMsg);
        }

        System.out.println("[x] Connection aborted by user");
        din.close();
        s.close();
        ss.close();
    }
}

package com.roma;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;

import static com.roma.CryptoUtil.*;


public class Client {

    public static void main(String args[]) throws Exception {

        Socket s = new Socket("localhost", port);
        System.out.println("[+] Connected to port " + port);

        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        KeyPair generateKeyPair = generateKeyPair();

        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();
        byte[] foreignKey = new byte[0];
        byte[] encryptedMsg = new byte[0];

        System.out.println("[+] Sending pubkey...");
        sendBytes(publicKey, dout);

        System.out.println("[+] Receiving pubkey...");
        foreignKey = receiveBytes(din);
        if (foreignKey.length > 0)
            System.out.println("[+] Successfully received pubkey!");

        String sendstr = "";
        String recstr = "";
        while (!recstr.equals("stop") && !sendstr.equals("stop")) {
            System.out.print("[Bob] Send a message -> ");
            sendstr = br.readLine();
            encryptedMsg = encrypt(foreignKey, sendstr.getBytes(StandardCharsets.UTF_8));

            sendBytes(encryptedMsg, dout);
            System.out.print("[+] Sent encrypted message: ");
            printHex(encryptedMsg);

            encryptedMsg = receiveBytes(din);
            System.out.print("[+] Received encrypted message: ");
            printHex(encryptedMsg);
            recstr = new String(decrypt(privateKey, encryptedMsg));
            System.out.println("[+] Alice\'s response: " + recstr);
        }

        System.out.println("[x] Connection aborted by user");
        dout.close();
        s.close();
    }
}

package com.roma;

import java.net.*;
import java.io.*;

public class Client {
    public static void main(String args[]) throws Exception {
        Socket s = new Socket("localhost", 31337);
        System.out.println("[+] Connected to 31337");
        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String sendstr = "";
        String recstr = "";
        while (!sendstr.equals("stop")) {
            System.out.print("[client] Send a message -> ");
            sendstr = br.readLine();
            dout.writeUTF(sendstr);
            dout.flush();
            recstr = din.readUTF();
            System.out.println("[+] Server\'s response: " + recstr);
        }

        System.out.println("[+] Connection aborted by user");
        dout.close();
        s.close();
    }
}

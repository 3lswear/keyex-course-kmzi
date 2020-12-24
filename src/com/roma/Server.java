package com.roma;

import java.net.*;
import java.io.*;

public class Server {
    public static void main(String args[]) throws Exception {
        ServerSocket ss = new ServerSocket(31337);
        System.out.println("[+] Listening on 31337");
        Socket s = ss.accept();
        System.out.println("[+] Connection accepted");
        DataInputStream din = new DataInputStream(s.getInputStream());
        DataOutputStream dout = new DataOutputStream(s.getOutputStream());
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String str = "", str2 = "";
        while (!str.equals("stop")) {
            str = din.readUTF();
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

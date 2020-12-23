package com.roma;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) throws InterruptedException {

        try (ServerSocket server = new ServerSocket(31337)) {
            System.out.println("[+] Server listening on 31337");
            Socket client = server.accept();
            System.out.println("Connection accepted.");

            DataOutputStream out = new DataOutputStream(client.getOutputStream());
            DataInputStream in = new DataInputStream(client.getInputStream());

            while (!client.isClosed()) {
                System.out.println("Server reading from channel");
                String entry = in.readUTF();
                System.out.println("READ from client message - " + entry);
                System.out.println("Server try writing to channel");

                if (entry.equalsIgnoreCase("quit")) {
                    System.out.println("Client initialize connections suicide ...");
                    out.writeUTF("Server reply - " + entry + " - OK");
                    out.flush();
                    break;
                }
                out.writeUTF("Server reply - " + entry + " - OK");
                out.flush();
            }
            System.out.println("Client disconnected");
            in.close();
            out.close();
            client.close();
            System.out.println("Closing connections & channels - DONE.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
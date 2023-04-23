package com.example.mchatserver.classes;

//import com.example.mchatserver.models.User;
import com.example.mchatserver.DBUtils;
import models.User;
import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.*;


public class Server {
    ServerSocket serverSocket;
    private int port;
    private boolean ouvert = true;

    private volatile ArrayList<ObjectOutputStream> usersOutput;

    public Server(int port) {
        this.port = port;
    }

    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        while (ouvert) {
            ArrayList<User> users = new ArrayList<User>();
            usersOutput = new ArrayList<ObjectOutputStream>();
            while (true) {
                Socket socket = serverSocket.accept();
                //creer un thread pour cet utilisateur
                ClientHandler handler = new ClientHandler(socket, usersOutput);
                Thread thread = new Thread(handler);
                thread.start();
            }
        }
    }

    public void stopServer() throws IOException {
        ouvert = false;
        try {
            DBUtils.ChangeAllUsersStatusToOffline();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        serverSocket.close();
    }

}


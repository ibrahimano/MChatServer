package com.example.mchatserver.classes;

import com.example.mchatserver.DBUtils;
import models.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ConnectedUsersHandler implements Runnable {
    private ArrayList<User> onlineUsers;

    private ObjectOutputStream output;

    private int userId;

    public ConnectedUsersHandler(ObjectOutputStream output, int userId) {
        this.output = output;
        this.userId = userId;
        this.onlineUsers = new ArrayList<User>();
    }

    @Override
    public void run() {
        try {
            //envoyer tous les users online
            ResultSet resultSet = DBUtils.findOnlineUsers();
            //DBUtils.findOfflineUsers();
            while(resultSet.next()) {
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                User user = new User();
                user.setId(resultSet.getInt("id_user"));
                user.setUsername(resultSet.getString("username"));
                user.setEmail(resultSet.getString("email"));
                user.setStatus(resultSet.getString("status"));
                user.setAvatar(resultSet.getString("avatar"));
                if(user.getId() == userId) {
                    continue;
                }
                onlineUsers.add(user);
            }
            if(output != null) {
                output.writeObject("@listOfUsersOnline");
                output.writeObject((ArrayList<User>) onlineUsers);
                System.out.println("@listOfUsersOnline envoye");
            }
            resultSet.close();

            //apres voir si il y a un nouveau online ajouter et si il y a un user qui devient offline supprimer
            while(!Thread.currentThread().isInterrupted()) {
                Thread.sleep(5000);
                ArrayList<User> offlineUsers = new ArrayList<>();
                //verifier le status des users online
                for(int i = 0; i < onlineUsers.size(); i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    String userStatus = DBUtils.checkUserStatusById(onlineUsers.get(i).getId());
                    if(!userStatus.equals("en ligne")) {
                        //user offline
                        offlineUsers.add(onlineUsers.get(i));
                    }
                }
                //envoyer les users offline
                for(int i = 0; i < offlineUsers.size(); i++) {
                    if(Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    onlineUsers.remove(offlineUsers.get(i));

                    if(offlineUsers.get(i).getId() == userId) {
                       break;
                    }
                    output.writeObject("@userOffline");
                    output.writeObject((User) offlineUsers.get(i));
                }

                //verifier si il ya un nouveau online users
                ResultSet newOnlineUsers = DBUtils.findOnlineUsers();
                while (newOnlineUsers.next()) {
                    if(Thread.currentThread().isInterrupted()) {
                        break;
                    }
                    int id = newOnlineUsers.getInt("id_user");
                    if(!isUserOnline(id)) {
                        //si user n'a pas dans la liste des onlineUsers on ajoute donc
                        User newUser = new User();
                        newUser.setId(id);
                        newUser.setUsername(newOnlineUsers.getString("username"));
                        newUser.setEmail(newOnlineUsers.getString("email"));
                        newUser.setStatus(newOnlineUsers.getString("status"));
                        newUser.setAvatar(newOnlineUsers.getString("avatar"));
                        if(newUser.getId() == userId) {
                            continue;
                        }
                        onlineUsers.add(newUser);
                        //envoyer user online
                        if(output != null) {
                            output.writeObject("@userOnline");
                            output.writeObject((User) newUser);
                        }
                    }
                }

                newOnlineUsers.close();

            }
        } catch (SQLException | IOException | InterruptedException e) {
            System.out.println("ConnectedUserHandler Error: ");
            e.printStackTrace();
        }
    }

    public boolean isUserOnline(int userId) {
        for(int i = 0; i < onlineUsers.size(); i++) {
            if(onlineUsers.get(i).getId() == userId) {
                return true;
            }
        }
        return false;
    }


}

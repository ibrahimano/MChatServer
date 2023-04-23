package com.example.mchatserver.classes;

import com.example.mchatserver.DBUtils;
//import com.example.mchatserver.models.Message;
//import com.example.mchatserver.models.User;
import models.User;
import models.Message;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


class ClientHandler implements Runnable {
    private Socket socket;
    private volatile ArrayList<ObjectOutputStream> usersOutput;

    public ClientHandler(Socket socket, ArrayList<ObjectOutputStream> usersOutput) {
        this.socket = socket;
        this.usersOutput = usersOutput;
    }

    public void run() {
        int userId = -1;
        ArrayList<Message> messages = new ArrayList<Message>();
        String userName = "";
        try {
            //pour envoyer les msg
            ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
            //pour recevoir les msg
            ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
            usersOutput.add(output); //pour envoyer les message du client vers les autres par leur output

            //d'abord recevoir le 1er message d'un client qui essaye de connecter
            //on verifie si il a envoyer à partir du login si c'est déja un utilisateur ou du sign up sinon
            //donc client envoie  "login"  ou "signup"
            //si login on recoie email et password et on verifie s'il existe dans BD
            //si signup
            // on recoie user info pour ajouter a la BD


            String type = (String) input.readObject();
            if(type.equals("login")) {
                String email = (String) input.readObject();
                String password = (String) input.readObject();
                try {
                    ResultSet resultSet = DBUtils.findUserByEmailAndPassword(email, password);
                    if (resultSet.next()) {
                        //user exist
                        output.writeObject("exist"); //pour notifier que user exist et donc vous allez recevoir user info et msg
                        //envoyer user info
                        User user = new User();
                        user.setId(resultSet.getInt("id_user"));
                        user.setUsername(resultSet.getString("username"));
                        user.setPassword(resultSet.getString("password"));
                        user.setEmail(resultSet.getString("email"));
                        user.setAccountCreatedDate(resultSet.getDate("account_created_date").toLocalDate());
                        user.setStatus(resultSet.getString("status"));
                        user.setLastLogin(resultSet.getDate("last_login").toLocalDate());
                        user.setAvatar(resultSet.getString("avatar"));
                        userId = user.getId();
                        userName = user.getUsername();
                        System.out.println("username " + userName);
                        System.out.println("email " + user.getEmail());
                        System.out.println("id " + user.getId());
                        System.out.println("password " + user.getPassword());
                        System.out.println("status " + user.getStatus());
                        System.out.println("last login " + user.getLastLogin());
                        System.out.println("date account " + user.getAccountCreatedDate());
                        System.out.println("status " + user.getAvatar());

                        //user status to online
                        DBUtils.ChangeStatusToOnline(userId);
                        output.writeObject((User) user);
                        // envoyer tous les msg qui existe depuis user a creer son compte cad  date > userCreateDate
                        ResultSet resultSetMsg = DBUtils.getAllMessagesAfterAccountCreatedDate(user.getAccountCreatedDate());

                        while(resultSetMsg.next()) {
                            //msg existe
                            Message message = new Message();
                            message.setId(resultSetMsg.getInt("id_msg"));
                            message.setMsgContent(resultSetMsg.getString("msg_content"));
                            message.setTimestamp(resultSetMsg.getTimestamp("timestamp"));
                            message.setUserId(resultSetMsg.getInt("id_user"));
                            message.setUsername(resultSetMsg.getString("username"));
                            messages.add(message);
                        }

                        output.writeObject((ArrayList<Message>) messages);

                        //System.out.println("recu: l'utlisateur " + userName + " est connecté");
                        //System.out.println("ca vient de: " + socket.getInetAddress() + ":" + socket.getPort());

                    } else {
                        //user n'existe pas
                        output.writeObject("invalid"); //notifier client pour afficher error a user qu'in n'existe pas
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            } else if(type.equals("signup")) {
                //recevoir username + email + password
                String email = (String) input.readObject();
                String password = (String) input.readObject();
                String username = (String) input.readObject();

                userName = username;
                try {
                    //verifier si il ya deja un user avec le meme email ou username sinon ajouter
                    ResultSet resultSetExistUser = DBUtils.findUserByEmail(email);
                    if(!resultSetExistUser.next()) {
                        // no user exist avec meme email
                        //verifier si il ya ave meme username
                        ResultSet resultSetExistUsernmae = DBUtils.findUserByUsername(username);
                        if(resultSetExistUsernmae.next()) {
                            //exist user avec ce username
                            output.writeObject("Nom d'utilisateur déjà pris ! Essayer quelque chose de nouveau.");
                        } else {
                            DBUtils.registerUser(username, email, password);
                            //envoyer user info
                            ResultSet resultSet = DBUtils.findUserByEmailAndPassword(email, password);
                            if (resultSet.next()) {
                                User user = new User();
                                user.setId(resultSet.getInt("id_user"));
                                user.setUsername(resultSet.getString("username"));
                                user.setPassword(resultSet.getString("password"));
                                user.setEmail(resultSet.getString("email"));
                                user.setAccountCreatedDate(resultSet.getDate("account_created_date").toLocalDate());
                                user.setStatus(resultSet.getString("status"));
                                user.setLastLogin(resultSet.getDate("last_login").toLocalDate());
                                user.setAvatar(resultSet.getString("avatar"));
                                userId = user.getId();


                                output.writeObject("ok"); //confirmer d'abord et apres envoyer user info
                                output.writeObject((User) user);
                            }
                        }
                    } else {
                        //user deja existe avec meme email
                        output.writeObject("L'e-mail n'est pas valide.");
                    }

                    //System.out.println("recu: l'utlisateur " + userName + " est connecté");
                    //System.out.println("ca vient de: " + socket.getInetAddress() + ":" + socket.getPort());

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            try {
                System.out.println("user id from client handler " + userId + "et username" + userName);
                if(userId != -1) {
                    //creer un thread qui va envoyer les users en ligne au client
                    ConnectedUsersHandler usersHandler = new ConnectedUsersHandler(output, userId);
                    Thread usersHandlerThread = new Thread(usersHandler);
                    usersHandlerThread.start();

                    //loop pour recevoir les msg du user
                    while (true) {
                        //recevoir un msg sauvegarder dans DB et distribuer aux autres users
                        String msg = (String) input.readObject();
                        System.out.println("msg recu " + msg);

                        System.out.println("add msg username: "+ userName);
                        //d'abord on verifie si user msg n'est pas pour deconnecter du serveur
                        String msg_quit = "@offline:" + userId + "_quit";
                        String msgChangeStatusToOffline = "@offline:" + userId;
                        String msgChangeStatusToOnline = "@online:" + userId;
                        if(msg.equals(msg_quit)) {
                            System.out.println(msg_quit);
                            //changer to offline
                            DBUtils.ChangeStatusToOffline(userId);

                            //********* supprimer user output pour ne pas recevoir des messages quand il est deconnecter
                            usersOutput.remove(output);
                            //System.out.println("recu: l'utlisateur " + userName + " est deconnecté");
                            //System.out.println("ca vient de: " + socket.getInetAddress() + ":" + socket.getPort());
                            break;
                        }
                        else {
                            //switch online ou offline et recevoir les messages
                            if (msg.equals(msgChangeStatusToOffline)) {
                                //pour switch status de online vers offline ou contraire
                                System.out.println(msgChangeStatusToOffline);
                                DBUtils.ChangeStatusToOffline(userId);
//                                usersHandlerThread.interrupt();
//                                try {
//                                    usersHandlerThread.join();
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                                usersHandlerThread = new Thread(usersHandler);
//                                usersHandlerThread.start();
                            } else if (msg.equals(msgChangeStatusToOnline)){
                                System.out.println(msgChangeStatusToOnline);
                                DBUtils.ChangeStatusToOnline(userId);
                            } else if (msg.equals("@editUsername")) {
                                System.out.println("changer username");
                                String newUsername = (String) input.readObject();
                                System.out.println("changer username a " + newUsername);
                                String isValid = DBUtils.EditUsername(newUsername, userId);
                                if(isValid.equals("ok")) {
                                    System.out.println("changer username ok " + newUsername);
                                    userName = newUsername;
                                    for(int i=0; i < messages.size(); i++) {
                                        if(messages.get(i).getUserId() == userId) {
                                            messages.get(i).setUsername(newUsername);
                                        }
                                    }
                                    output.writeObject("ok");
                                } else {
                                    //invalid
                                    System.out.println("changer username invalid");
                                    output.writeObject("invalid");
                                }

                            } else if (msg.equals("@editEmail")) {
                                String newEmail = (String) input.readObject();
                                System.out.println("changer email a " +  newEmail);
                                String isValid = DBUtils.EditEmail(newEmail, userId);
                                if(isValid.equals("ok")) {
                                    System.out.println("changer email ok " + newEmail);
                                    output.writeObject("ok");
                                } else {
                                    //invalid
                                    System.out.println("changer email invalid");
                                    output.writeObject("invalid");
                                }

                            } else if (msg.equals("@editPassword")) {
                                System.out.println("changer password");
                                String newPassword = (String) input.readObject();
                                System.out.println("changer password" + newPassword);
                                DBUtils.EditPassword(newPassword, userId);
                            } else if(msg.equals("@profil")) {
                                System.out.println("@stop est evoyeé ");
                                usersHandlerThread.interrupt();
                                try {
                                    usersHandlerThread.join();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                output.writeObject("@stop");

                            } else if(msg.equals("@chat")) {
                                System.out.println("@chat est evoyeé pour demarrer thread");
                                usersHandlerThread = new Thread(usersHandler);
                                usersHandlerThread.start();

                            }
                            else {
                                //si vous etes offline vous ne pouvez pas envoyer les message
                                String userCurrentStatus = DBUtils.checkUserStatusById(userId);
                                System.out.println("status pour envoyer les msg " + userCurrentStatus);
                                if (userCurrentStatus.equals("en ligne")) {
                                    DBUtils.addMessage(userId, msg, userName);
                                    System.out.println("dans le place ou msg va stocke le username: "+ userName);
                                    Message message = DBUtils.getLastMessageByUserId(userId);
                                    System.out.println("last msg est " + message.getMsgContent() + " username " + message.getUsername());
                                    System.out.println("verifier si msg est empty: ");
                                    if(message != null) {
                                        System.out.println("msg not null: ");
                                    }
                                    if (!message.getMsgContent().isEmpty()) {
                                        System.out.println("add msg username: "+ userName);
                                        //envoyer les nouveau msg a tous les users meme qui a envoyer avec sa date
                                        for (int j = 0; j < usersOutput.size(); j++) {

                                            //dire que c'est un message
                                            System.out.println("msg a envoye " + message.getMsgContent() + "msg userId :" + message.getUserId() + "userId" + userId);
                                            usersOutput.get(j).writeObject("@message");
                                            usersOutput.get(j).writeObject((Message) message);
                                            System.out.println(" msg envoye a " + j);
                                            messages.add(message);

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            System.err.println("Erreur lors de la communication avec le client: " + e.getMessage());
            System.err.println("Erreur dans clientHandler");
        } catch (ClassNotFoundException e) {
            System.err.println("Classe non trouvée lors de la communication avec le client: " + e.getMessage());
            System.err.println("Erreur dans clientHandler classNotFound");
        }
    }

}

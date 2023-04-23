package com.example.mchatserver;

//import com.example.mchatserver.models.Message;
import models.Message;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DBUtils {

    private static final String url = "jdbc:mysql://localhost:3306/mchat";
    private  static final String user = "ibrahim";
    private static final String password = "ibrahim";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
    public static void closeConnection(Connection connection) throws SQLException {
        if (connection != null) {
            connection.close();
        }
    }

    public static void closePreparedStatement(PreparedStatement statement) throws SQLException {
        if (statement != null) {
            statement.close();
        }
    }

    public  static  void closeResultSet(ResultSet resultSet) throws SQLException {
        if(resultSet != null) {
            resultSet.close();
        }
    }

    public static void executeUpdate(String sql, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
           statement.executeUpdate();
        } finally {
            closePreparedStatement(statement);
            closeConnection(connection);
        }
    }

    public static ResultSet executeQuery(String sql, Object... params) throws SQLException {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;

        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);

            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            closeResultSet(resultSet);
            closePreparedStatement(statement);
            closeConnection(connection);
            throw e;
        }
        return resultSet;
    }

    public  static void registerUser(String username, String email, String password) throws  SQLException {
        String sql = "INSERT INTO user(username, email, password, account_created_date, status, last_login, avatar) VALUES(?, ?, ?, ?, ?, ?, ?)";
        LocalDateTime currentDate = LocalDateTime.now();
        executeUpdate(sql, username, email, password, currentDate, "en ligne", currentDate, "avatars/default_avatar.png");
    }

    public static ResultSet findUserByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT * FROM user WHERE email=? and password=?";
        return executeQuery(sql, email, password);
    }

    public static ResultSet findUserByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM user WHERE email=?";
        return executeQuery(sql, email);
    }
    public static ResultSet findUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM user WHERE username=?";
        return executeQuery(sql, username);
    }
    public static ResultSet getAllUsers() throws SQLException {
        String sql = "SELECT * FROM user";
        return executeQuery(sql);
    }

    public static ResultSet findOnlineUsers() throws SQLException {
        String sql = "SELECT * FROM user WHERE status=?";
        return executeQuery(sql, "en ligne");
    }

    public static ResultSet findOfflineUsers() throws SQLException {
        String sql = "SELECT * FROM user WHERE status=?";
        return executeQuery(sql, "hors ligne");
    }

    public static String checkUserStatusById(int id) throws SQLException {
        String sql = "SELECT status FROM user WHERE id_user=?";
        ResultSet resultSet = executeQuery(sql, id);
        String status = "";
        if(resultSet.next()){
            status = resultSet.getString("status");
        }
        return status;
    }

    public static void ChangeStatusToOnline(int id) throws  SQLException {
        String sql = "UPDATE user SET status='en ligne' where id_user=?";
        executeUpdate(sql, id);
    }

    public static void ChangeStatusToOffline(int id) throws  SQLException {
        String sql = "UPDATE user SET status='hors ligne' where id_user=?";
        executeUpdate(sql, id);
    }

    public static void ChangeAllUsersStatusToOffline() throws  SQLException {
        ResultSet resultSet = getAllUsers();
        while(resultSet.next()) {
            int id = resultSet.getInt("id_user");
            String sql = "UPDATE user SET status='hors ligne' where id_user=?";
            executeUpdate(sql, id);
        }
    }

    public static ResultSet getAllMessagesAfterAccountCreatedDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM message WHERE timestamp > ?";
        return executeQuery(sql, date);
    }

    public  static void addMessage(int userId, String message, String username) throws  SQLException {
        String sql = "INSERT INTO message(msg_content, id_user, username) VALUES(?, ?, ?)";
        LocalDateTime currentDate = LocalDateTime.now();
        System.out.println("add msg username: "+ username);
        executeUpdate(sql, message, userId, username);
    }

    public static Message getLastMessageByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM message WHERE id_user= ? ORDER BY timestamp DESC LIMIT 1";
        ResultSet resultSet = executeQuery(sql, userId);
        if (resultSet.next()) {
            Message message = new Message();
            message.setId(resultSet.getInt("id_msg"));
            message.setMsgContent(resultSet.getString("msg_content"));
            message.setTimestamp(resultSet.getTimestamp("timestamp"));
            message.setUserId(resultSet.getInt("id_user"));
            message.setUsername(resultSet.getString("username"));
            return message;
        } else {
            return null;
        }
    }

    public static String EditUsername(String newUsername, int id) throws  SQLException {
        //username est unique
        ResultSet resultSet = DBUtils.findUserByUsername(newUsername); //chercher si il y a quelqu un avec cet username
        if(resultSet.next()){
            int userId = resultSet.getInt("id_user");
            if(userId == id) {
                return "ok"; //user a saisi le meme username
            }
            //deja existe
            return "invalid";
        } else {
            String sql = "UPDATE user SET username=? where id_user=?";
            String sql2 = "UPDATE message SET username=? where id_user=?";
            executeUpdate(sql, newUsername, id); //table user
            executeUpdate(sql2, newUsername, id); //table message
            return "ok"; //modifier

        }

    }

    public static String EditEmail(String newEmail, int id) throws  SQLException {
        //email est unique
        ResultSet resultSet = DBUtils.findUserByEmail(newEmail); //chercher si il y a quelqu un avec cet email
        if(resultSet.next()){
            int userId = resultSet.getInt("id_user");
            if(userId == id) {
                return "ok"; //user a saisi le meme email
            }
            //deja existe
            return "invalid";
        } else {
            String sql = "UPDATE user SET email=? where id_user=?";
            executeUpdate(sql, newEmail, id);
            return "ok"; //modifier

        }
    }

    public static void EditPassword(String newPassword, int id) throws  SQLException {
        String sql = "UPDATE user SET password=? where id_user=?";
        executeUpdate(sql, newPassword, id);
    }




}

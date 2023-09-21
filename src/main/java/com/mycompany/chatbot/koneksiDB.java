package com.mycompany.chatbot;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class koneksiDB {
    String url, usr, pwd, dbn;
    
    public koneksiDB(String dbn) {
        this.url = "jdbc:mysql://localhost/" + dbn;
        this.usr = "root";
        this.pwd = "";
    }
    
    public koneksiDB(String host, String user, String pass, String dbn) {
        this.url = "jdbc:mysql://" + host + "/" + dbn;
        this.usr = user;
        this.pwd = pass;
    }
    
    public Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(this.url, this.usr, this.pwd);
        } catch (SQLException e) {
            System.out.println("Error #2: " + e.getMessage());
            System.exit(0);
        }
        return con;
    }
    
    public static void main(String args[]) {
        koneksiDB kon = new koneksiDB("chatbot");
        Connection c = kon.getConnection();
    }
    public void insertCommand(String command, String response) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO cmdres (command, response) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, command);
                statement.setString(2, response);
                statement.executeUpdate();
                System.out.println("Command Berhasil Ditambahkan");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public void insertUser(Long id, String username) {
        try (Connection connection = getConnection()) {
            String sql = "INSERT INTO member (idtelegram, usernametelegram) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.setString(2, username);
                statement.executeUpdate();
                System.out.println("Data user berhasil diinput");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
    public void updateCommand(int id, String command, String response) {
        try (Connection connection = getConnection()) {
            String sql = "UPDATE cmdres SET command = ?, response = ? WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, command);
                statement.setString(2, response);
                statement.setInt(3, id);
                int rowsUpdated = statement.executeUpdate();
                if (rowsUpdated > 0) {
                    System.out.println("Data berhasil diperbarui");
                } else {
                    System.out.println("Tidak ada data yang diperbarui");
                }
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat memperbarui data");
        }
    }
    public void deleteCommand(int id) {
        try (Connection connection = getConnection()) {
            String sql = "DELETE FROM cmdres WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Data berhasil dihapus");
                } else {
                    System.out.println("Tidak ada data yang dihapus");
                }
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat menghapus data");
        }
    }
    public void deleteUser(Long idTele) {
        try (Connection connection = getConnection()) {
            String sql = "DELETE FROM member WHERE idtelegram = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, idTele);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Data berhasil dihapus");
                } else {
                    System.out.println("Tidak ada data yang dihapus");
                }
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat menghapus data");
        }
    }
    public void deleteUserWithID(int id) {
        try (Connection connection = getConnection()) {
            String sql = "DELETE FROM member WHERE id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    System.out.println("Data berhasil dihapus");
                } else {
                    System.out.println("Tidak ada data yang dihapus");
                }
            }
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat menghapus data");
        }
    }
    public void saveChatMessage(String sender_id, String username, String message,String status,String formattedTime) throws SQLException {
    String INSERT_QUERY = "INSERT INTO logchat (sender_id, username, message, status, timestamp) VALUES (?,? ,?,?, ?)";
        try (Connection connection = getConnection(); 
             PreparedStatement preparedStatement = connection.prepareStatement(INSERT_QUERY)) {
            preparedStatement.setString(1, sender_id);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, message);
            preparedStatement.setString(4, status);
            preparedStatement.setString(5, formattedTime);
            preparedStatement.executeUpdate();
            System.out.println("Data riwayat chat berhasil disimpan.");
        } catch (SQLException e) {
            System.out.println("Terjadi kesalahan saat menyimpan data riwayat chat.");
            e.printStackTrace();
        }
    }

            
}

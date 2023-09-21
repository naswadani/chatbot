/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.chatbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;



/**
 *
 * @author Naswa Khansa
 */

public class Chatbot extends TelegramLongPollingBot{
    koneksiDB kon = new koneksiDB("localhost","root","","chatbot"); 
    Connection con;
    private static final String API_KEY = "aab95b8bf070485ea2d106d19f39637e";
    private int currentNewsIndex = 0;
    private Object[] responses = null;

    public Chatbot(){
        open_db();
    }
    public static void main(String[] args) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new Chatbot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    
    private void open_db() { 
        try{ 
            con = kon.getConnection(); 
            System.out.println("Berhasil "); 
        }catch (Exception e) { 
            System.out.println("Error : "+e); 
        } 
    } 

    private boolean isUserExists(Connection connection, Long id) {
        try {
            String sql = "SELECT COUNT(*) FROM member WHERE idtelegram=?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Long count = resultSet.getLong(1);
                        return count > 0;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }
    
    @Override
        public void onUpdateReceived(Update update) {
            Long chatId = update.getMessage().getChatId();
            String receivedCommand = update.getMessage().getText();
            String newsData = getNewsData();
            boolean validCommandFound = false;
            boolean memberExist = false;
            String response = "";
            try {
                String sql = "SELECT * FROM cmdres";
                try (Statement statement = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                     ResultSet resultSet = statement.executeQuery(sql)) {

                    while (resultSet.next()) {
                        if (receivedCommand.equalsIgnoreCase(resultSet.getString("command"))) {
                            validCommandFound = true;
                            if (!isUserExists(con, update.getMessage().getFrom().getId()) && !receivedCommand.equalsIgnoreCase("/daftar") ) {
                                response = "Anda bukan member. Tidak diizinkan mengirim pesan. Untuk Mendaftar ketik /daftar";
                            }else{
                                if (receivedCommand.equalsIgnoreCase("/unsub")) {
                                    Long telegramId = update.getMessage().getFrom().getId();
                                    kon.deleteUser(telegramId);
                                    response = "Anda telah berhenti menjadi member";
                                } else if (receivedCommand.equalsIgnoreCase("/daftar")) {
                                    Long telegramId = update.getMessage().getFrom().getId();
                                    String telegramUsername = update.getMessage().getFrom().getUserName();
                                    if (!isUserExists(con, telegramId)) {
                                        memberExist = false;
                                        kon.insertUser(telegramId, telegramUsername);
                                        response = "Anda sekarang menjadi member";
                                    } else{
                                        response = resultSet.getString("response");
                                    }
                                }
                                else{
                                        response = resultSet.getString("response");
                                    }
                            }
                            //time
                            int timestamp = update.getMessage().getDate();
                            Date date = new Date((long) timestamp * 1000);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String formattedTime = sdf.format(date);
                            if(receivedCommand.equalsIgnoreCase("/news")) {
                                    JSONArray articles = new JSONArray(newsData);
                                    int startIndex = currentNewsIndex;
                                    int endIndex = Math.min(startIndex + 3, articles.length());
                                    for (int i = startIndex; i < endIndex; i++) {
                                        JSONObject article = articles.getJSONObject(i);
                                        String title = article.getString("title");
                                        sendResponseMessage(chatId,title);
                                        System.out.println("hai");
                                    }
                                    currentNewsIndex = endIndex;
                                    System.out.print("hai");
                            }
                            
                            kon.saveChatMessage(update.getMessage().getFrom().getId().toString(),update.getMessage().getFrom().getUserName(),receivedCommand,"masuk",formattedTime);
                            kon.saveChatMessage(update.getMessage().getFrom().getId().toString(),update.getMessage().getFrom().getUserName(),response,"keluar",formattedTime);
                            sendResponseMessage(chatId, response);
                            
                            break;
                        }
                    }
                }

                if (!validCommandFound && !receivedCommand.equalsIgnoreCase("/daftar")) {
                    String availableCommands = getAllAvailableCommands();
                    response = "Command tidak valid. Berikut adalah daftar command yang tersedia:\n\n" + availableCommands;
                    sendResponseMessage(chatId, response);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    private String getAllAvailableCommands() {
        List<String> commands = new ArrayList<>();

        try {
            String sql = "SELECT command FROM cmdres"; // 
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String command = resultSet.getString("command");
                commands.add(command);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Menggabungkan semua command dalam daftar menjadi satu string terpisah oleh koma
        String availableCommands = String.join(", ", commands);

        return availableCommands;
    }
    public void sendBroadcastMessage(String broadcast) {
        List<Long> userIds = getUserIdsFromDatabase();
        for (Long userId : userIds) {
            SendMessage message = new SendMessage();
            message.setChatId(userId);
            message.setText(broadcast);
            try {
                execute(message);
                System.out.println("Pesan terkirim ke pengguna dengan ID: " + userId);
            } catch (TelegramApiException e) {
                System.out.println("Gagal mengirim pesan ke pengguna dengan ID: " + userId);
                e.printStackTrace();
            }
        }
    }
    private List<Long> getUserIdsFromDatabase() {
        List<Long> userIds = new ArrayList<>();
        try {
            Statement statement = con.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT idtelegram FROM member");
            while (resultSet.next()) {
                userIds.add(resultSet.getLong("idtelegram"));
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userIds;
    }
    private void sendResponseMessage(Long chatId, String response) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(response);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private String getNewsData(){
        try {
        String apiUrl = "https://newsapi.org/v2/everything?q=apple&from=2023-07-12&to=2023-07-12&sortBy=popularity&apiKey=" + API_KEY;

        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray articles = jsonResponse.getJSONArray("articles");
        JSONArray topArticles = new JSONArray();
        for (int i = 0; i < articles.length(); i++) {
            JSONObject article = articles.getJSONObject(i);
            topArticles.put(article);
        }
        
        return topArticles.toString();
    } catch (Exception e) {
        e.printStackTrace();
        return "Error occurred while fetching news data";
    }
    }


    @Override
    public String getBotUsername() {
      return "hudududu_bot";
    }

    @Override
    public void onRegister() {
        super.onRegister(); 
    }
    
    @Override
    public String getBotToken(){
        return "5914492850:AAF7dVCyI0d_-9ewf94djixVSSout9dB2kA";
    }
}

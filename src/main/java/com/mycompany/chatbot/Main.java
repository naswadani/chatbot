/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.chatbot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 *
 * @author Naswa Khansa
 */
public class Main {
    public static void main(String[] args) throws TelegramApiException{
        TelegramBotsApi botAPI = new TelegramBotsApi(DefaultBotSession.class);
        Chatbot bot = new Chatbot();
        botAPI.registerBot(bot);
    }
}

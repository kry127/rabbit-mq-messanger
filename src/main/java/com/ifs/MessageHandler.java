package com.ifs;

@FunctionalInterface
public interface MessageHandler {
    void handle(String chatId, Message message);
}

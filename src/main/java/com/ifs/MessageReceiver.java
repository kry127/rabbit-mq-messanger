package com.ifs;

import com.Message;

@FunctionalInterface
public interface MessageReceiver {
    void receive(String chatId, Message msg);
}

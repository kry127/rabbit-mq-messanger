package com;

import com.ifs.MessageReceiver;
import com.ifs.MessageHandler;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentMap;

public class MessageReceiverImpl implements MessageReceiver {

    private final ConcurrentMap<String, ConcurrentLinkedDeque<Message>> history;
    private MessageHandler handler;

    public MessageReceiverImpl(MessageHandler handler) {
        history = new ConcurrentHashMap<>();
        this.handler = handler;
    }

    @Override
    public void receive(String chatId, Message msg) {
        if (!history.containsKey(chatId)) {
            history.put(chatId, new ConcurrentLinkedDeque<>());
        }
        history.get(chatId).addFirst(msg);
        handler.handle(chatId, msg);
    }

    public void debugPrint() {
        for (String chatName: history.keySet()) {
            System.out.println("Chat name: " + chatName);
            for (Message msg: history.get(chatName)) {
                System.out.println(msg.toString());
            }
        }
    }
}

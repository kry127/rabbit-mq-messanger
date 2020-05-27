package com;

import com.ifs.MessageHandler;
import com.ifs.MessageReceiver;

public class StatelessMessageReceiverImpl implements MessageReceiver {
    private MessageHandler handler;

    public StatelessMessageReceiverImpl(MessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void receive(String chatId, Message msg) {
        handler.handle(chatId, msg);
    }
}

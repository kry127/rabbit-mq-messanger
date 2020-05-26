package com.ifs;

import com.Message;

@FunctionalInterface
public interface MessageReceiver {
    void receive(Message msg);
}

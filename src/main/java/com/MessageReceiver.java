package com;

@FunctionalInterface
public interface MessageReceiver {

    void receive(Message msg);

}

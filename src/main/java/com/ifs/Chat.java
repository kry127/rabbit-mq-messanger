package com.ifs;

import com.Message;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public interface Chat {
    void send(Message message) throws IOException, TimeoutException;
}

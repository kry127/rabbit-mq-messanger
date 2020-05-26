package com;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class TaskSendMsg implements Task {
    private final Scanner sc;
    private final ConnectionFactory factory;
    private final String QUEUE_NAME;

    public TaskSendMsg(Scanner sc, ConnectionFactory factory, String QUEUE_NAME) {
        this.sc = sc;
        this.factory = factory;
        this.QUEUE_NAME = QUEUE_NAME;

    }

    @Override
    public void run() throws IOException, TimeoutException {
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            while (sc.hasNextLine()) {
                String message = sc.nextLine();
                channel.basicPublish("", QUEUE_NAME, null,
                        message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}

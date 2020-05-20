package com;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

public class Sender implements Runnable {


    private String queue;
    private String author;
    private String host;
    private int port;

    public Sender(String queue, String author, String host, int port) {
        this.queue = queue;
        this.author = author;
        this.host = host;
        this.port = port;
    }

    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        if(port >= 0)
            factory.setPort(port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            String msg = null;
            try {
                msg = reader.readLine();

                try (Connection connection = factory.newConnection();
                     Channel channel = connection.createChannel()) {
                    channel.queueDeclare(queue, false, false, false, null);
                    Message message = new Message(msg, author, ZonedDateTime.now());
                    channel.basicPublish("", queue, null, message.getBytes());
                    System.out.println(message);
                } catch (TimeoutException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

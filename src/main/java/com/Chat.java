package com;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

public class Chat {

    private final String chatId;
    private final MessageReceiver receiver;
    private final ConnectionFactory factory;

    public Chat(String chatId, MessageReceiver receiver, ConnectionFactory factory) throws IOException, TimeoutException {
        this.chatId = chatId;
        this.receiver = receiver;
        this.factory = factory;
        Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(chatId, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, chatId, "");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    receiver.receive(Message.frommBytes(delivery.getBody()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public void send(Message message) throws IOException, TimeoutException {
        try(Connection connection = factory.newConnection();
            Channel channel = connection.createChannel()) {
            channel.exchangeDeclare(chatId, "fanout");
            channel.basicPublish(chatId, "", null, message.getBytes());
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Chat chat = new Chat("abc", (r) -> System.out.println(r), factory);
        chat.send(new Message("test", "ya", ZonedDateTime.now()));
        Thread.sleep(10000);
    }
}

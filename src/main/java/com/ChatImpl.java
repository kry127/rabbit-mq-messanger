package com;

import com.ifs.MessageReceiver;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public class ChatImpl implements com.ifs.Chat {

    private final String chatId;
    private final MessageReceiver receiver;
    private final ConnectionFactory factory;
    private final ExecutorService service;

    public ChatImpl(String chatId, MessageReceiver receiver, ConnectionFactory factory, ExecutorService service) throws IOException, TimeoutException {
        this.chatId = chatId;
        this.receiver = receiver;
        this.factory = factory;
        this.service = service;
        Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(chatId, "fanout");
            String queueName = channel.queueDeclare().getQueue();
            channel.queueBind(queueName, chatId, "");
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                try {
                    receiver.receive(chatId, Message.frommBytes(delivery.getBody()));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            };
            channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }

    public Future<Optional<Exception>> send(Message message) {
        return service.submit(() -> {
            try (Connection connection = factory.newConnection();
                 Channel channel = connection.createChannel()) {
                channel.exchangeDeclare(chatId, "fanout");
                channel.basicPublish(chatId, "", null, message.getBytes());
            } catch (TimeoutException|IOException e) {
                return Optional.of(e);
            }
            return Optional.empty();
        });
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ExecutorService executorService = Executors.newFixedThreadPool(4);
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri("amqp://zavmsusv:llWnY9bP_iVXSdvhuaNd_WJoexursdVi@fish.rmq.cloudamqp.com/zavmsusv");
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);
        MessageReceiverImpl receiver = new MessageReceiverImpl((c, r) -> {});
        ChatImpl chatImpl = new ChatImpl("abc", receiver, factory, executorService);
        chatImpl.send(new Message("test", "ya", ZonedDateTime.now()));
        ChatImpl chatImpl1 = new ChatImpl("abc2", receiver, factory, executorService);
        chatImpl1.send(new Message("test2", "ne ya", ZonedDateTime.now()));
        Thread.sleep(3000);
        receiver.debugPrint();
    }
}

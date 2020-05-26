package com;

import org.apache.commons.cli.*;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.xml.transform.Result;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sender {
    // Опция - имя
    private static final Random PRNG = new Random();

    private static class Result {
        private final int wait;
        public Result(int code) {
            this.wait = code;
        }
    }

    public static Result compute(Task obj) throws InterruptedException,
            IOException, TimeoutException {
        int wait = PRNG.nextInt(3000);
        obj.run();
//        Thread.sleep(wait);
        return new Result(wait);
    }


    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        //factory.setPort(15672);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        Scanner sc = new Scanner(System.in);

        List<Callable<Result>> tasks = new ArrayList<Callable<Result>>();

        Task taskRecieve = new TaskRecievMsg(channel, QUEUE_NAME);
        Task taskSend = new TaskSendMsg(sc, factory, QUEUE_NAME);



        tasks.add(new Callable<Sender.Result>() {
            @Override
            public Sender.Result call() throws Exception {
                return compute(taskRecieve);
            }
        });

        tasks.add(new Callable<Sender.Result>() {
            @Override
            public Sender.Result call() throws Exception {
                return compute(taskSend);
            }
        });

        ExecutorService exec = Executors.newCachedThreadPool();
        List<Future<Result>> results = exec.invokeAll(tasks);


        taskSend.run();
        taskRecieve.run();

    }
}

package com.ifs;

import com.Message;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

public interface Chat {
    Future<Optional<Exception>> send(Message message);
}

package com;

import java.io.*;
import java.time.ZonedDateTime;

public class Message implements Serializable, com.ifs.Message {
    private final String message;
    private final String author;
    private final ZonedDateTime dateTime;

    public Message(String message, String author, ZonedDateTime dateTime) {
        this.message = message;
        this.author = author;
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor() {
        return author;
    }

    public ZonedDateTime getDateTime() {
        return dateTime;
    }

    public byte[] getBytes() throws IOException {
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(arr);
        out.writeObject(this);
        return arr.toByteArray();
    }

    public String toString() {
        return " " + author + " [" + dateTime + "]: " + message;
    }

    public static Message frommBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream arr = new ByteArrayInputStream(bytes);
        ObjectInputStream inputStream = new ObjectInputStream(arr);
        return (Message) inputStream.readObject();
    }
}

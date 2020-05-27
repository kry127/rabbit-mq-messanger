package com;

import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable, com.ifs.Message {
    private final String message;
    private final String author;
    private final ZonedDateTime dateTime;


    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd LLL HH:mm:ss");

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
        return " [" + dateTime.format(formatter) + "] "  + author + ": " + message;
    }

    public static Message frommBytes(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream arr = new ByteArrayInputStream(bytes);
        ObjectInputStream inputStream = new ObjectInputStream(arr);
        return (Message) inputStream.readObject();
    }
}

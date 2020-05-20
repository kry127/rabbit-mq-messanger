package com;

import org.apache.commons.cli.*;

public class CommandLineInterface {

    public static void main(String[] args) {
        // create Options object
        Options options = new Options();

        // add t option
        options.addOption("u", "user", true, "User name");
        options.addOption("t", "topic", true, "Topic name");
        options.addOption("a", "address", true, "IP address of RabbitMQ server");
        options.addOption("p", "port", true, "Port of peer");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException e) {
            // print help
            return;
        }

        String user = cmd.getOptionValue("u", "anonymous");
        String topic = cmd.getOptionValue("t", "sd-message-topic");
        String ip = cmd.getOptionValue("a", "127.0.0.1");
        String port = cmd.getOptionValue("p", "5672");

        int iip = IpUtils.ipToInt(ip);
        int iport = Integer.parseInt(port);
    }
}

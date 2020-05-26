package com.gui;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class GUI extends Application {

    private final Background errorBackground = new Background(
            new BackgroundFill(Color.MEDIUMVIOLETRED, new CornerRadii(3.0), Insets.EMPTY));
    private final Background normalBackground = new Background(
            new BackgroundFill(Color.WHITE, new CornerRadii(3.0), Insets.EMPTY));

    Stage loginStage;

    public static void main(String[] args) {
        launch(args);
    }

//    private void initializeMessaging(String username, String host, Integer port)  {
//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost(host);
//        factory.setPort(port);
//        try {
//            Chat chat = new Chat("chatid", System.out::println, factory);
//            chat.send(new Message("test", username, ZonedDateTime.now()));
//            Thread.sleep(10000);
//        } catch (IOException | TimeoutException | InterruptedException ex) {
//
//        }
//    }

    @Override
    public void start(Stage primaryStage) {
        loginStage = primaryStage;

        primaryStage.setTitle("SD chat [free licence]");
        primaryStage.show();

        // add grid pane
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));

        Scene scene = new Scene(grid, 425, 275);
        primaryStage.setScene(scene);

        // add text
        Text scenetitle = new Text("Welcome to godlike SD chad");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);

        // add username input box
        Label userName = new Label("User Name:");
        grid.add(userName, 0, 1);

        TextField userTextField = new TextField("anonymous");
        userTextField.setBackground(normalBackground);
        grid.add(userTextField, 1, 1);

        // add address and port controls
        Label pw = new Label("host and port:");
        grid.add(pw, 0, 2);

        HBox addresPortHbox = new HBox();
        TextField hostBox = new TextField("localhost");
        TextField portBox = new TextField("5672");
        hostBox.setBackground(normalBackground);
        portBox.setBackground(normalBackground);

        portBox.setPrefColumnCount(5);

        addresPortHbox.getChildren().add(hostBox);
        addresPortHbox.getChildren().add(new Label(":"));
        addresPortHbox.getChildren().add(portBox);

        grid.add(addresPortHbox, 1, 2);

        // add login button
        Button loginButton = new Button("Sign in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(loginButton);
        grid.add(hbBtn, 1, 4);

        // event example
        final Text actiontarget = new Text();
        grid.add(actiontarget, 0, 6, 2, 1);

        loginButton.setOnAction(e -> {
            String username = String.valueOf(userTextField.getCharacters());
            String host = String.valueOf(hostBox.getCharacters());
            String port = String.valueOf(portBox.getCharacters());

            // check parameters:
            boolean correct = true;
            Integer portInt = 0;
            try {
                portInt = Integer.parseInt(port);
                portBox.setBackground(normalBackground);
            } catch (NumberFormatException nfe) {
                correct = false;
                portBox.setBackground(errorBackground);
            }

            if (username.length() < 3) {
                correct = false;
                userTextField.setBackground(errorBackground);
            } else {
                userTextField.setBackground(normalBackground);
            }


            MainMessagingWindow messagingWindow = new MainMessagingWindow(username, host, portInt);
            if (correct) {
                boolean connected = messagingWindow.checkConnection();
                if (!connected) {
                    hostBox.setBackground(errorBackground);
                    portBox.setBackground(errorBackground);
                } else {
                    // automatically hides current stage!
                    loginStage.hide();
                    messagingWindow.show();
                }
            }

//            actiontarget.setFill(Color.FIREBRICK);
//
//            StringBuffer sb = new StringBuffer();
//            sb.append("Sign in button pressed with parameters: ");
//            sb.append(username);
//            sb.append(", ");
//            sb.append(host + ":" + port);
//            sb.append(".");
//            actiontarget.setText(sb.toString());
        });

    }



    public class MainMessagingWindow extends Stage {

        /**
         * Describes established connection
         */
        private ConnectionFactory factory = new ConnectionFactory();
        private boolean connected;


        private MainMessagingWindow(String username, String host, Integer port) {
            factory.setUsername(username);
            factory.setHost(host);
            factory.setPort(port);
            connected = false;
            try {
                Connection connection = factory.newConnection();
                connected = true;
                connection.close();
            } catch (IOException | TimeoutException ex) {
                return; // connection not established
            }
            initLayout();
        }

        private boolean checkConnection() {
            return connected;
        }

        private void initLayout() {
            super.setTitle("SD chat [free licence]");

            // add grid pane
            GridPane grid = new GridPane();
            grid.setAlignment(Pos.CENTER);
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(25, 25, 25, 25));

            Scene scene = new Scene(grid, 425, 275);
            super.setScene(scene);

            // add text
            Text scenetitle = new Text("Select chat");
            scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
            grid.add(scenetitle, 0, 0, 2, 1);

            // add username input box
            Label userName = new Label("User Name:");
            grid.add(userName, 0, 1);

            TextField userTextField = new TextField("anonymous");
            userTextField.setBackground(normalBackground);
            grid.add(userTextField, 1, 1);

            super.setOnCloseRequest(ev->{
                GUI.this.loginStage.show();
            });
        }
    }
}
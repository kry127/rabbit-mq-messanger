package com.gui;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

public class DedicatedHostGUI extends Application {

    private final Background errorBackground = new Background(
            new BackgroundFill(Color.MEDIUMVIOLETRED, new CornerRadii(3.0), Insets.EMPTY));
    private final Background normalBackground = new Background(
            new BackgroundFill(Color.WHITE, new CornerRadii(3.0), Insets.EMPTY));


    /**
     * Describes established connection
     */
    private ConnectionFactory factory = new ConnectionFactory();

    String username;
    private final String RABBITMQ_AUTHENTICATION_USERNAME = "guest";
    private final String CONNECTION_URI =
        "amqp://zavmsusv:llWnY9bP_iVXSdvhuaNd_WJoexursdVi@fish.rmq.cloudamqp.com/zavmsusv";

    public DedicatedHostGUI() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        factory.setUsername(RABBITMQ_AUTHENTICATION_USERNAME); // not provided username!!
        factory.setUri(CONNECTION_URI);
    }


    public static void main(String[] args) {
        launch(args);
    }

    private boolean canConnect() {
        boolean connected = false;
        try {
            Connection connection = factory.newConnection();
            connected = true;
            connection.close();
        } catch (IOException | TimeoutException ex) {
            connected = false; // connection not established
        }
        return connected;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("SD chat [free licence]");
        primaryStage.show();

        // add vBox panel as main panel
        VBox vBox = new VBox();

        // 1. vBox consists of upper selector of username
        HBox userNameBox = new HBox();
        userNameBox.setPadding(new Insets(5, 0, 5, 0));
        vBox.getChildren().add(userNameBox);

        // add username input box
        Label userName = new Label("User name:");
        userNameBox.getChildren().add(userName);

        TextField userTextField = new TextField("Timofey Bryksin");
        userTextField.setBackground(normalBackground);

        userNameBox.getChildren().add(userTextField);
        HBox.setHgrow(userTextField, Priority.ALWAYS);

        // 2. vBox consists of hBoxChatAndMsg

        HBox hBoxChatAndMsg = new HBox();
        hBoxChatAndMsg.setAlignment(Pos.CENTER);
        hBoxChatAndMsg.setPadding(new Insets(5, 0, 5, 0));

        vBox.getChildren().add(hBoxChatAndMsg);

        // 2.1 add topics scroll
        VBox scrollableTopics = new VBox();
        scrollableTopics.setPrefWidth(150.0);
        ObservableList<Node> children = scrollableTopics.getChildren();
        for (int i = 0; i < 20; i++) {
            children.add(new Label("Topic " + (i + 1)));
        }
        ScrollPane sp = new ScrollPane(scrollableTopics);
        hBoxChatAndMsg.getChildren().add(sp);

        // 2.2 add messaging part


        TextArea textArea = new TextArea();
        hBoxChatAndMsg.getChildren().add(textArea);


        // finally, add scene to window
        Scene scene = new Scene(vBox, 640, 480);
        primaryStage.setScene(scene);


    }
}
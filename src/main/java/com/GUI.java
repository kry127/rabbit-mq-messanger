package com;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GUI extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
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
        grid.add(userTextField, 1, 1);

        // add address and port controls
        Label pw = new Label("host and port:");
        grid.add(pw, 0, 2);

        HBox addresPortHbox = new HBox();
        TextField hostBox = new TextField("localhost");
        TextField portBox = new TextField("5672");

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
        grid.add(actiontarget, 1, 6);

        loginButton.setOnAction(e -> {
            actiontarget.setFill(Color.FIREBRICK);
            StringBuffer sb = new StringBuffer();
            sb.append("Sign in button pressed with parameters: ");
            sb.append(userTextField.getCharacters());
            sb.append(", ");
            sb.append(hostBox.getCharacters() + ":" + portBox.getCharacters());
            sb.append(".");
            actiontarget.setText(sb.toString());
        });

    }
}
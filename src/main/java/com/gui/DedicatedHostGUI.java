package com.gui;

import com.ChatImpl;
import com.StatelessMessageReceiverImpl;
import com.ifs.Chat;
import com.ifs.Message;
import com.ifs.MessageReceiver;
import com.rabbitmq.client.ConnectionFactory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.ZonedDateTime;
import java.util.concurrent.*;
import java.util.function.Consumer;


/**
 * Класс описывает запускаемое JavaFX десктопное приложение, которое служит клиентом для
 * обмена сообщений на сервере RabbitMQ
 */
public class DedicatedHostGUI extends Application {

    private static final double CHAT_SELECTION_WIDTH = 165;
    private static final double MIN_WINDOW_WIDTH = 525;
    private static final double MIN_WINDOW_HEIGHT = 320;
    private static final double INITIAL_WINDOW_WIDTH = 640;
    private static final double INITIAL_WINDOW_HEIGHT = 480;

    private final Background errorBackground = new Background(
            new BackgroundFill(Color.INDIANRED, new CornerRadii(3.0), Insets.EMPTY));
    private final Background normalBackground = new Background(
            new BackgroundFill(Color.WHITE, new CornerRadii(3.0), Insets.EMPTY));

    /**
     * Описание фабрики, по которой будет устанавливаться соединение
     */
    private ConnectionFactory factory = new ConnectionFactory();

    /**
     * Для подключения к серверу используется ссылка, в которой закодированы логин и пароль
     */
    private final String CONNECTION_URI =
        "amqp://jfndqzau:0294WZlihPnoE5GTDO-kXcpkddv_Ng3u@kangaroo.rmq.cloudamqp.com/jfndqzau";

    private TextArea outputTextArea; // Поле текстового вывода. Обновляется автоматически, это и есть поле чата
    private TextArea inputTextArea;  // В данное поле пользователь вводит своё сообщение
    private TextField usernameField; // Это поле с именем пользователя, которое будет использовано для отправки сообщ.

    /**
     * Храним отображение имён чата на их дескрипторы. Дескриптор чата хранит:
     *  1. имя чата
     *  2. сам объект типа Chat
     *  3. GUI-объект типа ссылка
     *  4. Историю сообщений
     *
     * Все объекты такого типа единообразно хранятся в этой коллекции
     */
    private final ConcurrentMap<String, ChatGuiDescriptor> activeChats;

    private MessageReceiver receiver; // получатель сообщений во всех открытых чатах (каждый в отдельном потоке)
    private ChatGuiDescriptor currentChatDescriptor; // описание текущего чата
    private VBox scrollableTopics; // контейнер ссылок, закреплённых за дескриптором чата
    private Parent rootScene; // корневой элемент для отображаемого графического интерфейса
    private Stage primaryStage; // главное окно приложения
    private ExecutorService executorService = Executors.newFixedThreadPool(4);


    /**
     * Конструктор класса. Выполняет инициализацию важных полей, а также инициализацию окна приложения
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     * @throws URISyntaxException
     */
    public DedicatedHostGUI() throws NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        activeChats = new ConcurrentHashMap<>();

        factory.setUri(CONNECTION_URI);
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);

        receiver = new StatelessMessageReceiverImpl((chatId, message) -> {
            if (activeChats.containsKey(chatId)) {
                activeChats.get(chatId).addMessage(message);
                if (chatId != null && chatId.equals(currentChatDescriptor.getName())) {
                    outputTextArea.appendText(String.valueOf(message));
                    outputTextArea.appendText(System.lineSeparator());
                }
            }
        });

        rootScene = initScene();
    }


    /**
     * Инициализация всей структуры графического интерфейса. Самый <b>страшный</b> и <b>запутанный</b> метод...
     *
     * @return родитель сцены
     */
    private Parent initScene() {
        // add vBox panel as main panel
        VBox vBox = new VBox();

        // 1. vBox consists of upper selector of username
        HBox userNameBox = new HBox();
        userNameBox.setPadding(new Insets(5, 0, 5, 0));
        vBox.getChildren().add(userNameBox);

        // add username input box
        Label userName = new Label("User name:");
        userName.setPadding(new Insets(5, 20, 5, 10));
        userNameBox.getChildren().add(userName);

        usernameField = new TextField("Timofey Bryksin");
        usernameField.setBackground(normalBackground);

        userNameBox.getChildren().add(usernameField);
        HBox.setHgrow(usernameField, Priority.ALWAYS);

        // 2. vBox consists of hBoxChatAndMsg

        HBox hBoxChatAndMsg = new HBox();
        hBoxChatAndMsg.setAlignment(Pos.CENTER);
        hBoxChatAndMsg.setPadding(new Insets(5, 0, 5, 0));

        vBox.getChildren().add(hBoxChatAndMsg);

        // 2.1 add topics scroll
        scrollableTopics = new VBox();
        scrollableTopics.setPrefWidth(150.0);
        ScrollPane sp = new ScrollPane(scrollableTopics);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(sp, Priority.ALWAYS);

        // also, add "+ chat button" above the scrollable list
        Button addChat = new Button("+ chat");
        addChat.setOnMouseClicked(mouseEvent -> this.addChatPrompt());
        // add them to VBox and then to hBoxChatAndMsg:
        VBox groupAddChatAndChats = new VBox();
        groupAddChatAndChats.setMinWidth(CHAT_SELECTION_WIDTH);
        groupAddChatAndChats.setMaxWidth(CHAT_SELECTION_WIDTH);
        groupAddChatAndChats.getChildren().addAll(addChat, sp);

        addChat.prefWidthProperty().bind(groupAddChatAndChats.widthProperty());

        hBoxChatAndMsg.getChildren().add(groupAddChatAndChats);

        // 2.2 add messaging part -- box for output messages
        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);
        outputTextArea.setWrapText(true);
        HBox.setHgrow(outputTextArea, Priority.ALWAYS);
        hBoxChatAndMsg.getChildren().add(outputTextArea);
        VBox.setVgrow(hBoxChatAndMsg, Priority.ALWAYS);

        // 3. finally, add text area with submit button
        inputTextArea = new TextArea();
        inputTextArea.setPadding(new Insets(5, 0, 0, 0));
        inputTextArea.setWrapText(true);
        inputTextArea.setPrefRowCount(4);
        inputTextArea.setOnKeyPressed(keyEvent ->  {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                if (keyEvent.isShiftDown() ^ keyEvent.isControlDown()) {
                    inputTextArea.appendText("\n");
                } else {
                    sendMessage(); // отправляем сообщение, когда нажали Enter в поле ввода
                }
            }
        });
        vBox.getChildren().add(inputTextArea);

        Button submitButton = new Button("Submit");
        submitButton.prefWidthProperty().bind(vBox.widthProperty());
        submitButton.setOnMouseClicked(e->this.sendMessage());

        vBox.getChildren().add(submitButton);

        return vBox;
    }

    /**
     * Внутренний класс описывает чат, а также некоторые его
     * важные характеристики -- его имя, по которому происходит его
     * идентификация, а также GUI элемент в левой части меню,
     * который в точности отвечает за выданный дескриптор
     */
    private class ChatGuiDescriptor {
        private String name; // имя чата
        private Chat chat; // объект типа Chat
        private Hyperlink guiHyperlink; // ссылка на элемент интерфейса слева в боковой панели

        ConcurrentLinkedDeque<Message> history; // известная история сообщений в этом чате

        /**
         * Конструируется объект типа чат с заданным именем. Не рекомендуется создавать
         * более одного объекта подобного вида для одного и того же имени, поскольку
         * имя chatName должно уникально идентифицировать объект.
         * @param chatName имя чата, которому присваивается дескриптор
         * @throws IOException
         * @throws TimeoutException
         */
        public ChatGuiDescriptor(String chatName) throws IOException, TimeoutException {
            name = chatName;
            this.chat = new ChatImpl(chatName, receiver, factory, executorService);
            guiHyperlink = new Hyperlink(chatName);
            guiHyperlink.setOnMouseClicked(event-> switchToChat(name));
            history = new ConcurrentLinkedDeque<>();
        }

        public String getName() {
            return name;
        }

        public Chat getChat() {
            return chat;
        }

        public Hyperlink getGuiHyperlink() {
            return guiHyperlink;
        }

        /**
         * Добавляет сообщение к текущему чату
         * @param message добавляемое сообщение в чате
         */
        public void addMessage(Message message) {
            history.addLast(message);
        }

        /**
         * Метод позволяет обновить историю сообщений в окне вывода исходя из своей истории сообщений
         */
        public void restoreMessages() {
            StringBuilder sb = new StringBuilder();
            for (Message m : history) {
                sb.append(m);
                sb.append('\n');
            }
            outputTextArea.setBackground(normalBackground);
            outputTextArea.setText(sb.toString());
            outputTextArea.positionCaret(sb.length());
        }
    }

    /**
     * Данный метод порождает окошко с сообщением об ошибке
     * @param title Заголовок окошка
     * @param error Текст ошибки
     */
    private void generateErrorPrompt(String title, String error) {
        TextArea errorLabel = new TextArea(error);
        errorLabel.setEditable(false);
        errorLabel.setWrapText(true);

        StackPane secondaryLayout = new StackPane();
        secondaryLayout.getChildren().add(errorLabel);

        Scene secondScene = new Scene(secondaryLayout, 230, 100);

        // New window (Stage)
        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.setScene(secondScene);
        newWindow.initModality(Modality.APPLICATION_MODAL);

        // Set position of second window, related to primary window.
        newWindow.show();
    }

    /**
     * Данный метод порождает окошко со строковым полем ввода
     * @param title Заголовок окошка
     * @param prompt Текст с описанием о запрашиваемом значении от пользователя
     * @param defaultValue Значение, оставляемое в поле ввода по умолчанию
     * @param resultConsumer Потребитель результата ввода
     */
    private void generateStringInputPrompt(String title, String prompt, String defaultValue,
                                           Consumer<String> resultConsumer) {
        Label promptLabel = new Label(prompt);
        TextField textInput = new TextField(defaultValue);
        Button okButton = new Button("OK");
        Button cancelButton = new Button("Cancel");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(promptLabel, 0, 0, 2, 1);
        grid.add(textInput, 0, 1, 2, 1);
        grid.add(okButton, 0, 2);
        grid.add(cancelButton, 1, 2);

        Scene secondScene = new Scene(grid, 230, 100);

        // New window (Stage)
        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.setScene(secondScene);
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(primaryStage);
        newWindow.setX(primaryStage.getX() + primaryStage.getWidth() / 2 - 150);
        newWindow.setY(primaryStage.getY() + primaryStage.getHeight() / 2 - 150);

        okButton.setOnMouseClicked(mouseEvent -> {
            resultConsumer.accept(textInput.getText());
            newWindow.close();
        });
        cancelButton.setOnMouseClicked(mouseEvent -> newWindow.close());

        // Set position of second window, related to primary window.
        newWindow.show();
    }

    /**
     * Данный метод порождает окошко, в котором можно ввести имя для нового чата
     */
    private void addChatPrompt() {
        generateStringInputPrompt("New chat", "Enter chat name",
                "chat #" + (activeChats.size() + 1),
                this::switchToChat);
    }

    /**
     * Вызов метода позволяет переключиться с одного чата на другой
     * @param chatName имя чата, на который происходит переключение
     */
    private void switchToChat(String chatName) {
        ChatGuiDescriptor cgd; // получаем дескриптор чата, на который хотим поменять текущий
        if (!activeChats.containsKey(chatName)) {
            try {
                cgd = new ChatGuiDescriptor(chatName);
            } catch (TimeoutException | IOException exception) {
                // не удалось подключиться ...
                generateErrorPrompt("Cannot create chat", exception.toString());
                return; // обязательно возвращаемся из метода, чтобы не поломать интерфейс
            }
            activeChats.put(chatName, cgd);
            scrollableTopics.getChildren().add(cgd.getGuiHyperlink());
        } else {
            cgd = activeChats.get(chatName);
        }

        // если добрались до этого момента, что смена чата произошла успешно
        if (currentChatDescriptor != null) {
            // отменяем покраску ссылки для старого дескриптора
            currentChatDescriptor.getGuiHyperlink().setVisited(false);
        }
        // затем, обновляем текущий дескриптор
        currentChatDescriptor = cgd;
        currentChatDescriptor.getGuiHyperlink().setVisited(true);

        // не забываем о том, что историю сообщений также необходимо обновить
        cgd.restoreMessages();
    }

    /**
     * Метод позволяет отправить сообщение в чат. Текст сообщения берётся из поля ввода,
     * в которое пользователь осуществил ввод
     */
    private void sendMessage() {
        String messageText = inputTextArea.getText();
        if ("".equals(messageText.trim())) {
            return; // ничего не отправляем, если ничего не введено
        }

        boolean success = sendMessage(messageText.trim());

        if (success) {
            inputTextArea.setText("");
            inputTextArea.positionCaret(0);
        }
    }

    /**
     * Метод позволяет отправить сообщение в чат
     * @param message отправляемое сообщение в чат
     */
    private boolean sendMessage(String message) {
        if (currentChatDescriptor == null) {
            outputTextArea.setText("Select chat first in the list to the left!");
            outputTextArea.setBackground(errorBackground);
            return false;
        }
        currentChatDescriptor.getChat().send(new com.Message(message, usernameField.getText(), ZonedDateTime.now()));
        return true;
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("SD chat [free licence]");
        primaryStage.show();

        if (rootScene == null) {
            rootScene = initScene();
        }

        // finally, add scene to window
        Scene scene = new Scene(rootScene, INITIAL_WINDOW_WIDTH, INITIAL_WINDOW_HEIGHT);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setScene(scene);

        // begin with prompting the chat name
        addChatPrompt();
    }

    @Override
    public void stop() throws Exception {
        activeChats.clear();
        System.exit(0);
    }
}
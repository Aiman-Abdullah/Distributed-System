

package server;

import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 *
 * @author Aiman
 */

/**
 * The controller class for the server control panel
 */
public class ServerControlPanel implements Initializable, DFSServerCallback {

    @FXML
    private Button btnStopOrStartServer;
    @FXML
    private TextField mServerDirectory;
    @FXML
    private ListView<String> clientList;
    @FXML
    private ListView<LogMessage> logList;

    private DFSServer server;

    public ServerControlPanel(){
        server = new DFSServer(this);
        ServerApplication.getApplication().getPrimaryStage().setOnCloseRequest((WindowEvent event) -> {
            stopServer();
            Platform.exit();
        });
    }

    /**
     * Initialized the controls
     *
     * @param resources
     * The resources used to localize the root object, or <tt>null</tt> if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ServerApplication.getApplication().getPrimaryStage().setOnHidden((WindowEvent event) -> {
            stopServer();
            Platform.exit();
            System.exit(0);
        });
        logList.setCellFactory((ListView<LogMessage> param) -> new LogItemCell());
    }

    /**
     * Called when the server is connected or disconnected
     *
     * @param server      the server details as string
     * @param isConnected true if connected, false otherwise
     */
    @Override
    public void onServerConnectedOrDisconnected(String server, boolean isConnected) {
        Platform.runLater(() -> {
            if (isConnected){
                printLog(LogType.INFO, server+" connected");
            }
            else {
                printLog(LogType.INFO, server+" disconnected");
            }
        });
    }

    /**
     * Called when some error occurs
     *
     * @param message the details of the error as string
     */
    @Override
    public void onError(String message) {
        printLog(LogType.ERROR, message);
    }

    /**
     * Called when a client is connected and disconnected
     *
     * @param client the client
     * @param isConnected true is connected, false otherwise
     */
    @Override
    public void onClientConnectedOrDisconnected(ClientHandler client, boolean isConnected) {
        Platform.runLater(() -> {
            String clientid = client.toString();
            if (isConnected){
                clientList.getItems().add(clientid);
                printLog(LogType.INFO, clientid+" connected");
            }
            else {
                clientList.getItems().remove(clientid);
                printLog(LogType.INFO, clientid+" disconnected");
            }
        });
    }

    /**
     * Called when a request has just arrived but not handled by the server
     *
     * @param client  the requester client details as string
     * @param request the request
     * @param allowed true if the request is allowed to be handled by the server, false otherwise
     */
    @Override
    public void onBeforeHandleRequest(ClientHandler client, Message request, boolean allowed) {
        Platform.runLater(() -> {
            MessageType type = request.getMessageType();
            String  clientid = client.toString();
            if (null != type)switch (type) {
                case PULL:{
                    String filename = request.getArgumentString(0);
                    printLog(allowed ? LogType.INFO : LogType.ERROR, "a pull request for \""+filename
                            +"\" from "+clientid+" is "+(allowed ? "in progress" : "aborted"));
                        break;
                    }
                case PUSH:{
                    String filename = request.getArgumentString(0);
                    printLog(allowed ? LogType.INFO : LogType.ERROR, "a push request for \""+filename
                            +"\" from "+clientid+" is "+(allowed ? "in progress" : "aborted"));
                        break;
                    }
                case DELETE:{
                    String filename = request.getArgumentString(0);
                    printLog(allowed ? LogType.INFO : LogType.ERROR, "a delete request for \""+filename
                            +"\" from "+clientid+" is "+(allowed ? "in progress" : "canceled"));
                        break;
                    }
                case VOTE:{
                    String decision = request.getArgumentString(0);
                    String filename= request.getArgumentString(1);
                    printLog(allowed ? LogType.INFO : LogType.ERROR, "a vote for \""+filename
                            +"\" from "+clientid+" is "+decision+" and vote "+(allowed ? "received" : "declined"));
                        break;
                    }
                default:
                    break;
            }
        });
    }

    /**
     * Called when a request is handled by the server
     *
     * @param client  the requester client details as string
     * @param request the request
     * @param handled true if the request is handled, false otherwise
     */
    @Override
    public void onAfterHandleRequest(ClientHandler client, Message request, boolean handled) {
        Platform.runLater(() -> {
            MessageType type = request.getMessageType();
            String  clientid = client.toString();
            if (MessageType.PULL == type){
                String filename = request.getArgumentString(0);
                printLog(handled ? LogType.INFO : LogType.ERROR, "a pull request for \""+filename+"\" by "+clientid+" is "+(handled ? "successful" : "failed"));
            }
            else if (MessageType.PUSH == type){
                String filename = request.getArgumentString(0);
                printLog(handled ? LogType.INFO : LogType.ERROR, "a push request for \""+filename+"\" by "+clientid+" is "+(handled ? "successful" : "fail"));
            }
        });
    }

    /**
     * Start/Stop button click action event handler which starts to stops the server
     * depending on its running status
     */
    @FXML
    private void stopOrStartServer(){
        if (server.isRunning()){
            stopServer();
        }
        else {
            startServer();
        }
    }

    /**
     * The choose directory button client action event handler method which opens a dialog chooser
     * to pick the shared directory for the server
     */
    @FXML
    private void chooseServerDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Server Directory");
        if (ServerSettings.canUseServerDirectory()) {
            directoryChooser.setInitialDirectory(ServerSettings.getServerDirectory());
        }
        File selectedDirectory = directoryChooser.showDialog(ServerApplication.getApplication().getPrimaryStage());
        if (selectedDirectory != null){
            mServerDirectory.setText(selectedDirectory.getAbsolutePath());
            ServerSettings.setServerDirectory(selectedDirectory);
        }
    }

    /**
     * Starts the server and if it starts successfully the change the start/stop button text to "Stop"
     */
    private void startServer(){
        server.start();
        if (server.isRunning()){
            btnStopOrStartServer.setText("Stop");
        }
    }

    /**
     *
     * @param type
     * @param message
     */
    private void printLog(LogType type, String message){
        logList.getItems().add(new LogMessage(type, Util.isNotEmptyString(message) ? message : ""));
    }

    /**
     * Stops the server and if it starts successfully the change the start/stop button text to "Start"
     */
    private void stopServer(){
        server.stop();
        if (!server.isRunning()) btnStopOrStartServer.setText("Start");
        clientList.getItems().clear();
    }

    /**
     * The exit button click action event handler method
     * that stops the server closed the main application window
     * and exit the application
     */
    @FXML
    private void exit(){
        stopServer();
        ServerApplication.getApplication().getPrimaryStage().close();
        Platform.exit();
    }

    /**
     * A custom ListCell class for the log list.
     * This class helps to change the text color depending on the log type
     */
    private class LogItemCell extends ListCell<LogMessage>{

        public LogItemCell(){
            super();
        }

        @Override
        protected void updateItem(LogMessage item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || null == item){
                setText(null);
                setGraphic(null);
            }
            else {
                Font font = new Font("monospace", 14);
                setFont(font);
                if (null != item.getType())switch (item.getType()) {
                    case ERROR:
                        setTextFill(Color.RED);
                        break;
                    case INFO:
                        setTextFill(Color.DARKBLUE);
                        break;
                    case DEBUG:
                        setTextFill(Color.BLUE);
                        break;
                    default:
                        break;
                }
                setText(item.getMessage());
            }
        }
    }
}

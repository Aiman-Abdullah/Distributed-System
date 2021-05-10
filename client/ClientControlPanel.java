
package client;

import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.WindowEvent;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import static Connection_client_server.Util.DEBUG;

/**
 *
 * @author Aiman
 */

/**
 * The controller class for the client control panel
 */
public class ClientControlPanel implements Initializable, DFSClientCallback, FileObserverCallback {

    @FXML
    private TextField mSharedDirectory;
    @FXML
    private TextField mUsername;
    @FXML
    private Button btnConnectDisconnect;

    private DFSClient client;
    private FileObserver observer;

    private File lastPulledFile = null;
    private File lastRemovedFile = null;

    public ClientControlPanel(){
        client = new DFSClient(this);
        observer = new FileObserver(this);
    }

    /**
     * Initializes the controllers
     *
     * @param location
     * The location used to resolve relative paths for the root object, or
     * <tt>null</tt> if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or <tt>null</tt> if
     * the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ClientApplication.getInstance().getPrimaryStage().setOnHidden((WindowEvent event) -> {
            disconnect();
            Platform.exit();
            System.exit(0);
        });
    }

    /**
     * Called when a server sends a response without any request from the client.
     *
     * @param response the response
     */
    @Override
    public void onNotification(Message response) {
        MessageType type = response.getMessageType();
        if (DEBUG) Util.log(type.toString());
        if (null != type) switch (type) {
            case INVALID:
                onDownload(new File(client.getSharedDirectory(), response.getArgumentString(0)));
                break;
            case QUERYDELETE:
                onQueryDelete(new File(client.getSharedDirectory(), response.getArgumentString(0)));
                break;
            case REMOVE:
            case RESTORE:
                onRestoreOrRemove(new File(client.getSharedDirectory(), response.getArgumentString(0)),
                        MessageType.RESTORE == type);
                break;
            default:
                break;
        }
    }

    /**
     * Called when a new response is available.
     *
     * @param request the last sent request
     * @param response the response
     */
    @Override
    public void onReply(Message request, Message response) {
        Platform.runLater(() -> {
            if (response.getMessageType() == MessageType.OK && request.getMessageType() == MessageType.LOGIN) {
                btnConnectDisconnect.setText("Disconnect");
                ClientApplication.getInstance().getPrimaryStage().setTitle(buildTitle("Client"));
                observer.startObserving(client.getSharedDirectory());
            }
            else if (response.getMessageType() == MessageType.ABORT){
                String cause = response.getArgumentString(0);
                if (null != request.getMessageType())switch (request.getMessageType()) {
                    case LOGIN:
                        showAlertMessage(Alert.AlertType.ERROR, "LogIn Error", cause);
                        break;
                    case PULL:
                        onDownloadComplete(false, cause);
                        break;
                    case PUSH:
                        onDownloadComplete(false, cause);
                        break;
                    case DELETE:
                        showAlertMessage(Alert.AlertType.ERROR, "Delete Fail", response.getArgumentString(1));
                        onDownload(new File(client.getSharedDirectory(), response.getArgumentString(0)));
                        break;
                    default:
                        break;
                }
            }
            else if (response.getMessageType() == MessageType.OPEN){
                if (request.getMessageType() == MessageType.PULL){
                    lastPulledFile = new File(client.getSharedDirectory(), request.getArgumentString(0));
                }
            }
            else if (response.getMessageType() == MessageType.CLOSE){
                String filename = response.getArgumentString(0);
                if (request.getMessageType() == MessageType.PULL){
                    onDownloadComplete(true, "File \"" + filename + "\" update successfully");
                }
                else if (request.getMessageType() == MessageType.PUSH){
                    onUploadComplete(true, "File \""+filename+"\" uploaded successfully");
                }
            }
        });
    }

    /**
     * Called when a new file  or child directory is created or moved to the observing directory
     *
     * @param which the newly created or moved file or child directory
     */
    @Override
    public void onCreated(File which) {
        if (DEBUG) Util.log(which.getAbsolutePath());
        Platform.runLater(() -> {
            if (!which.equals(lastPulledFile)){
                onUpload(which);
            }
        });
    }

    /**
     * Called when a file or child directory is deleted or moved from the observing directory
     *
     * @param which the deleted file or child directory
     */
    @Override
    public void onDeleted(File which) {
        if (DEBUG) Util.log(which.getAbsolutePath());
        if (!which.equals(lastRemovedFile)){
            client.delete(which);
        }
    }

    /**
     * Called when a file is modified
     *
     * @param which the file or directory which is modified
     */
    @Override
    public void onModified(File which) {}

    /**
     * Choose Directory button click action event callback method that opens a DirectoryChooser
     */
    @FXML
    private void onChooseSharedDirectory(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose Directory");
        if (client.canUseSharedDirectory()){
            directoryChooser.setInitialDirectory(client.getSharedDirectory());
        }
        File newSharedDirectory = directoryChooser.showDialog(ClientApplication.getInstance().getPrimaryStage());
        if (null != newSharedDirectory){
            mSharedDirectory.setText(newSharedDirectory.getAbsolutePath());
            client.changeSharedDirectory(newSharedDirectory);
        }
    }

    /**
     * Start/Stop button click action event callback method to connect or diconnect to server
     */
    @FXML
    private void onConnectOrDisconnect(){
        if (client.isConnected()){
            disconnect();
        }
        else {
            connect();
        }
    }

    /**
     *
     * @param file
     */
    private void onQueryDelete(File file){
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(buildTitle("Vote For Global Delete"));
            alert.setContentText("The file \""+file.getName()+"\" is deleted in a remote client, do you want to delete the local file?");
            ButtonType yes = new ButtonType("Yes");
            ButtonType no = new ButtonType("No");
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(yes, no);
            Optional<ButtonType> optional = alert.showAndWait();
            if (null != optional && optional.isPresent() && yes == optional.get()){
                client.vote("YES", file);
            }
            else {
                client.vote("NO", file);
            }
        });
    }

    /**
     *
     * @param which
     * @param restore
     */
    private void onRestoreOrRemove(File which, boolean restore){
        if (restore){
            if (!which.exists()){
                onDownload(which);
            }
        }
        else {
            try {
                which.delete();
                lastRemovedFile = which;
            }
            catch (Exception e){
                if (DEBUG) {
                }
                lastRemovedFile = null;
            }
        }
    }

    /**
     * upload a file to server
     */
    private void onUpload(File file){
        client.push(file);
    }

    /**
     * Called when upload is complete.
     *
     * @param success true means the request is handled successfully, false otherwise.
     *                if the request is aborted then it is false. on the other hand it is true
     *                when server sends close response. Though close does not mean that
     *                push is actually successful
     * @param message the message to show
     */
    private void onUploadComplete(boolean success, String message){
        if (success){
            showAlertMessage(Alert.AlertType.INFORMATION, "File Uploaded", message);
        }
        else {
            showAlertMessage(Alert.AlertType.ERROR, "File Not Uploaded", message);
        }
    }

    /**
     * download a file from the server
     *
     * @param file
     */
    private void onDownload(File file){
        client.pull(file);
    }

    /**
     * Called when the download is complete
     *
     * @param success true means the request is handled successfully, false otherwise.
     *                if the request is aborted then it is false. on the other hand it is true
     *                when server sends close response. Though close does not mean that
     *                push is actually successful
     * @param message the message to show
     */
    private void onDownloadComplete(boolean success, String message){
        if (success){
            showAlertMessage(Alert.AlertType.INFORMATION, "File Updated", message);
        }
        else {
            showAlertMessage(Alert.AlertType.ERROR, "File Not Updated", message);
        }
    }

    /**
     * Exit button client action event handler which exit the application.
     * First it disconnects the client from server and then  close the window
     * then exit the application
     */
    @FXML
    private void onExit(){
        disconnect();
        ClientApplication.getInstance().getPrimaryStage().close();
        Platform.exit();
        System.exit(0);
    }

    /**
     * connect to the server
     */
    private void connect(){
        if (!client.canUseSharedDirectory()){
            showAlertMessage(Alert.AlertType.ERROR, "Shared Directory Error", "the selected shared directory can not be used");
            return;
        }
        if (client.connect()){
            String username = mUsername.getText();
            client.login(username);
        }
        else {
            showAlertMessage(Alert.AlertType.ERROR, "Connect Error", "Fail to connected to server");
        }
    }

    /**
     * disconnect from the server
     */
    private void disconnect(){
        client.end();
        client.disconnect();
        if (!client.isConnected()){
            btnConnectDisconnect.setText("Connect");
            observer.stopObserving();
        }
        ClientApplication.getInstance().getPrimaryStage().setTitle(buildTitle("Client"));
    }

    /**
     * check weather client is connected or not
     * and show the message appropriately
     *
     * @return true if connected, false otherwise
     */
    private boolean checkConnectedOrAlert(){
        if (!client.isConnected()){
            showAlertMessage(Alert.AlertType.ERROR, "Connection Error", "Client not connected, connect to the server first");
            return false;
        }
        return true;
    }

    /**
     * Show a alert message
     *
     * @param type the AlertType
     * @param title the title of the Alert dialog
     * @param message the content of the Alert dialog
     */
    private void showAlertMessage(Alert.AlertType type, String title, String message){
        Alert alert = new Alert(type);
        alert.setTitle(buildTitle(title));
        alert.setContentText(message);
        alert.setResizable(false);
        alert.show();
    }

    /**
     * Build the title appending the current username of the user if connected successfully
     * otherwise rturns the title in the parameter
     *
     * @param title the title to modify
     * @return the build title
     */
    private String buildTitle(String title){
        String username = mUsername.getText();
        if (client.isConnected()){
            if (null == title || "".equals(title)){
                return "("+username+")";
            }
            else {
                return title+" ("+username+")";
            }
        }
        else {
            return title;
        }
    }
}

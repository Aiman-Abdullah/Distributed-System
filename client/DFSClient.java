
package client;

import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;

import java.io.File;

import static Connection_client_server.Util.DEBUG;

/**
 *
 * @author Aiman
 */

/**
 * The client class for the Distributed File System
 */
public class DFSClient implements DFSClientCallback {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 8910;

    private File sharedDirectory;

    private DFSClientThread dfsClientThread;
    private DFSClientCallback dfsClientCallback;
    private boolean isConnected = false;

    /**
     * Sets the shared directory
     *
     * @param directory the shared directory
     */
    public void changeSharedDirectory(File directory){
        sharedDirectory = directory;
    }

    /**
     * Returns the selected shared directory
     * @return the selected shared directory
     */
    public File getSharedDirectory(){
        return sharedDirectory;
    }

    /**
     * Checks if the shared directory can be used or not.
     * A directory is used only when it is not null and exists
     * and a directory and has read and write permission for the application
     *
     * @return true if can be used, false other wise
     */
    public boolean canUseSharedDirectory(){
        try{
            Util.checkValidDirectoryOrThrow(sharedDirectory);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    /**
     * Creates a new DFSClient instance with the callback
     *
     * @param callback the callback class
     */
    public DFSClient(DFSClientCallback callback){
        this.dfsClientCallback = callback;
    }

    /**
     * Connects to the server
     *
     * @return 
     */
    public boolean connect(){
        try {
            dfsClientThread = new DFSClientThread(sharedDirectory, SERVER_ADDRESS, SERVER_PORT, this);
            dfsClientThread.start();
            return true;
        }
        catch (Exception e){
            System.out.println(e.toString());
        }
        return false;
    }

    /**
     * Disconnects from the server
     */
    public void disconnect(){
        if (isConnected) {
            dfsClientThread.terminate();
            isConnected = !dfsClientThread.isTerminated();
            dfsClientThread = null;
        }
    }

    /**
     * Returns if the client is connected to the server.
     *
     * @return true if it is bound to the server and login successfully, false otherwise
     */
    public boolean isConnected(){
        return isConnected;
    }

    /**
     * Send login request
     *
     * @param username the username for the login
     */
    public void login(String username) {
        sendRequest(new Message(MessageType.LOGIN).addArgument(username));
    }

    /**
     * Send a push request
     *
     * @param file
     */
    public void push(File file) {
        String filename = extractRelativeFilename(file);
        sendRequest(new Message(MessageType.PUSH).addArgument(filename));
    }

    /**
     * Sends a pull request
     *
     * @param file
     */
    public void pull(File file) {
        String filename = extractRelativeFilename(file);
        sendRequest(new Message(MessageType.PULL).addArgument(filename));
    }

    public void delete(File file){
        String filename = extractRelativeFilename(file);
        sendRequest(new Message(MessageType.DELETE).addArgument(filename));
    }

    public void vote(String yourvote, File file){
        String filename = extractRelativeFilename(file);
        sendRequest(new Message(MessageType.VOTE).addArgument(yourvote).addArgument(filename));
    }

    /**
     * Sends a end request
     */
    public void end() {
        sendRequest(new Message(MessageType.END));
    }

    /**
     * Called when a new response is available.
     *
     * @param request  the last sent request
     * @param response the response
     */
    @Override
    public void onReply(Message request, Message response) {
        MessageType requestType = request.getMessageType();
        MessageType responseType = response.getMessageType();
        if (MessageType.LOGIN == requestType){
            isConnected = MessageType.OK == responseType;
        }
        else if (MessageType.END == requestType) {
            isConnected = false;
        }
        if (null != dfsClientCallback) dfsClientCallback.onReply(request, response);
    }

    /**
     * Called when a server sends a response without any request from the client.
     *
     * @param response the response
     */
    @Override
    public void onNotification(Message response) {
        if (null != dfsClientCallback) dfsClientCallback.onNotification(response);
    }

    private String extractRelativeFilename(File file){
        return file.getName();
    }
    
    private void sendRequest(Message request){
        if (null != dfsClientThread){
            dfsClientThread.sendRequest(request);
        }
    }
}

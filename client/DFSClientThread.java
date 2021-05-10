
package client;

import Connection_client_server.DaemonWorker;
import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;

import java.io.*;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

import static Connection_client_server.Util.DEBUG;

/**
 *
 * @author Aiman
 */

/**
 * The Thread class that actually handles the request sent by the client
 * and response received by the client
 */
class DFSClientThread extends DaemonWorker {

    private static final int BUFFER_SIZE = 512;

    private Object lock = new Object();

    private File sharedDirectory = null;
    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private DFSClientCallback dfsClientCallback;
    private Queue<Message> requestQueue;

    /**
     * The constructor to create a new DFSClientThread instance with the specified values
     *
     * @param sharedDirectory the directory designated for this client
     * @param serverAddress the address of the command server
     * @param serverPort the port of the command server on which it is listening
     * @param callback the DSFClientCallback to notify different states
     */
    public DFSClientThread(File sharedDirectory, String serverAddress, int serverPort, DFSClientCallback callback) throws Exception{
        connect(serverAddress, serverPort);
        changeSharedDirectory(sharedDirectory);
        this.dfsClientCallback = callback;
        requestQueue = new ArrayDeque<>();
    }

    /**
     * Connects to the command server using the specified server address and server port
     *
     * @param serverAddress the address of the command server
     * @param serverPort the port of at which the command server is listening
     * @throws Exception any exception occurred during the connection
     */
    private void connect(String serverAddress, int serverPort) throws Exception {
        clientSocket = new Socket(serverAddress, serverPort);
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
    }

    /**
     * Call this method to change the current shared directory
     *
     * @param file the new shared directory
     */
    public void changeSharedDirectory(File file){
        Util.checkValidDirectoryOrThrow(file);
        sharedDirectory = file;
    }

    /**
     * Call this method send a new request
     *
     * @param request the request to send
     */
    public void sendRequest(Message request){
        if (null == request) return;
        MessageType type = request.getMessageType();
        if (MessageType.PULL == type || MessageType.PUSH == type) {
            requestQueue.add(request);
            requestQueue.add(request);
        } else {
            requestQueue.add(request);
        }
        write(request);
    }

    @Override
    public void run() {
        while (!clientSocket.isClosed()){
            Message response = read();
            if (null != response) {
                synchronized (lock) {
                    onResponse(response);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTerminate() throws Throwable {
        Util.closeSilently(reader, writer, clientSocket);
    }

    /**
     * Called when a new response from command server is found
     *
     * @param response the response Message Object
     */
    private void onResponse(Message response){
        MessageType type = response.getMessageType();
        if (MessageType.INVALID == type || MessageType.QUERYDELETE == type
                || MessageType.REMOVE == type || MessageType.RESTORE == type){
            if (null != dfsClientCallback) dfsClientCallback.onNotification(response);
            return;
        }
        Message request = requestQueue.poll();
        if (MessageType.OPEN == type) {
            if (MessageType.PUSH == request.getMessageType()){
                openPush(request, response);
            }
            else if (MessageType.PULL == request.getMessageType()){
                openPull(request, response);
            }
        }
        if (null != dfsClientCallback) dfsClientCallback.onReply(request, response);
    }

    /**
     * Handles a push request. This method is called only when a push is allowed,
     * means only when OPEN response is received.
     *
     * @param request the sent push request
     * @param response the response of the push request
     */
    private void openPush(Message request, Message response){
        String address = response.getArgumentString(0);
        int port = response.getArgumentInteger(1);
        Socket client = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;
        try{
            if (DEBUG) Util.log("connecting to server");
            client = new Socket(address, port);
            if (DEBUG) Util.log("connected to server from "+client.getLocalSocketAddress()+" to "+client.getRemoteSocketAddress());
            String filename = request.getArgumentString(0);
            File file = new File(sharedDirectory, filename);
            bin = new BufferedInputStream(new FileInputStream(file));
            bout = new BufferedOutputStream(client.getOutputStream());
            byte[] buff = new byte[BUFFER_SIZE];
            int readlen = 0;
            if (DEBUG) Util.log("file transfer start "+filename);
            while ((readlen = bin.read(buff)) > 0){
                bout.write(buff, 0, readlen);
                bout.flush();
            }
            if (DEBUG) Util.log("file transfer complete "+filename);
        }
        catch (IOException e){
            System.out.println(e.toString());
        }
        finally {
            Util.closeSilently(bin, bout, client);
            if (DEBUG) Util.log("closing connection");
        }
    }

    /**
     * handles a pull request. This method is called only when a pull is allowed,
     * means only when OPEN response is received.
     *
     * @param request the sent pull request
     * @param response the response for the pull request
     */
    private void openPull(Message request, Message response){
        String address = response.getArgumentString(0);
        int port = response.getArgumentInteger(1);
        Socket client = null;
        BufferedInputStream bin = null;
        BufferedOutputStream bout = null;
        try{
            if (DEBUG) Util.log("connecting to server");
            client = new Socket(address, port);
            if (DEBUG) Util.log("connected to server from "+client.getLocalSocketAddress()+" to "+client.getRemoteSocketAddress());
            String filename = request.getArgumentString(0);
            File file = new File(sharedDirectory, filename);
            bin = new BufferedInputStream(client.getInputStream());
            bout = new BufferedOutputStream(new FileOutputStream(file));
            byte[] buff = new byte[BUFFER_SIZE];
            int readlen = 0;
            if (DEBUG) Util.log("file transfer start "+filename);
            while((readlen = bin.read(buff)) > 0){
                bout.write(buff, 0, readlen);
                bout.flush();
            }
            if (DEBUG) Util.log("file transfer complete "+filename);
        }
        catch (IOException e){
            System.out.println(e.toString());
        }
        finally {
            Util.closeSilently(bout, bin, client);
            if (DEBUG) Util.log("closing connection");
        }
    }

    /**
     * Writes the request message to the OutputStream connected to the command server
     *
     * @param request the Message object of the request to send
     */
    private void write(Message request){
        synchronized (lock) {
            try {
                writer.write(request.create());
                writer.flush();
                if (DEBUG) Util.log("request sent : " + request);
            }
            catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    /**
     * Reads the next command from the InputStream connected to the command server.
     *
     * @return the Message object of the response,
     *          <code>null</code> if there is any exception in reading or parsing the response
     */
    private Message read(){
        try {
            String line = reader.readLine();
            if (Util.isNotEmptyString(line)){
                Message response = new Message(line);
                if (DEBUG) Util.log("response received : "+response);
                return response;
            }
        }
        catch (IOException e) {
            System.out.println(e.toString());
        }
        return null;
    }
}

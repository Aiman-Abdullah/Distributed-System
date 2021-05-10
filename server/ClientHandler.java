
package server;

import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static Connection_client_server.Util.DEBUG;

/**
 *
 * @author Aiman
 */

/**
 * This class handles each incoming request from a single client
 */
public class ClientHandler implements Runnable {

    private static final int BUFFER_SIZE = 512;

    private final Object lock = new Object();

    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;

    private ClientHandlerCallback clientHandlerCallback;

    private String username;
    private boolean authenticated = false;

    /**
     * The constructor method
     *
     * @param clientSocket the {@link java.net.Socket} object which is bound a client
     * @param clientHandlerCallback the {@link server.ClientHandlerCallback} callback
     * @throws IOException thrown by clientSocket
     */
    public ClientHandler(Socket clientSocket, ClientHandlerCallback clientHandlerCallback) throws IOException {
        if (null == clientHandlerCallback){
            throw new NullPointerException("requestCallback must not null");
        }
        this.clientSocket = clientSocket;
        reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        this.clientHandlerCallback = clientHandlerCallback;
    }

    /**
     * Send a invalidation notice to the connected client
     *
     * @param filename the name of file to invalidate
     */
    public void invalidate(String filename){
        synchronized (lock)  {
            write(new Message(MessageType.INVALID).addArgument(filename));
        }
    }

    /**
     * Prepares and send a QUERYDELETE response
     *
     * @param filename the file for which voting for delete is required
     */
    public void queryDelete(String filename){
        synchronized (lock){
            write(new Message(MessageType.QUERYDELETE).addArgument(filename));
        }
    }

    /**
     * Prepares and sends a REMOVE response
     *
     * @param filename the file to remove
     */
    public void remove(String filename){
        synchronized (lock){
            write(new Message(MessageType.REMOVE).addArgument(filename));
        }
    }

    /**
     * Prepares and send a RESTORE response
     *
     * @param filename the file to restore
     */
    public void restore(String filename){
        synchronized (lock){
            write(new Message(MessageType.RESTORE).addArgument(filename));
        }
    }

    /**
     * Returns the username of the client
     *
     * @return the username of the client
     */
    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        while (!clientSocket.isClosed()) {
            Message request = read();
            if (null != request) {
                synchronized (lock) {
                    MessageType type = request.getMessageType();
                    if (MessageType.LOGIN == type) {
                        authenticate(request);
                        if (!authenticated) break;
                    }
                    else {
                        boolean allowed = clientHandlerCallback.onBeforeRequestHandle(this, request);
                        boolean handled = false;
                        if (null != type) switch (type) {
                            case PUSH:
                                handled = push(request, allowed);
                                break;
                            case PULL:
                                handled = pull(request, allowed);
                                break;
                            case DELETE:
                                handled = delete(request, allowed);
                                break;
                            case VOTE:
                            case END:
                                if (allowed){
                                    write(new Message(MessageType.OK));
                                    handled = true;
                                }
                                else {
                                    handled = false;
                                }   break;
                            default:
                                break;
                        }
                        clientHandlerCallback.onAfterRequestHandle(this, request, handled);
                        if (MessageType.END == type) break;
                    }
                }
            }
        }
        if (authenticated) clientHandlerCallback.onClientDisconnected(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj || !(obj instanceof ClientHandler)){
            return false;
        }
        if (null == username) return false;
        ClientHandler other = (ClientHandler) obj;
        return username.equals(other.getUsername());
    }

    @Override
    public String toString() {
        return username+" ("+clientSocket.getInetAddress().getHostAddress()+":"+clientSocket.getPort()+")";
    }

    /**
     * Closes all the bound sockets.
     * This method should be called before closing the thread.
     */
    public void terminate(){
        Util.closeSilently(reader, writer, clientSocket);
    }

    /**
     * Authenticates the new user.
     * A new user is authenticated if and only if it has a non empty and unique username
     *
     * @param request the request {@link Connection_client_server.Message Message} object
     */
    private void authenticate(Message request){
        authenticated = false;
        if (request.argumentCount() > 0){
            username = request.getArgumentString(0);
            if (Util.isNotEmptyString(username)){
                authenticated = clientHandlerCallback.onBeforeRequestHandle(this, request);
            }
        }
        if (authenticated){
            write(new Message(MessageType.OK));
        }
        else {
            write(new Message(MessageType.ABORT).addArgument("Double Names Not allwoed"));
        }
        clientHandlerCallback.onAfterRequestHandle(this, request, authenticated);
    }

    /**
     * Handles a pull request.
     *
     * @param request the request {@link Connection_client_server.Message Message} object
     */
    private boolean  pull(Message request, boolean allowed){
        if (allowed){
            ServerSocket dataServer = null;
            try {
                dataServer = new ServerSocket(0, 1, InetAddress.getByName(ServerSettings.getDataIP()));
                write(new Message(MessageType.OPEN)
                        .addArgument(ServerSettings.getDataIP())
                        .addArgument(dataServer.getLocalPort()));
            }
            catch (IOException e){
               
                write(new Message(MessageType.ABORT).addArgument("error in establishing data connection"));
                return false;
            }
            Socket dataClient = null;
            BufferedInputStream bin = null;
            BufferedOutputStream bout = null;
            String filename = request.getArgumentString(0);
            try {
                if (DEBUG) Util.log("waiting for new client @ "+dataServer.getLocalSocketAddress());
                dataClient = dataServer.accept();
                if (DEBUG)  Util.log("client connected "+dataClient.getRemoteSocketAddress());
                File file = new File(ServerSettings.getServerDirectory(), filename);
                bin = new BufferedInputStream(new FileInputStream(file));
                bout = new BufferedOutputStream(dataClient.getOutputStream());
                byte[] buff = new byte[BUFFER_SIZE];
                int readlen = 0;
                if (DEBUG)  Util.log("data transfer begin for "+filename);
                while ((readlen = bin.read(buff)) > 0) {
                    bout.write(buff, 0, readlen);
                    bout.flush();
                }
                if (DEBUG)  Util.log("data transfer complete for "+filename);
            }
            catch (IOException e){
                System.out.println(e.toString());
            }
            finally {
                Util.closeSilently(bin, bout, dataServer);
                if (DEBUG) Util.log("closing server");
            }
            write(new Message(MessageType.CLOSE).addArgument(filename));
            return true;
        }
        else {
            write(new Message(MessageType.ABORT).addArgument("can not complete due to another push request in progress"));
            return true;
        }
    }

    /**
     * Handles a push request
     *
     * @param request the request {@link Connection_client_server.Message Message} object
     */
    private boolean push(Message request, boolean allowed){
        if (allowed){
            ServerSocket dataServer = null;
            try{
                dataServer = new ServerSocket(0, 1, InetAddress.getByName(ServerSettings.getDataIP()));
                if (DEBUG) Util.log("data server connected @ "+dataServer.getLocalSocketAddress());
                write(new Message(MessageType.OPEN)
                        .addArgument(ServerSettings.getDataIP())
                        .addArgument(dataServer.getLocalPort()));
            }
            catch (IOException e){
               
                write(new Message(MessageType.ABORT).addArgument("error in establishing data connection"));
                return false;
            }
            Socket dataClient = null;
            BufferedInputStream bin = null;
            BufferedOutputStream bout = null;
            String filename = request.getArgumentString(0);
            try{
                if (DEBUG) Util.log("waiting for new client @ "+dataServer.getLocalSocketAddress());
                dataClient = dataServer.accept();
                if (DEBUG) Util.log("client connected: "+dataClient.getRemoteSocketAddress());
                File file = new File(ServerSettings.getServerDirectory(), filename);
                bin = new BufferedInputStream(dataClient.getInputStream());
                bout = new BufferedOutputStream(new FileOutputStream(file));
                byte[] buff = new byte[BUFFER_SIZE];
                int readlen = 0;
                if (DEBUG) Util.log("data transfer begin for "+filename);
                while((readlen = bin.read(buff)) > 0){
                    bout.write(buff, 0, readlen);
                    bout.flush();
                }
                if (DEBUG) Util.log("data transfer complete for "+filename);
            }
            catch (IOException e){
                System.out.println(e.toString());
            }
            finally {
                Util.closeSilently(bout, bin, dataClient, dataServer);
                if (DEBUG) Util.log("closing server");
            }
            write(new Message(MessageType.CLOSE).addArgument(filename));
            return true;
        }
        else {
            write(new Message(MessageType.ABORT).addArgument("can not complete due to another push request in progress"));
            return false;
        }
    }

    /**
     * Handles a delete request.
     *
     * @param request the request Message object
     * @param allowed true if the delete of the file is allowed, false otherwise
     * @return same as allowed
     */
    private boolean delete(Message request, boolean allowed){
        if (allowed){
            write(new Message(MessageType.OK));
            return true;
        }
        else {
            write(new Message(MessageType.ABORT)
                    .addArgument(request.getArgumentString(0))
                    .addArgument("delete is currently not possible"));
            return false;
        }
    }

    private void write(Message response){
        try {
            writer.write(response.create());
            writer.flush();
            if (DEBUG) Util.log("("+this+") response send : "+response);
        }
        catch (IOException e){
            terminate();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private Message read(){
        try {
            String line = reader.readLine();
            if (Util.isNotEmptyString(line)){
                Message request = new Message(line);
                if (DEBUG) Util.log("("+this+") request received : "+request);
                return request;
            }
        }
        catch (IOException e){
            terminate();
        }
        catch (Exception e) {
            System.out.println(e.toString());
        }
        return null;
    }
}



package server;

import Connection_client_server.DaemonWorker;
import Connection_client_server.Message;
import Connection_client_server.MessageType;
import Connection_client_server.Util;
import static Connection_client_server.Util.DEBUG;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Aiman
 */

/**
 * This class accepts incoming client and handles the inter client communications
 */
class DFSServerThread extends DaemonWorker implements ClientHandlerCallback {
    
    private Object lock = new Object();

    private ServerSocket commandServer;
    private ExecutorService executor;
    private List<ClientHandler> connectedClients;
    private DFSServerCallback dfsServerCallback;

    // the client which is currently issued a push request
    // null means no client doing push request. The current
    // implementation of DFS server handles a single push at a time
    private ClientHandler pushRequestBy = null;

    // the current delete action in progress, null means no action in progress
    // the current implementation of the DFS server
    // handles one delete request at a single time,
    private DeleteAction actionInProgress = null;

    public DFSServerThread(DFSServerCallback dfsServerCallback) throws NullPointerException, IOException {
        if (null == dfsServerCallback){
            throw new NullPointerException("DFSServerCallback is null");
        }
        this.commandServer = new ServerSocket(ServerSettings.getCommandPort(), 0, InetAddress.getByName(ServerSettings.getCommandIP()));
        this.executor = Executors.newCachedThreadPool();
        this.connectedClients = Collections.synchronizedList(new ArrayList<>());
        this.dfsServerCallback = dfsServerCallback;
    }

    @Override
    public void run() {
        while (!commandServer.isClosed()){
            Socket clientSocket = null;
            try {
                clientSocket = commandServer.accept();
            } catch (IOException e) {}

            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    executor.execute(handler);
                } catch (IOException e) {}
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onTerminate() throws Throwable {
        for (int i = 0; !connectedClients.isEmpty(); i++){
            connectedClients.remove(0).terminate();
        }
        Util.closeSilently(commandServer);
        executor.shutdownNow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onBeforeRequestHandle(ClientHandler handler, Message request) {
        if (DEBUG) Util.log(handler.toString());
        MessageType type = request.getMessageType();
        boolean allowed = false;
        if (null != type)switch (type) {
            case LOGIN:
                allowed = !connectedClients.contains(handler)
                        && connectedClients.size() < ServerSettings.getMaxClient();
                break;
            case PUSH:
                synchronized (lock) {
                    if (!isPushInProgress()) {
                        pushRequestBy = handler;
                        
                        allowed = true;
                    } else {
                        allowed = false;
                    }
                }   break;
            case PULL:
                synchronized (lock) {
                    allowed = !isPushInProgress();
                }   break;
            case DELETE:
                synchronized (lock){
                    actionInProgress = new DeleteAction(handler, request);
                    allowed = true;
                }   break;
            case VOTE:
                synchronized (lock){
                    if (null != actionInProgress){
                        String requestedFile = actionInProgress.getRequest().getArgumentString(0);
                        String votingFile = actionInProgress.getRequest().getArgumentString(0);
                        if (DEBUG) Util.log("requestedFile : "+requestedFile+" | votingFile : "+votingFile);
                        allowed = requestedFile.equals(votingFile);
                    }
                    else {
                        allowed = true;
                    }
                }   break;
            default:
                break;
        }
        dfsServerCallback.onBeforeHandleRequest(handler, request, allowed);
        return allowed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAfterRequestHandle(ClientHandler handler, Message request, boolean status) {
        if (DEBUG) Util.log(handler.toString());
        MessageType type = request.getMessageType();
        if (null != type)switch (type) {
            case LOGIN:
                if (status){
                    connectedClients.add(handler);
                    ServerSettings.setConnectedClient(connectedClients.size());
                    dfsServerCallback.onClientConnectedOrDisconnected(handler, true);
                }   break;
            case PUSH:
                synchronized (lock) {
                    if (status && handler == pushRequestBy) {
                        String filename = request.getArgumentString(0);
                        if (DEBUG) Util.log("push \""+filename+"\" successful, sending INVALID notice to other clients");
                        for (ClientHandler h : connectedClients) {
                            if (!h.equals(pushRequestBy)) {
                                h.invalidate(filename);
                            }
                        }
                        pushRequestBy = null;
                    }
                }   break;
            case DELETE:
                synchronized (lock){
                    if (status && handler == actionInProgress.getRequestedBy()){
                        String filename = request.getArgumentString(0);
                        if (DEBUG) Util.log("delete for \""+filename+"\" successful, sending QUERYDELETE notice to other clients");
                        for (ClientHandler h : connectedClients) {
                            if (!h.equals(actionInProgress.getRequestedBy())) {
                                h.queryDelete(filename);
                            }
                        }
                    }
                }   break;
            case VOTE:
                if (status){
                    boolean votingComplete = false, voteResult = false;
                    synchronized (lock){
                        actionInProgress.receiveDecision(handler, request);
                        if (DEBUG) Util.log(handler+" vote "+request.getArgumentString(0)
                                +" for \""+request.getArgumentString(1)+"\"");
                        
                        if ((votingComplete = actionInProgress.isVotingComplete())) voteResult = actionInProgress.getVoteResult();
                    }
                    /**
                     * In case of RESTORE the client send a pull request to download the deleted file
                     * Since the all the ClientHandlers handles the requests in a separate thread in synchronized block,
                     * so if the following is included into the synchronized block also the a dead lock may arise.
                     * So, it is handled un-synchronized
                     */
                    if (votingComplete) {
                        String filename = actionInProgress.getRequest().getArgumentString(0);
                        if (voteResult) {
                            sendRemoveOrRestoreNotice(filename, true);
                            try {
                                new File(ServerSettings.getServerDirectory(), filename).delete();
                            } catch (Exception e) {
                                
                            }
                        } else {
                            sendRemoveOrRestoreNotice( filename, false);
                        }
                        actionInProgress = null;
                    }
                }   break;
            default:
                break;
        }
        dfsServerCallback.onAfterHandleRequest(handler, request, status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClientDisconnected(ClientHandler handler) {
        if (DEBUG) Util.log(handler+": Disconnected");
        if (connectedClients.contains(handler)) {
            connectedClients.remove(handler);
            dfsServerCallback.onClientConnectedOrDisconnected(handler, false);
        }
        handler.terminate();
        ServerSettings.setConnectedClient(connectedClients.size());
    }

    @Override
    public String toString() {
        try{
            return " Server: " +commandServer.getLocalSocketAddress()+ "";
        }
        catch (Exception e){}
        return " Server ";
    }

    /**
     * Returns weather a push request by another client is in progress or not
     *
     * @return <code>true</code> is a push request is in progress,
     *          <code>false</code> otherwise
     */
    private boolean isPushInProgress(){
        return null != pushRequestBy;
    }

    /**
     * Send a REMOVE or RESTORE response to connected clients
     *
     * @param filename the filename to delete
     * @param remove true means send REMOVE response, false means send RESTORE response
     */
    private void sendRemoveOrRestoreNotice(String filename, boolean remove){
        if (DEBUG) Util.log("vote for \"" + filename + "\" complete, sending "+(remove ? "REMOVE" : "RESTORE")+" notice to other clients");
        final List<ClientHandler> clients = this.connectedClients;
        for (ClientHandler h : clients) {
            if (remove){
                h.remove(filename);
            }
            else {
                h.restore(filename);
            }
        }
    }
}

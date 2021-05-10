
package server;

import Connection_client_server.Util;
import java.io.IOException;

/**
 *
 * @author Aiman
 */

/**
 * The server class for Distributed File System
 * The GUI class use this class to operate the Distributed File System server
 */
public class DFSServer{

    private boolean isRunning = false;
    private DFSServerThread serverThread;
    private DFSServerCallback dfsServerCallback;

    public DFSServer(DFSServerCallback callback){
        this.dfsServerCallback = callback;
    }

    /**
     * starts the server
     */
    public void start(){
        if (ServerSettings.canUseServerDirectory()) {
            try {
                serverThread = new DFSServerThread(dfsServerCallback);
                serverThread.start();
                isRunning = true;
                dfsServerCallback.onServerConnectedOrDisconnected(serverThread.toString(), true);
            } catch (IOException | NullPointerException e) {
                Util.log(e.getMessage());
                if (null != dfsServerCallback) dfsServerCallback.onError("server not connected due to some error");
            }
        }
        else {
            if (null != dfsServerCallback) dfsServerCallback.onError("the current server directory can not be used, please change it in settings");
        }
    }

    /**
     * stops the server
     */
    public void stop(){
        if (isRunning){
            serverThread.terminate();
            isRunning = false;
            if (null != dfsServerCallback) dfsServerCallback.onServerConnectedOrDisconnected(serverThread.toString(), false);
        }
    }

    /**
     * Tells weathr the server is running or not
     *
     * @return true if the server is running, false otherwise
     */
    public boolean isRunning(){
        return this.isRunning;
    }
}

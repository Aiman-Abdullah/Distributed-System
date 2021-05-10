
package server;

import Connection_client_server.Util;

import java.io.File;

/**
 *
 * @author Aiman
 */

/**
 * This class stores values which are shared between different parts of the server
 */
public class ServerSettings {

    public static final String COMMAND_IP = "127.0.0.1";
    public static final int COMMAND_PORT = 8910;
    public static final String DATA_IP = COMMAND_IP;
    public static final int MAX_CLIENT = 3;

    private static int mConnectedClient = 0;
    private static File mServerDirectory = null;

    /**
     * Set the number of connected clients
     *
     * @param connectedClient the number of connected clients
     */
    public static void setConnectedClient(int connectedClient){
        mConnectedClient = connectedClient;
    }

    /**
     * Set the shared directory for the server
     *
     * @param newServerDirectory the shared directory for the server
     */
    public static void setServerDirectory(File newServerDirectory){
        mServerDirectory = newServerDirectory;
    }

    /**
     * The ip address of the server where all the requests are sent
     *
     * @return the server ip to send request
     */
    public static String getCommandIP(){
        return COMMAND_IP;
    }

    /**
     * The ip address of the server where actual file data are sent
     *
     * @return the server ip to send actual file data
     */
    public static String getDataIP(){
        return DATA_IP;
    }

    /**
     * The port for the server where the commands are send
     *
     * @return the port for sending commands to server
     */
    public static int getCommandPort(){
        return COMMAND_PORT;
    }

    /**
     * The max number of clients allowed to connect
     *
     * @return max number of clients allowed to connect
     */
    public static int getMaxClient(){
        return MAX_CLIENT;
    }

    /**
     * The number of current connected clients
     *
     * @return currently connected client number
     */
    public static int getConnectedClient(){
        return mConnectedClient;
    }

    /**
     * The shared directory for the server
     *
     * @return the shared directory for the server
     */
    public static File getServerDirectory(){
        return mServerDirectory;
    }

    /**
     * Weather the selected shared directory can be used or not.
     * A directory is used only when it is not null and exists
     * and a directory and has read and write permission for the application
     *
     * @return true if the server can be used, false otherwise
     */
    public static boolean canUseServerDirectory(){
        try{
            Util.checkValidDirectoryOrThrow(mServerDirectory);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }
}

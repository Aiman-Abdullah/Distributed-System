
package server;

import Connection_client_server.Message;

/**
 *
 * @author Aiman
 */

/**
 * The callback interface which notifies when a client connected status is changed.
 * Connected status means weather a new client is connected or a old client is disconnected.
 * It also notifies when new a incoming request is found and after the request is handles by the server.
 */
public interface DFSServerCallback {

    /**
     * Called when the server is connected or disconnected
     *
     * @param server the server details as string
     * @param isConnected true if connected, false otherwise
     */
    void onServerConnectedOrDisconnected(String server, boolean isConnected);

    /**
     * Called when some error occurs
     *
     * @param message the details of the error as string
     */
    void onError(String message);

    /**
     * Called when a client is connected and disconnected
     *
     * @param client the client
     * @param isConnected true is connected, false otherwise
     */
    void onClientConnectedOrDisconnected(ClientHandler client, boolean isConnected);

    /**
     * Called when a request has just arrived but not handled by the server
     *
     * @param client the client
     * @param request the request
     * @param allowed true if the request is allowed to be handled by the server, false otherwise
     */
    void onBeforeHandleRequest(ClientHandler client, Message request, boolean allowed);

    /**
     * Called when a request is handled by the server
     *
     * @param client the client
     * @param request the request
     * @param handled true if the request is handled, false otherwise
     */
    void onAfterHandleRequest(ClientHandler client, Message request, boolean handled);
}

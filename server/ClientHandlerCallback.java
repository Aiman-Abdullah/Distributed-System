

package server;

import Connection_client_server.Message;

/**
 *
 * @author Aiman
 */

/**
 * The callback interface used by each ClientHandler before and after handling each request
 * alse when a client is disconnected
 */
public interface ClientHandlerCallback {

    /**
     * Called before handling the request
     *
     * @param handler the called ClientHandle
     * @param message the Message object of the request
     * @return true if the request is allowed to process, false otherwise
     */
    boolean onBeforeRequestHandle(ClientHandler handler, Message message);

    /**
     * Called after hadeling the request
     *
     * @param handler the called ClintHandler
     * @param message the Message object of the request
     * @param status the value returned by the onBeforeRequestHandle(ClientHandler, Message) method
     */
    void onAfterRequestHandle(ClientHandler handler, Message message, boolean status);

    /**
     * Called when a client is disconnected
     *
     * @param handler the caller client
     */
    void onClientDisconnected(ClientHandler handler);
}

package client;

import Connection_client_server.Message;

/**
 *
 * @author Aiman
 */

/**
 * The callback interface for a new incoming response
 */
public interface DFSClientCallback {

    /**
     * Called when a new response is available.
     *
     * @param request the last sent request
     * @param response the response
     */
    void onReply(Message request, Message response);

    /**
     * Called when a server sends a response without any request from the client.
     *
     * @param response the response
     */
    void onNotification(Message response);
}

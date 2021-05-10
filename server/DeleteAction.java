

package server;

import Connection_client_server.Message;

/**
 *
 * @author Aiman
 */

/**
 * This class represents a complete delete request for each files from each clients.
 * This class stores the client who initiated the delete request and the request
 * {@link Connection_client_server.Message Mesage} object. The required information like how may voting
 * already completed and the final voting result, are also stored into this class.
 */
public class DeleteAction{

    private ClientHandler requestedBy;
    private Message request;
    private int voteCount = 0;
    private boolean voteResult = true;

    public DeleteAction(ClientHandler requestedBy, Message request){
        this.requestedBy = requestedBy;
        this.request = request;
    }

    /**
     * Returns the client who initiated the delete request
     *
     * @return the {@link server.ClientHandler ClientHandler} object who initiated the delete request
     */
    public ClientHandler getRequestedBy() {
        return requestedBy;
    }

    /**
     * Returns the delete request
     *
     * @return the delete request Message object
     */
    public Message getRequest() {
        return request;
    }

    /**
     * Returns the weather the voting process is complete or not.
     * Voting will complete only when the expected count of clients for vote
     * is equal to the number of votes already complete
     *
     * @return true if the completed vote count is equal to the expected vote count,
     *          false otherwise
     */
    public boolean isVotingComplete(){
        return voteCount == ServerSettings.getConnectedClient() - 1;
    }

    /**
     * Returns the final voting result.
     *
     * @return true if the all vote YES, false if at least one client vote NO.
     */
    public boolean getVoteResult(){
        return voteResult;
    }

    /**
     * Call this method when a VOTE request is received for the deleted file is received.
     * This method stores the stores the votes of the client and increases the completed
     * vote count.
     *
     * @param decisionBy the client who is voting
     * @param decision the vote of the client
     */
    public void receiveDecision(ClientHandler decisionBy, Message decision) {
        String vote = decision.getArgumentString(0);
        voteResult = voteResult && vote.equalsIgnoreCase("YES");
        voteCount++;
    }
}
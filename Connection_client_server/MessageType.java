


package Connection_client_server;

/**
 *
 * @author Aiman
 */
/**
 * This enum represents a single command type
 * Here are the meaning of each commands:
 *
 * LOGIN => used to login to the server with the username
 * OK => returned when last sent command executed successfully
 * PUSH => used to push a file to the server
 * PULL => used to pull a file from the server
 * INVALID => send by the server when a file has changed by someone
 * ABORT => send by the server when a open is canceled
 * OPEN => send by the server when PULL or PUSH is possible, the server ip and port
 *          of the server where the data will be, is sent with is command.
 * CLOSE => send by the server when PULL or PUSH operation is complete
 * DELETE => send a delete request a file specifies as the only argument
 * QUERYDELETE => whan a DELETE request from a client is received,
 *              this response is sent to other clients to initiate a voting.
 *              The file name is send as the only argument with this message.
 * VOTE => a client sends it decision about deletion of the specified file.
 *          If the client confirms the delete then it sends YES otherwise NO
 *          as first argument. The second argument is the filename for which
 *          the client is voting
 * REMOVE => the server send this to all client when all them VOTE YES for the file to delete.
 *           The filename is sent as the first argument
 * RESTORE => opposite to REMOVE, when at least one client VOTE NO, then the server send this response
 *             to all clients to instruct then to download the file if deleted.
 *             The filename is the only argument.
 * END => send by the client when wants to end the connection
 */
public enum MessageType
{
    LOGIN("LOGIN"),
    OK("OK"),
    PUSH("PUSH"),
    PULL("PULL"),
    INVALID("INVALID"),
    ABORT("ABORT"),
    OPEN("OPEN"),
    CLOSE("CLOSE"),
    DELETE("DELETE"),
    QUERYDELETE("QUERYDELETE"),
    VOTE("VOTE"),
    REMOVE("REMOVE"),
    RESTORE("RESTORE"),
    END("END");
    
    private String type;
    
    MessageType(String type){
        this.type = type;
    }

    @Override
    public String toString() {
        return type;
    }

    /**
     * Returns the MessageType from the String
     * @param s the type name
     * @return the MessageType object
     */
    public static MessageType from(String s){
        if (!Util.isNotEmptyString(s)){
            throw new NullPointerException("input string is empty");
        }
        s = s.toUpperCase();
        switch(s){
            case "LOGIN": return LOGIN;
            case "OK": return OK;
            case "PUSH": return PUSH;
            case "PULL": return PULL;
            case "INVALID": return INVALID;
            case "ABORT": return ABORT;
            case "OPEN": return OPEN;
            case "CLOSE": return CLOSE;
            case "DELETE": return DELETE;
            case "QUERYDELETE": return QUERYDELETE;
            case "VOTE": return VOTE;
            case "REMOVE": return REMOVE;
            case "RESTORE": return RESTORE;
            case "END": return END;
            default: {
                throw new IllegalArgumentException("type '"+s+"' not implemented");
            }
        }
    }
}


package server;

/**
 *
 * @author Aiman
 */


/**
 * A log message which will be displayed in the server log in the server control panel
 */
public class LogMessage {

    private LogType type;
    private String message;

    public LogMessage(LogType type, String message){
        this.type = type;
        this.message = message;
    }

    /**
     * The type of the log message
     *
     * @return the log message type
     */
    public LogType getType() {
        return type;
    }

    /**
     * The prepared log message
     *
     * @return the prepared log message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}

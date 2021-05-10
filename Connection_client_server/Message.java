

package Connection_client_server;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

/**
 *
 * @author Aiman
 */


/**
 * This class represents a single command.
 * This is a utility class to build and parse a command more easily
 * Each message must have a MessageType and option arguments
 */
public class Message
{
    // the string literal used to separate arguments
    public static final String ARG_SEPARATOR = " ";

    private String message = "";
    private MessageType type;
    private List<Object> args = new ArrayList<>();

    /**
     * Create a new Message object of the given MessageType
     *
     * @param type the MessageType of this message
     */
    public Message(MessageType type){
        this.type = type;
    }

    /**
     * Create a new Message object parsing the given message string
     *
     * @param message the string message to parse
     */
    public Message(String message){
        this.message = message;
        parse(message);
    }

    /**
     * Returns the MessageType of this message
     *
     * @return the MessageType of this message
     */
    public MessageType getMessageType(){
        return type;
    }

    /**
     * Returns the argument at the specified index as String
     *
     * @param index the index of the argument
     * @return the argument as String
     */
    public String getArgumentString(int index){
        return String.valueOf(args.get(index));
    }

    /**
     * Returns the argument at the specified index as long
     *
     * @param index the index of the argument
     * @return the argument as long
     */
    public long getArgumentLong(int index){
        return Long.parseLong(getArgumentString(index));
    }

    /**
     * Returns the argument at the specified index as int
     *
     * @param index the index of the argument
     * @return the argument as int
     */
    public int getArgumentInteger(int index){
        return Integer.parseInt(getArgumentString(index));
    }

    /**
     * Appends new string argument to this message
     *
     * @param arg the argument value as string
     * @return the current Message object
     */
    public Message addArgument(String arg){
        args.add(arg);
        return this;
    }

    /**
     * Appends new long argument to this message
     *
     * @param arg the argument value as long
     * @return the current Message object
     */
    public Message addArgument(long arg){
        args.add(arg);
        return this;
    }

    /**
     * Appends new int argument to this message
     *
     * @param arg the argument value as int
     * @return the current Message object
     */
    public Message addArgument(int arg){
        args.add(arg);
        return this;
    }

    /**
     * Returns the total number of arguments
     *
     * @return total number of arguments
     */
    public int argumentCount(){
        return args.size();
    }

    /**
     * Returns all arguments as array of Objects
     *
     * @return all arguments as array of Objects
     */
    public Object[] getArgumentsArray(){
        return args.toArray();
    }

    /**
     * Builds the command from the MessageType and given arguments
     *
     * @return the build command
     */
    public String create(){
        StringBuilder builder = new StringBuilder();
        builder.append(type.toString());
        if(!args.isEmpty()){
            for(Object arg : args){
                builder.append(ARG_SEPARATOR);
                
                 if(arg instanceof CharSequence){
                    builder.append("\"").append(arg).append("\"");
                }
                else{
                    builder.append(arg);
                }
            } 
        }
        builder.append("\r\n");
        message = builder.toString();

        return message;
    }

    /**
     * Parse the command string and create a new Message instance for the command
     *
     * @param m the command string
     */
    private void parse(String m){
        if(!Util.isNotEmptyString(m)) throw new IllegalArgumentException("message is empty");

        QuotedStringTokenizer tokenizer = new QuotedStringTokenizer(m, ARG_SEPARATOR);
        MessageType type2 = MessageType.from(tokenizer.nextToken());
        List<Object> args2 = new LinkedList<>();
        while (tokenizer.hasMoreToken()){
            args2.add(tokenizer.nextToken());
        }
        this.type = type2;
        this.args = args2;
    }

    @Override
    public String toString() {
        return message;
    }
}

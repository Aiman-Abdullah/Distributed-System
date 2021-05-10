package Connection_client_server;

import java.util.LinkedList;
import java.util.StringTokenizer;
/**
 *
 * @author Aiman
 */

/**
 * An utility class for split a given string by the given delimiter.
 * This class works in the similar way as {@link java.util.StringTokenizer StringTokennizer}
 * except that if a delimiter is found between two double quote (") then the string between
 * the double quotes treated as single.
 *
 * For example:
 *
 * PUSH "sample txt"
 *
 * this above string will be tokenized as if the delimiter is space
 *
 * PUSH
 * sample txt
 */
public class QuotedStringTokenizer
{
    private LinkedList<String> tokens;
    private String delim;

    /**
     * Create a new instance with the string to tokenize and space character ( )
     * as default delimiter
     *
     * @param target the string to tokenize
     */
    public QuotedStringTokenizer(String target){
        this(target, " ");
    }

    /**
     *  Create a new instance with the string to tokenize and the specified delimiter
     *
     * @param target the string to tokenize
     * @param delim the delimiter to split the string
     */
    public QuotedStringTokenizer(String target, String delim){
        if(null == target) throw new NullPointerException("target string is null");
        if(null == delim || "".equals(delim)) throw new NullPointerException("delimiter must not empty");
        
        this.delim = delim;
        tokens = new LinkedList<>(tokenize(target.trim()));
    }

    /**
     * Returns if more token are available or not
     *
     * @return true if more token is available, false otherwise
     */
    public boolean hasMoreToken(){
        return !tokens.isEmpty();
    }

    /**
     * Returns total available tokens
     *
     * @return the total available token
     */
    public int countTokens(){
        return tokens.size();
    }

    /**
     * Returns the next token
     *
     * @return the next token
     */
    public String nextToken(){
        return tokens.removeFirst();
    }

    /**
     * Tokenizes the string
     *
     * @param target the string to tokenize
     * @return the list of tokens
     */
    private LinkedList<String> tokenize(String target){
        LinkedList<String> tmp = new LinkedList<>();
        int quoteStart = target.indexOf('"');
        int quoteEnd = target.indexOf('"',quoteStart+1);
        
        if(quoteStart < 0){
            StringTokenizer tokenizer = new StringTokenizer(target, delim);
            while(tokenizer.hasMoreTokens()){
                tmp.add(tokenizer.nextToken());
            }
        }
        else if(quoteEnd < 0) {
            throw new IllegalArgumentException("broken quote at "+quoteStart);
        }
        else {
            String quotedToken = target.substring(quoteStart+1, quoteEnd);
            tmp.addFirst(quotedToken);
            if(quoteStart > 0){
                tmp.addAll(0, tokenize(target.substring(0, quoteStart-1).trim()));
            }
            if(target.length()-quoteEnd > 0){
                tmp.addAll(tokenize(target.substring(quoteEnd+1, target.length()).trim()));
            }
        }
        
        return tmp;
    }
}

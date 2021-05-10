
package Connection_client_server;
import java.io.File;

/**
 *
 * @author Aiman
 */

public class Util {

    // indicate weather to print the debugging message to the console or not
    public static final boolean DEBUG = false;

    /**
     * Checks weather the specified string is not empty. A string is non-empty
     * if and only if the string instance is non-null and the string length is greater than 0
     *
     * @param s the string to test fot nun-nullity
     * @return true if non-null, false otherwise
     */
    public static boolean isNotEmptyString(String s){
        return null != s & !"".equals(s);
    }

    public static boolean checkNonNull(Object o){
        return null != o;
    }

    /**
     * Checks if the specified directory is a valid directory.
     * A directory is valid if and only if the parameter is non-null,
     * the directory exists, it a type of directory, readable and writable.
     * If any one of these fails then throws a exception.
     *
     * @param directory the directory to test
     */
    public static void checkValidDirectoryOrThrow(File directory){
        if (null == directory){
            throw new NullPointerException("directory is null");
        }
        else if (!directory.isDirectory()){
            throw new IllegalArgumentException("not a valid directory");
        }
        else if (!directory.exists()){
            throw new IllegalArgumentException("directory does not exists");
        }
        else if (!directory.canRead()){
            throw new IllegalArgumentException("can not read the directory");
        }
        else if (!directory.canWrite()){
            throw new IllegalArgumentException("can not write to the directory");
        }
    }

    /**
     * Closes any {@link java.lang.AutoCloseable AutoClosable} without throwing any exception
     *
     * @param autoCloseables all the AutoCloseables to close
     */
    public static void closeSilently(AutoCloseable... autoCloseables){
        if (null == autoCloseables) return;
        for (AutoCloseable c : autoCloseables){
            try{
                c.close();
            }
            catch (Exception e){}
        }
    }

    /**
     * Prints a formatted message in the console for debugging purpose
     * The message includes the class name and the method name where the
     * following method is called.
     *
     * @param message the debug message
     */
    public static void log(String message){
        StackTraceElement e = new Throwable().getStackTrace()[1];
        String className = e.getClassName();
        String methodName = e.getMethodName();
        message = isNotEmptyString(message) ? message : "";
        System.out.println("["+className+"] ["+methodName+"] "+message);
    }
}

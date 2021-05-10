
package Connection_client_server;

/**
 *
 * @author Aiman
 */

/**
 * A worker thread which is daemon by default.
 * This kind of thread also handles the thread termination properly.
 */

public abstract class DaemonWorker extends Thread {

    // a boolean value that indicates weather the thread is terminated or not
    private boolean terminated = false;

    private Throwable terminationError = null;

    /**
     * Allocate a new DaemonThread by the default name
     */
    public DaemonWorker(){
        super();
        setDaemon(true);
    }

    /**
     * Allocate a new DaemonThread with the specified thread name
     *
     * @param name the name of the thread
     */
    public DaemonWorker(String name){
        super(name);
        setDaemon(true);
    }

    /**
     * Subclasses of <code>DaemonThread</code> should override this method.
     * The code for all the tasks handled by this thread goes here
     */
    @Override
    public abstract void run();

    /**
     * Returns weather this thread is terminated or not.
     *
     * @return true if terminated, false otherwise
     */
    public boolean isTerminated(){
        return terminated;
    }

    /**
     * Call this method when this thread need to be terminated.
     * If any error occurs during the termination this method
     * does not throw any error during method call. To get the error
     * use the method getTerminationError()
     */
    public void terminate(){
        if (!isTerminated()){
            try {
                onTerminate();
                interrupt();
                terminated = true;
            }
            catch (Throwable cause){
                terminationError = cause;
            }
            finally {
                terminated = true;
            }
        }
    }

    /**
     * Called when this thread is going to be terminated
     * either by calling terminate() or by the finalize().
     * Release all resources hold by this thread and stop any
     * infinite loop from here.
     *
     * @throws Throwable termination error
     */
    protected abstract void onTerminate() throws Throwable;

    /**
     * Returns the error occurs during the termination
     *
     * @return the cause of error during the thread termination, if any, {@code null} otherwise
     */
    public Throwable getTerminationError(){
        return terminationError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void finalize() throws Throwable {
        try {
            terminate();
        } finally {
            super.finalize();
        }
    }
}

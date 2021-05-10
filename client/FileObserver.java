
package client;

import java.io.File;

/**
 *
 * @author Aiman
 */

/**
 * This class observes for changes in the specified directory
 * and notifies the registered callback when something changes
 */
public class FileObserver {

    private FileObserverThread observerThread;
    private File observeDirectory;
    private FileObserverCallback fileObserverCallback;

    public FileObserver(FileObserverCallback callback){
        fileObserverCallback = callback;
    }

    /**
     * Start the observer to observer specified directory for any modification
     *
     * @param file the directory to observe to observe for changes,
     *             if the parameter is not a directory then the directory containing the file is used
     */
    public void startObserving(File file){
        observeDirectory = file.isDirectory() ? file : file.getParentFile();
        if (null == observerThread){
            startObserverThread(observeDirectory);
        }
        else if (!observeDirectory.equals(file)){
            stopObserving();
            startObserverThread(observeDirectory);
        }
    }

    /**
     * stop observing for changes
     */
    public void stopObserving(){
        if (null != observerThread){
            observerThread.interrupt();
            observerThread = null;
        }
    }

    /**
     * starts the observer thread
     *
     * @param directory the directory to observe
     */
    private void startObserverThread(File directory){
        observerThread = new FileObserverThread(directory, fileObserverCallback);
        observerThread.start();
    }
}

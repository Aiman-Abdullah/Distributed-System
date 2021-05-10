
package client;

import Connection_client_server.DaemonWorker;
import Connection_client_server.Util;

import static Connection_client_server.Util.DEBUG;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 *
 * @author Aiman
 */

/**
 * The Thread class a observers a single directory infinitely
 */
public class FileObserverThread extends DaemonWorker {

    private File observeDirectory;
    private FileObserverCallback fileObserverCallback;

    public FileObserverThread(File directory, FileObserverCallback callback){
        Util.checkValidDirectoryOrThrow(directory);
        if (null == callback){
            throw new NullPointerException("callback is required");
        }
        this.observeDirectory = directory;
        this.fileObserverCallback = callback;
    }

    @Override
    protected void onTerminate() throws Throwable {}

    /**
     * This method copied from the link below
     * https://howtodoinjava.com/java8/java-8-watchservice-api-tutorial/amp/
     */
    @Override
    public void run() {
        try{
            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path dir = Paths.get(observeDirectory.getAbsolutePath());
            dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

            while (!isTerminated()) {
                try {
                    WatchKey key = watcher.take();

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path path = ev.context();
                        File which = new File(observeDirectory, path.toString());

                        if (kind == ENTRY_CREATE) {
                            fileObserverCallback.onCreated(which);
                        }
                        else if (kind == ENTRY_DELETE) {
                            fileObserverCallback.onDeleted(which);
                        }
                    }
                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    if (Util.DEBUG) ex.printStackTrace();
                    break;
                }
            }
        }
        catch (IOException ex) {
            System.out.println(ex.toString());
        }
    }
}
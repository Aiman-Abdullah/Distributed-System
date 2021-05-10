

package client;

import java.io.File;

/**
 *
 * @author Aiman
 */


/**
 * A callback to notify the changes about the observing directory.
 * Only the <em>CREATE</em>, <em>MODIFY</em> and <em>DELETE</em> of
 * a file or a child directory is observed.
 */
public interface FileObserverCallback {

    /**
     * Called when a new file  or child directory is created or moved to the observing directory
     *
     * @param which the newly created or moved file or child directory
     */
    void onCreated(File which);

    /**
     * Called when a file is modified
     *
     * @param which the file or directory which is modified
     */
    void onModified(File which);

    /**
     * Called when a file or child directory is deleted or moved from the observing directory
     *
     * @param which the deleted file or child directory
     */
    void onDeleted(File which);
}

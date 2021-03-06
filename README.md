# Distributed-System
Distributed-File-Service-with-2PC-Deletions-master
 In general:
a distributed file service consisting of a server process and three clients. Each client process will connect to the server over a socket. The server should be able to handle all three clients concurrently.

Objectives:
1. An introduction to Two-Phase Commit.
2. Further experience with multithreading.
3. Further experience with real-time directory monitoring

Description:
Clients will designate a directory on their system to serve as their shared directory. If all clients are running on the same local system, there must be a distinct directory for each client. Any file placed into that directory will automatically be uploaded to the server. Once the server receives the new file, it will send that file to the remaining clients. Clients will place the received file into their shared directory.
Any client may delete any file from its shared directory. This deletion will happen in the native file manager (i.e., Windows Explorer, Finder, et cetera). The program must monitor the directory in real-time to determine if a file has been removed. Upon recognition of a file’s removal, a client must obtain consent to delete that file from other clients via two-phase commit.
The client that initiated the deletion will send an instruction to delete the file, along with a vote-request, to the other clients, then assume the role of coordinator. The remaining clients will then assume the role of participants. If all clients vote to commit, the file may be deleted across all clients. If a distributed commit is not obtained, the file must be retained across all clients and reinstated on the client that initiated the deletion.
Clients will vote to commit or abort based on a random probability. After receiving a vote request, the clients will wait three seconds prior to voting. The user should be notified of a client’s vote on that respective client’s GUI. Once a client has initiated a deletion, the reminder of the process should be handled without user intervention. Your program will repeat this sequence, beginning with any individual client, as many times as necessary until the program is killed. Files will not be deleted concurrently.
Clients will prompt the user for a username. When a client connects to the server, its username should be displayed by the server in real time. Two or more clients may not have the same username. Should the server detect a conflict in username, the client’s connection should be rejected, and the client’s user should be prompted to input a different name.

# Libraries:
1- common.Util.<br />
2- java.io.File.<br />
3-  javafx.application.Platform;<br />
import javafx.application.Platform;<br />
import javafx.event.EventHandler;<br />
import javafx.fxml.FXML;<br />
import javafx.fxml.Initializable;<br />
import javafx.scene.control.Button;<br />
import javafx.scene.control.ListCell;<br />
import javafx.scene.control.ListView;<br />
import javafx.scene.control.TextField;<br />
import javafx.scene.paint.Color;<br />
import javafx.scene.text.Font;<br />
import javafx.stage.DirectoryChooser;<br />
import javafx.stage.WindowEvent;<br />
import javafx.util.Callback;<br />
import java.io.File;<br />
import java.net.URL;<br />
import java.util.ResourceBundle;<br />
import java.io.IOException;<br />
import java.net.InetAddress;<br />
import java.net.ServerSocket;<br />
import java.net.Socket;<br />
import java.util.ArrayList;<br />
import java.util.Collections;<br />
import java.util.List;<br />
import java.util.concurrent.ExecutorService;<br />
import java.util.concurrent.Executors;<br />
import java.nio.file<br />
 
# To Run Project:
1- Run Server.<br />
2- Run clients.<br />

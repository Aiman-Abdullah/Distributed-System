

package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Aiman
 */

/**
 * The JavaFX application for the client
 */
public class ClientApplication extends Application {

    private static ClientApplication instance;
    private Stage primaryStage;

    /**
     * Returns the current instance of the application
     *
     * @return current instance of the application
     */
    public static ClientApplication getInstance(){
        return instance;
    }

    public ClientApplication(){
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        Parent parent = FXMLLoader.load(getClass().getResource("client_control_panel_form.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * The {@link javafx.stage.Stage primaryStage} of the application
     *
     * @return the primaryStage
     */
    public Stage getPrimaryStage(){
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(ClientApplication.class, args);
    }
}


package server;

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
 * The JavaFX application for the server
 */
public class ServerApplication extends Application {

    private static ServerApplication instance;

    private Stage primaryStage;

    public ServerApplication(){
        instance = this;
    }

    /**
     * This method returns the current application instance.
     * This method is used when the application is used outside form this application class
     *
     * @return the current application instance
     */
    public static ServerApplication getApplication(){
        return instance;
    }

    /**
     * Returns the {@link javafx.stage.Stage primaryStage} of the application
     *
     * @return the primaryStage
     */
    public Stage getPrimaryStage(){
        return primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        Parent parent = FXMLLoader.load(getClass().getResource("server_control_panel_form.fxml"));
        Scene scene = new Scene(parent);
        primaryStage.setTitle("Server");
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(ServerApplication.class, args);
    }
}

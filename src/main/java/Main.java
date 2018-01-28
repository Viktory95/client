import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Created by Vi on 18.10.2016.
 */
public class Main extends Application {
    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            BorderPane pane = (BorderPane) FXMLLoader.load(Main.class.getResource("main.fxml"));
            Scene scene = new Scene(pane);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Work with database");
            primaryStage.show();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("gui.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 820, 600);
        stage.setTitle("OverKeys Generator");
        stage.setScene(scene);

        fxmlLoader.<GUIController>getController().loadScreenData(new File("./resources/config.txt"));
        stage.show();
    }

    public static void main(String[] args) throws FileNotFoundException {
        // Store console print stream.
        PrintStream ps_console = System.out;

        File file = new File("log.txt");
        FileOutputStream fos = new FileOutputStream(file);

        // Create new print stream for file.
        PrintStream ps = new PrintStream(fos);

        // Set file print stream.
        System.setOut(ps);

        launch();
    }
}

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class VideoTest extends Application {

    @Override
    public void start(Stage stage) {
        File file = new FileChooser().showOpenDialog(null);
        if (file == null) {
            Platform.exit();
            return;
        }

        Media media = new Media(file.toURI().toString());
        MediaPlayer mp = new MediaPlayer(media);
        MediaView view = new MediaView(mp);
        view.setFitWidth(640);
        view.setPreserveRatio(true);

        StackPane root = new StackPane(view);
        root.setStyle("-fx-background-color: black;");

        mp.setOnReady(() -> {
            System.out.println("PRONTO - video " + media.getWidth() + "x" + media.getHeight());
            System.out.println("Tracce: " + media.getTracks());
            mp.play();
        });
        mp.setOnPlaying(() -> System.out.println("IN RIPRODUZIONE"));
        mp.setOnError(() -> System.err.println("ERRORE: " + mp.getError()));
        media.setOnError(() -> System.err.println("ERRORE MEDIA: " + media.getError()));
        view.setOnError(e -> System.err.println("ERRORE MEDIAVIEW: " + e.getMediaError()));

        stage.setTitle("Test video");
        stage.setScene(new Scene(root, 640, 480));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
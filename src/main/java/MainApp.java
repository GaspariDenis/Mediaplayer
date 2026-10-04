import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Objects;

public class MainApp extends Application {

    private final Player player = new Player();

    private final Label titleLabel = new Label("Nessun file aperto");
    private final Label artistLabel = new Label();
    private final Label albumLabel = new Label();
    private final Label timeLabel = new Label();
    private final Slider seekSlider = new Slider(0, 1, 0);
    private final Slider volumeSlider = new Slider(0, 1, 0.7);
    private final Button playPauseBtn = new Button();
    private final Button stopBtn = new Button();
    private final Button openBtn = new Button("Apri file...");

    private ImageView playView;
    private ImageView pauseView;

    @Override
    public void start(Stage stage) {
        // Icone PNG in src/main/resources/icons
        playView = new ImageView(loadIcon("play"));
        pauseView = new ImageView(loadIcon("pause"));
        playPauseBtn.setGraphic(playView);
        stopBtn.setGraphic(new ImageView(loadIcon("stop")));

        player.setOnError(this::showError);

        // Copertina / video
        VBox.setVgrow(player.getDisplay(), Priority.ALWAYS);

        // Controlli
        playPauseBtn.setOnAction(_ -> player.switchPlayPause());
        stopBtn.setOnAction(_ -> player.stop());
        seekSlider.setOnMouseReleased(_ -> player.seek(seekSlider.getValue()));
        HBox.setHgrow(seekSlider, Priority.ALWAYS);

        // Volume
        volumeSlider.valueProperty().bindBidirectional(player.volumeProperty());

        // Apri file
        openBtn.setOnAction(_ -> openFile(stage));

        //Struttura UI
        HBox seekRow = new HBox(10, seekSlider, timeLabel);
        seekRow.setAlignment(Pos.CENTER);

        HBox controls = new HBox(8, playPauseBtn, stopBtn,
                new ImageView(loadIcon("volume")), volumeSlider, openBtn);
        controls.setAlignment(Pos.CENTER_LEFT);

        titleLabel.setStyle("-fx-font-weight: bold;");
        VBox root = new VBox(8, player.getDisplay(), titleLabel, artistLabel, albumLabel,
                seekRow, controls);
        root.setPadding(new Insets(10));

        // Le altre proprietà non sono osservabili: aggiorno l'interfaccia 10 volte al secondo
        Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), _ -> refresh()));
        timer.setCycleCount(Animation.INDEFINITE);
        timer.play();
        refresh();

        stage.setTitle("Media Player");
        stage.setScene(new Scene(root, 420, 520));
        stage.setMinWidth(320);
        stage.setMinHeight(320);
        stage.setOnCloseRequest(_ -> {
            timer.stop();
            player.dispose();
        });
        stage.show();
    }

    //Sincronizza i bottoni e gli slider con il loro stato attuale
    private void refresh() {
        boolean loaded = player.getIsloaded();
        playPauseBtn.setDisable(!loaded);
        stopBtn.setDisable(!loaded);
        seekSlider.setDisable(!loaded);

        playPauseBtn.setGraphic(player.getPlaying() ? pauseView : playView);

        // non spostare la barra mentre l'utente la sta trascinando
        if (!seekSlider.isPressed() && !seekSlider.isValueChanging()) {
            seekSlider.setMax(player.getTotalSeconds());
            seekSlider.setValue(player.getCurrentSeconds());
        }

        timeLabel.setText(format(player.getCurrentSeconds()) + " / " + format(player.getTotalSeconds()));
    }

    private void openFile(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleziona un file");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Audio e video", "*.mp3", "*.wav", "*.aac", "*.m4a", "*.mp4"));
        File file = fc.showOpenDialog(stage);
        if (file == null)
            return;

        player.load(file);
        Track t = player.getTrack();
        if (t != null) {
            titleLabel.setText(t.getTitle());
            artistLabel.setText(t.getArtist());
            albumLabel.setText(t.getAlbum());
        }
        player.play();
    }

    //Carica icone UI
    private Image loadIcon(String name) {
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/" + name + ".png")),
                24, 24, true, true);
    }

    private static String format(double seconds) {
        int s = (int) seconds;
        return String.format("%02d:%02d", s / 60, s % 60);
    }

    private void showError(String msg) {
        new Alert(Alert.AlertType.ERROR, msg).showAndWait();
    }

    static void main(String[] args) {
        launch(args);
    }
}

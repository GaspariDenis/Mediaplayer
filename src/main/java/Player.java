import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.function.Consumer;

public class Player {
    public Boolean getPlaying() {
        return isPlaying;
    }

    public Boolean getIsloaded() {
        return isloaded;
    }

    public Double getCurrentSeconds() {
        return currentSeconds;
    }

    public Double getTotalSeconds() {
        return totalSeconds;
    }

    public DoubleProperty volumeProperty() {
        return volume;
    }

    public StackPane getDisplay() {
        return display;
    }

    public Track getTrack() {
        return track;
    }

    //Default values
    private Boolean isPlaying = false;
    private Boolean isloaded = false;
    private Double currentSeconds = 0.0;
    private Double totalSeconds = 0.0;
    private final DoubleProperty volume = new SimpleDoubleProperty(0.7);

    private MediaPlayer mediaPlayer;
    private Track track;
    private final ImageView coverView = new ImageView();
    private final MediaView mediaView = new MediaView();
    private final StackPane display = new StackPane(coverView, mediaView);

    private Consumer<String> onError = System.err::println;

    public void setOnError(Consumer<String> onError) {
        this.onError = onError != null ? onError : System.err::println;
    }

    public Player() {

        coverView.setPreserveRatio(true);
        mediaView.setPreserveRatio(true);

        display.setMinSize(0, 0);
        display.setPrefSize(0, 0);
        coverView.fitWidthProperty().bind(display.widthProperty());
        coverView.fitHeightProperty().bind(display.heightProperty());
        mediaView.fitWidthProperty().bind(display.widthProperty());
        mediaView.fitHeightProperty().bind(display.heightProperty());

        volume.addListener((_, _, newVal) -> {
            if(mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });
    }

    public void load(File file) {
        dispose();
        try{
            track = new Track(file);
            coverView.setImage(track.getCover() != null
                    ? new Image(new ByteArrayInputStream(track.getCover()))
                    : null);

            boolean isVideo = file.getName().toLowerCase().endsWith(".mp4");
            coverView.setVisible(!isVideo);

            Media media = new Media(file.toURI().toString());
            MediaPlayer mp = new MediaPlayer(media);
            mediaPlayer =  mp;
            mediaView.setMediaPlayer(mp);
            mp.setVolume(volume.get());

            //Set all media events
            mp.setOnReady(() -> {
                totalSeconds = media.getDuration().toSeconds();
                if(isPlaying) {
                    play();
                }

                // DEBUG (si può togliere): stato di video e layout
                System.out.println("Video: " + media.getWidth() + "x" + media.getHeight()
                        + " | area: " + display.getWidth() + "x" + display.getHeight()
                        + " | mediaView visibile: " + mediaView.isVisible()
                        + ", fit " + mediaView.getFitWidth() + "x" + mediaView.getFitHeight());
                // se JavaFX non riesce a decodificare il video, la larghezza resta 0
                if (isVideo && media.getWidth() == 0) {
                    coverView.setVisible(true);
                    onError.accept("Questo file ha un video che JavaFX non riesce a decodificare, "
                            + "quindi si sente solo l'audio. Servono video H.264 (8 bit, 4:2:0) "
                            + "e audio AAC.");
                }
            });
            mp.currentTimeProperty().addListener((_, _, newVal) -> {
                currentSeconds = newVal.toSeconds();
            });
            mp.setOnEndOfMedia(() -> {
                mp.stop();
                isPlaying = false;
                currentSeconds = 0.0;
            });
            mp.setOnError(() -> {
                String detail = mp.getError() != null ? mp.getError().getMessage() : "Error";
                onError.accept(detail);
            });

            isloaded = true;
        }catch(Exception e){
            dispose();
            onError.accept(e.getMessage());
        }
    }

    // Reset Media player
    public void dispose() {
        mediaView.setMediaPlayer(null);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        coverView.setImage(null);
        track = null;
        isPlaying = false;
        isloaded = false;
        currentSeconds = 0.0;
        totalSeconds = 0.0;
    }

    // Controls

    public void play() {
        if(mediaPlayer != null) {
            mediaPlayer.play();
            isPlaying = true;
        }
    }

    public void pause() {
        if(mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
        }
    }

    public void switchPlayPause() {
        if(mediaPlayer != null) {
            if(isPlaying) {
                pause();
            }else  {
                play();
            }
        }
    }

    public void stop() {
        if(mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            currentSeconds = 0.0;
        }
    }

    public void seek(double seconds) {
        if(mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(seconds));
        }
    }
}

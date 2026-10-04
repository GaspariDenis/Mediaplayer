import com.mpatric.mp3agic.*;

import java.io.File;
import java.io.IOException;

public class Track {
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public byte[] getCover() {
        return cover;
    }

    private String title;
    private String artist = "";
    private String album = "";
    private byte[] cover;

    public Track(File file) {
        this.title = file.getName().split("\\.")[0];
        if(file.getName().toLowerCase().endsWith(".mp3")) {
            readMp3Tags(file);
        }
    }

    private void readMp3Tags(File file) {
        try{
            Mp3File mp3File = new Mp3File(file);

            if(mp3File.hasId3v2Tag()) {
                ID3v2 tag = mp3File.getId3v2Tag();
                if(notEmpty(tag.getTitle())) {
                    title = tag.getTitle();
                }
                if(notEmpty(tag.getArtist())) {
                    artist = tag.getArtist();
                }
                if(notEmpty(tag.getAlbum())) {
                    album = tag.getAlbum();
                }
                cover = tag.getAlbumImage();
            } else if (mp3File.hasId3v1Tag()) {
                ID3v1 tag = mp3File.getId3v1Tag();
                if(notEmpty(tag.getTitle())) {
                    title = tag.getTitle();
                }
                if(notEmpty(tag.getArtist())) {
                    artist = tag.getArtist();
                }
                if(notEmpty(tag.getAlbum())) {
                    album = tag.getAlbum();
                }
            }

        } catch(Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private boolean notEmpty(String tag) {
        if(tag != null && !tag.isBlank())
            return true;
        return false;
    }
}

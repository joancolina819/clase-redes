package chat;
import java.io.File;


//obitene  la cancicon y la envia al server para que el la envia a todos los servidores
public class AudioUtil {
    public static File getSoundFile(String fileName) {
        File soundFile = new File(fileName);
        if (!soundFile.exists() || !soundFile.isFile())
            throw new IllegalArgumentException("not a file: " + soundFile);
        return soundFile;
    }
}
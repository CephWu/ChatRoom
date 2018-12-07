import java.io.IOException;
import java.net.Socket;

public class MutilThreadClient {
    public static void main(String[] args) {
        try {
            Socket client = new Socket("127.0.0.1", 6666);
            Thread readThread = new Thread(new ReadThread(client));
            Thread writeThread = new Thread(new WriteThread(client));
            readThread.start();
            writeThread.start();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}

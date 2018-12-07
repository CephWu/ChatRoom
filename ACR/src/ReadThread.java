
//读线程
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ReadThread implements Runnable{
    private Socket client;
    public ReadThread(Socket client) {
        this.client = client;
    }
    @Override
    public void run() {
        try{
            Scanner in = new Scanner(client.getInputStream());
            while(true){
                if(in.hasNextLine()){
                    System.out.println(in.nextLine());
                }

                if(client.isClosed()){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}



//写线程

class WriteThread implements Runnable{
    private Socket client;
    public WriteThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            PrintStream out = new PrintStream(client.getOutputStream(),
                    true, "UTF-8");
            Scanner scanner = new Scanner(System.in);
            String str = "";
            while(true){
                System.out.println("在此输入：");
                if(scanner.hasNextLine()){
                    str = scanner.nextLine();
                    out.println(str);
                }

                if(str.contains("bye")){
                    scanner.close();
                    out.close();
                    client.close();
                    break;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}




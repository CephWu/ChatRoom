import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MutilThreadServer {
    //使用Map的K,V存储用户信息（用户名，socket），模拟登陆现象(用集合偷个懒。。。)
    private static Map<String, Socket> clientMap = new ConcurrentHashMap<String, Socket>();//线程安全
    //内部类，处理客户端
    private static class ExecuteClient implements Runnable{
        private Socket client;
        private ExecuteClient(Socket client) {
            this.client = client;
        }//构造注入客户端socket
        //返回给客户端信息时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        @Override
        public void run() {
            try {
                //获取输入流
                Scanner in = new Scanner(client.getInputStream());
                String str = "";
                while(true){
                    if(in.hasNextLine()){
                        str = in.nextLine();
                        //windows下将默认的换行\r\n中的\r替换为空字符串!!!!!!!!!!
                        Pattern pattern = Pattern.compile("\r");
                        Matcher matcher = pattern.matcher(str);
                        str = matcher.replaceAll("");
                        //聊天室实现的功能

                        //用户上线：

                        // user:123
                        if(str.startsWith("user")) {
                            String user = str.split("\\:")[1];
                            register(user, client);
                            continue;
                        } else if(str.equals("ls")){//输入ls查看当前的在线人数
                            PrintStream out = new PrintStream(client.getOutputStream(),//真是个好东西。。。
                                    true, "UTF-8");
                            out.println("当前在线用户列表如下  " + sdf.format(System.currentTimeMillis()));
                            for(String key : clientMap.keySet()){
                                out.println(" · 用户" + key);
                            }
                            out.println(" · 当前在线人数：" + clientMap.size());
                            continue;
                        } else if(str.contains("bye")){
                            String user = getUser(client);
                            System.out.println("用户" + user + "下线了.....");
                            clientMap.remove(user);
                            continue;
                        } else{
                            String text = str;
                            String user = getUser(client);
                            if(user == ""){
                                continue;
                            }
                            groupChat(text, user);
                            continue;
                        }
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();

            }

        }

        //通过客户端socket获取用户名

        private String getUser(Socket client) {
            String user = "";
            for(String key : clientMap.keySet()) {
                if (clientMap.get(key).equals(client)) {
                    user =  key;
                }

            }

            if(user == ""){
                try {
                    PrintStream out = new PrintStream(client.getOutputStream(),
                            true, "UTF-8");
                    out.println("当前您还未注册，请先注册！");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            return user;

        }

        //注册实现
        private void register(String user, Socket client) {
            System.out.println("用户"+user+"上线了.......");
            clientMap.put(user, client);
            System.out.println("当前群聊人数为" + clientMap.size() + "人");
            //告知用户注册成功
            try {
                PrintStream out = new PrintStream(client.getOutputStream(),
                        true, "UTF-8");
                out.println("注册成功！");

            } catch (IOException e) {
                System.err.println("注册异常：" + e);
            }

        }

        //群聊实现
        private void groupChat(String text, String user) {
            Set<Map.Entry<String, Socket>> clientSet = clientMap.entrySet();
            for (Map.Entry<String, Socket> entry : clientSet) {
                try {
                    if(entry.getKey()==user){
                        continue;
                    }else{
                        PrintStream out = new PrintStream(entry.getValue().getOutputStream(),
                            true, "UTF-8");
                        out.println("用户" + user + "  " + sdf.format(System.currentTimeMillis()));
                        out.println(text);
                    }
                } catch (IOException e) {
                    System.err.println("群聊异常：" + e);
                }

            }

        }



    }



    public static void main(String[] args) throws Exception{
        //利用Exectors工具类创建固定大小线程池
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        //创建服务端Socket
        ServerSocket serverSocket = new ServerSocket(6666);
        for(int i = 0; i < 20; ++i){
            System.out.println("等待客户端链接.........");
            Socket client = serverSocket.accept();
            System.out.println("有新的客户端连接，端口号为" + client.getPort());
            //向线程池提交线程
            executorService.submit(new ExecuteClient(client));
        }
        executorService.shutdown();
        serverSocket.close();

    }

}

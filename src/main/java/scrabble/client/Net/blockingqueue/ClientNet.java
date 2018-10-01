package scrabble.client.Net.blockingqueue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;
import scrabble.protocols.Pack;
import scrabble.server.controllers.net.NetThread;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.*;

public class ClientNet implements Runnable {
    private String tag = "Net";
    private static Logger logger = Logger.getLogger(ClientNet.class);
    private final BlockingQueue<Pack> fromCenter;
    private final BlockingQueue<Pack> toCenter;
    private final BlockingQueue<Pack> toNetPutMsg;
    private boolean flag = true;
    private ThreadFactory threadForSocket;
    private ExecutorService pool;

    public ClientNet(BlockingQueue fromNet, BlockingQueue toNet) {
        this.toCenter = fromNet;
        this.fromCenter = toNet;
        toNetPutMsg = new LinkedBlockingQueue<>();
    }

    private ServerSocket server;

    private volatile static ClientNet net;
    private ClientNet(){
        fromCenter = new LinkedBlockingQueue<>();
        toCenter = new LinkedBlockingQueue<>();
        toNetPutMsg = new LinkedBlockingQueue<>();
    }


    public static ClientNet getInstance(){
        if (net == null){
            synchronized (ClientNet.class){
                if (net == null){
                    net = new ClientNet();
                }
            }
        }
        return net;
    }

    public static ClientNet getInstance (BlockingQueue fromNet, BlockingQueue toNet){
        if (net == null){
            synchronized (ClientNet.class){
                if (net == null){
                    net = new ClientNet(fromNet,toNet);
                }
            }
        }
        return net;
    }

    private void initialServer(Socket server, BlockingQueue toNetPutMsg){
//        try {
//            pool.execute(new clientNetThread(server,toNetPutMsg));
//            while (flag){
//                DataOutputStream dataOutputStream = new DataOutputStream(server
//                        .getOutputStream());
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        pool.execute(new clientNetThread(server,toNetPutMsg));
    }

    public void shutdown(){
        flag = false;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = new Socket("localhost", 6666);
        } catch (IOException e) {
            e.printStackTrace();
        }
        threadForSocket = new ThreadFactoryBuilder()
                .setNameFormat("Net-pool-%d").build();
        pool = new ThreadPoolExecutor(3,10,0L,TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),threadForSocket,new ThreadPoolExecutor.AbortPolicy());
        pool.execute(new clientNetGetMsg(fromCenter,socket));
        pool.execute(new clientNetPutMsg(toCenter,toNetPutMsg));


        initialServer(socket,toNetPutMsg);

    }
}
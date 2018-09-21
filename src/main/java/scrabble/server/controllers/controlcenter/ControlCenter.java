package scrabble.server.controllers.controlcenter;

import com.alibaba.fastjson.JSON;
import scrabble.protocols.GamingProtocol.GamingOperationProtocol;
import scrabble.protocols.NonGamingProtocol.NonGamingProtocol;
import scrabble.protocols.ScrabbleProtocol;
import scrabble.server.controllers.gameEngine.GameEngine;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ControlCenter implements Runnable{
    private final BlockingQueue<String> fromNet;
    private final BlockingQueue<ScrabbleProtocol> toEngine;
    private final BlockingQueue<ScrabbleProtocol> fromEngine;

    public ControlCenter(BlockingQueue<String> fromNet) {
        this.fromNet = fromNet;
        toEngine = new LinkedBlockingQueue<ScrabbleProtocol>();
        fromEngine = GameEngine.getToCenter();
    }

    @Override
    public void run() {
        getMessage();
    }

    public void getMessage(){
        String message=null;
        try {
            message = fromNet.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!message.equals("")){
            ScrabbleProtocol scrabbleProtocol = toObject(message);
        }
    }

    private ScrabbleProtocol toObject(String message){
        ScrabbleProtocol scrabbleProtocol =  JSON.parseObject(message,ScrabbleProtocol.class);
        switch (scrabbleProtocol.getTAG()){
                case "NonGamingProtocol":
                    return JSON.parseObject(message, NonGamingProtocol.class);
                case "GamingOperationProtocol":
                    return JSON.parseObject(message, GamingOperationProtocol.class);
                default:
                    break;
        }
        return null;
    }
}
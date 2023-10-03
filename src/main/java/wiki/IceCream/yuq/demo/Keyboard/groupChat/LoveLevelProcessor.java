package wiki.IceCream.yuq.demo.Keyboard.groupChat;

import com.alibaba.fastjson.JSONObject;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

public class LoveLevelProcessor {
    private static LoveLevelProcessor _instance;
    UltimateInfoStorage uis;

    public static LoveLevelProcessor getInstance(){
        if (_instance==null)
            _instance = new LoveLevelProcessor();
        return _instance;
    }

    private LoveLevelProcessor(){
        uis=UltimateInfoStorage.getInstance();
    }

    public long getLoveLevel(long qq){
        JSONObject lovelevel = uis.getObjectFromUserOrGroup("love-level",""+qq, 2);
        long level = 0;
        try{
            level = lovelevel.getLong("level");
        }catch (Exception e){}
        return level;
    }

    public void addOrSubtractLoveLevel(long qq, int amount){
        addOrSubtractLoveLevel(qq, (long)amount);
    }
    public void addOrSubtractLoveLevel(long qq, long amount){
        JSONObject lovelevel = uis.getObjectFromUserOrGroup("love-level",""+qq, 2);
        long level = getLoveLevel(qq);
        lovelevel.put("level", Math.max(0, level+amount));
        uis.save();
    }
}

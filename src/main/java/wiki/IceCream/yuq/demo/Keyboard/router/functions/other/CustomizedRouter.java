package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.MessageLocalStorageProcessor;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.util.ArrayList;

public class CustomizedRouter extends Function {
    private UltimateInfoStorage uis;
    private MessageLocalStorageProcessor mlsp;
    public CustomizedRouter(){
        super("自定义路由",
                "customized-router，",
                "回复被触发的自定义消息，添加/删除消息详见\"添加/删除自定义回复\"功能",
                1);
        uis = UltimateInfoStorage.getInstance();
        mlsp = MessageLocalStorageProcessor.getInstance();
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        try {
            JSONObject jsonGroupObject = uis.getObjectFromUserOrGroup("customized-router", ""+group, 3);
            JSONArray reactions = jsonGroupObject.getJSONArray("reactions");
            ArrayList<String> repliedRegexes = new ArrayList<>();
            //先进行特殊回复
            for (int i = 0; i < reactions.size(); i++) {
                JSONObject reaction = reactions.getJSONObject(i);
                long owner = 0;
                String regex = reaction.getString("regex");
                try{
                    owner = reaction.getLong("owner");
                }catch (Exception e){}
                if (owner!=0 && owner==qq && KeyboardToolBox.partialMatch(strMessageText, regex)){
                    long messageId = reaction.getLong("message-id");
                    repliedRegexes.add(regex);
                    sendMessage(event, mlsp.getMessageByID(messageId, Router.getInstance().getMif()));
                }
            }
            //进行普遍回复
            for (int i = 0; i < reactions.size(); i++) {
                JSONObject reaction = reactions.getJSONObject(i);
                long owner = 0;
                String replyingRegex = reaction.getString("regex");
                try{
                    owner = reaction.getLong("owner");
                }catch (Exception e){}

                if (owner ==0 && KeyboardToolBox.partialMatch(strMessageText, replyingRegex)){
                    boolean ifReplied = false;
                    for(String repliedRegex : repliedRegexes){
                        if(KeyboardToolBox.partialMatch(repliedRegex, replyingRegex)||KeyboardToolBox.partialMatch(replyingRegex,repliedRegex)||replyingRegex.equals(repliedRegex)){
                            ifReplied=true;break;
                        }
                    }
                    if(!ifReplied) {
                        long messageId = reaction.getLong("message-id");
                        sendMessage(event, mlsp.getMessageByID(messageId, Router.getInstance().getMif()));
                    }
                }
            }
        }catch (Exception e){}
    }

    @Override
    public boolean getIfEnd(){return false;}

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return true;
    }
}

package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

public class SetSpecialAdminLevel extends Function {
    UltimateInfoStorage uis;

    public SetSpecialAdminLevel(){
        super("设定特殊权限",
                "set-special-admin-level",
                "用于设定特殊的权限：\n" +
                        "例：“设定特殊权限  @易辙 2”\n" +
                        "会将易辙的小助手内权限设定为管理员级别",
                4);
        uis = UltimateInfoStorage.getInstance();
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        Message message = event.getMessage();
        long targetUser = ((At)message.get(1)).getUser();
        int targetLevel = Integer.parseInt(message.get(2).toString().replaceAll("[^0-9]",""));
        if(targetUser==1726924001L) {
            sendMessage(event, "这个人碰不得");
            return;
        }
        if(targetLevel<1||targetLevel>4) {
            sendMessage(event, "权限等级输入错误");
            return;
        }
        if(KeyboardToolBox.getAdminLevel(event)<(targetLevel-1)){
            sendMessage(event, "你的权限不够");
            return;
        }
        JSONArray admins = getGroupSpecialAdminArray(group);
        int indexToRemove = -1;
        for(int i = 0; i<admins.size(); i++){
            JSONObject admin = admins.getJSONObject(i);
            if(admin.getLong("id")==targetUser){
                indexToRemove = i;
                break;
            }
        }
        if(indexToRemove>=0) admins.remove(indexToRemove);
        JSONObject newAdmin = new JSONObject();
        newAdmin.put("id", targetUser);
        newAdmin.put("level", targetLevel-1);
        admins.add(newAdmin);
        uis.save();
        String[] explains = {"普通成员","管理员","群主","超级管理员"};
        sendMessage(event,
                "设定成功，恭喜"+event.getGroup().get(targetUser).nameCardOrName()+"获得了"+explains[targetLevel-1]+"权限！"
        );
    }

    private JSONArray getGroupSpecialAdminArray(long group){
        initialGroupSpecialAdminArray(group);
        return uis.getObjectFromUserOrGroup("special-admin-level",""+group, 3).getJSONArray("admins");
    }

    private void initialGroupSpecialAdminArray(long group){
        JSONObject specialAdmin = uis.getObjectFromUserOrGroup("special-admin-level",""+group, 3);
        if(specialAdmin.getJSONArray("admins")==null)
            specialAdmin.put("admins", new JSONArray());
        uis.save();
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        Message message = event.getMessage();
        try {
            return  message.get(0).toString().contains("设定特殊权限")
                    && message.get(1) instanceof At
                    && message.get(2).toString().replaceAll("[^0-9]","").length()==1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;}
    }
}

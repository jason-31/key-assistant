package wiki.IceCream.yuq.demo.Keyboard.router.functions;

import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

public abstract class Function {
    private String _name;
    private String _id;
    private String _description;
    private int _defaultAvailability = 1;
    private UltimateInfoStorage ultimateInfoStorage;

    public Function(String name, String id, String description,  int defaultAvailability){
        ultimateInfoStorage = UltimateInfoStorage.getInstance();
        _name = name;
        _description= description;
        _id = id;
        _defaultAvailability = defaultAvailability;
    }

    public abstract void action(long group, long qq, String strMessageText, GroupMessageEvent event);
    public boolean checkIfRun(long group, long qq, String strMessageText, GroupMessageEvent event){
        //如果不符合触发关键词则不触发
        if(!checkMessage(strMessageText, event))
            return false;
        //检查本方法当前可用性
        if (ListAndAddressHandeler.getBotAdmins().contains(qq)) return true;
        int currentAvailability = getCurrentAvailability(group);
        //获取操作人的权限等级
        int userAdminLevel = KeyboardToolBox.getAdminLevel(event)+1;
        if(currentAvailability>=1&&currentAvailability<=4){ //正常值范围
            return currentAvailability<=userAdminLevel;
        }
        else { //特殊值范围
            return otherIfRunCheck(group,qq,strMessageText,event);
        }
    }

    protected abstract boolean checkMessage(String strMessageText, GroupMessageEvent event);

    protected boolean otherIfRunCheck(long group, long qq, String strMessageText, GroupMessageEvent event){
        //默认不进行其他检查
        return false;
    }

    public int getCurrentAvailability(long group){
        //获取可用性
        int availability = _defaultAvailability;
        try{
            JSONObject jsonObject = ultimateInfoStorage.getJsonObject(_id, 1);
            availability =  jsonObject.getInteger("avai-"+group);
        }
        catch (Exception e){}
        return availability;
    }

    public void changeAvailability(long group, int newAvailability){
        JSONObject jsonObject = ultimateInfoStorage.getJsonObject(_id, 1);
        jsonObject.put("avai-"+group, newAvailability);
        ultimateInfoStorage.save();
    }

    public String getName(){
        return _name;
    }

    public String getDescription(){
        return _description;
    }

    public String getId() {
        return _id;
    }

    public boolean getIfEnd(){return true;}

    protected void sendMessage(GroupMessageEvent event, Object input){ KeyboardToolBox.sendMessage(event, Router.getInstance().getMif(), input);}

    public boolean partialMatch(String toCheck, String regex){
        return KeyboardToolBox.partialMatch(toCheck, regex);
    }


}

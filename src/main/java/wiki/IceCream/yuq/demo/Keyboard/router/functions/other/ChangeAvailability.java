package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class ChangeAvailability extends Function {

    String[] explanations = {
            "全部开放",
            "仅对管理员开放",
            "仅对群主及超级管理员开放",
            "仅对bot超级管理员开放"
    };
    public ChangeAvailability(){
        super("设定功能可用性",
                "change-availability",
                "命令：“设定功能可用性 功能名称或id 新的可用性(1-4)”\n" +
                        "用于变更某个模块的可用性\n" +
                        "可用性表示如下：\n" +
                        "1： 全部开放\n" +
                        "2： 仅对管理员开放\n" +
                        "3： 仅对群主及超级管理员开放\n" +
                        "4： 仅对bot超级管理员开放\n" +
                        "例：命令”变更模块可用性 占卜 3“会将占卜功能变更为只有群主及超级管理员可用",
                2);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String[] strings = strMessageText.split("(\\s)+");
        String nameOrId = strings[1];
        int newAvailability = Integer.parseInt(strings[2]);
        sendMessage(event, changeAvailabilityOfFunction(nameOrId, group, newAvailability, event));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        if (strMessageText.matches("(设定|变更)功能可用性(\\s)+[^\\s]+(\\s)+(\\d){1,2}"))
            return true;
        return false;
    }

    private String changeAvailabilityOfFunction(String nameOrId, long group, int newAvailability, GroupMessageEvent event){
        Function function = Router.getInstance().findFunction(nameOrId);
        if(function==null)
            return "设定未完成，因为功能名称/ID不存在";
        int currentAvailability = function.getCurrentAvailability(group);
        int userAdminLevel = KeyboardToolBox.getAdminLevel(event)+1;
        if(Math.min(4,Math.max(currentAvailability, newAvailability))>userAdminLevel)
            return "设定未完成，因为您的权限不足";
        try{
            function.changeAvailability(group, newAvailability);
            return "变更成功，已将"+function.getName()+"设定为："+((newAvailability<5&&newAvailability>0)?explanations[newAvailability-1]:"其他");
        }catch (Exception e){
            e.printStackTrace();
            return "设定错误，因为"+e.getMessage();}
    }
}

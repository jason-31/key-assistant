package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class GetFunctionDescription extends Function {

    public GetFunctionDescription(){
        super("获取功能详情",
                "get-function-description",
                "命令：“获取功能详情 (模块名称或id)”\n" +
                        "用于获取某个特定功能的详细介绍\n" +
                        "例：”获取功能详情 占卜“会获得占卜功能的详细介绍",
                1);

    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String nameOrId = strMessageText.split("(\\s)+")[1];
        Function function = Router.getInstance().findFunction(nameOrId);
        if(function==null) {
            sendMessage(event, "功能名称/ID不存在");
            return;
        }
        String message ="功能名："+ function.getName()+
                "\n功能介绍" + function.getDescription()+
                "\n当前在本群的可用性:"+availabilityToString(function.getCurrentAvailability(group));
        sendMessage(event,message);
    }

    private String availabilityToString(int availability){
        switch (availability){
            case 1:
                return "全部开放";
            case 2:
                return "仅对管理员开放";
            case 3:
                return "仅对群主及超级管理员开放";
            case 4:
                return "仅对bot超级管理员开放";
            default:
                return "其他可用性";
        }
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("(获取)?功能详情(\\s)+[^\\s]+");
    }
}

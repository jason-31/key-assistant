package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class GetFunctionList extends Function {

    public GetFunctionList(){
        super(
                "获取功能列表",
                "get-function-list",
                "命令：“获取功能列表”\n" +
                        "用于获取功能列表，如果需要获取某个功能的详细信息，请使用”功能详情“命令",
                1
        );
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        Function[] functions = Router.getInstance().getFunctionList();
        String list = "功能列表如下：";
        for(Function function : functions){
            list+=("\n"+function.getName());
        }
        sendMessage(event, list);
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("(获取)?功能列表");
    }
}

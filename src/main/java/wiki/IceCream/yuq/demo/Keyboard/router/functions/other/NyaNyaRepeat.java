package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class NyaNyaRepeat extends Function {
    public NyaNyaRepeat(){
        super("喵喵复读",
                "nya-nya-repeat",
                "指令：喵喵+（复读内容)" +
                        "\n例：喵喵我啊，是真的生气了",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        Message message = new Message(), eventMessage = event.getMessage();
        message.plus(eventMessage.get(0).toString().substring(2));
        for(int i = 1; i<eventMessage.getBody().getSize(); i++){
            message.plus(eventMessage.get(i));
        }
        sendMessage(event, message);
        return;
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("喵喵(.)+");
    }
}

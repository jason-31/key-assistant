package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class PickOne extends Function {

    public PickOne(){
        super("选一个",
                "pick-one",
                "命令：小助手A和B(掉河里了)(可选)你(选)(可以任意词)哪个\n" +
                        "例：小助手菠萝面包和铜锣烧掉河里了你选哪个",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String items = strMessageText.split("(掉河里了)?你[^你]{1,4}哪(.)")[0].substring(3);
        String itemA = items.split("和")[0];
        String itemB = items.split("和")[1];
        String verb = strMessageText.split("你")[strMessageText.split("你").length-1];
        verb = verb.substring(0,verb.length()-2);
        sendMessage(event, "我"+verb+((Math.random()>0.5f)?itemA:itemB));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("小助手(.)+和(.)+(掉河里了)?你[^你]{1,4}哪(.)");
    }
}

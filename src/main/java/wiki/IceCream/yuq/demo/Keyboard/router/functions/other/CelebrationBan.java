package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class CelebrationBan extends Function {
    public CelebrationBan(){
        super("禁止好耶",
                "celebration-ban",
                "例：禁止禁止禁止好耶" +
                        "\n支持套娃",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String strMessage = "禁止" + strMessageText.replaceAll("!|！|【|】|，|,|。|\\s", "") + (strMessageText.contains("呵呵") ? "\uD83D\uDEAB" : "");
        sendMessage(event, strMessage);
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("(\\s|禁止)*(好耶|套娃|呵呵)+(!|！|【|】|，|,|。|\\s)*");
    }
}

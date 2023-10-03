package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.TianXingAPIs;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class Divination extends Function {

    public Divination(){
        super("占卜",
                "divination",
                "命令：“今日运势(要占卜的事（可选）)\n" +
                        "来让小助手给你求个签吧！",
                1);
    }
    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        Bank bank = Bank.getInstance();
        String result = "";
        if(!bank.addOrSpend(group, qq, -30))  {
            sendMessage(event, "施主要先攒够钱贫道才有缘为您求签呐");
            return;
        }
        if (strMessageText.length()>4) result = TianXingAPIs.getTodayYunShi(strMessageText.split("今日运势(\\s)*")[1]);
        else result = TianXingAPIs.getTodayYunShi(qq);
        sendMessage(event, result+"\n已成功扣款30金币");
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("今日运势(.)*");
    }
}

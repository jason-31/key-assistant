package wiki.IceCream.yuq.demo.Keyboard.router.functions.bank;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class CheckMyBalance extends Function {

    public CheckMyBalance(){
        super("我的金币",
                "check-my-balance",
                "命令：“我的金币”",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        sendMessage(event, event.getSender().nameCardOrName()+"的金币余额为："+ Bank.getInstance().getBalance(group,qq));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.equals("我的金币");
    }
}

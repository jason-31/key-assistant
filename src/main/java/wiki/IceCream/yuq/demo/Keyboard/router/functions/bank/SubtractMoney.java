package wiki.IceCream.yuq.demo.Keyboard.router.functions.bank;

import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class SubtractMoney extends Function {
    private Bank bank;

    public SubtractMoney(){
        super("扣钱",
                "subtract-money",
                "命令：”扣钱 (at目标) (金额)\n" +
                        "例：“扣钱 @键盘小助手 100”",
                2);
        bank = Bank.getInstance();
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        String[] strings = strMessageText.split("(\\s)+");
        long targetQq = Long.parseLong(strings[1].replaceAll("[^0-9]",""));
        Member target = event.getGroup().getOrNull(targetQq);
        long amount = Long.parseLong(strings[2]);
        if (amount <= 0) sendMessage(event,"哪有这么扣法的");
        long balance = bank.getBalance(group, target.getId());
        amount = (int)Math.min(amount,balance);
        bank.addOrSpend(group,target.getId(),-amount);
        sendMessage(event, "扣钱成功了，"+target.nameCardOrName()+"现在拥有"+bank.getBalance(group, target.getId())+"枚金币");
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("扣钱(\\s)+At_(\\d){5,15}(\\s)+(\\d){1,10}");
    }
}

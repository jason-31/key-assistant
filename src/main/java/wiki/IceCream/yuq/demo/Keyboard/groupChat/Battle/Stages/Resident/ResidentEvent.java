package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Resident;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Stage;

public abstract class ResidentEvent implements Stage {

    public boolean getActive(long group, long qq) {
        return Bank.getInstance().getBalance(group, qq)>=getCost();
    }

    public abstract int getCost();

}

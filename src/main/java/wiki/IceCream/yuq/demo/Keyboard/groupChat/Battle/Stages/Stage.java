package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages;

import java.io.File;
import java.util.HashMap;

public interface Stage  {

    public HashMap<Integer, Integer> getEquipments();

    public boolean getActive(long group, long qq);

    public String getName();

    public String getDescription();

    public int getMaxBlood(long group, long qq);

    public int getLeftBlood(long group, long qq);

    public File getAvatar();

    public Object[] getReward(long group, long qq, int score);

//    public boolean
}

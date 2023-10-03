package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Limited;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateUtil;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class ErTiJiao implements Equipment {
    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        return originalScoreA;
    }

    @Override
    public String getName() {
        return "年的二踢脚";
    }

    @Override
    public String getDescription() {
        return "啊，这可是最新产品，高八尺宽三尺半，采用泰拉大地前所未有的物质引爆——年心爱的二踢脚！\n可以用来吓唬年(māo)兽(māo)";
    }

    @Override
    public int getMaxLevel() {
        return 20;
    }

    @Override
    public boolean canGetOrUpgrade(long qq) {
        try {
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
            ChineseDate chineseDate = new ChineseDate(DateUtil.date(calendar));
            return (chineseDate.getMonth() == 1 && chineseDate.getDay() <= 10);
        } catch(Exception e){}
        return false;
    }
}

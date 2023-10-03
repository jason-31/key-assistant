package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public class Rice implements Equipment {
    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        return originalScoreA;
    }

    @Override
    public String getName() {
        return "大米饭";
    }

    @Override
    public String getDescription() {
        return "人活着就是为了干饭！当敌方刃甲等级不超过己方大米饭等级两级时免疫敌方刃甲伤害";
    }

    @Override
    public int getMaxLevel() {
        return 10;
    }

    @Override
    public boolean canGetOrUpgrade(long qq) {
        return true;
    }
}

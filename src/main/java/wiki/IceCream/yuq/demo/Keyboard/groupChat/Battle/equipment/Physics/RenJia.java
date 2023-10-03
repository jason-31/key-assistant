package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public class RenJia implements Equipment {

    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        return ((!ifPlayerA)&&(level-equipmentsA[2])>2 )?originalScoreA-4*level:originalScoreA;
    }

    @Override
    public String getName() {
        return "刃甲";
    }

    @Override
    public String getDescription() {
        return "当成为受攻击方时，根据等级增加得分";
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

package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public class Shield implements Equipment {
    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        int minBlood = -2*(10-level);
        return (ifPlayerA)?Math.max(minBlood, originalScoreA): Math.min(scoreRange-minBlood, originalScoreA);
    }

    @Override
    public String getName() {
        return "护心镜";
    }

    @Override
    public String getDescription() {
        return "可保护您在对剑后血量不低于一定值";
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

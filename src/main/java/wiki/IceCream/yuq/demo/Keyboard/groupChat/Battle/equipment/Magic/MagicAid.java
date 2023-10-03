package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public abstract class MagicAid implements Equipment {

    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        return originalScoreA;
    }

    public abstract String getName();

    public abstract String getDescription();

    public int getMaxLevel() {
        return 10;
    }

    public boolean canGetOrUpgrade(long qq) {return true;}
}

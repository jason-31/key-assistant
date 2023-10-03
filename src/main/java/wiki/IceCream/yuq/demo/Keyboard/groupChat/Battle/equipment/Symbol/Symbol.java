package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Symbol;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public abstract class Symbol implements Equipment {
    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        return originalScoreA;
    }

    public abstract String getName();

    public abstract String getDescription();

    @Override
    public int getMaxLevel() {
        return 100;
    }

    @Override
    public boolean canGetOrUpgrade(long qq) {
        return true;
    }
}

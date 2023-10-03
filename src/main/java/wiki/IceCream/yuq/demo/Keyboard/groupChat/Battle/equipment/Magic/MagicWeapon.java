package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public abstract class MagicWeapon implements Equipment {

    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        int debuffIndex = getDebuffIndex();
        int aidIndex = getAidIndex();
        int debuffLevel = (ifPlayerA)?equipmentsB[debuffIndex]:equipmentsA[debuffIndex];
        int aidLevel = (ifPlayerA)?equipmentsA[aidIndex]:equipmentsB[aidIndex];
        int levelMultiple = 6;
        float aidBuff = level*levelMultiple*aidLevel/10.0f;
        float propertyDebuff = 1.0f - 0.08f*debuffLevel;
        int attack = Math.max(0, (int)(level*levelMultiple*propertyDebuff+aidBuff+0.5));
        return originalScoreA+((ifPlayerA)?attack:-attack);
    }

    public abstract String getName();

    public abstract String getDescription();

    public int getMaxLevel(){
        return 10;
    }

    public abstract int getDebuffIndex();

    public abstract int getAidIndex();

    @Override
    public boolean canGetOrUpgrade(long qq) {
        return true;
    }
}

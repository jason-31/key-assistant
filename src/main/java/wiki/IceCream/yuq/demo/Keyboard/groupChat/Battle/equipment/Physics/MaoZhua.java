package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public class MaoZhua implements Equipment {
    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        int a = (int)(Math.random()*(12*level+1))-4*level;
        int oppositeErTiJiaoLevel = (ifPlayerA)?equipmentsB[18]:equipmentsA[18];
        int selfErTiJiaoLevel = (!ifPlayerA)?equipmentsB[18]:equipmentsA[18];
        //让猫爪对己方伤害不低于
        a = Math.max(2*selfErTiJiaoLevel-40, a);
        //让猫爪对对方伤害不高与
        a=(a>0)?Math.max(0,a-oppositeErTiJiaoLevel):a;
        return (ifPlayerA)? originalScoreA+a:originalScoreA-a;
    }

    @Override
    public String getName() {
        return "猫爪";
    }

    @Override
    public String getDescription() {
        return "猫猫才不会听你的！随机产生伤害值加成（负四倍等级到正八倍等级）";
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

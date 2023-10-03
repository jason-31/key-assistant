package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics;

import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Equipment;

public class Sword implements Equipment {


    @Override
    public int action(int[] equipmentsA, int[] equipmentsB, boolean ifPlayerA, int level, int originalScoreA, int scoreRange) {
        int attack = 2*level;
        int a = level;
        while (a > 0){
            attack+=a;
            a-=8;
        }
        return (ifPlayerA)?originalScoreA+attack:originalScoreA-attack;
    }

    @Override
    public String getName() {
        return "长剑";
    }

    @Override
    public String getDescription() {
        return "深受骑士们喜爱的长剑，但对方有护心镜可就不好了捏";
    }

    @Override
    public int getMaxLevel() {return 80;}

    @Override
    public boolean canGetOrUpgrade(long qq) {
        return true;
    }
}

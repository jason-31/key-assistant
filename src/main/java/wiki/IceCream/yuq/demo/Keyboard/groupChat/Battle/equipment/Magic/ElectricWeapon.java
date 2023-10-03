package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

public class ElectricWeapon extends MagicWeapon {
    @Override
    public String getName() {
        return "雷球";
    }

    @Override
    public String getDescription() {
        return "雷球，但是魔法要看对方风精的脸色哟";
    }

    @Override
    public int getDebuffIndex() {
        return 7;
    }

    @Override
    public int getAidIndex() {
        return 10;
    }
}

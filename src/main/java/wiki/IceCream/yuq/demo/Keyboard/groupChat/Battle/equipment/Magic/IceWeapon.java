package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

public class IceWeapon extends MagicWeapon {
    @Override
    public String getName() {
        return "冰稜";
    }

    @Override
    public String getDescription() {
        return "冰稜，但是要看对方雷球的脸色哟";
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

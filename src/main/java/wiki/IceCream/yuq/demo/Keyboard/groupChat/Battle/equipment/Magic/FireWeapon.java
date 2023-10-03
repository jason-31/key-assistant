package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

public class FireWeapon extends MagicWeapon {

    @Override
    public String getName() {
        return "爆裂海胆";
    }

    @Override
    public String getDescription() {
        return "爆裂海胆，但是要看对方冰稜的脸色哟";
    }

    @Override
    public int getDebuffIndex() {
        return 6;
    }

    @Override
    public int getAidIndex() {
        return 9;
    }
}

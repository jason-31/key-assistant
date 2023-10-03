package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic;

public class WindWeapon extends MagicWeapon {

    @Override
    public String getName() {
        return "风精";
    }

    @Override
    public String getDescription() {
        return "风精，但是要看对方爆裂海胆的脸色哟";
    }

    @Override
    public int getDebuffIndex() {
        return 5;
    }

    @Override
    public int getAidIndex() {
        return 12;
    }
}

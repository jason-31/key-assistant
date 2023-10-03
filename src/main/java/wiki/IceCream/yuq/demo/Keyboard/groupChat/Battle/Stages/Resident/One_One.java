package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Resident;

import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.File;
import java.util.HashMap;

public class One_One extends ResidentEvent {

    @Override
    public HashMap<Integer, Integer> getEquipments() {
        HashMap<Integer, Integer> equipments = new HashMap<>();
        equipments.put(3,80);
        return equipments;
    }

    @Override
    public String getName() {
        return "1-1 子鼠";
    }

    @Override
    public String getDescription() {
        return "十二生肖系列-鼠\n每次挑战需要30金币";
    }

    @Override
    public int getMaxBlood(long group, long qq) {
        return 50;
    }

    @Override
    public int getLeftBlood(long group, long qq) {
        return 50;
    }

    @Override
    public File getAvatar() {
        return new File(ListAndAddressHandeler.getImagePath()+"stages\\mouse.jpg");
    }

    @Override
    public Object[] getReward(long group, long qq, int score) {
        int reward = (score>=50)? score/2-getCost()-(int)(Math.random()*10) : -getCost();
        Object[] result = {reward,null};
        return result;
    }

    @Override
    public int getCost() {
        return 30;
    }
}

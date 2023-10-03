package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Limited;

import cn.hutool.core.date.ChineseDate;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Stage;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

public class NianShou implements Stage {
    int maximumBlood = 50000;
    private String date = KeyboardToolBox.getBeijingDate();
    private int year = 2022;
    HashMap<Long, JSONObject> groupProperties;
    String path = ListAndAddressHandeler.getDependenciesFolderPath()+"infos/nianshou.json";

    public NianShou(){
        groupProperties = new HashMap<>();
        //pares json
        try {
            String json = KeyboardToolBox.readAllBytesJava7(path);
            JSONObject root = JSON.parseObject(json);
            date = (root.getString("date") != null) ? root.getString("date") : date;
            year = (root.getInteger("year") != null) ? root.getInteger("year") : year;
            JSONArray groups = root.getJSONArray("groups");
            for (int i = 0; i < groups.size(); i++) {
                JSONObject group = groups.getJSONObject(i);
                groupProperties.put(group.getLong("id"), group);
            }
        } catch(Exception e){
            e.printStackTrace();
            checkAndInitialize();
        }
    }

    private void checkAndInitialize(){
        //check if is last year
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
        if (year != new ChineseDate(DateUtil.date(calendar)).getChineseYear()){
            System.out.println("new year");
            year = new ChineseDate(DateUtil.date(calendar)).getChineseYear();
            groupProperties = new HashMap<>();
        }
        //update the group properties
        System.out.println("old date"+date);
        for(long group : ListAndAddressHandeler.getServiceGroupList()){
            //construct the json object
            JSONObject groupJsonObject = groupProperties.get(group);
            if(groupJsonObject == null) {
                System.out.println("nulled");
                groupJsonObject = new JSONObject();
                groupJsonObject.put("id", group);
                groupJsonObject.put("history", new JSONArray());
                groupJsonObject.put("blood_left", maximumBlood);
                groupProperties.put(group,groupJsonObject);
            }
            if (!date.equals(KeyboardToolBox.getBeijingDate())){
                groupJsonObject.replace("history", new JSONArray());
            }
        }
        //update date
        date = date.equals(KeyboardToolBox.getBeijingDate())? date : KeyboardToolBox.getBeijingDate();
        store();
    }

    private void store(){
        JSONObject root = new JSONObject();
        root.put("date", date);
        root.put("year", year);
        JSONArray groupsArray = new JSONArray();
        for(JSONObject groupInfo : groupProperties.values()){
            groupsArray.add(groupInfo);
        }
        root.put("groups", groupsArray);
        String json = root.toJSONString();
        KeyboardToolBox.writeToFile(path,json);
    }

    private boolean addReference(long group, long qq){
        checkAndInitialize();
        JSONObject groupJsonObject = groupProperties.get(group);
        JSONArray usersHistoryJsonArray = groupJsonObject.getJSONArray("history");
        JSONObject userHistoryJsonObject = new JSONObject();
        userHistoryJsonObject.put("qq", qq);
        userHistoryJsonObject.put("times", 0);
        //遍历users history array
        for (int i = 0; i<usersHistoryJsonArray.size(); i++){
            JSONObject temp = usersHistoryJsonArray.getJSONObject(i);
            if (temp.getLong("qq") == qq) {
                userHistoryJsonObject = temp;
                usersHistoryJsonArray.remove(temp);
                break;
            }
        }
        usersHistoryJsonArray.add(userHistoryJsonObject);
        int times = userHistoryJsonObject.getInteger("times");
        if (times >=3) return false;
        userHistoryJsonObject.replace("times", ++times);
        store();
        return true;
    }

    @Override
    public HashMap<Integer, Integer> getEquipments() {
        return new HashMap<>();
    }

    @Override
    public boolean getActive(long group, long qq) {
        try {
            Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
            ChineseDate chineseDate = new ChineseDate(DateUtil.date(calendar));
            return chineseDate.getMonth() == 1;
        } catch(Exception e){e.printStackTrace();}
        return false;
    }

    @Override
    public String getName() {
        return "年兽大挑战";
    }

    @Override
    public String getDescription() {
        return "正月限定任务：打年兽！\n打倒年兽的话可以获取丰厚的奖励！";
    }

    @Override
    public int getMaxBlood(long group, long qq) {
        return maximumBlood;
    }

    @Override
    public int getLeftBlood(long group, long qq) {
        checkAndInitialize();
        int blood = groupProperties.get(group).getInteger("blood_left");
        return blood;
    }

    @Override
    public File getAvatar() {
        String path = ListAndAddressHandeler.getImagePath()+"stages\\nianshou.jpg";
        return new File(path);
    }

    @Override
    public Object[] getReward(long group, long qq, int score) {
        System.out.println("score: "+score);
        int oldBlood = getLeftBlood(group, qq);
        System.out.println("old blood: "+oldBlood);
        boolean ifAdded = addReference(group, qq);
        System.out.println(ifAdded);
        int newLeftBlood = 2*maximumBlood-score;
        System.out.println("new blood: "+newLeftBlood);
        System.out.println("now get: "+getLeftBlood(group, qq));
        int trueScore = oldBlood-newLeftBlood;
        System.out.println("true score: "+trueScore);
        newLeftBlood = Math.max(0,newLeftBlood);
        String string = "您本次打年兽的得分为："+trueScore+"分\n";
        //设定新年兽血量
        if(ifAdded){
            groupProperties.get(group).replace("blood_left", newLeftBlood);
            store();
        }
        boolean dead = newLeftBlood<=0;
        string+=(ifAdded)?
                (!dead)?"感谢您为本群消灭年兽做出的贡献":"年兽已经死了，请收好您的金币奖励":
                (!dead)?"因为超过今日上限，您本次的得分不会计入总伤害，请明天再来":"因为超过今日上限，本次挑战不会获得奖励，请明天再来";
        int reward = (dead&&ifAdded)?trueScore:0;
        Object[] result = {reward, string};
        return result;
    }
}

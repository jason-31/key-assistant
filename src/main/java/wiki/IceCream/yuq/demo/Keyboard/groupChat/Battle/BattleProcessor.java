package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.*;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Limited.ErTiJiao;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Magic.*;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Physics.*;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.equipment.Symbol.*;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.util.ArrayList;
import java.util.HashMap;

public class BattleProcessor {

    private Bank bank;
    final Equipment[] equipments = {
            new RenJia(),
            new MaoZhua(),
            new Rice(),
            new Sword(),
            new Shield(),//5
            new FireWeapon(),
            new IceWeapon(),
            new ElectricWeapon(),
            new WindWeapon(),
            new FireAid(),//10
            new IceAid(),
            new ElectricAid(),
            new WindAid(),
            new Kanon(),
            new Keke(), //15
            new Sumire(),
            new Chisato(),
            new Ren(),
            new ErTiJiao()
    };
    final int[][] calculateStages = {
            {3,4,0,1},
            {5,6,7,8}
    };

    final String jsonPath = ListAndAddressHandeler.getDependenciesFolderPath()+"infos/battle.json";
    HashMap<Long, HashMap<Long, int[]>> groupsEquipment;
    HashMap<Long, ArrayList<Long>> used;
    private static BattleProcessor instance = null;
    private ArrayList<Long> serviceGroups;

    public static BattleProcessor getInstance(){
        if (instance == null) instance = new BattleProcessor();
        return instance;
    }

    public BattleProcessor(){
        bank = Bank.getInstance();
        groupsEquipment = new HashMap<>();
        used = new HashMap<>();
        serviceGroups = ListAndAddressHandeler.getServiceGroupList();
        try {
            //读取信息
            String json = KeyboardToolBox.readAllBytesJava7(jsonPath);
            JSONObject root = JSON.parseObject(json);
            JSONArray groups = root.getJSONArray("groups");
            for (int i = 0; i < groups.size(); i++) {
                HashMap<Long, int[]> playersMap = new HashMap<>();
                JSONObject group = groups.getJSONObject(i);
                long groupId = group.getLong("group id");
                if(serviceGroups.contains(groupId)) {
                    JSONArray players = group.getJSONArray("players");
                    if (players == null) {
                        System.out.println(groupId);
                        groupsEquipment.put(groupId, new HashMap<Long, int[]>());
                    } else {
                        for (int j = 0; j < players.size(); j++) {
                            JSONObject player = players.getJSONObject(j);
                            long playerQQ = player.getLong("qq id");
                            JSONArray equipmentLevels = player.getJSONArray("equipments");
                            int[] levels = new int[equipments.length];
                            for (int k = 0; k < levels.length; k++) {
                                levels[k] = 0;
                            }
                            for (int k = 0; k < equipmentLevels.size(); k++) {
                                JSONObject equipment = equipmentLevels.getJSONObject(k);
                                int code = equipment.getInteger("code");
                                int maxLevel = equipments[code].getMaxLevel();
                                int level = equipment.getInteger("level");
                                levels[code] = Math.min(maxLevel, level);
                            }
                            playersMap.put(playerQQ, levels);
                        }
                        groupsEquipment.put(groupId, playersMap);
                    }
                }
            }
        }catch (Exception e){System.out.println("read record failed");}
        try {
            for (long group : serviceGroups) {
                groupsEquipment.computeIfAbsent(group, k -> new HashMap<Long, int[]>());
                used.put(group, new ArrayList<>());
            }
        }catch (Exception e){e.printStackTrace(); System.out.println("unknown exception");}
    }

    public void clearHistory(){
        used = new HashMap<>();
        for (long group : ListAndAddressHandeler.getServiceGroupList()) {
            used.put(group, new ArrayList<>());
        }
    }

    public int battle(long group, long playerA, HashMap<Integer, Integer> playerBEquipmentsMap, int scoreRange){
        int[] playerBEquipments = new int[equipments.length];
        for(int index : playerBEquipmentsMap.keySet()){
            playerBEquipments[index] = Math.min(equipments[index].getMaxLevel(), playerBEquipmentsMap.get(index));
        }
        HashMap players = groupsEquipment.get(group);
        int[] playerAEquipments = (players.get(playerA)==null)? new int[equipments.length] : (int[]) players.get(playerA);
        return battle(playerAEquipments, playerBEquipments, scoreRange);
    }

    public int battle(long group, long playerA, int[] playerBEquipments, int scoreRange){
        HashMap players = groupsEquipment.get(group);
        int[] playerAEquipments = (players.get(playerA)==null)? new int[equipments.length] : (int[]) players.get(playerA);
        return battle(playerAEquipments, playerBEquipments, scoreRange);
    }

    public int battle(long group, long playerA, long playerB, int scoreRange){
        HashMap players = groupsEquipment.get(group);
        int[] playerAEquipments = (players.get(playerA)==null)? new int[equipments.length] : (int[]) players.get(playerA);
        int[] playerBEquipments = (players.get(playerB)==null)? new int[equipments.length] : (int[]) players.get(playerB);
        return battle(playerAEquipments, playerBEquipments, scoreRange);
    }

    public int battle(int[] playerAEquipments, int[] playerBEquipments, int scoreRange){
        int stageCount = 1;
        int bloodBuffer = 3;
        int scoreA = scoreRange/2;
        //each stage
        for(int[] calculateStage : calculateStages){
            bloodBuffer*=stageCount++;
            for (int equipmentIndex : calculateStage){
                if (playerAEquipments[equipmentIndex]>0) scoreA = equipments[equipmentIndex].action(playerAEquipments,playerBEquipments,
                        true, playerAEquipments[equipmentIndex], scoreA,  scoreRange);
                if (playerBEquipments[equipmentIndex]>0) scoreA = equipments[equipmentIndex].action(playerAEquipments,playerBEquipments,
                        false, playerBEquipments[equipmentIndex], scoreA, scoreRange);
            }
            //test if blood is too low after each stage
            if(scoreA<-bloodBuffer||scoreA>scoreRange+bloodBuffer) return scoreA;
        }
        return scoreA;
    }

    public String addPlayer(long group, long player){
        int[] playerEquipment = (int[]) groupsEquipment.get(group).get(player);
        if(playerEquipment!=null) return "用户已存在";
        int index = (int)(Math.random()*13);
        playerEquipment = new int[equipments.length];
        for(int i = 0; i < playerEquipment.length; i++){
            playerEquipment[i]=(i==index)?1:0;
        }
        groupsEquipment.get(group).put(player, playerEquipment);
        store();
        return "开户成功，并赠送"+equipments[index].getName()+"(1级)一件";
    }

    public String upgrade(long group, long player, String name, YuQ yuq){
        return upgrade(group, player, findEquipmentIndex(name),yuq);
    }

    public String upgrade(long group, long player, int code, YuQ yuq){
        if(code < 0 || code >= equipments.length) return "装备不存在";
        int[] playerEquipments = groupsEquipment.get(group).get(player);
        if(playerEquipments==null) return "该用户未在本群开户，可发送“开户”";
        if(!equipments[code].canGetOrUpgrade(player)) return "该用户暂时无法获取/升级此装备";
        int oldLevel = playerEquipments[code];
        if (oldLevel>=equipments[code].getMaxLevel()) return "该装备已满级";
        if (oldLevel<1) return "你还未拥有该装备";
        if(used.get(group).contains(player)) {
            //如果已购买过
            String reply = "";
            try{
                yuq.getGroups().get(group).sendMessage("该时段内已进行过升级/购买操作，再次升级需要缴纳500金币，是否继续？\n回复”是“继续");
                ContextSession session = yuq.getGroups().get(group).getMembers().get(player).getGroupChatSession();
                reply = Message.Companion.firstString(session.waitNextMessage(20000));
            }
            catch (WaitNextMessageTimeoutException e){
                return("超时未输入");
            }
            catch (Exception e){
                return "未知错误";
            }
            if (reply.matches("(\\s)*是(\\s)*")){
                if(!bank.addOrSpend(group, player, -500)) return "你的余额不足，赶快开始打工吧";
            }
            else {
                return "好的，升级取消，请您下个时段再来";
            }
        }
        used.get(group).add(player);
        playerEquipments[code] ++;
        store();
        return equipments[code].getName()+"已升为"+(oldLevel+1)+"级！";
    }

    public String buy(long group, long player, String name, YuQ yuq){
        return buy(group, player, findEquipmentIndex(name), yuq);
    }

    public String buy(long group, long player, int code, YuQ yuq){
        if(code < 0 || code >= equipments.length) return "装备不存在";
        int[] playerEquipments = groupsEquipment.get(group).get(player);
        if(playerEquipments==null) return "该用户未在本群开户，可发送“开户”";
        if(!equipments[code].canGetOrUpgrade(player)) return "该用户暂时无法获取/升级此装备";
        int oldLevel = playerEquipments[code];
        if (oldLevel>0) return "你已拥有该装备";
        if(used.get(group).contains(player)) {
            //如果已购买过
            String reply = "";
            try{
                yuq.getGroups().get(group).sendMessage("该时段内已进行过升级/购买操作，再次购买需要缴纳500金币，是否继续？\n回复”是“继续");
                ContextSession session = yuq.getGroups().get(group).getMembers().get(player).getGroupChatSession();
                reply = Message.Companion.firstString(session.waitNextMessage(20L*1000));
            }
            catch (WaitNextMessageTimeoutException e){
                return("超时未输入");
            }
            catch (Exception e){
                return "未知错误";
            }
            if (reply.matches("(\\s)*是(\\s)*")){
                if(!bank.addOrSpend(group, player, -500)) return "你的余额不足，赶快开始打工吧";
            }
            else {
                return "好的，购买取消，请您下个时段再来";
            }
        }

        used.get(group).add(player);
        playerEquipments[code] ++;
        store();
        return "你已成功购买"+equipments[code].getName()+"！";
    }

    public String setEquipmentLevel(long group, long player, String name, int targetLevel){
        return setEquipmentLevel(group, player,findEquipmentIndex(name), targetLevel);
    }

    public String setEquipmentLevel(long group, long player, int code, int targetLevel){
        if(code < 0 || code >= equipments.length) return "装备不存在";
        int[] playerEquipments = groupsEquipment.get(group).get(player);
        if(playerEquipments==null) {
            addPlayer(group, player);
            playerEquipments = groupsEquipment.get(group).get(player);
        }
        if(!equipments[code].canGetOrUpgrade(player)) return "该用户暂时无法获取/升级此装备";
        int maximumLevel = equipments[code].getMaxLevel();
        playerEquipments[code] = Math.min(maximumLevel, targetLevel);
        store();
        return "已将该用户的"+equipments[code].getName()+"设定为"+Math.min(maximumLevel, targetLevel)+"/"+maximumLevel+"级";
    }


    public int findEquipmentIndex(String name){
        for(int i = 0; i< equipments.length; i++){
            Equipment equipment = equipments[i];
            if (equipment.getName().equals(name)) return i;
        }
        return -1;
    }

    public String getEquipmentProperty(String name, long qq){
        return getEquipmentProperty(findEquipmentIndex(name),qq);
    }

    public String getEquipmentProperty(int code, long qq){
        if(code < 0 || code >= equipments.length) return "装备不存在";
        return "装备："+equipments[code].getName()+"\n最大等级："+equipments[code].getMaxLevel()+"\n"+(equipments[code].canGetOrUpgrade(qq)?"您可以获取/升级此装备":"您暂时无法获取/升级此装备")+"\n介绍："+equipments[code].getDescription();
    }

    public void store(){
        JSONObject jsonObject = new JSONObject();
        JSONArray groups = new JSONArray();
        for(long group: groupsEquipment.keySet()){
            HashMap<Long, int[]> groupEquipment = groupsEquipment.get(group);
            JSONObject groupJson = new JSONObject();
            groupJson.put("group id", group);
            JSONArray players = new JSONArray();
            for(long qq: groupEquipment.keySet()){
                int[] equipmentLevels = groupEquipment.get(qq);
                JSONObject player = new JSONObject();
                player.put("qq id", qq);
                JSONArray equipments = new JSONArray();
                for(int i = 0; i<equipmentLevels.length; i++){
                    if(equipmentLevels[i]!=0){
                        JSONObject equipment = new JSONObject();
                        equipment.put("code", i);
                        equipment.put("level", equipmentLevels[i]);
                        equipments.add(equipment);
                    }
                }
                player.put("equipments",equipments);
                players.add(player);
            }
            groupJson.put("players", players);
            groups.add(groupJson);
        }
        jsonObject.put("groups", groups);
        KeyboardToolBox.writeToFile(jsonPath, jsonObject.toJSONString());
    }

    public String getEquipmentList(){
        String strInfo = "以下是可用装备列表：";
        int a = 0;
        for(Equipment equipment : equipments){
            strInfo+=("\n装备"+(++a)+"，“"+equipment.getName()+"”");
        }
        return strInfo;
    }

    public String getPlayerInfo(long group, long qq){
        int[] playerEquipments = groupsEquipment.get(group).get(qq);
        if(playerEquipments==null) return "该用户未在本群开户，可发送“开户”";
        String strInfo = "以下是该用户的可用装备：";
        int b = 0;
        for (int a : playerEquipments){
            if (a!=0) {
                strInfo += ("\n" + equipments[b].getName() + ", 等级"+a);
            }
            b++;
        }
        return strInfo;
    }

}

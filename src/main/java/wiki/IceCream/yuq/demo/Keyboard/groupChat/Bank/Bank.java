package wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.util.HashMap;

public class Bank {
    private static Bank _instance = null;
    private HashMap<Long, HashMap<Long, Long>> bank;
    private String bankPath = ListAndAddressHandeler.getDependenciesFolderPath()+"infos/bank.json";

    public static Bank getInstance(){
        if(_instance==null){
            _instance= new Bank();
        }
        return _instance;
    }

    public Bank(){
        bank = new HashMap<>();
        //parseJson
        try{
            String json = KeyboardToolBox.readAllBytesJava7(bankPath);
            JSONArray groupsJsonArray = JSON.parseObject(json).getJSONArray("groups");
            for (int i = 0; i<groupsJsonArray.size(); i++){
                JSONObject groupJsonObject = groupsJsonArray.getJSONObject(i);
                long group = groupJsonObject.getLong("group_id");
                JSONArray usersJsonArray = groupJsonObject.getJSONArray("users");
                HashMap users = new HashMap();
                for (int j = 0; j<usersJsonArray.size(); j++){
                    JSONObject userJsonObject = usersJsonArray.getJSONObject(j);
                    users.put(userJsonObject.getLong("qq"), userJsonObject.getLong("amount"));
                }
                bank.put(group, users);
            }
        }
        catch(Exception e) {e.printStackTrace();}
        for(long group : ListAndAddressHandeler.getServiceGroupList()){
            if(bank.get(group)==null)  bank.put(group, new HashMap<>());
        }
    }

    private boolean addUser(long group, long qq){
        HashMap<Long, Long> groupAccounts = bank.get(group);
        if (groupAccounts == null) {
            groupAccounts = new HashMap<>();
            bank.put(group, groupAccounts);
        }
        if (groupAccounts.get(qq) != null) return false;
        groupAccounts.put(qq, 0L);
        store();
        return true;
    }

    public boolean addOrSpend(long group, long qq, long amount){
//        System.out.println(qq +"  " + getBalance(group, qq));
        addUser(group, qq);
        HashMap<Long, Long> groupUsersMap = bank.get(group);
        long initialAmount = groupUsersMap.get(qq);
        if (initialAmount+amount<0) return false;
        groupUsersMap.replace(qq, (initialAmount+amount));
//        System.out.println(qq +"  " + getBalance(group, qq));
        store();
        return true;
    }

    public long getBalance(long group, long qq){
        addUser(group, qq);
        HashMap<Long, Long> usersMap = bank.get(group);
        return usersMap.get(qq);
    }

    private void store(){
        JSONObject root = new JSONObject();
        JSONArray groupsJsonArray = new JSONArray();
        for(long group : bank.keySet()){
            JSONObject groupJsonObject = new JSONObject();
            groupJsonObject.put("group_id", group);
            JSONArray usersJsonArray = new JSONArray();
            HashMap<Long, Long> users = bank.get(group);
            for (long qq : users.keySet()){
                JSONObject userJsonObject = new JSONObject();
                userJsonObject.put("qq", qq);
                userJsonObject.put("amount", users.get(qq));
                usersJsonArray.add(userJsonObject);
            }
            groupJsonObject.put("users", usersJsonArray);
            groupsJsonArray.add(groupJsonObject);
        }
        root.put("groups",groupsJsonArray);
        String json = root.toJSONString();
        KeyboardToolBox.writeToFile(bankPath,json);
    }
}

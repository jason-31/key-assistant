package wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

public class UltimateInfoStorage {

    private static UltimateInfoStorage _instance;
    private String path = ListAndAddressHandeler.getDependenciesFolderPath()+"infos/ultimate.json";
    private JSONObject root;

    public static UltimateInfoStorage getInstance(){
        if (_instance == null){
            _instance = new UltimateInfoStorage();
        }
        return _instance;
    }

    public UltimateInfoStorage(){
        String json = KeyboardToolBox.readAllBytesJava7(path);
        root = JSONObject.parseObject(json);

    }

    public void deleteInactiveGroups(long activeGroups[]){
        JSONArray jsonArray = root.getJSONArray("groups");
        for(int i = 0; i<jsonArray.size(); i++){
            boolean isInactive = true;
            for(int j = 0; j<activeGroups.length; j++){
                if(jsonArray.getJSONObject(i).getString("id").equals(String.valueOf(activeGroups[j]))){
                    isInactive = false;
                    break;
                }
            }
            if(isInactive){
                jsonArray.remove(i);
                i--;
            }
        }
        save();
    }

    public JSONObject getObjectFromUserOrGroup(String objectId, String userOrGroupId, int type){
        JSONObject groupOrUserRoot = getJsonObject(userOrGroupId, type);
        JSONObject result = groupOrUserRoot.getJSONObject(objectId);
        if(result == null) {
            result = new JSONObject();
            groupOrUserRoot.put(objectId, result);
            save();
        }
        return result;
    }

    public JSONObject getJsonObject(String id,int type){
        //type 1 = services
        //type 2 = users
        //type 3 = groups
        String[] strTypes = {"services", "users", "groups"};
        String strType = strTypes[type-1];
        JSONArray jsonArray = root.getJSONArray(strType);
        JSONObject searchResult = null;
        //find the right target
        for(int i = 0; i<jsonArray.size(); i++){
            if (jsonArray.getJSONObject(i).getString("id").equals(id))
                searchResult = jsonArray.getJSONObject(i);
        }
        //constract new if failed to search
        if (searchResult == null){
            searchResult = constructJson(String.valueOf(id), type);
            jsonArray.add(searchResult);
        }
        return searchResult;
    }

    private JSONObject constructJson(String id, int type){
        //type 1 = service
        //type 2 = user
        //type 3 = group
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", id);
        switch (type){
            case 1 :
                //enter code here
                break;
            case 2 :
                //enter code here
                break;
            case 3 :
                //enter code here
                break;
        }
        return jsonObject;
    }

    public void save(){
        String json = root.toJSONString();
        KeyboardToolBox.writeToFile(path, json);
    }
}

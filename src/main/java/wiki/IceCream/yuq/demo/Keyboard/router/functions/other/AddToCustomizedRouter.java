package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.MessageLocalStorageProcessor;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.util.ArrayList;
import java.util.HashMap;

public class AddToCustomizedRouter extends Function {
    private UltimateInfoStorage uis;

    public AddToCustomizedRouter(){
        super("添加/删除自定义回复",
                "add-to-customized-router",
                "允许用户添加/删除自定义回复",
                2);
        uis = UltimateInfoStorage.getInstance();
        for(long _group : ListAndAddressHandeler.getServiceGroupList()){
            initializeGroup(_group);
        }
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        boolean ifAdd = strMessageText.startsWith("添加"); //确认是否添加
        boolean universal = strMessageText.contains("全局"); //确认是否添加为全局
        boolean specific = strMessageText.contains("专属"); //确认是否添加为专属
        long owner = specific?qq:0;

        //确认需要的权限等级
        int requireAdminLevel = universal?4:getCurrentAvailability(group);
        //确认用户的权限等级
        int senderAdminLevel = KeyboardToolBox.getAdminLevel(event);
        //确认用户拥有足够权限
        if(senderAdminLevel+1<requireAdminLevel){
            sendMessage(event, "你的权限不足");
            return;
        }

        //初始化所有群
        Long[] groupsToOperate = {group};

        //如果全局
        if(universal){
            ArrayList<Long> serviceGroups = ListAndAddressHandeler.getServiceGroupList();
            groupsToOperate = new Long[serviceGroups.size()];
            for(int i = 0; i<serviceGroups.size();i++){
                groupsToOperate[i]=serviceGroups.get(i);
            }
        }

        //获取要添加的群的信息
        HashMap<Long, JSONArray> reactionsInAllGroups = new HashMap<>();
        for(long groupToOperate : groupsToOperate){
            reactionsInAllGroups.put(groupToOperate,uis.getObjectFromUserOrGroup("customized-router", ""+groupToOperate, 3).getJSONArray("reactions"));
        }


        String keywordRegex = "";
        Message toStore = null;
        ContextSession session = event.getSender().getGroupChatSession();

        //获取关键词正则
        try{
            sendMessage(event,"好的，请输入关键词（支持正则）");
            keywordRegex = Message.Companion.firstString(session.waitNextMessage(30*1000));
        }
        catch (WaitNextMessageTimeoutException e){
            sendMessage(event, "超时未输入");
            return;
        }
        catch (Exception e){
            sendMessage(event, "未知错误");
            return;
        }

        //检索已经存在的reaction
        HashMap<Long,ArrayList<JSONObject>> conflictingReactions = findConflictingReactions(keywordRegex, reactionsInAllGroups, owner);

        //如果添加
        if(ifAdd) {
            try {
                sendMessage(event, "好的，请输入回复消息");
                toStore = session.waitNextMessage(30 * 1000);
            } catch (WaitNextMessageTimeoutException e) {
                sendMessage(event, "超时未输入");
                return;
            } catch (Exception e) {
                sendMessage(event, "未知错误");
                return;
            }

            //如果有重合项
            if(conflictingReactions.size()>0){
                try {
                    sendMessage(event, "已有重叠的关键词，目前已存在的关键词：\n"+conflictingReactionToString(conflictingReactions)
                            +"要替换吗?（回复“是”替换）");
                    if(Message.Companion.firstString(session.waitNextMessage(30*1000)).equals("是")) {
                        removeAllConflictingReactions(reactionsInAllGroups, conflictingReactions);
                        if(universal) {
                            try {
                                JSONArray universalReactions = uis.getJsonObject("universal-reactions", 1).getJSONArray("reactions");
                                for (int i = 0; i < universalReactions.size(); i++) {
                                    JSONObject checking = universalReactions.getJSONObject(i);
                                    String checkingRegex = checking.getString("regex");

                                    if (partialMatch(checkingRegex, keywordRegex)
                                            || partialMatch(keywordRegex, checkingRegex)
                                            || keywordRegex.equals(checkingRegex)) {
                                        universalReactions.remove(i);
                                        break;
                                    }
                                }
                            } catch (Exception e) {}
                        }
                    }
                    else{
                        sendMessage(event,"好的，添加取消");
                        return;
                    }
                } catch (WaitNextMessageTimeoutException e) {
                    sendMessage(event, "超时未输入");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(event, "未知错误");
                    return;
                }
            }

            //添加
            long messageId = MessageLocalStorageProcessor.getInstance().storeMessage(toStore);
            if(messageId<0){
                sendMessage(event, (messageId==-1)?"不支持的消息":"未知错误");
                return;
            }
            JSONObject reaction = new JSONObject();
            reaction.put("regex", keywordRegex);
            reaction.put("message-id", messageId);
            reaction.put("owner", owner);
            for (JSONArray reactionsInOneGroup : reactionsInAllGroups.values()){
                reactionsInOneGroup.add(reaction.clone());
            }
            if(universal){
                JSONArray universalReactions = uis.getJsonObject("universal-reactions", 1).getJSONArray("reactions");
                universalReactions.add(reaction.clone());
            }
            uis.save();
            sendMessage(event,"添加成功");
        }
        //如果删除
        else{
            if(conflictingReactions.size()==0){
                sendMessage(event, "不存在匹配的关键词");
                return;
            }
            else{
                removeAllConflictingReactions(reactionsInAllGroups, conflictingReactions);
                uis.save();
                sendMessage(event, "好的，删除成功");
                return;
            }
        }

    }


    private String conflictingReactionToString(HashMap<Long,ArrayList<JSONObject>> conflictingReactions){
        System.out.println(conflictingReactions);
        String toReturn = "";
        for(long group : conflictingReactions.keySet()){
            String groupName = ""+group;
            try{
                groupName = Router.getInstance().getYuq().getGroups().get(group).getName();
            }catch (Exception e){}
            toReturn+=groupName+"：\n";
            for(JSONObject reaction : conflictingReactions.get(group)){
                toReturn+=(reaction.getString("regex")+"\n");
            }
        }
        System.out.println(toReturn);
        return toReturn;
    }

    private void removeAllConflictingReactions(HashMap<Long, JSONArray> allReactions,HashMap<Long,ArrayList<JSONObject>> conflictingReactions){
        for (long group : conflictingReactions.keySet()){
            for(JSONObject reaction : conflictingReactions.get(group)){
                allReactions.get(group).remove(reaction);
            }
        }
    }

    private HashMap<Long,ArrayList<JSONObject>> findConflictingReactions(String regexToAddOrDelete, HashMap<Long, JSONArray> reactionsInAllGroups, long addingReactionOwnerQq){
        HashMap<Long, ArrayList<JSONObject>> conflictingReactions = new HashMap<>();
        for (long groupToOperate : reactionsInAllGroups.keySet()){
            ArrayList<JSONObject> conflictingReactionsInEachGroup = new ArrayList<>();
            for(int i = 0; i < reactionsInAllGroups.get(groupToOperate).size(); i++){
                //get information from each reaction
                JSONObject reaction = reactionsInAllGroups.get(groupToOperate).getJSONObject(i);
                String regexFromExistingReaction = reaction.getString("regex");
                long reactionOwner = 0;
                try{
                    reactionOwner = reaction.getLong("owner");
                } catch (Exception e){}
                //see if they conflict
                if(reactionOwner==addingReactionOwnerQq
                        && (
                                KeyboardToolBox.partialMatch(regexToAddOrDelete,regexFromExistingReaction)
                                ||
                                KeyboardToolBox.partialMatch(regexFromExistingReaction,regexToAddOrDelete)
                                ||
                                regexFromExistingReaction.equals(regexToAddOrDelete)
                        ))
                    conflictingReactionsInEachGroup.add(reaction);
            }
            if (conflictingReactionsInEachGroup.size()>0)
                conflictingReactions.put(groupToOperate, conflictingReactionsInEachGroup);
        }
        return conflictingReactions;
    }

    private void initializeGroup(long group){
        JSONObject jsonGroupObject = uis.getObjectFromUserOrGroup("customized-router", ""+group, 3);
        if(jsonGroupObject.getJSONArray("reactions") == null){
            JSONArray reactions = new JSONArray();
            JSONArray universalReactions = uis.getJsonObject("universal-reactions", 1).getJSONArray("reactions");
            for (int i = 0; i<universalReactions.size();i++) {
                reactions.add(universalReactions.getJSONObject(i).clone());
            }
            jsonGroupObject.put("reactions", reactions);
            uis.save();
        }
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.matches("(添加|删除)(专属|全局)*自定义回复");
    }
}

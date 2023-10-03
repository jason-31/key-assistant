package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.util.ArrayList;

public class PoHai extends Function {
    UltimateInfoStorage uis;

    public PoHai(){
        super("迫害吉祥物",
                "po-hai",
                "大家一起来迫害吉祥物吧！",
                2);
        uis = UltimateInfoStorage.getInstance();
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        if(getCurrentAvailability(group)!=1) {
            sendMessage(event, "本群还未开通此功能，请联系管理员");
            return;
        }
        if(strMessageText.startsWith("我投"))
            voteForTomorrowSucker(group, event.getSender().getId(),Long.parseLong(strMessageText.replaceAll("[^0-9]","")),event);
        else if(strMessageText.contains("迫害今日吉祥物")){
            poHai(event,group,qq);
        }
        else{
            initializeSucker();
        }
    }

    private void poHai(GroupMessageEvent event, long group, long robber){
        try{
            long todaySucker = uis.getObjectFromUserOrGroup("ji-xiang-wu",""+group, 3).getLong("today-sucker");
            if(todaySucker==0) throw new Exception();
            //check if the robber robbed today
            JSONObject groupJiXiangWuInfo = uis.getObjectFromUserOrGroup("ji-xiang-wu",""+group, 3);
            String robbed = groupJiXiangWuInfo.getString("robbed");

            if(robbed.contains("["+robber+"]")){
                sendMessage(event, "您今天已经迫害过了，请勿重复迫害");
                return;
            }
            int robbedAmount = (int)(Math.random()*201)+200;

            if(Bank.getInstance().addOrSpend(group,todaySucker,-1*robbedAmount)) {
                Bank.getInstance().addOrSpend(group, robber, robbedAmount);
                groupJiXiangWuInfo.put("robbed", groupJiXiangWuInfo.getString("robbed") + "[" + robber + "]");
                sendMessage(event,"迫害成功了，抢到了"+robbedAmount+"！");
            }
            else{
                sendMessage(event,new Message().plus("迫害失败了，").plus(Router.getInstance().getMif().at(todaySucker)).plus("已经没钱了！"));
            }

            uis.save();
        }catch (Exception e){
            e.printStackTrace();
            sendMessage(event, "本群今天还没有吉祥物哦，请等明天");
            return;
        }
    }

    private void voteForTomorrowSucker(long group, long voter, long candidate,GroupMessageEvent event){
        MessageItemFactory mif = Router.getInstance().getMif();
        JSONObject groupJiXiangWuInfo = uis.getObjectFromUserOrGroup("ji-xiang-wu",""+group, 3);
//        System.out.println(groupJiXiangWuInfo);
        if(groupJiXiangWuInfo.getJSONArray("tomorrow-suckers")==null) groupJiXiangWuInfo.put("tomorrow-suckers",new JSONArray());
        if(groupJiXiangWuInfo.getString("voted")==null) groupJiXiangWuInfo.put("voted","[10001]");

        //check if user voted
        String voted = groupJiXiangWuInfo.getString("voted");
        if(voted.contains("["+voter+"]")){
            sendMessage(event, "您今天已经投过票了，请勿重复投票");
            return;
        }

        //count the vote
        int voterAdminLevel = KeyboardToolBox.getAdminLevel(event.getGroup(), voter);
        int newSuckerRate = voterAdminLevel==0?1:voterAdminLevel*5;
        //check if the voted user is in the list already
        groupJiXiangWuInfo.put("voted",groupJiXiangWuInfo.getString("voted")+"["+voter+"]");
        JSONArray tomorrowSuckers = groupJiXiangWuInfo.getJSONArray("tomorrow-suckers");
        if(getVotes(group,candidate)!=0){ //if the candidate already exist
            //find the existed sucker piece
            for (int i = 0; i < tomorrowSuckers.size(); i++) {
                JSONObject suckerPiece = tomorrowSuckers.getJSONObject(i);
                if (suckerPiece.getLong("qq") == candidate) {
                    newSuckerRate += suckerPiece.getInteger("rate");
                    suckerPiece.put("rate",newSuckerRate);
                }
            }
        }
        else{
//            System.out.println("Ran into else");
//            System.out.println(groupJiXiangWuInfo);
            //generate new sucker piece
            JSONObject newSuckerPiece = new JSONObject();
            newSuckerPiece.put("qq", candidate);
            newSuckerPiece.put("rate", newSuckerRate);
            tomorrowSuckers.add(newSuckerPiece);
        }
        uis.save();
        sendMessage(event,new Message().plus("投票成功了，").plus(mif.at(candidate)).plus("目前的票数为："+newSuckerRate));
    }

    public void initializeSucker(){
        MessageItemFactory mif = Router.getInstance().getMif();
        for(long group: ListAndAddressHandeler.getServiceGroupList()){
            //clear all the suckers from yesterdays
            JSONObject groupJiXiangWuInfo = uis.getObjectFromUserOrGroup("ji-xiang-wu",""+group, 3);
            ArrayList<Long> newSuckerQqs = new ArrayList<>();
            ArrayList<Integer> newSuckerRates = new ArrayList();
            //get the tomorrow sucker info
                try{
                    JSONArray tomorrowSuckerInfo = groupJiXiangWuInfo.getJSONArray("tomorrow-suckers");
                    for(int i = 0; i<tomorrowSuckerInfo.size();i++){
                        JSONObject suckerPiece = tomorrowSuckerInfo.getJSONObject(i);
                        newSuckerQqs.add(suckerPiece.getLong("qq"));
                        newSuckerRates.add(suckerPiece.getInteger("rate"));
                    }
                }catch (Exception e){}

            groupJiXiangWuInfo.clear();

            if(getCurrentAvailability(group)==1){ //this group should have a sucker today
                //generate the new Sucker
                //get a random user to ensure there's at least one user
                ArrayList<Long> usersLeftInTheGroup = new ArrayList<>();
                usersLeftInTheGroup.addAll(Router.getInstance().getYuq().getGroups().get(group).getMembers().keySet());
                //removed all voted members from the list
                usersLeftInTheGroup.removeAll(newSuckerQqs);
                if(usersLeftInTheGroup.size()!=0){
                    newSuckerQqs.add(usersLeftInTheGroup.get((int)(Math.random() * usersLeftInTheGroup.size())));
                    newSuckerRates.add(3);
                }
                long finalTodaySucker = 0;
                int sum = 0;
                for (int i = 0; i<newSuckerQqs.size();i++){
                    sum+=newSuckerRates.get(i);
                }
                int result = (int)(Math.random()*sum);
                int count = 0;
                for(int rate : newSuckerRates){
                    result-=rate;
                    if(result<0){
                        finalTodaySucker=newSuckerQqs.get(count);
                        break;
                    }
                    count++;
                }

                //put the information in
                groupJiXiangWuInfo.put("today-sucker", finalTodaySucker);
                groupJiXiangWuInfo.put("tomorrow-suckers",new JSONArray());
                groupJiXiangWuInfo.put("voted","[10001]");
                groupJiXiangWuInfo.put("robbed","[10001]");

                //announce it
                Bank.getInstance().addOrSpend(group,finalTodaySucker,3000);
                KeyboardToolBox.sendMessage(group, Router.getInstance().getYuq(),mif, new Message().plus("本群今天的吉祥物就决定是").plus(mif.at(finalTodaySucker)).
                        plus("了！TA现在的余额为："+Bank.getInstance().getBalance(group,finalTodaySucker)));
            }

            uis.save();
        }
    }

    private int getVotes(long group, long candidate){
        JSONObject groupJiXiangWuInfo = uis.getObjectFromUserOrGroup("ji-xiang-wu",""+group, 3);
        try {
            JSONArray tomorrowSuckers = groupJiXiangWuInfo.getJSONArray("tomorrow-suckers");
            for (int i = 0; i < tomorrowSuckers.size(); i++) {
                JSONObject suckerPiece = tomorrowSuckers.getJSONObject(i);
                if (suckerPiece.getLong("qq") == candidate) {
                    return suckerPiece.getInteger("rate");
                }
            }
            return 0;
        }catch (Exception e){
            return -1;
        }
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        Message message = event.getMessage();
        System.out.println(strMessageText);
        return (message.getBody().size()==3&&strMessageText.matches("我投(\\s)*At_(\\d)+(\\s)*当明天的吉祥物(.)*"))||
                strMessageText.replaceAll("\\s","").equals("迫害今日吉祥物")||
                ListAndAddressHandeler.getBotAdmins().contains(event.getSender().getId())&&strMessageText.contains("roll一下吉祥物");
    }
}

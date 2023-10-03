package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.LoveLevelProcessor;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class TrickOrTreat extends Function {
    HashMap<String, Integer> candyList = new HashMap<>();
    HashMap<Long, ArrayList<Long>> tricking = new HashMap<>();

    public TrickOrTreat(){
        super("万圣节要糖特别行动",
                "trick-or-treat",
                "万圣节期间的小魔法，会降临在谁身上呢？\n" +
                        "如需调整要糖概率，请由管理员发送“更改小助手要糖概率 {0-100}\n”" +
                        "例：更改小助手要糖概率 0，会将小助手药汤的概率设定为0%",
                1);
        candyList.put("酸三色水果糖",5);
        candyList.put("棉花糖",10);
        candyList.put("费列罗巧克力",30);
        candyList.put("Candy Lab水果糖",50);
        candyList.put("星空棒棒糖",100);
        candyList.put("新疆切糕",300);
        for(long group : ListAndAddressHandeler.getServiceGroupList()){
            tricking.put(group,new ArrayList<>());
        }
    }
    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        UltimateInfoStorage uis = UltimateInfoStorage.getInstance();
        MessageItemFactory mif = Router.getInstance().getMif();

        //getDefaultPossibility
        String strHalloween = DateUtil.year(DateUtil.date())+"-10-31";
        Date halloween = DateUtil.parse(strHalloween, "yyyy-MM-dd");
        String strToday = KeyboardToolBox.getBeijingDate();
        Date today = DateUtil.parse(strToday, "yyyy年MM月dd日");
        int defaultPossibility  = (Math.abs(DateUtil.between(halloween, today, DateUnit.DAY))<8)?3:0;

        //如果变更可能性
        if(strMessageText.matches("更改小助手(要糖|TOT)概率(\\s)*(\\d){1,3}")
            &&KeyboardToolBox.getAdminLevel(group, qq, Router.getInstance().getYuq())>0){ //is admin
            int changePossibilityTo = Integer.parseInt(strMessageText.replaceAll("[^0-9]",""));
            if(changePossibilityTo!=defaultPossibility)
                uis.getObjectFromUserOrGroup("trick-or-treat", ""+group, 3).put("possibility",changePossibilityTo);
            else {
                try {
                    uis.getObjectFromUserOrGroup("trick-or-treat", "" + group, 3).remove("possibility");
                } catch (Exception e) {
                }
            }
            uis.save();
            sendMessage(event,"更改成功");
            return;
        }

        //所有其他情况
        int possibility = defaultPossibility;
        try{
            possibility = uis.getObjectFromUserOrGroup("trick-or-treat", ""+group, 3).getInteger("possibility");
        }catch (Exception  e){} //see if overwritten
        System.out.println(possibility);
        if((!tricking.get(group).contains(qq))&&((int)(Math.random()*100))<possibility){
            //todo trick or treat
            //add the poor guy to the list
            tricking.get(group).add(qq);
            //get session
            ContextSession session = event.getSender().getGroupChatSession();
            //ask if user wants to treat the bot
            String candyType = candyList.keySet().toArray()[((int)(Math.random()*candyList.size()))].toString();
            int amount = candyList.get(candyType);
            System.out.println(candyType+"-"+amount);
            sendMessage(event, new Message().plus(mif.at(qq)).plus("Trick or Treat! 要给小助手打赏一"
                    +(candyType.contains("棒棒糖")?"根":"块")
                    +amount+"金币的"+candyType+"吗？(回复“是”打赏)\n" +
                    "如不喜欢本功能请回复“功能详情 万圣节要糖特别行动”查看关闭方法"));
            String response = "";
            long timeLeft =180*1000;
            //检测规定时间内是否由合格回复
            while (true) {
                Message toSend = new Message();
                toSend.setReply(event.getMessage().getSource());
                try {
                    long timeStart = System.currentTimeMillis();
                    try {
                        response = Message.Companion.firstString(session.waitNextMessage(timeLeft)).replaceAll("\\s","");
                    }catch (Exception e){}
                    if(response.equals("是")){
                        if (!Bank.getInstance().addOrSpend(group,qq, -1*amount)){
                            sendMessage(event, toSend.plus("你的余额不足，感受小助手的恶作剧吧！"));
                            break;
                        }
                        else {
                            tricking.get(group).remove(qq);
                            int loveLevelToAdd=((int)(amount*(Math.random()*0.8+1.2)));
                            LoveLevelProcessor.getInstance().addOrSubtractLoveLevel(qq, loveLevelToAdd);
                            sendMessage(event, toSend.plus("收到糖糖了！超———喜欢你的！\n"+loveLevelToAdd+"好感度的喜欢！"));
                            return;
                        }
                    }
                    else {
                        if(response.replaceAll("\\s","").equals("否")){
                            sendMessage(event, toSend.plus("很大胆嘛！感受小助手的恶作剧吧！"));
                            break;
                        }
                        timeLeft-=(System.currentTimeMillis()-timeStart);
                    }
                } catch (WaitNextMessageTimeoutException e) {
                    sendMessage(event, toSend.plus("超时未输入，感受小助手的恶作剧吧！"));
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    tricking.get(group).remove(qq);
                    sendMessage(event, "未知错误");
                    return;
                }
            }

            //if no, trick
            tricking.get(group).remove(qq);
            trick(group,qq,event,candyType,amount);
        }
    }

    private void trick(long group, long qq, GroupMessageEvent event, String candyType, int candyAmount){
        //todo
        //10% 扣钱
        //20% 羞耻发病
        //30% 降低好感度
        //40% 更改群名片
        YuQ yuq = Router.getInstance().getYuq();
        int a = (int)(Math.random()*100);
        boolean ifCanSetNameCard = event.getGroup().getBot().getPermission()>event.getSender().getPermission();
        int d1 = ifCanSetNameCard?10:25;
        int d2 = ifCanSetNameCard?20:35;
        int d3 = ifCanSetNameCard?30:40;
        int caseNumber = a<d1?1:a<(d1+d2)?2:a<(d1+d2+d3)?3:4;
        String returnMessage = "\n哼哼哼！你抽中了拔刺game "+caseNumber+"!\n";

        switch (caseNumber){
            case 1:
                //扣钱
                Bank bank = Bank.getInstance();
                long balance = bank.getBalance(group, qq);
                int amount = (int)(Math.random()*1000);
                long moneyToSubtract = Math.min(amount, balance);
                bank.addOrSpend(group,qq,-1*moneyToSubtract);
                //扣除好感度
                long lovelLevelToSubstract = (amount-moneyToSubtract)/5;
                LoveLevelProcessor.getInstance().addOrSubtractLoveLevel(qq, -1*lovelLevelToSubstract);
                returnMessage+="扣除了你"+amount+"金币"+((lovelLevelToSubstract==0)?"！":"，不够的部分用"+lovelLevelToSubstract+"好感度弥补了唷！");
                break;
            case 2:
                //羞耻发病
                returnMessage+="大家知道吗，"+KeyboardToolBox.getBeijingDate()+event.getSender().nameCardOrName()+"说好想要当小助手的猫呢！虽然我很讨厌猫就是了！";
                break;
            case 3:
                LoveLevelProcessor.getInstance().addOrSubtractLoveLevel(qq, -1*100);
                returnMessage+="对你失望透顶了啦！扣除了100好感度！";
                break;
            case 4:
                try{
                    String[] possibleNameCards = {
                            "小助手的狗",
                            "小助手的🐕",
                            "小助手的\uD83D\uDC36",
                            "小助手的\uD83D\uDC29",
                            "小助手的猫",
                            "小助手的🐱",
                            "小助手的🍗",
                            "好喜欢小助手啊！",
                            "为什么不能嫁给小助手呢！"
                    };
                    event.getSender().setNameCard(possibleNameCards[(int)(Math.random()*possibleNameCards.length)]);
                    returnMessage+="帮你改了个会非常受欢迎的群名片哟";
                }catch (Exception e){
                    returnMessage+="好像失败了！";
                }
        }
        MessageItemFactory mif = Router.getInstance().getMif();
        sendMessage(event,new Message().plus(mif.at(qq)).plus(returnMessage));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return true;
    }

    @Override
    public boolean getIfEnd(){return false;}
}

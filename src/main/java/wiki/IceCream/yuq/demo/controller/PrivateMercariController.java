package wiki.IceCream.yuq.demo.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.mercari.MercariServiceProcessor;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;

import javax.inject.Inject;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

@PrivateController
public class PrivateMercariController extends QQController {
    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    private MercariServiceProcessor mercariServiceProcessor;

    private long nextWechatSendTime = 0;

    public PrivateMercariController(){
        //get instance
        try {
            mercariServiceProcessor = MercariServiceProcessor.getInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //---------------------------------切煤---------------------------------

    @Action("test")
    public String test(){
        return "tested";
    }

    @Action("#切煤 {link}")
    @Synonym("#切煤{link}")
    public String mercari(String link, long qq, ContextSession session) {
        String strReturn="键盘切煤小助手为您服务";
        int price = 0;
        float rmbPrice = 0.0f;
        String nameCard = "";
        String date = KeyboardToolBox.getBeijingDate();

        //获取网址
        link=link.replaceAll("[^0-9]","");
        if (link.length()==11){
            strReturn+="\n获取编号成功";
        }
        else{
            strReturn+="\n获取编号失败：不是煤炉编号";
            return strReturn;
        }

        //检测用户身处的群聊
        long group = 0l;
        ArrayList<Long> participatedGroups = mercariServiceProcessor.getParticipateGroups(qq);
        if(participatedGroups.size()==0) {
            strReturn+=("\n"+"未找到用户，请联系管理员");
            return strReturn;
        }
        else if (participatedGroups.size()==1){
            strReturn+=("\n"+"查找用户成功");
            group = participatedGroups.get(0);
        }
        else{
            String reply = "您在以下群都有账号，请问要使用哪个群切煤呢？";
            int a = 1;
            for (long participateGroup : participatedGroups){
                reply+="\n"+(a++)+" - "+yuq.getGroups().get(participateGroup).getName();
            }
            reply+="\n请直接回复编号即可，请在一分钟内回复";
            reply(reply);
            String response = "";
            try{
                response = Message.Companion.firstString(session.waitNextMessage(60 * 1000));
            }
            catch(IllegalStateException e){}
            catch (WaitNextMessageTimeoutException e){return ("超时未输入，切煤失败");}
            catch (Exception e) {return "未知错误，切煤失败";}
            try {
                int index = Integer.parseInt(response.replaceAll("\\s",""))-1;
                if (index<0||index>=participatedGroups.size()) return "输入错误，切煤失败";
                group = participatedGroups.get(index);
            }
            catch (NumberFormatException e){return "输入错误，切煤失败";}
            catch (Exception e) {return "未知错误，切煤失败";}
        }

        //查看是否已有记录
        if (mercariServiceProcessor.checkIfAlreadyInRecord(link)){
            strReturn+=("\n已有该记录，您来晚了");
            return strReturn;
        }

        //获取群名片
        try{
            nameCard = yuq.getGroups().get(group).getMembers().get(qq).nameCardOrName();
            if (nameCard==null||nameCard=="") throw new Exception();
        } catch (Exception e) {
            strReturn+=("\n获取群名片出错");
            return strReturn;
        }

        //获取价格
        String strPrice = "";
        try{
            String[] info = mercariServiceProcessor.getItemInfoFromMercari("https://jp.mercari.com/item/m" + link);
            if (info == null) throw new Exception("未获取到有效信息");
            strPrice = info[0];
            price = Integer.parseInt(strPrice);
            strReturn+=("\n获取成功："+price);
        } catch(Exception e){
            strReturn+=("\n获取出错："+e.getMessage());
            return strReturn;
        }

        //计算人民币价格
        int exchangeRate = mercariServiceProcessor.getExchangeRates(group);
        rmbPrice=price*(float)exchangeRate/1000;
        strReturn+=("\n"+exchangeRate+"汇对应为："+String.format("%.2f", rmbPrice));

        //检测是否超过上限

        int balance = mercariServiceProcessor.getUserBalance(qq, group);
        if (price > balance){
            strReturn+=("\n"+"超过今日上限");
            return strReturn;
        }
        balance-=price;
        strReturn+=("\n"+"今日上限还剩"+balance);
        if (!mercariServiceProcessor.setUserBalance(qq, group, balance)){
            strReturn+=("\n"+"保存出错，请联系管理员");
            return strReturn;
        }



        //发送消息到切煤群
        long[] cutGroupAndCutter = mercariServiceProcessor.getCutGroupAndCutter(group);
        System.out.println(group);
        long mercariGroup = cutGroupAndCutter[0];
        long mercariCutter = cutGroupAndCutter[1];
        String cutterMessage = "麻烦您啦！https://jp.mercari.com/item/m" + link;
        try {
            KeyboardToolBox.sendMessage(yuq.getGroups().get(mercariGroup),mif,  new Message().plus(mif.at(mercariCutter)).plus("\n" + cutterMessage));
            Thread.sleep(5000);
            System.out.printf("等待五秒\n");
            //保存切煤记录
            strReturn += (mercariServiceProcessor.saveNewHistory(group, qq, price, link, nameCard, date))?
                    ("\n" + "切煤记录保存成功"):("\n"+"切煤记录保存出错，请联系管理员");
        } catch (Exception e) {
            e.printStackTrace();
            strReturn += "\n发送给切咪出错";
        }

        //把记录发到对应的通知
        try {
            KeyboardToolBox.sendMessage(mercariServiceProcessor.getNoticeTarget(group, yuq),
                    mif, new Message().plus(nameCard + "于" + date + "切煤" +
                    price + "y，" + exchangeRate + "汇对应" + String.format("%.2f", rmbPrice) + "r，编号为：" + link));
            Thread.sleep(5000);
            System.out.printf("等待五秒\n");
        } catch (Exception e) {}

        //返回消息
        return strReturn;
    }

    @Action("#查询 {link}")
    @Synonym("#查询{link}")
    public Object mercariPriceSearch(String link) {
        return mercariServiceProcessor.browseMercari(link, mif);
    }

    @Action("#搜索{anything}")
    public Object searchMercari(Message message){
        return mercariServiceProcessor.searchMercari(message, mif);
    }

    //---------------------我的权限-------------------------

    @Action("#我的权限")
    public Object displayOwnAuthority(long qq, ContextSession session){
        //检测用户身处的群聊
        long group = getRightParticipatedGroup(qq, session);
        String parsedMessage = parseParticipatedGroupCode(group);
        if (parsedMessage!=null) return parsedMessage;
        return mercariServiceProcessor.getUserAllowance(qq, group,yuq);
    }

    @Action("#用户权限 {userId}")
    public Object displayUserAuthority(long userId, long qq, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.getUserAllowance(userId, group, yuq);
    }

    @Action("#添加 {userId} {allowance}")
    public Object addTrustedUser(long userId, long qq, int allowance, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return (mercariServiceProcessor.addNewUser(group, userId, allowance));
    }

    @Action("#移除 {user}")
    public Object removeTrustedUser(long user,long qq, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return (mercariServiceProcessor.removeUser(group, qq));
    }

    @Action("#更改权限 {user} {newAllowance}")
    public Object changeUserAuthority(long user, int newAllowance, long qq, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.changeUserAllowance(group, user, newAllowance);
    }

    @Action("#权限列表")
    @Synonym({"#查看用户列表","#查看权限列表","#用户列表"})
    public Object displayTrustedUsers(long qq, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.getTrustedUsersString(qq,group,yuq);
    }

    @Action("#全部用户列表")
    public Object displayAllTrustedUsers(long qq){
        return mercariServiceProcessor.getTrustedUsersString(qq,yuq);
    }


    //---------------------------------Mercari History---------------------------------


    @Action("#用户记录 {user}")
    @Synonym("#用户切煤记录 {user}")
    public Object userMercariHistory(long user, long qq){
        return mercariServiceProcessor.getUserHistory(user, qq);
    }

    @Action("#我的记录")
    @Synonym({"#我的切煤记录","我的切煤"})
    public Object myMercariHistory(long qq){
        return mercariServiceProcessor.getUserHistory(qq);
    }

    @Action("#某日记录 {date}")
    @Synonym("#某日切煤记录 {date}")
    public Object somedayMercari(long qq, String date){
        return mercariServiceProcessor.getCertainDayHistory(qq, date);
    }

    @Action("#今日记录")
    @Synonym({"#今天的切煤记录","#今日切煤记录"})
    public Object todayMercari(long qq){
        return mercariServiceProcessor.getTodayHistory(qq);
    }

    @Action("#最近记录 {num}")
    @Synonym({"#查看条记录 {num}","#查看最近记录 {num}"})
    public Object getNumHistory(int num, long qq, ContextSession session){
        if (num<=0) return "错误输入";
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.getRecentHistory(num, group);
    }

    @Action("#已切 {id}")
    public Object cut(String id, long qq){
        return mercariServiceProcessor.cut(id, qq, yuq);
    }

    @Action("#已肾 {id}")
    public Object kidneyed(String id, long qq){
        return mercariServiceProcessor.setProperty(qq, id, 4, true);
    }

    @Action("#已切已肾 {id}")
    public Object cutAndKidneyed(String id, long qq) {
        String a = mercariServiceProcessor.cut(id, qq, yuq);
        String b = mercariServiceProcessor.setProperty(qq, id, 4, true);
        return (a.equals("记录成功")&&b.equals("记录成功"))? a : "出错了\n设定1:"+a+"\n设定2:"+b;
    }

    @Action("#未切 {id}")
    public Object notCut(String id, long qq){
        return mercariServiceProcessor.setProperty(qq, id, 3, false);
    }

    @Action("#未肾 {id}")
    public Object notKidneyed(String id, long qq){
        return mercariServiceProcessor.setProperty(qq, id, 4, false);
    }

    @Action("#已传 {id}")
    public Object uploaded(String id, long qq){
        return mercariServiceProcessor.setProperty(qq, id, 7, true);
    }

    @Action("#未传 {id}")
    public Object notUploaded(String id, long qq){
        return mercariServiceProcessor.setProperty(qq, id, 7, false);
    }

    @Action("#全部已传")
    public Object alluploaded(long qq, ContextSession session){
        long group = getRightAdminedGroup(qq, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.allUploaded(group);
    }

    @Action("#sold {id}")
    @Synonym("#删除记录 {id}")
    public Object mercariSold(String id, long qq){
        return mercariServiceProcessor.deleteHistroy(id, qq, yuq);
    }



    //-------------------------------通用方法------------------------------


    public long getRightParticipatedGroup(long qq, ContextSession session){
        //检测用户身处的群聊
        long group = 0l;
        ArrayList<Long> participatedGroups = mercariServiceProcessor.getParticipateGroups(qq);
        if(participatedGroups.size()==0) {
            return 0;
        }
        else if (participatedGroups.size()==1){
            group = participatedGroups.get(0);
        }
        else{
            String reply = "您在以下群都有账号，请问要使用哪个群呢？";
            int a = 1;
            for (long participateGroup : participatedGroups){
                reply+="\n"+(a++)+" - "+yuq.getGroups().get(participateGroup).getName();
            }
            reply+="\n请直接回复编号即可，请在一分钟内回复";
            reply(reply);
            String response = "";
            try{
                response = Message.Companion.firstString(session.waitNextMessage(60 * 1000));
            }
            catch(IllegalStateException e){}
            catch (WaitNextMessageTimeoutException e){return 1;}
            catch (Exception e) {return 2;}
            try {
                int index = Integer.parseInt(response.replaceAll("\\s",""))-1;
                if (index<0||index>=participatedGroups.size()) return 3;
                group = participatedGroups.get(index);
            }
            catch (NumberFormatException e){return 3;}
            catch (Exception e) {return 2;}
        }
        return group;
    }

    public String parseParticipatedGroupCode(long code){
        if (code > 10) return null;
        switch ((int) code){
            case 0:
                return "你还没有开户";
            case 1:
                return "超时未输入";
            case 2:
                return "未知错误";
            case 3:
                return "输入错误";
        }
        return null;
    }

    public long getRightAdminedGroup (long qq, ContextSession session){
        //获取admin group list
        long group;
        ArrayList<Long> adminedGroups = mercariServiceProcessor.getAdminedGroups(qq);
        if(adminedGroups.size()==0) {
            return 0;
        }
        else if (adminedGroups.size()==1){
            return adminedGroups.get(0);
        }
        else{
            String reply = "您有切煤管理权限的群聊是以下几个，请问您要操作哪个群呢？";
            int a = 1;
            for (long participateGroup : adminedGroups){
                reply+="\n"+(a++)+" - "+yuq.getGroups().get(participateGroup).getName();
            }
            reply+="\n请直接回复编号即可，请在一分钟内回复";
            reply(reply);
            String response = "";
            try{
                response = Message.Companion.firstString(session.waitNextMessage(60 * 1000));
            }
            catch(IllegalStateException e){}
            catch (WaitNextMessageTimeoutException e){return 1;}
            catch (Exception e) {return 2;}
            try {
                int index = Integer.parseInt(response.replaceAll("\\s",""))-1;
                if (index<0||index>=adminedGroups.size()) return 3;
                group = adminedGroups.get(index);
            }
            catch (NumberFormatException e){return 3;}
            catch (Exception e) {return 2;}
        }
        return group;
    }

    public String parseAdminedGroupsCode(long code){
        if (code > 10) return null;
        switch ((int) code){
            case 0:
                return "你没有切煤的管理权限";
            case 1:
                return "超时未输入";
            case 2:
                return "未知错误";
            case 3:
                return "输入错误";
        }
        return null;
    }


}

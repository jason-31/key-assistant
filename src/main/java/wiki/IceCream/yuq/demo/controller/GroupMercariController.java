package wiki.IceCream.yuq.demo.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.mercari.MercariServiceProcessor;

import javax.inject.Inject;
import java.util.ArrayList;

@GroupController
public class GroupMercariController extends QQController {
    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    private MercariServiceProcessor mercariServiceProcessor;

    public GroupMercariController(){
        //get instance
        try {
            mercariServiceProcessor = MercariServiceProcessor.getInstance();
        } catch (Exception e){
            e.printStackTrace();
        }
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

    @Action("#我的权限")
    public Object displayOwnAuthority(long qq, ContextSession session){
        //检测用户身处的群聊
        long group = getRightParticipatedGroup(qq, session);
        String parsedMessage = parseParticipatedGroupCode(group);
        if (parsedMessage!=null) return parsedMessage;
        return mercariServiceProcessor.getUserAllowance(qq, group,yuq);
    }

    @Action("#用户权限 {target}")
    public Object displayUserAuthority(Member target, long qq, long group, ContextSession session){
        long userId = target.getId();
        group = getRightAdminedGroup(qq, group, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.getUserAllowance(userId, group, yuq);
    }

    @Action("#添加 {target} {allowance}")
    public Object addTrustedUser(Member target, long qq, long group, int allowance, ContextSession session){
        long userId = target.getId();
        group = getRightAdminedGroup(qq, group,session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return (mercariServiceProcessor.addNewUser(group, userId, allowance));
    }

    @Action("#移除 {user}")
    public Object removeTrustedUser(long user,long qq, long group, ContextSession session){
        group = getRightAdminedGroup(qq, group, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return (mercariServiceProcessor.removeUser(group, qq));
    }

    @Action("#更改权限 {user} {newAllowance}")
    public Object changeUserAuthority(long user, int newAllowance, long qq, long group, ContextSession session){
        group = getRightAdminedGroup(qq, group, session);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.changeUserAllowance(group, user, newAllowance);
    }

    @Action("#权限列表")
    @Synonym({"#查看用户列表","#查看权限列表","#用户列表"})
    public Object displayTrustedUsers(long qq, long group,  ContextSession session){
        group = getRightAdminedGroup(qq, group, session);
        System.out.println(group);
        String message = parseAdminedGroupsCode(group);
        if (message!=null) return message;
        return mercariServiceProcessor.getTrustedUsersString(qq,group,yuq);
    }

    @Action("#全部用户列表")
    public Object displayAllTrustedUsers(long qq){
        return mercariServiceProcessor.getTrustedUsersString(qq,yuq);
    }

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


    @Action("#最近记录 {num}")
    @Synonym({"#查看条记录 {num}","#查看最近记录 {num}"})
    public Object getNumHistory(int num, long qq, long group, ContextSession session){
        if (num<=0) return "错误输入";
        group = getRightAdminedGroup(qq, group, session);
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
    public Object alluploaded(long qq, long group, ContextSession session){
        group = getRightAdminedGroup(qq, group, session);
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
            String reply = "您在以下群都有账号，请问要找哪个群呢？";
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

    public long getRightAdminedGroup (long qq, long group, ContextSession session){
        //获取admin group list
        ArrayList<Long> adminedGroups = mercariServiceProcessor.getAdminedGroups(qq);
        if (adminedGroups.contains(group)) return group;
        System.out.println("1");
        group = mercariServiceProcessor.getMercariGroupFromNoticeGroup(group,yuq);
        if (group!=0) return group;
        System.out.println(group);
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

package wiki.IceCream.yuq.demo.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.StageProcessor;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.BattleProcessor;
import wiki.IceCream.yuq.demo.Keyboard.mercari.MercariInfoGetter;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;


import javax.imageio.ImageIO;
import javax.inject.Inject;

@GroupController
public class MyGroupController {
    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    private ArrayList<Long> mercariGroupList = ListAndAddressHandeler.getMercariGroupList();
    private ArrayList<Long> botAdminList = ListAndAddressHandeler.getBotAdmins();

    private MercariInfoGetter mercariInfoGetter;
    private BattleProcessor battleProcessor;
    private StageProcessor stageProcessor;
    String imagePath = ListAndAddressHandeler.getImagePath();

    //constructor
    public MyGroupController() {
        try {
            mercariInfoGetter = MercariInfoGetter.getInstance();
            battleProcessor = BattleProcessor.getInstance();
            stageProcessor = StageProcessor.getInstance();
            System.out.println("initializing success");
        } catch(Exception e) {e.printStackTrace();}
    }


    @Action("菜单")
    @Synonym("menu")
    public Object[] menu(long group){
        int wait = 3000;
        String basicMenu = "现在支持的指令：\n"
                + "喵\n"
                + "Nya\n"
                + "草死了\n"
                + "土味情话\n"
                + "1st扭蛋\n"
                +"罗森吧唧/挂件\n"
                + "心跳在悦动\n"
                + "艾特我一下\n"
                + "{名字}是美女\n"
                + "喵喵{复读内容}\n"
                +"battle +at一名和你打架的幸运群友";
        if (mercariGroupList.contains(group)) {
            Object[] arrReturn = {
                    basicMenu,
                    wait,
                    "#我的权限(查看自己的权限）\n"
                            + "#最近纪录+空格+所需要的条数\n"
                            + "#查询+空格+煤炉网址或编号"
                            + "#切煤+空格+链接或商品编号（请确认数字部分正确）",
                    wait,
                    "- -以下指令仅管理员可用- -\n"
                            + "#全部已传\n"
                            + "#已切+空格+商品id\n"
                            + "#已肾+空格+商品id\n"
                            + "#未切+空格+商品id\n"
                            + "#未肾+空格+商品id\n"
                            + "#已传+空格+商品id\n"
                            + "#未传+空格+商品id",
                    wait,
                    "#已切已肾+空格+商品id\n"
                            + "#sold+空格+商品id（应用于当天没切到的商品）\n"
                            + "#删除记录+空格+商品id（应用于以往没切到的商品）",
                    wait,
                    "#添加+空格+新用户QQ号+空格+每日上限\n"
                            + "#移除+空格+被移除用户QQ号\n"
                            + "#更改权限+空格+用户QQ号+空格+新每日上限\n"
                            + "#最近纪录+空格+所需要的条数\n"
                            + "#用户权限+空格+目标用户QQ号（查询某个成员的权限）"};
            return arrReturn;
        }
        Object[] arrReturn = {basicMenu,0};
        return arrReturn;
    }

    @Action("土味情话")
    public Object dirtTasteFlirt(long qq){
        String[] flirtList = {"想要带我去哪里玩啊","今天你哪也不许去，陪在我身边好吗","今天你就跟着我走啦，把手给我","今天想去哪，你说了算",
                                "到底要不要约会啊","陪我看看夜景，再跳个舞如何？"};
        return new Message().plus(mif.at(qq).plus(flirtList[(int)(Math.random()*flirtList.length)]));
    }

    @Action("关于")
    public Object about(){
        return "赞助名单：芽，知由\n" +
                "开发者：脸滚键盘\n" +
                "机器人框架：yuq-ArtQQ\n" +
                "关于捐助：如果可以的话请帮忙开通svip以减少被封号几率";
    }

    @Action("Nya")
    @Synonym("喵")
    public Object nyaNyaNya() {
        return "喵喵喵";
    }

    @Action("心跳在悦动")
    @Synonym("心跳在跃动")
    public Object heartBeats(){
        return "幸福满天空";
    }

    @Action("{uu}是美女")
    @Synonym("{uu}超可爱")
    public void uuCute(String uu, long group){
        if(uu.matches("(.){1,5}"))
            sendMessage(group, "♪♪"+uu+"是美女♪♪\n♪♪"+uu+"超可爱♪♪\n一般通过恋口上ver.\n我有话要跟你说！！\n果然"+uu+"超可爱！！");

    }

    @Action("艾特我一下")
    public Object atMe(Member qq){
        return mif.at(qq.getId()).plus("，你好");
    }

    @Action("更改群名片 {target} {newNameCard}")
    public Object changeNameCard(Member qq, long group, Member target, String newNameCard){
        String oldNameCard = target.nameCardOrName();
        if (KeyboardToolBox.getAdminLevel(yuq.getGroups().get(group), qq.getId())<1){
            return "你有什么权利管人家叫什么？";
        }
        if(yuq.getGroups().get(group).getBot().getPermission()<=target.getPermission()
        && yuq.getGroups().get(group).getBot()!=target){
            return "我没有此权限，我是废物，对不起";
        }
        try {
            target.setNameCard(newNameCard);
        } catch (Exception e){return "改名失败了，我是废物，嘤嘤嘤";}
        return oldNameCard+"现在叫"+newNameCard+"啦！";
    }

    @Action("那就是传说中的")
    @Synonym({"传说中的","那个传说中的"})
    public Object legend(){return "传说中的！";}

    @Action("竟然是")
    @Synonym({"竟然是！","竟然！","竟然"})
    public Object surprise(){return "竟然是！";}

    @Action("太好听了8")
    @Synonym({"太好听了吧","太好听了叭","太好听了叭~"})
    public Object helloGoodbye(){return "你好谢谢小笼包再见！";}

    @Action("{strRate}汇计算 {strAmount}")
    @Synonym({"{strRate}汇 {strAmount}","{strRate}汇计算:{strAmount}","{strRate}汇计算：{strAmount}"})
    public Object exchangeCalculator (String strRate, String strAmount){
        System.out.println("strRate:"+strRate+" strAmount:"+strAmount);
        int rate = 0;
        double amount = 0.0;
        try{
            rate = Integer.parseInt(strRate);
            amount = Double.parseDouble(strAmount);
        }catch (Exception e){return "输入错误";}
        rate = (rate<50)? rate*10:(rate>100)?rate/10:rate;
        double rmbPrice=amount*rate/1000;
        return strRate+"汇对应为："+String.format("%.2f", rmbPrice);
    }

    //打架

    @Action("开户")
    public Object openAccount (long group, long qq){
        System.out.println("开户");
        return battleProcessor.addPlayer(group, qq);
    }

    @Action("购买 {name}")
    public Object buy(long group, long qq, Message message){
        String name = message.getBody().get(0).toString().split("购买(\\s)+")[1];
        try {
            int code = Integer.parseInt(name.replaceAll("\\s",""))-1;
            return battleProcessor.buy(group,qq,code,yuq);
        } catch (Exception e){}
        return battleProcessor.buy(group,qq,name,yuq);
    }

    @Action("升级 {name}")
    public Object upgrade (long group, long qq, Message message){
        String name = message.getBody().get(0).toString().split("升级(\\s)+")[1];
        try {
            int code = Integer.parseInt(name.replaceAll("\\s",""))-1;
            return battleProcessor.upgrade(group,qq,code,yuq);
        } catch (Exception e){}
        return battleProcessor.upgrade(group,qq,name,yuq);
    }

    @Action("查询装备 {name}")
    public Object getEquipmentInfo (Message message, long qq){
        String name = message.getBody().get(0).toString().split("查询装备(\\s)+")[1];
        try {
            int code = Integer.parseInt(name.replaceAll("\\s",""))-1;
            return battleProcessor.getEquipmentProperty(code, qq);
        } catch (Exception e){}
        return battleProcessor.getEquipmentProperty(name, qq);
    }

    @Action("我的装备")
    public void battleInfo(long group, long qq){
        sendMessage(group, battleProcessor.getPlayerInfo(group, qq));
    }

    @Action("用户装备 {target}")
    public void userBattleInfo(long group, Member target){
        sendMessage(group, battleProcessor.getPlayerInfo(group, target.getId()));
    }

    @Action("装备列表")
    public void equipmentsInfo(long group){
        sendMessage(group,battleProcessor.getEquipmentList());
    }

    @Action("打架菜单")
    public void battleMenu(long group){
        String strReturn = "开户 - 创建账户\n"+
                "打架+at群友 - 比武\n"+
                "我的装备 - 查看已经拥有的装备\n"+
                "购买+空格+装备名或编号 - 购买装备\n"+
                "升级+空格+装备名或编号 - 升级装备\n"+
                "查询装备+空格+装备名或编号 - 查询装备信息";
        sendMessage(group, strReturn);
    }

    @Action("设定我的装备等级 {name} {level}")
    public void setMyEquipmentLevel(long group, long qq, String name, int level){
        if (!(botAdminList.contains(qq)||(yuq.getGroups().get(group).getMembers().get(qq).getPermission()>0))) {
            sendMessage(group, "你没有此项权限");
            return;
        }
        int code = -1;
        try {
            code = Integer.parseInt(name.replaceAll("\\s",""))-1;
        } catch (Exception e){}
        sendMessage(group, battleProcessor.setEquipmentLevel(group, qq, (code>0)?""+code:name, level));
    }

    @Action("设定用户装备等级 {target} {name} {level}")
    public Object setUserEquipmentLevel(long group, long qq, Member target, String name, int level){
        if (!(botAdminList.contains(qq)||(yuq.getGroups().get(group).getMembers().get(qq).getPermission()>0)))
            return "你没有此项权限";
        long targetQq = target.getId();
        try {
            int code = Integer.parseInt(name.replaceAll("\\s",""))-1;
            return battleProcessor.setEquipmentLevel(group, targetQq, code, level);
        } catch (Exception e){}
        return battleProcessor.setEquipmentLevel(group, targetQq, name, level);
    }

    @Action("我的权限等级")
    public Object adminLevel(long qq, long group){
        return (KeyboardToolBox.getAdminLevel(group,qq,yuq)+1);
    }

    @Action("battle {target}")
    @Synonym({"打架 {target}", "Revue {target}"})
    public Object battle(long group, Member qq, Member target){
        try {
            int scoreRange = 70;
            int rawBattleScoreA = battleProcessor.battle(group, qq.getId(), target.getId(),scoreRange);
            int battleScoreA = Math.max(0,rawBattleScoreA);
            battleScoreA = Math.min(scoreRange, battleScoreA);
            int randomScoreA = 0;
            int scoreA = 0;
            do{
                randomScoreA = (int)(Math.random()*(100-scoreRange+1));
                scoreA = randomScoreA+battleScoreA;
            } while(scoreA==50);
            int scoreB = 100-scoreA;
            Color clrWin = new Color(216,33,13);
            Color clrLose = new Color(0,98,150);
            Color colorA = (scoreA>scoreB)?clrWin:clrLose;
            Color colorB = (colorA!=clrWin)?clrWin:clrLose;
            BufferedImage avatar1 = KeyboardToolBox.getBufferedImageFromUrl(qq.getAvatar());
            BufferedImage avatar2 = KeyboardToolBox.getBufferedImageFromUrl(target.getAvatar());
            BufferedImage paint = new BufferedImage(1280,720 , BufferedImage.TYPE_INT_RGB);
            //得到它的绘制环境(这张图片的笔)
            Graphics2D g2 = (Graphics2D) paint.getGraphics();
            //设置背景颜色
            g2.setColor(new Color(252,221,174));
            g2.fillRect(0,0,1280,720);
            //绘制图片
            g2.drawImage(avatar1, 50,270,180,180,null,null);
            g2.drawImage(avatar2, 1050,270,180,180,null,null);
            //绘制头像边框
            int penSize = 5;
            g2.setStroke(new BasicStroke(penSize));
            g2.setColor(colorA);
            g2.drawRoundRect(50,270,180,180,5,5);
            g2.setColor(colorB);
            g2.drawRoundRect(1050,270,180,180,5,5);
            //绘制胜负方块
            int squareHeight = 70;
            g2.setColor(colorA);
            g2.fillRect(50+180+60,360-squareHeight/2,7*scoreA,squareHeight);
            g2.setColor(colorB);
            g2.fillRect(50+180+60+7*scoreA,360-squareHeight/2,7*scoreB,squareHeight);
            //为胜负方块绘制边框
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(50+180+60,360-squareHeight/2,700,squareHeight,5,5);
            g2.fillRect(50+180+60+7*scoreA-2,360-squareHeight/2,4,squareHeight);
            //书写分数
            Font font = new Font("Times New Roman",Font.BOLD,30);
            g2.setFont(font);
            g2.setColor(colorA);
            String text = scoreA+"("+rawBattleScoreA+"/"+scoreRange+")";
            g2.drawString(text,50+180+70,420);
            g2.setColor(colorB);
            FontMetrics metrics = g2.getFontMetrics(font);
            text = scoreB+"("+(scoreRange-rawBattleScoreA)+"/"+scoreRange+")";
            g2.drawString(text,50+180+60+700-metrics.stringWidth(text),420);
            //书写胜者
            g2.setColor(Color.RED);
            font = new Font("KaiTi",Font.BOLD,50);
            g2.setFont(font);
            metrics = g2.getFontMetrics(font);
            text = "恭喜：";
            g2.drawString(text,(paint.getWidth() - metrics.stringWidth(text)) / 2,metrics.getHeight()+10);
            text = (scoreA>scoreB)? qq.nameCardOrName():target.nameCardOrName();
            g2.drawString(text,(paint.getWidth() - metrics.stringWidth(text)) / 2,metrics.getHeight()*2+40);

            File savedFile = new File (imagePath+"temp/bt_"+System.currentTimeMillis()+".png");
            ImageIO.write(paint, "png", savedFile);
            return new Message().plus(mif.imageByFile(savedFile));
        } catch (Exception e){e.printStackTrace(); return "出错啦";}
    }

    @Action("攻打副本 {stageName}")
    @Synonym("挑战副本 {stageName}")
    public Object tryStage(long group, long qq, String stageName){
        return stageProcessor.battle(group,qq,stageName,yuq,mif);
    }

    @Action("副本列表")
    public Object stageList(long group, long qq){
        return stageProcessor.stageList(group, qq);
    }

    @Action("查询副本 {stageName}")
    public String checkStage(long group, long qq, String stageName){
        return stageProcessor.checkStage(group, qq, stageName);
    }

    @Action("生成群友头像拼图")
    public Object avatarPuzzle(Group group){
        ArrayList<String> avatars = new ArrayList<>();
        for (Member member : group.getMembers().values()){
            avatars.add(member.getAvatar());
        }
        avatars.add(group.getBot().getAvatar());
        File puzzle = generateAvatarPuzzle(avatars);
        if (puzzle==null){return "头像获取出错啦！";}
        return new Message().plus("以下是"+avatars.size()+"名群友的头像拼图：\n").plus(mif.imageByFile(puzzle));
    }

    @Action("检测群友重合度 {strGroupB}")
    public Object detectSimilarity(Group group, String strGroupB){
        long groupBNUmber = 0;
        Group groupB;
        try {
             groupBNUmber = Long.parseLong(strGroupB.replaceAll("[^0-9]", ""));
            groupB = yuq.getGroups().get(groupBNUmber);
             if (groupB==null) throw new Exception();
        } catch (Exception e){return "输入的不是群号，或bot不在该群中";}
        ArrayList<String> sameMembersAvatars= new ArrayList();
        for(long qq: group.getMembers().keySet()){
            if (groupB.getMembers().get(qq)!= null){
                sameMembersAvatars.add(group.getMembers().get(qq).getAvatar());
            }
        }
        sameMembersAvatars.add(group.getBot().getAvatar());
        String toReturn = group.getName()+"共有"+(group.getMembers().size()+1)+"名成员\n"+
                groupB.getName()+"共有"+(groupB.getMembers().size()+1)+"名成员\n"+
                "两群共有"+(sameMembersAvatars.size()+1)+"名相同的群成员\n"+
                "以下是TA们的头像拼图\n";
        return new Message().plus(toReturn).plus(mif.imageByFile(generateAvatarPuzzle(sameMembersAvatars)));
    }

    @Action("ban {target} {timeMinute}")
    @Synonym("禁言 {target} {timeMinute}")
    public void banMinute(Member qq, long group, Member target, int timeMinute){
        if(qq.isAdmin()||botAdminList.contains(qq.getId())){
            try{
                target.ban((int)timeMinute*60);
            } catch (Exception e) {}
        }
        else{
            try{
                qq.ban((int)timeMinute*60);
            } catch (Exception e) {}
        }
    }

    @Action("#BanAll")
    @Synonym({"#打开全体禁言","#打开全员禁言"})
    public void banAll(Member qq, long group){
        if(qq.isAdmin()||botAdminList.contains(qq.getId())){
            try{
                yuq.getGroups().get(group).banAll();
            } catch (Exception e) {}
        }
        else{
            try{
                qq.ban(60*60);
                yuq.getGroups().get(group).sendMessage("拜托，你很弱耶，不能做这种事的啦");
            } catch (Exception e) {}
        }
    }

    //---------------------------------通用方法---------------------------------

    public File generateAvatarPuzzle(ArrayList<String> avatars){
        int width = (avatars.size()<7)?180*avatars.size():1080;
        int height = (1+(avatars.size()-1)/6)*180;
        BufferedImage paint = new BufferedImage(width,height , BufferedImage.TYPE_INT_RGB);
        //得到它的绘制环境(这张图片的笔)
        Graphics2D g2 = (Graphics2D) paint.getGraphics();
        //set background colod
        g2.setColor(new Color(253,208,0));
        g2.fillRect(0,0,width,height);
        //load avatars
        BufferedImage[] avatarBufferedImages = new BufferedImage[avatars.size()];
        GettingBufferedImageThread[] gettingBufferedImageThreads = new GettingBufferedImageThread[avatars.size()];
        for (int i = 0; i<avatars.size(); i++){
            gettingBufferedImageThreads[i] = new GettingBufferedImageThread(avatars.get(i), "getting avatar-"+i);
            gettingBufferedImageThreads[i].start();
        }
        for (int i = 0; i< gettingBufferedImageThreads.length; i++) {
            while (!gettingBufferedImageThreads[i].isReady()) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {}
            }
            avatarBufferedImages[i] = gettingBufferedImageThreads[i].getLoaded();
            gettingBufferedImageThreads[i].close();
        }
        try{
            g2.setColor(Color.WHITE);
            for(int i = 0; i<avatars.size(); i++) {
                BufferedImage avatar = avatarBufferedImages[i];
                int x = (i%6)*180;
                int y = (i/6)*180;
                g2.drawImage(avatar,x,y,180,180,null,null);
                //画边框
                int penSize = 5;
                g2.setStroke(new BasicStroke(penSize));
                g2.drawRoundRect(x+penSize,y+penSize,180-2*penSize,180-2*penSize,5,5);
            }
            File savedFile = new File (imagePath+"temp/ap_"+System.currentTimeMillis()+".png");
            ImageIO.write(paint, "png", savedFile);
            return savedFile;
        } catch (Exception e){return null;}
    }

    private void sendMessage(Group grpGroup, Object input){
        KeyboardToolBox.sendMessage(grpGroup, mif, input);
    }

    private void sendMessage(long group, Object input){
        KeyboardToolBox.sendMessage(group, yuq, mif, input);
    }
}

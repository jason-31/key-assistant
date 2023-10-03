package wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages;

import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Limited.*;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.Stages.Resident.One_One;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.BattleProcessor;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class StageProcessor {
    private String imagePath = ListAndAddressHandeler.getImagePath();
    private BattleProcessor battleProcessor;
    private Bank bank;
    private static StageProcessor _instance = null;

    public StageProcessor(){
        battleProcessor = BattleProcessor.getInstance();
        bank = Bank.getInstance();
    }


    public static StageProcessor getInstance(){
        if(_instance==null){
            _instance= new StageProcessor();
        }
        return _instance;
    }

    private Stage[] stages = {
            new One_One(),
            new NianShou()
    };

    public int getStageIndex(String stageName){
        try {
            return Integer.parseInt(stageName.replaceAll("\\s",""))-1;
        } catch (Exception e){}
        for(int i = 0; i<stages.length; i++){
            Stage stage = stages[i];
            if (stageName.equals(stage.getName())) return i;
        }
        return -1;
    }

    public Object battle (long group, long qq, String stageName, YuQ yuq, MessageItemFactory mif){
        return battle(group, qq, getStageIndex(stageName),yuq,mif);
    }

    public Object battle (long group, long qq, int stageIndex, YuQ yuq, MessageItemFactory mif){
        Member member = yuq.getGroups().get(group).getMembers().get(qq);
        if(stageIndex >= stages.length) return new Message().plus("副本不存在");
        Stage stage = stages[stageIndex];
        if(!stage.getActive(group, qq)) return new Message().plus("该副本暂时不可用");
        //打一架
        int scoreRange = stage.getMaxBlood(group, qq)*2;
        //之前造成的伤害
        int damaged = stage.getMaxBlood(group, qq)-stage.getLeftBlood(group,qq);
        //计算最终得分
        int score = damaged+battleProcessor.battle(group, qq,stage.getEquipments(),scoreRange);
        Object[] result = stage.getReward(group, qq, score);
        score=Math.max(0, score);
        score = Math.min(scoreRange,score);
        int dollar = (int)result[0];
        bank.addOrSpend(group,qq,dollar);
        try {
            int rawBattleScoreA = score*100/scoreRange;
            int scoreA = Math.max(0,rawBattleScoreA);
            int scoreB = 100-scoreA;
            Color clrWin = new Color(216,33,13);
            Color clrLose = new Color(0,98,150);
            Color colorA = (scoreA>=scoreB)?clrWin:clrLose;
            Color colorB = (colorA!=clrWin)?clrWin:clrLose;
            BufferedImage avatar1 = KeyboardToolBox.getBufferedImageFromUrl(member.getAvatar());
            BufferedImage avatar2 = KeyboardToolBox.convertFileToBufferedImage(stage.getAvatar());
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
            String text = score+"/"+scoreRange;
            g2.drawString(text,50+180+70,420);
            g2.setColor(colorB);
            FontMetrics metrics = g2.getFontMetrics(font);
            text = (scoreRange-score)+"/"+scoreRange;
            g2.drawString(text,50+180+60+700-metrics.stringWidth(text),420);
            //书写胜者
            g2.setColor(Color.RED);
            font = new Font("KaiTi",Font.BOLD,50);
            g2.setFont(font);
            metrics = g2.getFontMetrics(font);
            text = "恭喜：";
            g2.drawString(text,(paint.getWidth() - metrics.stringWidth(text)) / 2,metrics.getHeight()+10);
            text = (scoreA>scoreB)? member.nameCardOrName():stage.getName();
            g2.drawString(text,(paint.getWidth() - metrics.stringWidth(text)) / 2,metrics.getHeight()*2+40);
            File savedFile = new File (imagePath+"temp\\stg_"+System.currentTimeMillis()+".png");
            ImageIO.write(paint, "png", savedFile);
            return new Message().plus(mif.imageByFile(savedFile)).plus("您本次挑战副本获得了"+dollar+"金币"
                    +((result[1]==null)?"":"\n"+result[1]));
        } catch (Exception e){e.printStackTrace(); return "生成图片出错啦";}
    }

    public String stageList(long group, long qq){
        String string = "以下是目前可攻略的副本";
        for(int i = 0; i < stages.length; i++){
            Stage stage = stages[i];
            if (stage.getActive(group, qq)) string+="\n"+stage.getName()+" 编号："+ (i+1);
        }
        return string;
    }

    public String checkStage(long group, long qq, String stageName){
        return checkStage(group, qq, getStageIndex(stageName));
    }

    public String checkStage(long group, long qq, int stageIndex){
        if(stageIndex >= stages.length) return "副本不存在";
        Stage stage = stages[stageIndex];
        return "副本名称："+stage.getName()+
                "\n副本介绍："+stage.getDescription()+
                "\n血量剩余"+stage.getLeftBlood(group,qq)+"/"+stage.getMaxBlood(group, qq)+
                "\n开放状态："+((stage.getActive(group, qq))?"开放中":"未开放");
    }

}

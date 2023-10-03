package wiki.IceCream.yuq.demo.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.annotation.PrivateController;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import wiki.IceCream.yuq.demo.Keyboard.myClasses.GettingBufferedImageThread;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

@PrivateController
public class MyPrivateController {

    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    private final int exchangeRate = 62;
    private long nextWechatSendTime = 0L;
    String imagePath= ListAndAddressHandeler.getImagePath();

    //constructor
    public MyPrivateController() throws IOException, FileNotFoundException {
    }

    @Action("Nya")
    @Synonym("喵")
    public Object replayNya() {
        return "喵喵喵";
    }

    @Action("喵喵{text}")
    public Object nyaNyaReplay(String text){
        return text;
    }

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
        rate = (rate<50)? rate*10:rate;
        double rmbPrice=amount*rate/1000;
        return strRate+"汇对应为："+String.format("%.2f", rmbPrice);
    }

    @Action("生成群友头像拼图 {strGroupNumber}")
    public Object avatarPuzzle(String strGroupNumber){
        long groupNumber = 0;
        try {
            groupNumber = Long.parseLong(strGroupNumber);
        } catch(Exception e){return "输入错误";}
        Group group = yuq.getGroups().get(groupNumber);
        if(group==null) return "Bot不在该群中";
        ArrayList<String> avatars = new ArrayList<>();
        for (Member member : group.getMembers().values()){
            avatars.add(member.getAvatar());
        }
        avatars.add(group.getBot().getAvatar());
        File puzzle = generateAvatarPuzzle(avatars);
        if (puzzle==null){return "头像获取出错啦！";}
        return new Message().plus("以下是"+avatars.size()+"名群友的头像拼图：\n").plus(mif.imageByFile(puzzle));
    }

    @Action("检测群友重合度 {strGroupA} {strGroupB}")
    public Object detectSimilarity(String strGroupA, String strGroupB){
        long groupANumber = 0;
        long groupBNumber = 0;
        Group groupA, groupB;
        try {
            groupANumber = Long.parseLong(strGroupA.replaceAll("[^0-9]", ""));
            groupBNumber = Long.parseLong(strGroupB.replaceAll("[^0-9]", ""));
            groupA = yuq.getGroups().get(groupANumber);
            groupB = yuq.getGroups().get(groupBNumber);
            if (groupA==null||groupB==null) throw new Exception();
        } catch (Exception e){return "输入的不是群号，或bot不在该群中";}
        ArrayList<String> sameMembersAvatars= new ArrayList();
        for(long qq: groupA.getMembers().keySet()){
            if (groupB.getMembers().get(qq)!= null){
                sameMembersAvatars.add(groupA.getMembers().get(qq).getAvatar());
            }
        }
        sameMembersAvatars.add(groupA.getBot().getAvatar());
        String toReturn = groupA.getName()+"共有"+(groupA.getMembers().size()+1)+"名成员\n"+
                groupB.getName()+"共有"+(groupB.getMembers().size()+1)+"名成员\n"+
                "两群共有"+(sameMembersAvatars.size())+"名相同的群成员\n"+
                "以下是TA们的头像拼图\n";
        return new Message().plus(toReturn).plus(mif.imageByFile(generateAvatarPuzzle(sameMembersAvatars)));
    }

    @Action("菜单")
    @Synonym("menu")
    public Object menu(){
        return "现在支持的指令：\n"
                +"喵\n"
                +"Nya\n"
                +"喵喵{复读内容}\n"
                +"#我的权限(查看自己的权限）\n"
                +"#我的记录（查看自己的切煤记录）\n"
                +"#切煤+空格+链接或商品编号（请确认数字部分正确）";
    }

//



    //---------------------------------通用方法---------------------------------


    public String getDate(){
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return formatter.format(calendar.getTime());
    }

    public BufferedImage getBufferedImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        return bufferedImage;
    }

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
        int a = 0;
        for (GettingBufferedImageThread gettingBufferedImageThread : gettingBufferedImageThreads) {
            while (!gettingBufferedImageThread.isReady()) {
                try {
                    Thread.sleep(500);
                } catch (Exception ex) {}
            }
            avatarBufferedImages[a] = gettingBufferedImageThread.getLoaded();
            gettingBufferedImageThread.close();
            a++;
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
            File savedFile = new File (imagePath+"temp\\ap_"+System.currentTimeMillis()+".png");
            ImageIO.write(paint, "png", savedFile);
            return savedFile;
        } catch (Exception e){return null;}
    }


}
package wiki.IceCream.yuq.demo.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.controller.QQController;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.File;
import java.util.ArrayList;

@GroupController
public class InteractionController extends QQController {
    String imagePath = ListAndAddressHandeler.getImagePath();
    String voicePath = ListAndAddressHandeler.getVoicePath();

    @Action("抽东京塔挂")
    public Object tokyoTower(ContextSession session){
        String[] members = {"涩谷香音", "唐可可", "平安名堇", "岚千砂都", "叶月恋"};
        String[] boxes = {"\uD83D\uDC8C","\uD83D\uDC8C","\uD83D\uDC8C","\uD83D\uDC8C","\uD83D\uDC8C"};
        int keyAmount = (int)(Math.random()*5)+1;
        String boxesLeft = "";
        String keysLeft = "";
        for (int j = 0; j<5; j++){boxesLeft += (boxes[j]+(j+1)+" ");}
        for (int j = 0; j<keyAmount; j++){keysLeft +="\uD83D\uDD11 ";}
        reply("现在共有5个袋子\n" +
                boxesLeft +
                "\n您本次能开"+keyAmount+"个袋子\n" +
                keysLeft + "\n"+
                "您要先开第几个呢？");
        int a = keyAmount;
        for (int i = 0; i<a; i++){
            int index = -1;
            try{
                String reply = Message.Companion.firstString(session.waitNextMessage(10L*1000));
                index = Integer.parseInt(reply.replaceAll("[^0-9]",""))-1;
                if (!boxes[index].equals("\uD83D\uDC8C")) throw new Exception("Already Opened");
            }
            catch (WaitNextMessageTimeoutException e){return "超时未输入，游戏结束";}
            catch (Exception e){return "输入错误，游戏结束";}
            boxes[index] = "\uD83C\uDF89";
            keyAmount--;
            boxesLeft = "";
            keysLeft = "";
            for (int j = 0; j<5; j++){boxesLeft += (boxes[j]+(j+1)+" ");}
            for (int j = 0; j<keyAmount; j++){keysLeft +="\uD83D\uDD11 ";}
            int result = (int)(Math.random()*5);
            Message message = new Message().plus("您抽到了："+members[result]+"挂件\n");
            message.plus(mif.imageByFile(new File(imagePath+"tokyo_tower\\"+result+".png")));
            message.plus("\n现存的袋子还剩"+(4-i)+"个：\n"+boxesLeft+"\n现存的钥匙还剩"+keyAmount+"个\n"+((keyAmount!=0)?"：\n"+keysLeft+"还要继续吗？（如是：直接输入要开的编号，如不是请回复任意内容或直接忽略）":""));
            reply(message);
        }
        try{
            Thread.sleep(5000);
        } catch(Exception e){}
        return "游戏结束(＾Ｕ＾)ノ~ＹＯ";
    }

    @Action("真心话大冒险")
    public void truthOrDare(ContextSession session, long qq, long group){
        ArrayList<String> players = new ArrayList<>();
        players.add(yuq.getGroups().get(group).getMembers().get(qq).nameCardOrName());
        String[] truths = {"第二喜欢的Love Live成员是？","最初是因为什么开始喜欢Love Live的？","过去一周你吃了几碗米饭？","你和鲤鱼姐最大的相似点是？","你为什么现在没在学习/工作？",
        "摸鱼的时候最喜欢干什么？","你为什么喜欢/不喜欢吃米饭？","键盘好饿啊，你愿意请他吃顿饭吗？","过去一周你用小助手发病了几次？","现在的梦情是？","收到过最梦的梦情是？","迄今为止您遇到几个瓜人了？",
        "第三喜欢的Love Live成员是？","第四喜欢的Love Live成员是？","第五喜欢的Love Live成员是？","上次遇到瓜人是什么时候？","最欧的一次抽卡","最近打了什么其他音游吗？水平怎么样？",
        "印象最深的Love Live生放送","最近d的谷是？","如何邂逅的Love Live？","Love Live企划中最喜欢的部分","Love Live企划中最不喜欢的部分","对Love Live最喜欢的角色的第一印象是？",
        "除了Love Live以外最喜欢的动画片？","昨天睡了几小时？", "最喜欢的泡面口味", "生活中上一次的小感动", "最喜欢的电影"};
        String[] dares = {"发送“对小助手发病”", "做一件需要跨火盆的事", "发送手机里的最后一张图片", "发送“对嘉然发病”", "分享一首难过时听的歌","发一张谷美","拍一张离你最近的谷谷",
        "发送“对谷谷发病”", "去最近在玩的手游里抽一发吧","想不出来了，去玩真心话吧","想不出来了，去玩真心话吧"};
        //添加成员
        while (true){
            reply("要添加其他成员吗？（直接回复成员名字添加，回复否结束）");
            String reply = "";
            try{reply = Message.Companion.firstString(session.waitNextMessage(20L*1000));}
            catch (WaitNextMessageTimeoutException e){reply("超时未输入");break;}
            catch (Exception e){e.printStackTrace();}
            if(reply.equals("否")) break;
            else players.add(reply);
        }
        //游戏
        while (true){
            String reply = "抽到的成员为：" + players.get((int) (Math.random() * players.size())) +
                    "\n真心话题目为：" + truths[(int) (Math.random() * truths.length)] +
                    "\n大冒险题目为：" + dares[(int) (Math.random() * dares.length)] +
                    "\n如要继续请回复“下一轮”，如要结束请回复“结束”";
            boolean ifFailed = false;
            try {
                reply(reply);
            }
            catch (IllegalStateException illegalStateException){}
            catch (Exception e) { e.printStackTrace();
                System.out.println(e.getClass().toString());
                ifFailed = true;
            }
            if (!ifFailed) {
                long a = System.currentTimeMillis();
                while (System.currentTimeMillis() - a < 90 * 1000) {
                    try {
                        String response = Message.Companion.firstString(session.waitNextMessage(60 * 1000));
                        if(response.equals("结束")) {
                            reply("好的，游戏结束");
                            return;
                        }
                        if (response.contains("下一轮")) break;
                    }
                    catch(IllegalStateException e){}
                    catch (WaitNextMessageTimeoutException e){reply("超时未输入，游戏结束");return;}
                    catch (Exception e) {
                        reply("未知错误，游戏结束");
                        return;
                    }
                }
            }
        }
    }
    @Action("点歌")
    public Object playSong(ContextSession session){
        File folder = new File(voicePath+"liella music\\");
        File[] listOfFiles = folder.listFiles();
        //抽选4首歌
        ArrayList<File> pickedSongs = new ArrayList<>();
        for(int i=0; i<4;i++){
            while (true){
                File f = listOfFiles[(int)(Math.random()*listOfFiles.length)];
                if(!pickedSongs.contains(f)){
                    pickedSongs.add(f);
                    break;
                }
            }
        }
        String firstReply = "抽到的4首歌是：";
        int index = 1;
        for(File song : pickedSongs){
            firstReply+=("\n"+index+"- "+song.getName().substring(3).split(".mp3")[0]);
            index++;
        }
        firstReply+="\n请输入您要点的编号";
        int picked = 0;
        try {
            reply(firstReply);
            String response = Message.Companion.firstString(session.waitNextMessage(60 * 1000));
            picked = Integer.parseInt(response.replaceAll("[^0-9]",""));
            if (picked<1||picked>4) return("不在范围内");
        }catch (WaitNextMessageTimeoutException e){return "超时未输入";}
        catch (NumberFormatException numberFormatException){return "输入错误";}
        catch (IllegalStateException illegalStateException){}
        return new Message().plus(mif.voiceByFile(pickedSongs.get(picked-1)));
    }
}
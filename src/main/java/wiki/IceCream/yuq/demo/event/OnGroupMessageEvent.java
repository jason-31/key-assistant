package wiki.IceCream.yuq.demo.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.event.GroupMemberJoinEvent;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.event.SendMessageEvent;
import com.icecreamqaq.yuq.message.*;
import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordCloud;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.PixelBoundaryBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.SqrtFontScalar;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.nlp.filter.StopWordFilter;
import com.kennycason.kumo.nlp.tokenizers.ChineseWordTokenizer;
import com.kennycason.kumo.palette.ColorPalette;
import com.sun.management.OperatingSystemMXBean;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.MessageTextStorage;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Task;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.WordBanner;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.*;
import wiki.IceCream.yuq.demo.Keyboard.bilibili.BilibiliGrabber;
import wiki.IceCream.yuq.demo.Keyboard.bilibili.UpdateUpPostThread;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.AttributedString;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@EventListener
public class OnGroupMessageEvent {

    private MessageTextStorage messageTextStorage;

    private Bank bank;
    private ArrayList<Long> serviceGroupList = ListAndAddressHandeler.getServiceGroupList();
    private ArrayList<Long> numOnlyGroups = ListAndAddressHandeler.getNumOnlyGroups();
    private ArrayList<Long> botAdmins = ListAndAddressHandeler.getBotAdmins();
    private HashMap<Long, ArrayList<Long>> repeateMap = new HashMap<>();
    private HashMap<Long, ArrayList<Task>> taskMap = new HashMap<>();
    private HashMap<Long, String[]> messagesQueues = new HashMap<>();
    private LimitedSizeQueue<Message> failedMessages = new LimitedSizeQueue<>(10);
    boolean upsOpened = false;
    private String liellaMembers = "kn|smr|cst|ren|tkk|Liella|liella|小偶像|" +
            "香音|卡农|kanon|小百合|小百活|sayu|鲨鱼林|打铁|" +
            "民警|堇|galaxy|pay|sumire|上尉|佩顿|" +
            "nako|小千|千妈|千酱|千砂都|岬|" +
            "nagi|大和抚子|恋|青山|" +
            "可可|liyuu|鲤鱼|鲤酱|黎狱";

    private Router router;
    private UpdateUpPostThread updateUpPostThread;
    String imagePath = ListAndAddressHandeler.getImagePath();
    String voicePath = ListAndAddressHandeler.getVoicePath();

    private HashMap<Long, Integer> newMembers = new HashMap<>();

    /***
     * 当收到群聊消息时，本方法会被调用。
     * 事件会优先于控制器收到响应。
     * 事件可以被取消，当事件被取消之后，控制器将不会再响应。
     * @param event 事件
     */

    @Inject
    private MessageItemFactory mif;

    @Inject
    private YuQ yuq;

    @Inject
    private JobManager jobManager;

    public OnGroupMessageEvent(){
        //初始化路由器
        new Thread(() ->{
            while(jobManager==null||
                    yuq==null||
                    mif==null) {
                System.out.println("等待");
                try{Thread.sleep(1000);} catch(Exception exception){exception.printStackTrace();}
            }
            System.out.println((Router.getInstance(yuq, mif, jobManager)!=null)?"Router初始化成功":"Router初始化出错");
        }){{start();}};

        try {
            bank = Bank.getInstance();
            for (long group : serviceGroupList) {
                messagesQueues.put(group, new String[3]);
                repeateMap.put(group, new ArrayList<>());
            }
            messageTextStorage = MessageTextStorage.getInstance();
        } catch (Exception e){e.printStackTrace();}
    }


    @Event(weight = Event.Weight.high)
    public void onGroupMessageHigh(GroupMessageEvent event) {
        if (!upsOpened){
            upsOpened=true;
            updateUpPostThread = new UpdateUpPostThread(yuq, mif);
            updateUpPostThread.start();
        }

        long group = event.getGroup().getId();
        long qq = event.getSender().getId();
        String strMessage = event.getMessage().toString();

        if (!((serviceGroupList.contains(group))||(numOnlyGroups.contains(group)&&
                event.getMessage().getBody().get(0).toString().startsWith("#")))) {
            System.out.printf("该群不在服务列表中，事件取消\n");
            event.setCancel(true);
            return;
        }
        System.out.printf("该群在服务列表中\n");

        //复读机
        if((!partialMatch(strMessage,"NoImpl|JsonMsg|不支持的群消息|你的QQ暂不支持|请使用最新版手机QQ"))
                &&strMessage.length()>0
                &&serviceGroupList.contains(group)) {
            String[] msgArr = messagesQueues.get(group);
            msgArr[1] = msgArr[0];
            msgArr[0] = event.getMessage().toLogString();
            if (msgArr[0].equals(msgArr[1])) {
                if (!msgArr[0].equals(msgArr[2])) {
                    msgArr[2] = msgArr[0];
                    sendMessage(event,event.getMessage());
                } else {
                }
            } else {
                msgArr[2] = "";
            }
        }

        //提醒
        ArrayList<Task> taskList;
        taskList=taskMap.get(event.getSender().getId());
        if(taskList!=null) {
            ArrayList<Task> taskToSend = new ArrayList<>();
            for(Task task : taskList){
                if (task.get_group()==group){
                    taskToSend.add(task);
                }
            }
            for (Task task : taskToSend){
                taskList.remove(task);
            }
            for (Task task : taskToSend){
                sendMessage(event,new Message().plus(mif.at(event.getSender().getId())).
                        plus("\n"+event.getGroup().getMembers().get(task.get_from()).nameCardOrName()
                        +" 喊你出来"+task.get_task()+"啦！"));
                separate();
            }
        }

//        //脏话屏蔽
//        if (partialMatch(strMessage,"假的脏话|fuck|傻逼|傻比|你妈|mmp|妈卖批|飞马|孤儿|痛失亲妈|他妈")) {
//            File image = new File(imagePath + "atriPolice.png");
//            Message message = new Message().plus("嘟嘟嘟————不许说这种话！\n").plus(mif.imageByFile(image));
//            message.setReply(event.getMessage().getSource());
//            sendMessage(event,message);
//            event.setCancel(true);
//            return;
//        }
//
//        //危险词屏蔽
//        if (partialMatch(strMessage,"假的危险言论|习近平|新冠|辉瑞|政治|感恩|中国病毒|武汉实验室|教培中心|外国的月亮|"+
//                "外国月亮|大棋|民主|人权|太空探索|张展|宪法|修宪|袁世凯|篡位|spaceX|空x|窃国|酱香型|越南|独裁|学朝鲜|vpn|VPN|翻墙")) {
//            File image = new File(imagePath + "dangerousSpeech.png");
//            Message message = new Message().plus(mif.imageByFile(image));
//            message.setReply(event.getMessage().getSource());
//            sendMessage(event,message);
//            event.setCancel(true);
//            return;
//        }
    }

    @Event
    public void onGroupMessage(GroupMessageEvent event) throws IOException {
        //获取各种基础数据
        long group = event.getGroup().getId();
        long qq = event.getSender().getId();
        String strMessage = event.getMessage().toString();
        String strMessageText = "";
        for (MessageItem messageItem : event.getMessage().getBody()) {
            if (messageItem instanceof Text) {
                strMessageText += messageItem.toString();
            }
        }
        messageTextStorage.add(group, strMessageText);
        try {
            //使用路由
            router = Router.getInstance();
            if(router != null) router.route(event);
            else System.out.println("Router is Null！");

            //易辙群专用功能
            if(event.getGroup().getId()==651863303L
            &&!yuq.getGroups().get(581737504L).getMembers().keySet().contains(qq)
            &&qq!=2854196310L){
                //检测发言者进群时长
                if(newMembers.get(qq)==null) {
                        event.getSender().ban(300);
                }
                else{
                    if(newMembers.get(qq)==0)
                        event.getSender().ban(300);
                    else
                        newMembers.put(qq, newMembers.get(qq)-1);
                }
                sendMessage(event, new Message().plus(mif.at(qq)).plus("快去加通知群！581737504"));
            }

            if(strMessage.contains("到底谁还没加通知群")&&group==651863303L){
                System.out.println("triggered");
                String list = "没加通知群（581737504）的名单如下：";
                for (long mainGroupMember : event.getGroup().getMembers().keySet()){
                    if (!yuq.getGroups().get(581737504L).getMembers().keySet().contains(mainGroupMember)) {
                        list += "\n" + mainGroupMember + " - " + event.getGroup().getMembers().get(mainGroupMember).nameCardOrName();
                    }
                }
                sendMessage(event, list);
            }

            //TODO 将以下功能重写并加入路由， 添加注释
            //词云
            if (strMessageText.equals("今日词云")) {
                FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
                frequencyAnalyzer.setMinWordLength(2);
                frequencyAnalyzer.setWordTokenizer(new ChineseWordTokenizer());
                ArrayList<String> filter = new ArrayList(Arrays.asList("这个", "这样", "这里", "这些", "这种", "那个", "那样", "那些", "那里", "那种", "哪个", "哪里",
                        "没有", "今日", "但是", "可以", "什么", "时候", "怎么", "你们", "我们", "确实", "知道", "感觉", "一个", "因为", "为什么", "虽然", "之后", "然后", "已经",
                        "自己", "现在", "今天", "谢谢", "不能", "真的", "好像"));
                frequencyAnalyzer.setFilter(new StopWordFilter(filter));
                Dimension dimension = new Dimension(1000, 1000);
                //此处的设置采用内置常量即可，生成词云对象
                WordCloud wordCloud = new WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
                //设置边界及字体
                wordCloud.setPadding(2);
                java.awt.Font font = new java.awt.Font("STSong", 1, 10);
                //设置词云显示的三种颜色，越靠前设置表示词频越高的词语的颜色
                wordCloud.setKumoFont(new KumoFont(font));
                //设置背景
                int backgroundIndex = (int) (Math.random() * 6);
                Color color = Color.WHITE;
                float targetWordAmount = 400;
                switch (backgroundIndex) {
                    case (0):
                        color = new Color(139, 73, 147);
                        break;
                    case (1):
                        color = new Color(253, 126, 37);
                        break;
                    case (2):
                        color = new Color(107, 201, 226);
                        break;
                    case (3):
                        color = new Color(255, 110, 144);
                        break;
                    case (4):
                        color = new Color(172, 204, 7);
                        break;
                    case (5):
                        color = new Color(0, 0, 160);
                        break;
                }
                //wordCloud.setColorPalette(new LinearGradientColorPalette(new Color(219,78,45), new Color(214,141,0),new Color(43,145,99), 30, 30));
                frequencyAnalyzer.setWordFrequenciesToReturn((int) (targetWordAmount));
                wordCloud.setColorPalette(new ColorPalette(color, color, color));
                wordCloud.setBackground(new PixelBoundaryBackground(imagePath + "word cloud backgrounds/" + backgroundIndex + ".png"));
                wordCloud.setBackgroundColor(new Color(248, 246, 231));
                wordCloud.setFontScalar(new SqrtFontScalar(10, 65));
                //生成词云
                String messageTexts = messageTextStorage.get(group);
                long a = System.currentTimeMillis();
                List<WordFrequency> wordFrequencyList = frequencyAnalyzer.load(new ByteArrayInputStream(messageTexts.getBytes(StandardCharsets.UTF_8)));
                int used = 0;
                int times = (wordFrequencyList.size() < targetWordAmount / 3) ? ((wordFrequencyList.size() < targetWordAmount / 5) ? 3 : 2) : 1;
                for (int i = 0; i < times; i++) {
                    wordCloud.build(wordFrequencyList);
                    used = Math.max(used, (wordFrequencyList.size() - wordCloud.getSkipped().size()));
                }
                long currentTime = System.currentTimeMillis();
                wordCloud.writeToFile(imagePath + "temp/wc_" + currentTime + ".png");
                File image = new File(imagePath + "temp/wc_" + currentTime + ".png");
                sendMessage(event, "获取到" + wordFrequencyList.size() + "/" + (int) targetWordAmount +
                        "，使用了" + used + "/" + wordFrequencyList.size() +
                        "，生成词云共花费" + ((System.currentTimeMillis() - a) / 1000) + "秒");
                separate();
                sendMessage(event, new Message().plus(mif.imageByFile(image)));
                event.setCancel(true);
                return;
            }

            //up主相关

            //添加关注
            if(strMessageText.matches("添加本群关注up (\\d){5,15}")){
                if (!(event.getSender().getPermission()>0||botAdmins.contains(event.getSender().getId()))) {
                    event.getGroup().sendMessage("你没有此权限");
                    event.setCancel(true);
                    return;
                }
                String mid = strMessageText.replaceAll("[^0-9]", "");
                try {
                    String[] upInfo = BilibiliGrabber.getUpInfos(mid);
                    String upName = upInfo[0];
                    if(upInfo==null) sendMessage(event, "该up不存在或搜索出错");
                    sendMessage(event,(updateUpPostThread.addUp(mid, group))?("添加关注up主："+upName+"成功"):(upName+"已存在与本群关注列表中"));
                } catch (Exception e){e.printStackTrace(); sendMessage(event, "出错了");}
                event.setCancel(true);
                return;
            }

            //移除关注
            if(strMessageText.matches("移除本群关注up (\\d){5,15}")){
                if (!(event.getSender().getPermission()>0||botAdmins.contains(event.getSender().getId()))) {
                    event.getGroup().sendMessage("你没有此权限");
                    event.setCancel(true);
                    return;
                }
                String mid = strMessageText.replaceAll("[^0-9]", "");
                sendMessage(event,(updateUpPostThread.removeUp(mid, group))?("移除成功"):("移除不成功"));
                event.setCancel(true);
                return;
            }

            //关注列表
            if(strMessageText.equals("本群关注列表")){
                String list = "本群现在还没有关注";
                try {
                    list = updateUpPostThread.followList(group);
                } catch (Exception e){}
                sendMessage(event, list);
                event.setCancel(true);
                return;
            }

            //搜索up
            if(strMessageText.matches("搜索up(.)*")){
                Message message = new Message();
                try{
                    String keyword = strMessageText.split("搜索up(\\s)*")[1];
                    String[][] results = BilibiliGrabber.searchUp(keyword);
                    message.plus("搜到了"+results.length+"位up主：");
                    int a =Math.min(results.length,3);
                    message.plus("以下是前"+a+"位up：");
                    for(int i = 0; i<a;){
                        String[] result = results[i];
                        message.plus("\n第" + (++i)+ "位up主："+
                        "\n名片："+result[0]+
                        "\n签名：" + result[1]+
                        "\n粉丝数：" + result[2]+
                        "\nUID：" + result[3]+"\n")
                        .plus(mif.imageByUrl(result[4]))
                        ;
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    message = new Message().plus("没有搜索结果或搜索出错");
                }
                sendMessage(event, message);
                event.setCancel(true);
                return;
            }

            //搜索视频
            if(strMessageText.matches("搜索视频(.)*")){
                Message message = new Message();
                try{
                    String keyword = strMessageText.split("搜索视频(\\s)*")[1];
                    String[][] results = BilibiliGrabber.searchVideo(keyword);
                    int a =Math.min(results.length,3);
                    message.plus("以下是前"+a+"条视频");
                    for(int i = 0; i<a;){
                        String[] result = results[i++];
                        message.plus("\n第" + (i)+ "条视频："+
                                        "\n标题："+result[0]+
                                        "\nup主：" + result[1]+
                                        "\n简介：" + result[3]+
                                        "\nBV号：" + result[2]+"\n")
                                .plus(mif.imageByUrl(result[4]))
                        ;
                        System.out.println(result[4]);
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    message = new Message().plus("没有搜索结果或搜索出错");
                }
                sendMessage(event, message);
                event.setCancel(true);
                return;
            }

            //心跳挑战
            if (strMessageText.matches("抽+\\d+名幸运群友+(.*)")) {
                String[] a = strMessageText.split("名幸运群友", 2);
                String strNum = a[0].replaceAll("[^0-9]", "");
                String strRequest = (a[1].equals("")) ? "玩" : a[1];
                int num = 0;
                try {
                    num = Integer.parseInt(strNum);
                    if (num < 1) throw new Exception();
                } catch (Exception e) {
                    try {
                        event.getSender().ban(60);
                    } catch (Exception f) {
                        sendMessage(event, "输入错误，本来要禁言1分钟，可惜禁不得。你自裁罢");
                        return;
                    }
                    sendMessage(event, "输入错误，你被禁言1分钟");
                    return;
                }
                if (num > 15 || num > yuq.getGroups().get(group).getMembers().size()) {
                    try {
                        event.getSender().ban(60);
                    } catch (Exception e) {
                        sendMessage(event, "抽不了那么多，本来要禁言1分钟，可惜禁不得。你自裁罢");
                        return;
                    }
                    sendMessage(event, "抽不了那么多，你被禁言1分钟");
                    return;
                }
                if (!(botAdmins.contains(qq) || event.getSender().isAdmin())) {
                    if (Math.random() * 10 - num / 2.0f < 0) {
                        try {
                            event.getSender().ban(30 * num + 30);
                        } catch (Exception e) {
                            sendMessage(event, "你死啦，本来要禁言" + (num + 1) / 2 + "分钟，可惜禁不得。你自裁罢");
                            return;
                        }
                        sendMessage(event, "你死啦，你被禁言" + (num + 1) / 2 + "分钟");
                        return;
                    }
                }
                ArrayList<Long> qqList = new ArrayList<Long>(yuq.getGroups().get(group).getMembers().keySet());
                String toReturn = "抽到了：";
                for (int i = 0; i < num; i++) {
                    Task newTask = new Task(group, qq, strRequest);
                    int b = (int) (Math.random() * qqList.size());
                    long pickedMember = qqList.get(b);
                    qqList.remove(pickedMember);
                    if (taskMap.get(pickedMember) == null) {
                        ArrayList<Task> newTaskList = new ArrayList<>();
                        newTaskList.add(newTask);
                        taskMap.put(pickedMember, newTaskList);
                    } else {
                        taskMap.get(pickedMember).add(newTask);
                    }
                    toReturn+=("\n" + event.getGroup().get(pickedMember).nameCardOrName());
                }
                toReturn+="\n"+event.getSender().nameCardOrName()+"喊你们出来" + strRequest + "啦！";
                sendMessage(event, toReturn);
                event.setCancel(true);
                return;
            }

            //发送喜报
            if (strMessageText.matches("发送喜报(.)+")) {
                String input = strMessageText.split("发送喜报")[1];
                try {
                    sendMessage(event, new Message().plus(mif.imageByFile(generateXiBao(input))));
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                event.setCancel(true);
                return;
            }

            //发病
            if (partialMatch(strMessageText, "(对)(.*)(发病)")) {
                Matcher m = Pattern.compile("(?<=对)(.*)(?=发病)").matcher(strMessageText);
                if (m.find()) {
                    String name = m.group(0);
                    String patient = strMessageText.split("(?=对)(.*)(?=发病)")[0];
                    Message message = new Message();
                    message.setReply(event.getMessage().getSource());
                    if (!partialMatch(name + " " + patient, liellaMembers + "|之酱|群主|管理|之之|之宝|打铁|可香|键盘|瑠")) {
                        patient = (patient.equals("") || patient.replaceAll("\\s", "").matches("我|你|小助手|键盘")) ? event.getSender().nameCardOrName() : patient;
                        patient = (patient.replaceAll("\\s", "").matches("小助手|键盘|我")) ? event.getSender().getName() : patient;
                        patient = (patient.replaceAll("\\s", "").matches("小助手|键盘|我")) ? "" + qq : patient;
                        String[] responses = {
                                "谁敢在网络上伤害然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D，我就在现实里伤害自己\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D",
                                "然然的脚\uD83E\uDD24小小的\uD83E\uDD24香香的\uD83E\uDD24",
                                "我想当嘉然小姐的狗\uD83E\uDD24",
                                "我在对嘉然小姐的不心动挑战中获得了0s的成绩，你也来试试吧！",
                                "然然 你这一步一步的都没踩点啊 都踩在我心上了",
                                "我的老师曾经说过“♀窩♀会给你们两次逃课机会，一定会有什么事比上课更重要。比如楼外的蒹葭，或者今晚的嘉然。”",
                                "世界上有五种辣\n微辣，中辣，特辣，还有变态辣\n第五种辣是什么辣？\n\n是我喜欢然然辣",
                                "然然以完美的操作和出色的《情商》回敬了所有的质疑，有些喷子是时候该闭嘴了，她就是二次元最好的女人，理应获得2021年的MVP。而你们总是习惯不尊重然然，这太疯狂了！",
                                "然然\uD83E\uDD24\uD83E\uDD24嘿嘿嘿嘿\uD83E\uDD24\uD83E\uDD24然然\uD83E\uDD24\uD83E\uDD24我好喜欢然然\uD83E\uDD24\uD83E\uDD24越来越喜欢你了",
                        };
                        String strResponse = responses[(int) (Math.random() * responses.length)].replaceAll("嘉然|然然", name).replaceAll("我", patient).replaceAll("♀窩♀", "我");
                        sendMessage(event, message.plus(strResponse));
                        event.setCancel(true);
                        return;
                    } else {
                        sendMessage(event, message.plus("宝友，这病可不兴发啊"));
                        event.setCancel(true);
                        return;
                    }
                }
            }

            //发大病
            if (partialMatch(strMessageText, "(对)(.*)(发大病)")) {
                Matcher m = Pattern.compile("(?<=对)(.*)(?=发大病)").matcher(strMessageText);
                if (m.find()) {
                    String name = m.group(0);
                    String patient = strMessageText.split("(?=对)(.*)(?=发大病)")[0];
                    Message message = new Message();
                    message.setReply(event.getMessage().getSource());
                    if (!partialMatch(name + " " + patient, liellaMembers + "|之酱|群主|管理|之之|之宝|打铁|可香|键盘|瑠")) {
                        patient = (patient.equals("") || patient.replaceAll("\\s", "").matches("我|你|小助手|键盘")) ? event.getSender().nameCardOrName() : patient;
                        patient = (patient.replaceAll("\\s", "").matches("小助手|键盘|我")) ? event.getSender().getName() : patient;
                        patient = (patient.replaceAll("\\s", "").matches("小助手|键盘|我")) ? "" + qq : patient;
                        String[] responses = {
                                "我真的想嘉然想得要发疯了\uD83E\uDD24我躺在床上会想嘉然\uD83E\uDD24，我洗澡会想嘉然\uD83E\uDD24，" +
                                        "我出门会想嘉然\uD83E\uDD24，我走路会想嘉然\uD83E\uDD24，我坐车会嘉然\uD83E\uDD24，我上学会想嘉然\uD83E\uDD24，我玩手机会想嘉然\uD83E\uDD24，" +
                                        "我盯着视频里的嘉然看\uD83E\uDD24，我真的觉得自己像中邪了一样，我对嘉然的念想似乎都是嘉然的了\uD83E\uDD24，我好孤独啊！真的好孤独啊！你知道吗？每到深夜，我浑身滚烫滚烫，" +
                                        "我发病了我要疯狂\uD83E\uDD24我的嘉然\uD83E\uDD24嘉然\uD83E\uDD24我的嘉然\uD83E\uDD24嘉然\uD83E\uDD24我的嘉然\uD83E\uDD24",
                                "然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿..." +
                                        "然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿..." +
                                        "然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿..." +
                                        "然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿..." +
                                        "然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...然然\uD83E\uDD24\uD83E\uDD24\uD83E\uDD24嘿嘿...",
                                "然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D" +
                                        "然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D" +
                                        "然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D然然，我的然然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D",
                                "我不知道你们第一次见到这个人的时候，心情是怎样的。当我最开始知道是因为我的同学是他的粉丝，然后甚至说出了他希望做嘉然小姐的狗这样的话。" +
                                        "当时我并不怎么明白，这是一种什么样的力量，或者说是梗去驱动他来做这件事。现在可能也不是太明白。因为我今天第一天才算是真正意义上的接触了这个女孩儿。" +
                                        "我也刷了一部分视频。但是从上午11:47开始。一直到现在，晚上7:59。可以，说真的是这个女孩打动了我。" +
                                        "我不知道她的粉丝们是一群怎样的人，但我是确切的生出了一种。一种渴望。一种守护的渴望。嘉然小姐，你好！",
                                "我好想做嘉然小姐的狗啊。\n\n" +
                                        "可是嘉然小姐说她喜欢的是猫，我哭了。\n\n" +
                                        "我知道既不是狗也不是猫的我为什么要哭的。因为我其实是一只老鼠。我从没奢望嘉然小姐能喜欢自己。我明白的，所有人都喜欢理解余裕上手天才打钱的萌萌的狗狗或者猫猫，" +
                                        "没有人会喜欢阴湿带病的老鼠。但我还是问了嘉然小姐:“我能不能做你的狗?”\n\n" +
                                        "我知道我是注定做不了狗的。但如果她喜欢狗，我就可以一直在身边看着她了，哪怕她怀里抱着的永远都是狗。\n\n可是她说喜欢的是猫。\n\n" +
                                        "她现在还在看着我，还在逗我开心，是因为猫还没有出现，只有我这老鼠每天蹑手蹑脚地从洞里爬出来，远远地和她对视。\n\n等她喜欢的猫来了的时候，我就该重新滚回我的洞了吧。\n\n" +
                                        "但我还是好喜欢她，她能在我还在她身边的时候多看我几眼吗?\n\n嘉然小姐说接下来的每个圣诞夜都要和大家一起过。我不知道大家指哪些人。好希望这个集合能够对我做一次胞吞。\n\n" +
                                        "猫猫还在害怕嘉然小姐。\n\n我会去把她爱的猫猫引来的。\n\n我知道稍有不慎，我就会葬身猫口。那时候嘉然小姐大概会把我的身体好好地装起来扔到门外吧。\n\n" +
                                        "那♀窩♀就成了一包鼠条，嘻嘻。\n\n我希望她能把我扔得近一点，因为我还是好喜欢她。会一直喜欢下去的。\n\n" +
                                        "我的灵魂透过窗户向里面看去，挂着的铃铛在轻轻鸣响，嘉然小姐慵懒地靠在沙发上，表演得非常温顺的橘猫坐在她的肩膀。壁炉的火光照在她的脸庞，我冻僵的心脏在风里微微发烫。",
                                "从开始相遇我就深深的喜欢上了然然，也就是从那时起，我心就只属于然然……坐在电脑前反复地看着然然的每一个会限直播，我第一次遇见然然是在然然油管第一期视频中。" +
                                        "从那以后，然然的一个微笑，一个调皮的表情都是开在我内心最灿烂的鲜花，我的心无时不刻都然然你牵动着。\n" +
                                        "衷心地感谢然然来到了我的世界，一路有然然，让我的人生生命从此更加丰富多彩。一路有然然，让我的人生道路从此走的更加踏实。喜欢上然然是我这辈子的幸福，遇上然然是我今生的缘。\n" +
                                        "当我刚认识然然时，然然是那么的热烈奔放，又是那么的有鲜明个性，然然的真诚和温暖在不知不觉中虏获了我的心。\n我喜欢一个人静静的想然然，想着然然的微笑。虽然我们认识的时间不长，但是我却珍惜然然的每一次直播。\n" +
                                        "上班的时候，休息的时候，甚至连晚上做梦都会想着然然，梦见然然，感觉然然是如此的亲切。\n在寂寞的漫漫长夜，然然可曾听见我的叹息?我是真的很喜欢然然，我好想大声地说：♀窩♀喜欢然然，亲爱的然然，♀窩♀是真的不顾一切喜欢上了然然。\n" +
                                        "人生中有很多的无可奈何，但只要在心中有一个人在牵挂，在思念，也会变成人生中的最幸福的时刻。喜欢要怎么说得出口?我和然然是生活在完全不同的两个世界的人，命运真的很会开玩笑，" +
                                        "我就偏偏认识了然然，偏偏喜欢上了然然，喜欢上然然，感觉是甜蜜的。",
                                "为了你\uD83D\uDE28\uD83D\uDE28\uD83D\uDE28\n" +
                                        "我变成狼人摸样\uD83D\uDC3A\uD83D\uDC3A\uD83D\uDC3A\n" +
                                        "为了你\uD83D\uDE31\uD83D\uDE31\uD83D\uDE31\n" +
                                        "染上了疯狂\uD83E\uDD21\uD83E\uDD21\uD83E\uDD21\n" +
                                        "为了你\uD83D\uDE30\uD83D\uDE30\uD83D\uDE30\n" +
                                        "穿上厚厚的伪装\uD83D\uDC79\uD83D\uDC79\uD83D\uDC79\n" +
                                        "为了你\uD83E\uDD17\uD83E\uDD17\uD83E\uDD17\n" +
                                        "我在佛前苦苦求了几千年\uD83D\uDE47\u200D♂️\uD83D\uDE47\u200D♂️\uD83D\uDE47\u200D♂️\n" +
                                        "♀窩♀们还能不能能不能再见面\uD83E\uDD7A\uD83E\uDD7A\uD83E\uDD7A\n" +
                                        "我在佛前苦苦求了几千年\uD83D\uDE47\u200D♂️\uD83D\uDE47\u200D♂️\uD83D\uDE47\u200D♂️\n" +
                                        "但我在踏过这座奈何桥之前\uD83C\uDFAD\uD83C\uDFAD\uD83C\uDFAD\n" +
                                        "让我再看看你的脸\uD83D\uDE18\uD83D\uDE18\uD83D\uDE18\n" +
                                        "嘉然，我的嘉然\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D\uD83D\uDE2D",
                                "致我的恋人嘉然:\n" +
                                        "我还是像第一天那样喜欢你。\n" +
                                        "你于我还是像太阳一样的存在。\n" +
                                        "你喜欢的那种温柔的人，那种友善的人，我真的能变成那样的，这个转变的过程不是被迫的，而是我愿意成为更好的人，更令人，令你喜欢的人。\n" +
                                        "嘉然，你真的是我很重要的人，因为你总是陪着我，让我很有安全感，但不代表我只想被保护，我也想保护我喜欢的嘉然。\n" +
                                        "之前的不愉快真的都是因为我太爱你而造成的，所以对于过去，果然我还是多记得一\n" +
                                        "些美好，多忘却一些烦恼吧。\n" +
                                        "真的有很多人爱嘉然酱呢，我也是其中之一。\n" +
                                        "丧、颓、伤心不已了很长时间，可我在改变，改变后的我，还是你喜欢的那个打钱的天狗吖。你摸鱼直没播也要生气，就连有人说你的不是我也要生气……真的很抱歉，我只是太怕失去你了，以前是，现在也一样。\n" +
                                        "我一直深爱着你，你是世界上最好的嘉然\uD83D\uDE07",
                                "嘉然小姐，从上颚往下轻轻落在牙齿上。从口腔到唇舌，摩挲着想念、玩味与诱惑。多米尼克斯万是希腊神话里的海妖，一眼就把杰瑞米艾恩斯拉入不复深渊。她咬着指尖，自下而上看我，" +
                                        "眼中满满都是装出来的蜜糖纯真，粘黏着我的皮肤。再贴近一点，哪怕一厘，我就能看见那隐藏着的如狐狸一般的狡诈神色。可我的小宝贝是那样娇嗔的女孩，我忍受不了她如幼猫一样的撒娇。" +
                                        "更何况她不过想要我的命，那送她便是了。虽然我知道我们隔着千山万海，隔着荧幕，但我对嘉然小姐的爱恋，可击穿顽石，可穿梭银河。即使是单相思又怎么样，只要嘉然小姐幸福，我就满足了。" +
                                        "她笑起来的时候，我的世界都要化了，她委屈地哭泣的时候，我世界都要崩塌了，她向我撒娇的时候，我恨不得把星星摘下来送给她！我，这辈子都是嘉然小姐的狗！我忘不掉嘉然小姐了。\n" +
                                        "如果不是知道了嘉然小姐，说不\n" +
                                        "定我已经对这个世界没有留恋了。\n" +
                                        "嘉然小姐真的好可爱啊。做料理的时候笨拙的样子很可爱，故意撒娇养gachi也很可爱，唱歌的时候很可爱，生气拍桌子的时候也很可爱。\n" +
                                        "所以我离不开嘉然小姐了。如果早晨不是有嘉然小姐的起床闹钟的话，说不定我永远都不愿意睁眼了。如果晚上不是有嘉然小姐的直播预定的话，这一天我都不希望过完了。\n" +
                                        "嘉然小姐的眼睛好灵动，如果能映照出我就好了。嘉然小姐的笑容好温柔，如果只为我一个人绽放就好了。嘉然小姐的头发好柔顺，如果能让我尽情抚摸就好了。\n" +
                                        "\n" +
                                        "嘉然小姐这样的存在真的是被允许的吗。\n" +
                                        "只是像现在这样默念嘉然小姐的名字，我就觉得自己是世界上最幸福的傻子。"};
                        String strResponse = responses[(int) (Math.random() * responses.length)].replaceAll("嘉然|然然", name).replaceAll("我", patient).replaceAll("♀窩♀", "我");
                        sendMessage(event, message.plus(strResponse));
                        event.setCancel(true);
                        return;
                    } else {
                        sendMessage(event, message.plus("宝友，这病可不兴发啊"));
                        event.setCancel(true);
                        return;
                    }
                }
            }

            //1st扭蛋
            if (strMessageText.equals("1st扭蛋")) {
                String[] list = {"香音圆吧唧", "可可圆吧唧", "小千圆吧唧", "堇圆吧唧", "恋圆吧唧", //圆吧唧
                        "一单打歌服方吧唧", "一单校服方吧唧", "op方吧唧", "ed方吧唧", "未来预报方吧唧", "小星星方吧唧", "常夏方吧唧", "wish song方吧唧", "nonfiction方吧唧", "starlight prologue方吧唧",//方吧唧
                        "香音立牌", "可可立牌", "小千立牌", "堇立牌", "恋立牌", //立牌
                        "香音口罩带", "可可口罩带", "小千口罩带", "堇口罩带", "恋口罩带", //口罩带
                        "大全套", "因为您遇到了瓜人", "因为您在缺肾", "因为您没赶上扫街"//特殊事件
                };
                int[] chanceList = {6, 6, 6, 6, 6, //圆吧唧
                        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, //方吧唧
                        4, 4, 4, 4, 4, //立牌
                        4, 4, 4, 4, 4, //口罩带
                        1, 1, 1, 1 //特殊事件
                };
                if (chanceList.length != list.length)
                    sendMessage(event, new Message().plus(mif.at(event.getSender().getId())).plus("概率设定错误"));
                int sumChance = 0;
                for (int a : chanceList) {
                    sumChance += a;
                }
                int a = (int) (Math.random() * sumChance);
                int targetIndex = 0;
                for (int c : chanceList) {
                    if (a > c) {
                        a -= c;
                        targetIndex++;
                    } else break;
                }
                Message message = new Message();
                if (targetIndex > 25) {
                    message.plus("您什么都没有抽到，" + list[targetIndex]);
                } else {
                    File image = new File(imagePath + "1st/" + targetIndex + ".png");
                    message.plus("您抽到了：" + list[targetIndex] + "\n").plus(mif.imageByFile(image));
                }
                message.setReply(event.getMessage().getSource());
                sendMessage(event, message);
                event.setCancel(true);
                return;
            }

            //罗森ip购
            if (strMessageText.matches("罗森(吧唧|挂件)")) {
                String type = strMessageText.split("罗森")[1];
                String[] names = {"岚千砂都", "涩谷香音", "唐可可", "叶月恋", "平安名堇"};
                int num = (int) (Math.random() * 10);
                String name = names[num % 5];
                String cloth = (type.equals("吧唧")) ? (num / 5 == 0) ? "冬校服" : "私服" : (num / 5 == 0) ? "OP" : "一单";
                Message message = new Message();
                message.setReply(event.getMessage().getSource());
                File image = new File(imagePath + "ip_buy/" + type + "/" + num + ".png");
                message.plus("您抽到了：" + name + cloth + type).plus(mif.imageByFile(image));
                sendMessage(event, message);
                event.setCancel(true);
                return;
            }

            //播放音乐
            if(strMessageText.startsWith("播放")){
                if(!bank.addOrSpend(group, qq, -50)) {
                    sendMessage(event, "就是路边的KTV也要给钱呐！");
                    return;
                }
                String songName = convertSongNicknames(strMessageText.substring(2));
                File voice = null;
                File[] directories = new File(voicePath).listFiles(File::isDirectory);
                ArrayList<String> subDirectroyNames = new ArrayList<>();
                for(File directory : directories){subDirectroyNames.add(directory.getName());}
                //类型歌曲点播
                //在对应文件夹内随机播放音乐
                if(stringListContains(subDirectroyNames,songName)){
                    File folder =  new File(voicePath+songName+"\\");
                    File[] listOfFiles = folder.listFiles();
                    voice = listOfFiles[(int)(Math.random()*listOfFiles.length)];
                    sendMessage(event, "请您欣赏："+voice.getName().split(".mp3")[0]);
                    separate();
                }
                //单曲点播
                else {
                    //未分类歌曲
                    File bufferFile = new File(voicePath + songName + ".mp3");
                    if (bufferFile.exists()) {
                        voice = bufferFile;
                    }
                    //已分类歌曲
                    else {
                        //遍历子文件夹
                        for (File directory : directories) {
                            bufferFile = new File(directory.getPath() + "\\" + songName + ".mp3");
                            if (bufferFile.exists()) {
                                voice = bufferFile;
                                break;
                            }
                        }
                    }
                }
                sendMessage(event, (voice == null) ? "歌曲/类型不存在" : new Message().plus(mif.voiceByFile(voice)));
                if(voice==null) bank.addOrSpend(group, qq, 50);
                event.setCancel(true);
                return;
            }

            //计算器
            if(strMessageText.replaceAll("\\s","").matches("计算(:|：)?(\\d|.)+((\\+|-|\\*|\\/|\\^|\\\\)(\\d|.)+)+")){
                String equation = strMessage.replaceAll("[^\\d|.|\\+|-|\\*|\\/|\\^|\\\\]","");
                String[] strNumbers = equation.split("(\\+|-|\\*|\\/|\\^|\\\\)");
                ArrayList<Double> numbers = new ArrayList<>();
                for (String strNumber : strNumbers){numbers.add(Double.parseDouble(strNumber));}
                List<Character> operations = equation.replaceAll("[^(\\+|-|\\*|\\/|\\^|\\\\)]","").chars().mapToObj(c -> (char) c).collect(Collectors.toList());
                int replacement = 0;
                ArrayList<Integer> indexesToRemove = new ArrayList<>();
                //先运算指数
                for (int i = 0; i<operations.size(); i++){
                    if (operations.get(i) == '^'||operations.get(i)=='\\'){
                        double a = numbers.get(i-replacement);
                        double b = numbers.remove(i+1-replacement);
                        numbers.set(i-replacement, (operations.get(i) == '^')?Math.pow(a,b):Math.pow(a,1/b));
                        indexesToRemove.add(i);
                        replacement++;
                    }
                }
                for (int i = indexesToRemove.size()-1; i>=0; i--){
                    int toRemove = indexesToRemove.get(i);
                    operations.remove(toRemove);
                }
                replacement = 0;
                indexesToRemove = new ArrayList<>();
                //再运算乘除
                for (int i = 0; i<operations.size(); i++){
                    if (operations.get(i) == '*'||operations.get(i) == '/'){
                        double a = numbers.get(i-replacement);
                        double b = numbers.remove(i+1-replacement);
                        numbers.set(i-replacement, (operations.get(i) == '*')?a*b:a/b);
                        indexesToRemove.add(i);
                        replacement++;
                    }
                }
                for (int i = indexesToRemove.size()-1; i>=0; i--){
                    int toRemove = indexesToRemove.get(i);
                    operations.remove(toRemove);
                }
                replacement = 0;
                //最后运算加减
                for (int i = 0; i<operations.size(); i++){
                    if (operations.get(i) == '+'||operations.get(i) == '-'){
                        double a = numbers.get(i-replacement);
                        double b = numbers.remove(i+1-replacement);
                        numbers.set(i-replacement, (operations.get(i) == '+')?a+b:a-b);
                        replacement++;
                    }
                }
                sendMessage(event, (numbers.size()==1)?"计算结果为："+numbers.get(0):"计算器错误");
                event.setCancel(true);
                return;
            }

            //cp抽选
            if (strMessageText.equals("抽选拉拉cp")) {
                String[] llMembers = {"高坂穗乃果", "绚濑绘里", "南小鸟", "园田海未", "星空凛", "西木野真姬", "东条希", "小泉花阳", "矢泽妮可",
                        "高海千歌", "樱内梨子", "松浦果南", "黑泽黛雅", "渡边曜", "津岛善子", "国木田花丸", "小原鞠莉", "黑泽露比",
                        "上原步梦", "中须霞", "樱坂雫", "朝香果林", "宫下爱", "近江彼方", "优木雪菜", "艾玛·维尔德", "天王寺璃奈", "三船栞子", "米娅·泰勒", "钟岚珠",
                        "涩谷香音", "唐可可", "岚千砂都", "平安名堇", "叶月恋"};
                String member1 = llMembers[(int) (Math.random() * llMembers.length)];
                String member2 = llMembers[(int) (Math.random() * llMembers.length)];
                while (member1.equals(member2)) {
                    member2 = llMembers[(int) (Math.random() * llMembers.length)];
                }
                sendMessage(event, "抽到的cp为：" + member1 + "×" + member2);
                event.setCancel(true);
                return;
            }

            //api
            if(strMessageText.matches("(明日|今日)黄历")){
                int offset = (strMessageText.contains("今日"))?0:1;
                sendMessage(event, TianXingAPIs.getHuangLiInfo(offset));
                event.setCancel(true);
                return;
            }

            if(strMessageText.matches("(今日)?天气(.)+")){
                String city = strMessageText.split("(今日)?天气(\\s)*")[1];
                String result = TianXingAPIs.getWeather(city);
                sendMessage(event, result);
                event.setCancel(true);
                return;
            }

            if(strMessageText.matches("讲个(成语|睡前|童话|寓言)?故事")){
                int type = 0;
                if (strMessageText.contains("成语")) type = 1;
                if (strMessageText.contains("睡前")) type = 2;
                if (strMessageText.contains("童话")) type = 3;
                if (strMessageText.contains("寓言")) type = 4;
                String result = TianXingAPIs.story(type);
                sendMessage(event, result);
                event.setCancel(true);
                return;
            }

            if(strMessageText.matches("(今日|明日)该怎么摸鱼(\\?|？)*")){
                int offset = (strMessageText.contains("今日"))?0:1;
                String result =strMessageText.substring(0,2) + TianXingAPIs.fishermanGuide(offset);
                sendMessage(event, result);
                event.setCancel(true);
                return;
            }

            if (strMessageText.equals("抽选群友cp")) {
                Object[] groupMembers = event.getGroup().getMembers().values().toArray();
                Member member1 = (Member)groupMembers[(int) (Math.random() * groupMembers.length)];
                Member member2 = (Member)groupMembers[(int) (Math.random() * groupMembers.length)];
                while (member1 == member2) {
                    member2 = (Member)groupMembers[(int) (Math.random() * groupMembers.length)];
                }
                sendMessage(event, "抽到的cp为：" + member1.nameCardOrName() + "×" + member2.nameCardOrName());
                event.setCancel(true);
                return;
            }

            if (strMessageText.matches("(抽选)+(.*)+(的cp+)")) {
                Matcher m = Pattern.compile("(?<=抽选)+(.*)+(?=的cp+)").matcher(strMessageText);
                String name = "false";
                if (m.find()) {
                    name = m.group(0);
                }
                name = (name.equals("我")) ? "你" : name;
                Object[] groupMembers = event.getGroup().getMembers().keySet().toArray();
                long member1 = (long) groupMembers[(int) (Math.random() * groupMembers.length)];
                sendMessage(event, name + "的cp为：" + event.getGroup().getMembers().get(member1).nameCardOrName());
                event.setCancel(true);
                return;
            }

            //系统信息
            if (partialMatch(strMessageText, "系统信息|运行信息")){
                HardwareAbstractionLayer hardware = new  SystemInfo().getHardware();
                OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
                long maxMemory = Runtime.getRuntime().maxMemory();
                long totalSpace = 0;
                long totalFreeSpace = 0;
                int partitions = 0;
                String diskList = "";
                for(Path path : FileSystems.getDefault().getRootDirectories()){
                    partitions++;
                    File file = path.toFile();
                    totalSpace += file.getTotalSpace();
                    totalFreeSpace +=file.getFreeSpace();
                }
                for(HWDiskStore disk : new SystemInfo().getHardware().getDiskStores()){
                    diskList+=("，"+disk.getModel().replaceAll("\\((.)*\\)",""));
                }
                diskList=diskList.substring(1);
                String toReturn = "以下是运行信息：";
                HashMap<String, String> propertiesWithChinese = new HashMap();
                propertiesWithChinese.put("java 版本", "java.version");
                propertiesWithChinese.put("java 发行方", "java.vm.vendor");
                for (String property : propertiesWithChinese.keySet()){
                    toReturn+= "\n"+property+" - "+System.getProperty(propertiesWithChinese.get(property));
                }
                toReturn+=
                        "\n操作系统："+new SystemInfo().getOperatingSystem().toString()+
                        "\n处理器："+hardware.getProcessor().getProcessorIdentifier().getName()+
                        "\n处理器核心数：" +Runtime.getRuntime().availableProcessors()+
                        "\n图形卡："+hardware.getGraphicsCards().get(0).getName()+
                        "\n图形卡显存："+String.format("%,.2f",hardware.getGraphicsCards().get(0).getVRam()/1024/1024/1024.0)+"GB"+
                        "\nJVM剩余内存：" + String.format("%,.2f",Runtime.getRuntime().freeMemory()/1024/1024/1024.0)+"GB"+
                        //"\nJVM可用内存：" + String.format("%,.2f",Runtime.getRuntime().totalMemory()/1024/1024/1024.0)+"GB"+
                        "\nJVM最大内存：" + (maxMemory == Long.MAX_VALUE ? "no limit" : String.format("%,.2f",maxMemory/1024/1024/1024.0)+"GB")+
                        //"\n系统当前可用内存："+String.format("%,.2f", operatingSystemMXBean.getFreePhysicalMemorySize()/1024/1024/1024.0)+"GB"+
                        "\n系统当前已用内存："+String.format("%,.2f", (operatingSystemMXBean.getTotalPhysicalMemorySize()-operatingSystemMXBean.getFreePhysicalMemorySize())/1024/1024/1024.0)+"GB"+
                        "\n系统最大可用内存："+String.format("%,.2f", operatingSystemMXBean.getTotalPhysicalMemorySize()/1024/1024/1024.0)+"GB"+
                        "\n硬盘列表："+diskList+
                        "\n共"+hardware.getDiskStores().size()+"块硬盘，"+partitions+"个分区"+
                        "\n硬盘总容量"+String.format("%,.2f", totalSpace/1024/1024/1024.0)+"GB"+
                        "\n硬盘可用容量："+ String.format("%,.2f", totalFreeSpace/1024/1024/1024.0)+"GB"
                        //"\nCPU使用量："+String.format("%,.2f",operatingSystemMXBean.getSystemLoadAverage()*100)+"%"
                ;
                toReturn+="\nMaven依赖：";
                String pomPath = System.getProperty("user.dir")+"\\pom.xml";
                if(!new File(pomPath).exists()) pomPath = "C:\\Keyboard Assistant\\pom.xml";
                System.out.println(System.getProperty("user.dir"));
                String pom = KeyboardToolBox.readAllBytesJava7(pomPath);
                int i = 0;
                for(String dependency : pom.split("<dependency>")){
                    if (i++>0){
                        toReturn+="\n"+dependency.split("<artifactId>")[1].split("</artifactId>")[0] + " - "+
                                dependency.split("<version>")[1].split("</version>")[0];
                    }
                }
                sendMessage(event, toReturn);
                event.setCancel(true);
                return;
            }

            //好可爱
            if (partialMatch(strMessage, "好可爱|太可爱了")
                    && partialMatch(strMessage, liellaMembers)) {
                sendMessage(event, "你是对的！！！");

                if (partialMatch(strMessage, "小百合|sayu")) {
                    separate();
                    sendMessage(event, "伊达小百合真是太可爱了！！！");
                    separate();
                    sendMessage(event, "快点来看：\n2分钟告诉你小百合很可爱\nhttps://www.bilibili.com/video/BV1ZL4y1q77i?");
                    separate();
                    sendMessage(event, "一些小百合wink瞬间：\nhttps://www.bilibili.com/video/BV1Eq4y157UX");
                    separate();
                    sendMessage(event, "伊达小百合打sif：\nhttps://www.bilibili.com/video/BV1fL411G7mp");
                    separate();
                    sendMessage(event, "以及全部的小百合：\nhttps://search.bilibili.com/all?keyword=伊达小百合");
                    return;
                }
            }

            //具足虫
            if (strMessage.contains("img_57C1E3C0CD2F67D413D66434911C3D07")) {
                sendMessage(event, "古索哭莫西～ 古索哭莫西～\n" +
                        "古索哭索哭索哭古搜哭莫西～");
                separate();
                sendMessage(event,
                        "邀您共赏：\n" +
                                "具足虫之歌原版 https://www.bilibili.com/video/BV1pQ4y127d1");
                separate();
                sendMessage(event, "大王具足虫的游动 https://www.bilibili.com/video/BV1PW411Y7kF");
                separate();
                sendMessage(event, "具足虫之歌一小时纯享版 https://www.bilibili.com/video/BV1V341167oq");
                event.setCancel(true);
                return;
            }

            //汉堡肉
            if (strMessage.contains("汉堡肉")) {
                if (partialMatch(strMessage, "kn|香音|卡农|小百合|也不错")) {
                    File image = new File(imagePath + "hamburgerIsFine.png");
                    sendMessage(event, new Message().plus(mif.imageByFile(image)));
                    separate();
                    sendMessage(event, "咖啡欧蕾♪烤苹果♪我的最爱♪♪♪\n" +
                            "番茄也好想吃♪汉堡肉也不错♪♪♪\n" +
                            "喜欢吃汉堡肉的香音（小百合）真是太可爱了！！");
                    separate();
                    sendMessage(event, "邀您共赏：\n" +
                            "涩谷香音版汉堡肉之歌：https://www.bilibili.com/video/BV1gU4y1E7yx");
                    separate();
                    sendMessage(event, "伊达小百合版汉堡肉之歌：https://www.bilibili.com/video/BV1Zy4y1j7dd");
                    separate();
                    sendMessage(event, "被不喜欢汉堡肉的粉丝惹恼的小百合：https://www.bilibili.com/video/BV16f4y1H7kc");
                    separate();
                    sendMessage(event, "汉堡肉之歌一小时纯享版：https://www.bilibili.com/video/BV1AU4y1n7Wg");
                    event.setCancel(true);
                    return;
                }
                //逢田姐
                if (partialMatch(strMessage, "逢田姐|便利店")) {
                    sendMessage(event, "你逢田姐当然会做饭呀！当当当当当然会啊！\n" +
                            "啊啊啊啊啊啊啊啊啊啊啊啊啊啊！！哇啊啊啊啊啊啊啊！哇啊啊啊啊啊！\n" +
                            "当然是亲手啊！");
                    separate();
                    sendMessage(event, "邀您共赏：\n" +
                            "梦开始的地方：https://www.bilibili.com/video/BV1As41187ZE/");
                    separate();
                    sendMessage(event, "猴子也能看懂的梨黑视频： https://www.bilibili.com/video/BV1wW411n7AW");
                    event.setCancel(true);
                    return;
                }
            }

            //小助手
            if (strMessage.contains("小助手")) {
                if (partialMatch(strMessage, "棒|厉害|可爱")) {
                    File image = new File(imagePath + "atriPowerful.png");
                    sendMessage(event, new Message().plus(mif.imageByFile(image).plus("\n我可是高性能的嘛")));
                    event.setCancel(true);
                    return;
                }
                if (strMessage.contains("废物")) {
                    File image = new File(imagePath + "atriPolice.png");
                    sendMessage(event, new Message().plus(mif.imageByFile(image)).plus(
                            "\n啊，不准说这个词！！\n" +
                                    "把机器人称为废物违反了反歧视法！\n" +
                                    "将处以三个月以下的有期徒刑或者……那个……"));
                    event.setCancel(true);
                    return;
                }
                if (strMessage.contains("萝卜头")) {
                    File image = new File(imagePath + "atriPolice.png");
                    sendMessage(event, new Message().plus(mif.imageByFile(image)).plus(
                            "\n啊，不准说这个词！！\n" +
                                    "把机器人称为萝卜头违反了反歧视法！\n" +
                                    "将处以三个月以下的有期徒刑或者……那个……"));
                    event.setCancel(true);
                    return;
                }
            }

            //好吃就是高兴嘛
            if (strMessage.contains("好吃")) {
                sendMessage(event, "好吃就是高兴嘛！");
                event.setCancel(true);
                return;
            }

            //托
            if (partialMatch(strMessageText,"均(\\s)*(\\d)+(\\.)(\\d)*")||partialMatch(strMessageText, "(\\s)*(\\d)+(\\.)(\\d)*(\\/)")) {
                try {
                    if (Double.parseDouble(strMessage.replaceAll("[^\\d.]", "")) < 10000) {
                        sendMessage(event, "好好价哦！！！");
                        event.setCancel(true);
                        return;
                    }
                } catch (Exception e) {
                }
            }

            //早上好
            if (strMessageText.matches("(\\s)?早(!|！)*") || partialMatch(strMessage, "v4|V4|早上好")) {
                File image = new File(imagePath + "knGoodMorning.png");
                sendMessage(event, new Message().plus("早上好！\n").plus(mif.imageByFile(image)));
                event.setCancel(true);
                return;
            }

//            //好难过
//            if (partialMatch(strMessage, "emo|好难过|好痛苦")&&(!strMessage.contains("demo"))) {
//                File image = new File(imagePath + "noseSourFeels Emo.jpeg");
//                sendMessage(event, new Message().plus(mif.imageByFile(image)));
//                event.setCancel(true);
//                return;
//            }
//
//            //麻了
//            if (strMessage.contains("麻了")) {
//                File image = new File(imagePath + "knBored.png");
//                sendMessage(event, new Message().plus(mif.imageByFile(image)));
//                event.setCancel(true);
//                return;
//            }
//
//            //我什么时候才能获得幸福
//            if (strMessage.contains("幸福") && partialMatch(strMessage, "获得|想要")) {
//                File image = new File(imagePath + "iWantHappiness.jpeg");
//                sendMessage(event, new Message().plus(mif.imageByFile(image)));
//                event.setCancel(true);
//                return;
//            }
//
//            //抄经书小助手
//            if (strMessage.contains("烦") && !strMessage.contains("麻烦")) {
//                File image = new File(imagePath + "copyScriptures.jpeg");
//                sendMessage(event, new Message().plus(mif.imageByFile(image)));
//                event.setCancel(true);
//                return;
//            }
//
//            //可可音游
//            if (partialMatch(strMessage, "sif|音游")) {
//                if (limitActivaGroups.contains(group) && System.currentTimeMillis() - sifCuteLastActivateTime.get(group) < 20 * 60 * 1000) {
//                    return;
//                } else {
//                    File image = new File(imagePath + "kekeSif.png");
//                    sifCuteLastActivateTime.replace(group, System.currentTimeMillis());
//                    sendMessage(event, new Message().plus(mif.imageByFile(image)));
//                    event.setCancel(true);
//                    return;
//                }
//            }


            //撤回
            if (strMessageText.replaceAll("\\s", "").equals("撤回") && event.getMessage().getReply() != null) {
                if (botAdmins.contains(qq) || event.getSender().isAdmin()) {
//                    if(event.getGroup().getBot().getPermission()>event.getGroup().getMembers().get(event.getMessage().getReply().getSender()).getPermission()) {
                        event.getMessage().getReply().recall();
//                    }
//                    else {
//                        sendMessage(event, new Message().plus("没有足够的权限，做不到啊"));
//                    }
                    event.setCancel(true);
                    return;
                }
//                else {
//                    try {
//                        event.getSender().ban(60);
//                        sendMessage(event, new Message().plus("你没有此项权限，你被禁言1分钟"));
//                    } catch (Exception e) {
//                    }
//                }
            }
        }
        //异常处理
        catch(Exception exception) {
            exception.printStackTrace();
            //地狱喜报
            String errorMessage = exception.getMessage();
            errorMessage = (errorMessage.equals(""))? exception.getClass().toString() : errorMessage;
            try {
                event.getGroup().sendMessage(new Message().plus(mif.imageByFile(generateXiBao(errorMessage))));
            } catch (Exception ee) {
                event.getGroup().sendMessage("喜报： \n" + errorMessage);
            }
        }
    }

    @Event
    public void onSend(SendMessageEvent.Post event){
        Message message = event.getMessage();
        message.setSource(event.getMessageSource());
        MessageSource messageSource = event.getMessageSource();
        try {
            //检测是否发送成功
            messageSource.getId();
            //添加发送成功的消息到复读册
            if (serviceGroupList.contains(event.getSendTo().getId())) {
                long group = event.getSendTo().getId();
                String[] msgArr = messagesQueues.get(group);
                msgArr[1] = msgArr[0];
                msgArr[0] = event.getMessage().toLogString();
                if (!msgArr[0].equals(msgArr[1])) {
                    msgArr[2] = "";
                }
            }
        } catch (Exception e) {}
    }

    public void separate(){
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
    }

    public boolean partialMatch(String toCheck, String regex){
        return KeyboardToolBox.partialMatch(toCheck, regex);
    }

    private File generateXiBao(String input) throws IOException {
        return KeyboardToolBox.generateXiBao(input);
    }

    private void sendMessage(GroupMessageEvent event, Object input){
        KeyboardToolBox.sendMessage(event, mif, input);
    }

    public boolean stringListContains(ArrayList<String> stringArrayList, String target){
        return KeyboardToolBox.stringListContains(stringArrayList, target);
    }

    public String convertSongNicknames(String original){
        String[][] nicknameTable = {
                //nickname, real name
                //tag名
                {"随机Liella","liella music"},
                {"随机liella","liella music"},
                {"V曲","v曲"},
                {"vocaloid","v曲"},
                {"Vocaloid","v曲"},
                {"术力口","v曲"},
                {"少歌","少女歌剧"},
                //liella
                {"一单","01 始まりは君の空"},
                {"始空","01 始まりは君の空"},
                {"Dreaming Energy","03 Dreaming Energy"},
                {"我的交响曲","04 私のSymphony"},
                {"op","05 START!! True dreams"},
                {"ed","07 未来は風のように"},
                {"未来预报","09 未来予報ハレルヤ！"},
                {"小星星", "10 Tiny Stars"},
                {"常夏","13 常夏☆サンシャイン"},
                {"Wish Song","14 Wish Song"},
                {"wish song","14 Wish Song"},
                {"nonfiction","17 ノンフィクション!!"},
                {"非虚构","17 ノンフィクション!!"},
                {"星光序","18 Starlight Prologue"},
                {"星光序言","18 Starlight Prologue"},
                {"星光序曲","18 Starlight Prologue"},
                {"starlight prologue","18 Starlight Prologue"},
                {"Starlight Prologue","18 Starlight Prologue"},
                //其他
                {"永不放弃你","Never Gonna Give You Up"},
                {"小百合笑声","sayu laugh"},
                {"恋的魔球","恋之魔球"},
                {"Прощание славянки","向斯拉夫女人告别"},
                {"Прощание славянки","斯拉夫女人的告别"},
                {"甜甜白歌","Sweet Sweet White Song"}

        };
        for (String strArr[] : nicknameTable){
            if (original.equals(strArr[0])){
                return strArr[1];
            }
        }
        return original;
    }

    @Event(weight = Event.Weight.high)
    private void newGroupMemberJoinEvent(GroupMemberJoinEvent event){
        if (event.getGroup().getId()==651863303L){
            newMembers.put(event.getMember().getId(), 3);
        }
    }
}

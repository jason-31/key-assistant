package wiki.IceCream.yuq.demo.job;

import com.IceCreamQAQ.Yu.annotation.Cron;
import com.IceCreamQAQ.Yu.annotation.JobCenter;
import com.IceCreamQAQ.Yu.util.DateUtil;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.message.Message;
import java.io.File;

import javax.inject.Inject;

import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.MessageTextStorage;
import org.apache.commons.io.FileUtils;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Battle.BattleProcessor;
import wiki.IceCream.yuq.demo.Keyboard.mercari.MercariInfoGetter;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.other.PoHai;


@JobCenter
public class JobMain {


    private long[] v4GreetingGroups = {716878201L,651863303L,926754397L};
    private MessageTextStorage messageTextStorage;
    private MercariInfoGetter mercariInfoGetter;
    private BattleProcessor battleProcessor;

    @Inject
    private DateUtil dateUtil;

    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    public JobMain(){
        mercariInfoGetter = MercariInfoGetter.getInstance();
        messageTextStorage = MessageTextStorage.getInstance();
        battleProcessor = BattleProcessor.getInstance();
    }

    @Cron("At::d::7:00")
    public void v4Greeting() {
        Message message = new Message().plus("v4 v4 v4！！！\n七点了大家起床了吗");
        for (long group : v4GreetingGroups) {
            yuq.getGroups().get(group).sendMessage(message);
            try{Thread.sleep(5000);} catch(Exception e){}
        }
    }

    @Cron("At::d::16:30")
    public void clearBattlle1(){
        battleProcessor.clearHistory();
    }

    @Cron("At::d::0:30")
    public void clearBattlle2(){
        battleProcessor.clearHistory();
    }

    //晚安提醒
    @Cron("At::d::22:30")
    public void goodNight(){
        yuq.getGroups().get(651863303L).sendMessage(new Message().plus(mif.imageByFile(new File(ListAndAddressHandeler.getImagePath()+"/keke_goodnight.jpeg"))).plus("该睡觉啦！"));
    }

    //迫害启动
    @Cron("At::d::0:00")
    public void initialziePoHai(){
        PoHai pohai = (PoHai) Router.getInstance().findFunction("po-hai");
        pohai.initializeSucker();
    }
}

package wiki.IceCream.yuq.demo.Keyboard.router.functions.working;

import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.WorkProcessor;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class StartWork extends Function {
    WorkProcessor workProcessor;

    public StartWork(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        super("打工",
                "start-work",
                "命令：”打工“\n" +
                        "现在不努力工作的话，将来你吃什么？",
                1);
        workProcessor = WorkProcessor.getInstance(yuq, mif, jobManager);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        sendMessage(event,workProcessor.work(event.getGroup(), event.getSender()));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.equals("打工");
    }
}

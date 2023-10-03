package wiki.IceCream.yuq.demo.Keyboard.router.functions.working;

import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.WorkProcessor;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class CheckMyWork extends Function {
    WorkProcessor workProcessor;

    public CheckMyWork(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        super("我的工作",
                "check-my-work",
                "命令：”我的工作“\n" +
                        "没在打工的话记得赶紧上工！",
                1);
        workProcessor = WorkProcessor.getInstance(yuq, mif, jobManager);
    }
    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        sendMessage(event, workProcessor.myWork(group,qq));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.equals("我的工作");
    }
}

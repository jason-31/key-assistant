package wiki.IceCream.yuq.demo.Keyboard.router.functions.working;

import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.WorkProcessor;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

public class CancelWork extends Function {

    WorkProcessor workProcessor;

    public CancelWork(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        super("取消打工",
                "cancel-work",
                "命令：”取消打工“\n" +
                        "现在不工作的话，你将来吃什么？",
                1);
        workProcessor = WorkProcessor.getInstance(yuq, mif, jobManager);
    }
    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        sendMessage(event,workProcessor.cancelWork(group, qq));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return strMessageText.equals("取消打工");
    }
}

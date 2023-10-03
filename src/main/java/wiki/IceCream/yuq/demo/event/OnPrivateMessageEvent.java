package wiki.IceCream.yuq.demo.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Friend;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.event.PrivateMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItem;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import com.icecreamqaq.yuq.message.Text;

import javax.inject.Inject;

@EventListener
public class OnPrivateMessageEvent {

    @Inject
    private MessageItemFactory mif;

    @Inject
    private YuQ yuq;

    @Event(weight = Event.Weight.high)
    public void onPrivateEvent(PrivateMessageEvent event) {
        if (event.getSender() instanceof Friend) return;
        //取消事件
        event.setCancel(true);
        System.out.println("不是好友，事件取消");
//        //公开处刑
//        if (event.getSender() instanceof Member ){
//            Member member = (Member) event.getSender();
//            String strMessageText = "";
//                for(MessageItem messageItem : event.getMessage().getBody()){
//                if(messageItem instanceof Text) strMessageText+=messageItem.toString();
//            }
//            try{
//                member.getGroup().sendMessage(new Message().plus(mif.at(member)).
//                        plus(" 又在给小助手发临时消息了"+(strMessageText.length()==0?"":"\n消息内容为：\n"+strMessageText)));
//            } catch (Exception e){}
//        }
    }
}

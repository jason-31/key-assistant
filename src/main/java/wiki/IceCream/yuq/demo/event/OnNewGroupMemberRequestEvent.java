package wiki.IceCream.yuq.demo.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupMemberRequestEvent;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import javax.inject.Inject;


@EventListener
public class OnNewGroupMemberRequestEvent {

    @Inject
    private MessageItemFactory mif;

    @Inject
    private YuQ yuq;

    @Event
    public void requestHandler(GroupMemberRequestEvent event) {
        long groupID = event.getGroup().getId();
        String strMessage = event.getMessage();
        if(ListAndAddressHandeler.getServiceGroupList().contains(event.getGroup().getId()))
            event.getGroup().sendMessage("有新成员申请啦！\n昵称为："+event.getQq().getName()+"\nQQ号为："+event.getQq().getId()+"\n验证消息为：\n"+strMessage);
        if (groupID == 716878201L) {
            String[] answers = {"可爱","声","音","活","身高","笑容 ","刘海","脸","8","汉堡肉","鱼"};
            for (String answer : answers){
                if (strMessage.contains(answer)) {
                    event.setAccept(true);
                    event.setCancel(true);
                    return;
                }
            }
        }
        if(groupID==743186065){
            event.setAccept(true);
            event.setCancel(true);
            return;
        }
    }
}

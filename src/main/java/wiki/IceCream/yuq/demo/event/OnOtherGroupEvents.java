package wiki.IceCream.yuq.demo.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupBanMemberEvent;
import com.icecreamqaq.yuq.event.GroupMemberLeaveEvent;
import com.icecreamqaq.yuq.event.GroupUnBanMemberEvent;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import javax.inject.Inject;

@EventListener
public class OnOtherGroupEvents {
    @Inject
    private MessageItemFactory mif;

    @Inject
    private YuQ yuq;

    @Event
    public void onGroupMemberLeaveEvent(GroupMemberLeaveEvent event){
        if(ListAndAddressHandeler.getServiceGroupList().contains(event.getGroup().getId()))
            event.getGroup().sendMessage(event.getMember().nameCardOrName()+"咋就退群了嘞");
        event.setCancel(true);
    }

    @Event
    public void onGroupBanMemberEvent(GroupBanMemberEvent event){
        if(ListAndAddressHandeler.getServiceGroupList().contains(event.getGroup().getId()))
            event.getGroup().sendMessage(event.getMember().nameCardOrName()+"好惨啊，被"+event.getOperator().nameCardOrName()+"禁言了"+event.getTime()/60+"分钟耶");
    }

    @Event
    public void onGroupUnBanMemberEvent(GroupUnBanMemberEvent event){
        if(ListAndAddressHandeler.getServiceGroupList().contains(event.getGroup().getId()))
            event.getGroup().sendMessage(event.getMember().nameCardOrName()+"好好运哦，被"+event.getOperator().nameCardOrName()+"放出来啦！");
    }

}

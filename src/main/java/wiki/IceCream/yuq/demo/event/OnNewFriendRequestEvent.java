package wiki.IceCream.yuq.demo.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupInviteEvent;
import com.icecreamqaq.yuq.event.GroupMemberJoinEvent;
import com.icecreamqaq.yuq.event.NewFriendRequestEvent;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItem;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import javax.inject.Inject;

@EventListener
public class OnNewFriendRequestEvent {

    /***
     * 事件的注册，并不会限制你在某个类去注册，只要你的类标记了 EventListener_ 注解。
     *
     * NewFriendRequestEvent 事件
     * 当有新的好友申请的时候，会触发本事件。
     * 如果您将事件的 accept 属性设置为 true，并同时取消了事件，那么将同意好友请求。
     * 否则将忽略（不进行任何处理）这个好友请求。
     */
    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    @Event
    public void newFriendRequestEvent(NewFriendRequestEvent event) {
        event.setAccept(true);
        event.setCancel(true);
    }

    @Event
    public void newGroupInviteEvent(GroupInviteEvent event){
        event.setAccept(ListAndAddressHandeler.getBotAdmins().contains(event.getQq().getId())||
                ListAndAddressHandeler.getServiceGroupList().contains(event.getGroup().getId()));
        event.setCancel(true);
    }
}

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
import java.io.File;

@EventListener
public class OnGroupMemberJoinEvent {

    @Inject
    private YuQ yuq;

    @Inject
    private MessageItemFactory mif;

    private String imagePath = ListAndAddressHandeler.getImagePath()+"\\";

    @Event
    public void newGroupMemberJoinEvent(GroupMemberJoinEvent event){
        long qq = event.getMember().getId();
        long group = event.getGroup().getId();
        if (group==479955083L)
            event.getGroup().sendMessage(new Message().plus(mif.at(qq)).plus("欢迎新妈咪，请将群名片改为cn+推♪。进群可以看精华消息和群相册群规♪"));
        if (group==716878201L||group==743186065L) {
            File image = new File(imagePath+"sayuriWelecome.png");
            String id = yuq.getFriends().get(1726924001L).uploadImage(image).getId();
            event.getGroup().sendMessage(new Message().plus(mif.at(qq)).plus("\n欢迎到来~麻烦改一下群昵称方便大家称呼和认识~祝您玩得愉快~\n【请务必看看群公告里的所有公告】"));
            try{Thread.sleep(5000);} catch (Exception e) {}
            event.getGroup().sendMessage(new Message().plus(mif.imageById(id)));
        }
        if(group==545993596L)
            event.getGroup().sendMessage(new Message().plus(mif.at(qq)).plus("欢迎新妈咪，排无料和其他商品麻烦看看群公告♪"));
    }
}

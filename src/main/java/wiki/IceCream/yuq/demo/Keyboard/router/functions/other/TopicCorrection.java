package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.Message;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.WordBanner;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

import java.util.ArrayList;
import java.util.HashMap;

public class TopicCorrection extends Function {
    private HashMap<Long, ArrayList> groupWordBanned = null;

    public TopicCorrection(){
        super("话题矫正",
                "topic-correction",
                "命令：（数字）（分钟/小时）内禁止说（内容，支持正则）" +
                        "\n例：1小时内禁止说嘉然可爱捏",
                1);
        groupWordBanned = new HashMap<>();
        for (long group : ListAndAddressHandeler.getServiceGroupList()) {
            groupWordBanned.put(group, new ArrayList());
        }
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        //话题矫正
        if (event.getGroup().getBot().getPermission() > 0) {
            long currentTime = System.currentTimeMillis();
            ArrayList<WordBanner> banned = groupWordBanned.get(group);
            for (WordBanner wordBanner : banned) {
                if (currentTime <= wordBanner.getEndTime()) {
                    if (partialMatch(strMessageText, wordBanner.getWord())) {
                        if (event.getGroup().getBot().getPermission() > event.getSender().getPermission()) {
                            try {
                                event.getSender().ban(wordBanner.getBanLength() * 60);
                                event.setCancel(true);
                                return;
                            } catch (Exception e) {
                                Message message = new Message().plus("虽然因为" + e.getMessage() + "禁言不了你，但请你不要说“" + wordBanner.getWord() + "”了");
                                message.setReply(event.getMessage().getSource());
                                sendMessage(event, message);

                            }
                        } else {
                            Message message = new Message().plus("因为权限不够禁言不了你，但请你不要说“" + wordBanner.getWord() + "”了\n按理说应该禁言" + wordBanner.getBanLength() + "分钟，请你自裁");
                            message.setReply(event.getMessage().getSource());
                            sendMessage(event, message);
                        }
                        event.setCancel(true);
                        return;
                    }
                } else {
                    banned.remove(wordBanner);
                }
            }
        }

        //添加话题矫正
        if (strMessageText.matches("(\\d*)(分钟|小时)(内禁止说)(.)*")
                && KeyboardToolBox.getAdminLevel(event)>1) {
            int banLength = Integer.parseInt
                    (
                        strMessageText.split("禁止说")[0]
                        .replaceAll("[^0-9]", "")
                    )
                    *
                    ((strMessageText.split("禁止说")[0].contains("小时")) ? 60 : 1);
            String word = strMessageText.split("禁止说")[1];
            groupWordBanned.get(group).add(new WordBanner(word, banLength));
            sendMessage(event, "已添加禁词“" + word + "”,时长" + banLength + "分钟");
            event.setCancel(true);
            return;
        }
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return true;
    }

    @Override
    public boolean getIfEnd(){return false;}
}

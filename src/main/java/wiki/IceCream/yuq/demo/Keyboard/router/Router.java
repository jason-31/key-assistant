package wiki.IceCream.yuq.demo.Keyboard.router;

import com.IceCreamQAQ.Yu.job.JobManager;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.MessageItem;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import com.icecreamqaq.yuq.message.Text;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.bank.AddMoney;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.other.*;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.bank.CheckMyBalance;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.bank.SubtractMoney;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.working.CancelWork;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.working.CheckMyWork;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.working.StartWork;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

public class Router {
    private MessageItemFactory _mif;
    private YuQ _yuq;
    private JobManager _jobManager;
    private static Router _instant = null;
    private UltimateInfoStorage _ultimateInfoStroage;
    Function[] functions;


    public static Router getInstance(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        if (_instant==null)
            _instant = new Router(yuq, mif, jobManager);
        System.out.println(_instant);
        return _instant;
    }

    public static Router getInstance(){
        return _instant;
    }

    private Router(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        try {
            _ultimateInfoStroage = UltimateInfoStorage.getInstance();
            _yuq = yuq;
            _mif = mif;
            _jobManager = jobManager;
            //write an array of all functions here
            functions = new Function[]{
                    new TopicCorrection(),
                    new NyaNyaRepeat(),
                    new AddToCustomizedRouter(),
                    new CustomizedRouter(),
                    new ChangeAvailability(),
                    new GetFunctionList(),
                    new GetFunctionDescription(),
                    new Divination(),
                    new StartWork(yuq, mif, jobManager),
                    new CancelWork(yuq, mif, jobManager),
                    new CheckMyWork(yuq, mif, jobManager),
                    new CheckMyBalance(),
                    new PoHai(),
                    new AddMoney(),
                    new SubtractMoney(),
                    new ChoiceAssistant(),
                    new PickOne(),
                    new TrickOrTreat(),
                    new SetSpecialAdminLevel(),
                    new TalkToChatGPT(),
                    new KidneyCollector()
            };
            long[] activeGroups = _yuq.getGroups().keySet().stream().mapToLong(Long::longValue).toArray();
            _ultimateInfoStroage.deleteInactiveGroups(activeGroups);
        }catch (Exception e){e.printStackTrace();}
    }

    public void route(GroupMessageEvent event){
        String strMessageText = messageToString(event);
        for (Function function : functions){
            if(function.checkIfRun(event.getGroup().getId(), event.getSender().getId(), strMessageText, event)){
                function.action(event.getGroup().getId(), event.getSender().getId(), strMessageText, event);
                if(function.getIfEnd()||event.cancel) {
                    event.setCancel(true);
                    return;
                }
            }
        }
    }

    public Function[] getFunctionList(){
        return functions;
    }

    public JobManager getJobmanager(){return _jobManager;}

    public MessageItemFactory getMif(){return _mif;}

    public YuQ getYuq(){return _yuq;}

    public Function findFunction(String nameOrId){
        for (Function function : functions){
            if (function.getName().equals(nameOrId)||function.getId().equals(nameOrId))
                return function;
        }
        return null;
    }
    private String messageToString(GroupMessageEvent event){
        String strMessageText = "";
        for (MessageItem messageItem : event.getMessage().getBody()) {
            if (messageItem instanceof Text)
                strMessageText += messageItem.toString();
            if (messageItem instanceof At)
                strMessageText += " At_"+((At) messageItem).getUser()+" ";
        }
        return strMessageText;
    }

}

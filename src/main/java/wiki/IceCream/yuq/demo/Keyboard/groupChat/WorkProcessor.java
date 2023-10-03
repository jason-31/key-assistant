package wiki.IceCream.yuq.demo.Keyboard.groupChat;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import com.IceCreamQAQ.Yu.job.JobManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.Bank.Bank;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.Job;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TimeZone;

public class WorkProcessor {

    private MessageItemFactory _mif;
    private YuQ _yuq;
    private JobManager _jobManager;
    private Bank bank;
    private HashMap<Long, HashMap<Long, Job>> currentWorkersMap;
    private String path = ListAndAddressHandeler.getDependenciesFolderPath()+"infos/jobs.json";
    private static WorkProcessor _instance = null;

    public WorkProcessor(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        try {
            _yuq = yuq;
            _mif = mif;
            _jobManager = jobManager;
            //initialize the map
            currentWorkersMap = new HashMap<>();
            for (long group : ListAndAddressHandeler.getServiceGroupList()) {
                currentWorkersMap.put(group, new HashMap<Long, Job>());
            }
            bank = Bank.getInstance();
            String json = KeyboardToolBox.readAllBytesJava7(path);
            JSONObject root = JSON.parseObject(json);
            JSONArray groups = root.getJSONArray("groups");
            for (int i = 0; i < groups.size(); i++){
                JSONObject group = groups.getJSONObject(i);
                long groupId = group.getLong("id");
                JSONArray jobs = group.getJSONArray("jobs");
                for (int j = 0; j<jobs.size(); j++) {
                    JSONObject jobJsonObject = jobs.getJSONObject(j);
                    long endTime = jobJsonObject.getLong("end_time");
                    long qq = jobJsonObject.getLong("qq");
                    int earn = jobJsonObject.getInteger("earn");
                    System.out.println("添加打工：\nqq: "+qq+"\n group: "+groupId);
                    currentWorkersMap.get(groupId).put(qq, new Job(endTime, earn, qq, groupId));
                }
            }
            //添加所有群聊
            for (long group : ListAndAddressHandeler.getServiceGroupList()){
                if (currentWorkersMap.get(group)==null){
                    currentWorkersMap.put(group, new HashMap<>());
                }
            }
            //拿到jobmanager后遍历jobs，如果完成的结算，没有完成的添加任务
            new Thread(() ->{
                int i = 0;
                while(_jobManager ==null){System.out.println("等待"+i++);try{Thread.sleep(1000);} catch(Exception exception){}}
                for (HashMap<Long, Job> groupJobs : currentWorkersMap.values()){
                    long g = 0;
                    //添加要移除的任务
                    ArrayList<Long> toRemoveIds = new ArrayList<>();
                    for (Job job : groupJobs.values()) {
                        long qq = job._qq;
                        long group = job._group;
                        g=group;
                        int earn = job._earn;
                        if (job._endTime < System.currentTimeMillis()) {
                            //结算
                            bank.addOrSpend(group, qq, earn);
                            toRemoveIds.add(qq);
                            System.out.println("结算：\nqq:" + qq);
                            store();
                        } else {
                            //添加事件
                            _jobManager.registerTimer(() -> {
                                if (currentWorkersMap.get(group).get(qq) != null) {
                                    bank.addOrSpend(group, qq, earn);
                                    currentWorkersMap.get(group).remove(qq);
                                    store();
                                    _yuq.getGroups().get(group).sendMessage(new Message().plus(_mif.at(qq)).plus("\n打工已经结束了\n获得了" + earn + "枚金币"));
                                }
                            }, (job._endTime - System.currentTimeMillis()));
                        }
                    }
                    for(long qq : toRemoveIds){
                        currentWorkersMap.get(g).remove(qq);
                    }
                }
            }){{start();}};
        } catch (Exception e){e.printStackTrace(); store();}
    }

    public static WorkProcessor getInstance(YuQ yuq, MessageItemFactory mif, JobManager jobManager){
        if (_instance == null)
            _instance = new WorkProcessor(yuq, mif, jobManager);
        return _instance;
    }

    private void store(){
        JSONObject root = new JSONObject();
        JSONArray groups = new JSONArray();
        for (long group : currentWorkersMap.keySet()){
            JSONObject groupJsonObject = new JSONObject();
            groupJsonObject.put("id",group);
            JSONArray jobsArray = new JSONArray();
            for(Job job : currentWorkersMap.get(group).values()){
                JSONObject jobJsonObject = new JSONObject();
                jobJsonObject.put("end_time", job._endTime);
                jobJsonObject.put("qq", job._qq);
                jobJsonObject.put("earn", job._earn);
                jobsArray.add(jobJsonObject);
            }
            groupJsonObject.put("jobs", jobsArray);
            groups.add(groupJsonObject);
        }
        root.put("groups", groups);
        String json = root.toJSONString();
        KeyboardToolBox.writeToFile(path, json);
    }

    public Object work(Group group, Member qq){
        long groupId = group.getId();
        long memberQq = qq.getId();
        if (currentWorkersMap.get(groupId).keySet().contains(memberQq)) return "你已经在打工了";
        //打工时间在10-12小时之间
        int minute = 600+ (int)(Math.random()*120);
        //10% random
        int random = (int)(Math.random()*minute/10-minute/20.0);
        int coin = (int)(minute/2.64)+random;
        long endTime = System.currentTimeMillis()+minute*60*1000;
        _jobManager.registerTimer(() ->{
            if (currentWorkersMap.get(groupId).get(memberQq)!=null
                    &&Math.abs(currentWorkersMap.get(groupId).get(memberQq)._endTime-System.currentTimeMillis())<30*1000) {
                bank.addOrSpend(groupId,memberQq,coin);
                currentWorkersMap.get(groupId).remove(memberQq);
                store();
                group.sendMessage(new Message().plus(_mif.at(qq)).plus("\n打工已经结束了\n获得了" + coin + "枚金币"));
            }
        } , minute*60*1000);
        currentWorkersMap.get(groupId).put(memberQq, new Job(endTime, coin, memberQq, groupId));
        System.out.println(DateUtil.format(DateUtil.date(System.currentTimeMillis()),"MM月dd日 HH时mm分"));
        System.out.println(DateUtil.format(DateUtil.date(endTime),"MM月dd日 HH时mm分"));
        store();
        return new Message().plus(_mif.at(qq)).plus("\n打工已经开始了\n本次打工长"+minute/60+"小时"+minute%60+"分钟\n预计收入"+coin+"枚金币");
    }

    public Object cancelWork(long group, long qq){
        Job job = currentWorkersMap.get(group).remove(qq);
        store();
        return (job==null)?"您没有在进行的打工":"好的，打工已经取消了，"+job._earn+"金币的工资将不会被结算";
    }

    public Object myWork(long group, long qq){
        Job job = currentWorkersMap.get(group).get(qq);
        if (job == null) return "你现在没在工作，还不赶快上工？";
        return "亲爱的打工人，您现在正在工作" +
                "\n正在进行的工作预期收益为：" +
                "\n"+job._earn+"枚金币"+
                "\n预计结束时间为：" +
                "\n"+ DateUtil.format(DateUtil.date(job._endTime).setTimeZone(TimeZone.getTimeZone("GMT+8")), "MM月dd日 HH时mm分")+
                "\n距离打工结束还有："+DateUtil.formatBetween((job._endTime-System.currentTimeMillis()), BetweenFormatter.Level.MINUTE)+
                "\n加油打工人！";
    }

    public Object subtractMoney(long group, Member qq, Member target, int amount){
        long balance = bank.getBalance(group, target.getId());
        amount = (int)Math.min(amount,balance);
        bank.addOrSpend(group,target.getId(),-amount);
        return "扣钱成功了，"+target.nameCardOrName()+"现在拥有"+bank.getBalance(group, target.getId())+"枚金币";
    }

    public Object myCoins(long group, long qq){
        return "您现在在本群拥有"+bank.getBalance(group,qq)+"枚金币";
    }

    public Object userCoins(long group, Member target){
        return target.nameCardOrName()+"现在在本群拥有"+bank.getBalance(group,target.getId())+"枚金币";
    }
}

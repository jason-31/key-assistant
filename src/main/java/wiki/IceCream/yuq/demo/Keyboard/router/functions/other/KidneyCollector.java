package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.error.WaitNextMessageTimeoutException;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.*;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;
import wiki.IceCream.yuq.demo.Keyboard.router.Router;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class KidneyCollector extends Function {
    UltimateInfoStorage uis;
    public KidneyCollector(){
        super("收肾助手",
                "kidney-collector",
                "",
                2);
        uis =UltimateInfoStorage.getInstance();
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        //检查权限
        //如果没有开放，回复消息结束对话
        if(getCurrentAvailability(group)!=1) {
            sendMessage(event, "本群收肾助手功能暂时没有开放，请联系管理员，如果您是管理员，请联系键盘获取本功能详细使用说明");
            return;
        }

        //管理员功能
        //如果发起者权限等级不够，则拒回复消息结束对话
        if((strMessageText.matches("(添加新肾表|删除肾表|设定肾表属性|催肾)(.)*"))
        && KeyboardToolBox.getAdminLevel(event)<1){
            sendMessage(event, "您的权限不够，请定时向小助手祈祷来帮助您早点当上管理哦");
            return;
        }

        //从uif获取本群肾表json
        JSONObject groupKidneyStatus = getGroupKidneyStatus(group, false);
        JSONArray forms = groupKidneyStatus.getJSONArray("forms");

        //routing
        if(strMessageText.equals("添加新肾表"))
            uploadNewForm(group,qq,strMessageText,event,forms);
        if(strMessageText.matches("删除肾表(.)+"))
            deleteForm(group,qq,strMessageText,event,forms, groupKidneyStatus);
        if(strMessageText.matches("催肾(.)+"))
            kidneyColleting(group,qq,strMessageText,event,forms);
        if(strMessageText.matches("交肾情况(.)+"))
            collectingStatus(group,qq,strMessageText,event,forms);
        if(strMessageText.contains("收肾列表"))
            formsList(group,qq,strMessageText,event,forms);
        if (strMessageText.matches("肾表详情(.)+"))
            formDetail(group,qq,strMessageText,event,forms);
        if(strMessageText.matches("(添加交肾记录|已交|已肾)(.)+"))
            addKidneyPaidHistory(group,qq,strMessageText,event,forms,groupKidneyStatus);
    }

    private JSONObject getGroupKidneyStatus(long group, boolean needToInitializeNew){
        JSONObject groupKidneyStatus = uis.getObjectFromUserOrGroup("kidney-forms",""+group,3);
        //初始化json
        if ((!needToInitializeNew)&&groupKidneyStatus.getJSONArray("forms")==null) {
            groupKidneyStatus.put("forms", new JSONArray());
            groupKidneyStatus.put("archived-forms", new JSONArray());
            groupKidneyStatus.put("auto-collecting", false);
            uis.save();
        }
        return groupKidneyStatus;
    }

    private JSONObject findFormByName(JSONArray forms, String name){
        for(int i = 0; i<forms.size(); i++){
            JSONObject form = forms.getJSONObject(i);
            if (form.getString("name").equals(name)){
                return form;
            }
        }
        return null;
    }

    private void sendKidneyCollectingMessage(JSONObject form, long group){
        ArrayList<Message> messages = generateKidneyCollectingMessages(form);
        for(Message message : messages){
            KeyboardToolBox.sendMessage(group, Router.getInstance().getYuq(),Router.getInstance().getMif(), message);
            try{
                Thread.sleep(3000);
            }catch (Exception e){}
        }
    }
    private ArrayList<Message> generateKidneyCollectingMessages(JSONObject form){
        ArrayList<Message> messages = new ArrayList<>();
        MessageItemFactory mif = Router.getInstance().getMif();

        String formName = form.getString("name");
        String imagePath = ListAndAddressHandeler.getImagePath()+"kidney_forms/"+form.getString("image")+".png";
        File image = new File(imagePath);

        Message message = new Message();
        message.plus(mif.imageByFile(image));
        //put debtors into arraylist
        JSONArray jsaDebtors = form.getJSONArray("debtors");

        ArrayList<Long> unpaid = new ArrayList<>();
        for(int i = 0; i<jsaDebtors.size(); i++){
            JSONObject jsoDebtor = jsaDebtors.getJSONObject(i);
            if(!jsoDebtor.getBoolean("paid")) {
                unpaid.add(jsoDebtor.getLong("qq"));
            }
        }

        if(unpaid.size()==0) {
            message.plus("\n已经收齐了！");
        }
        messages.add(message);

        if(unpaid.size()!=0){
            //5个一组
            message = new Message();
            int atEveryMessage = 5;

            int count = 0;
            for (int i = 0; i<unpaid.size();i++){
                message.plus(mif.at(unpaid.get(i)));
                count++;
                if(count==atEveryMessage||i==(unpaid.size()-1)){
                    count = 0;
                    message.plus("\n快交肾！交完肾后在群里发送消息“已交 "+formName+" wx1/zfb1”即为完成交肾");
                    messages.add(message);
                    message=new Message();
                }
            }
        }
        return messages;
    }

    //添加肾表
    private void uploadNewForm(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms){
        String imageUrl;
        String name;
        ArrayList<Long> debtors;
        final boolean defaultAutoDelete = true;
        final boolean defaultAutoCollect = false;
        final int defaultRequireImageProof = 1;

        ContextSession session = event.getSender().getGroupChatSession();

        try {
            //要求上传肾表图片
            sendMessage(event, "请上传肾表图片");
            Message imageResponse = session.waitNextMessage(45 * 1000);
            if(!(imageResponse.get(0) instanceof Image)){
                sendMessage(event, "不是图片，上传取消");
                return;
            }
            imageUrl = ((Image)imageResponse.get(0)).getUrl();

            //要求发送肾表名
            int round = 0;
            final int allowedNameSeconds = 30;
            while (true){
                String question = (round++==0)?"请输入肾表名您有" + allowedNameSeconds + "秒钟时间":"肾表不合法，请重新输入";
                sendMessage(event, question);
                //肾表名内不允许存在空格
                String nameResponse = Message.Companion.firstString(session.waitNextMessage(allowedNameSeconds * 1000)).replaceAll("\\s","");
                //遍历forms,确定是否有重复或不合法的肾表名
                if(findFormByName(forms,nameResponse)==null||nameResponse.equals("所有")) {
                    name = nameResponse;
                    break;
                }
            }

            //要求把所有要交的成员at一遍
            round = 0;
            final int allowedAtMinutes = 5;
            debtors = new ArrayList<>();
            //用while循环多次收集
            while (true) {
                String question = (round++ == 0) ? "请at所有要交肾的群成员，您有" + allowedAtMinutes + "分钟时间" : "这就是全部了吗？如果不是请继续at，如果是的话请回复“结束了”结束设定";
                sendMessage(event, question);
                Message response = session.waitNextMessage(60 * allowedAtMinutes * 1000);
                //收到结束口令，就结束循环
                if (Message.Companion.firstString(response).replaceAll("\\s", "").matches("结束了|是|已经结束了|是的"))
                    break;
                //将收集到的QQ号加入arraylist
                for(int i = 0; i<response.getBody().size(); i++){
                    MessageItem miAt = response.get(i);
                    //检查是否已经加入，确保不会重复加入
                    if(miAt instanceof At){
                        At at = (At)miAt;
                        if (!debtors.contains(at.getUser()))
                            debtors.add(at.getUser());
                    }
                }
            }
            if(debtors.size()<1){
                sendMessage(event, "收集到的交肾人数为0，上传取消");
                return;
            }

            //储存
            //store the image
            String imageName = "kf_"+System.currentTimeMillis();
            String imagePath = ListAndAddressHandeler.getImagePath()+"kidney_forms/"+imageName+".png";
            KeyboardToolBox.getImageFileFromUrl(imageUrl, imagePath);
            //store it to uis
            JSONObject form = new JSONObject();
            form.put("name", name);
            form.put("owner", qq);
            form.put("image", imageName);
            JSONArray jsaDebtors = new JSONArray();
            for(long debtor : debtors){
                JSONObject jsoDebtor = new JSONObject();
                jsoDebtor.put("qq", debtor);
                jsoDebtor.put("paid", false);
                jsaDebtors.add(jsoDebtor);
            }
            form.put("debtors", jsaDebtors);
            form.put("auto-delete", defaultAutoDelete);
            form.put("auto-collect", defaultAutoCollect);
            form.put("require-proof", defaultRequireImageProof);
            forms.add(form);
            uis.save();
            sendMessage(event, "上传成功");
        }
        catch (WaitNextMessageTimeoutException waitNextMessageTimeoutException){
            sendMessage(event, "超时未输入，上传取消");
        }
        catch (Exception e){
            sendMessage(event, "未知错误，上传取消");
            e.printStackTrace();
        }
    }

    //删除肾表
    private void deleteForm(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms, JSONObject groupKidneyStatus){
        String formName = strMessageText.substring(4).replaceAll("\\s","");
        JSONObject toDelete = findFormByName(forms,formName);
        if(toDelete==null){
            sendMessage(event, "肾表不存在，请检查肾表名是否准确");
            return;
        }
        groupKidneyStatus.getJSONArray("archived-forms").add(toDelete.clone());
        sendKidneyCollectingMessage(toDelete, group);
        forms.remove(toDelete);
        uis.save();
        sendMessage(event, "删除成功");
    }

    //催肾
    private void kidneyColleting(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms){
        String formName = strMessageText.substring(2).replaceAll("\\s","");
        JSONObject toCollect = findFormByName(forms,formName);
        if(toCollect==null){
            sendMessage(event, "肾表不存在，请检查肾表名是否准确");
            return;
        }
        sendKidneyCollectingMessage(toCollect, group);
    }

    //交肾情况
    private void collectingStatus(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms){
        String formName = strMessageText.substring(4).replaceAll("\\s","");
        JSONObject toReport = findFormByName(forms,formName);
        if(toReport==null){
            sendMessage(event, "肾表不存在，请检查肾表名是否准确");
            return;
        }
        JSONArray jsaDebtors = toReport.getJSONArray("debtors");
        String response = "以下是"+formName+"的交肾情况";
        YuQ yuq = Router.getInstance().getYuq();
        String paidDebtors = "\n已交：";
        String unpaidDebtors = "\n未交：";
        for(int i = 0; i<jsaDebtors.size(); i++){
            JSONObject jsoDebtor = jsaDebtors.getJSONObject(i);
            long debtorQq = jsoDebtor.getLong("qq");
            boolean ifPaid = jsoDebtor.getBoolean("paid");
            String message = null;
            try{
                message = jsoDebtor.getString("message");
            }catch (Exception e){}
            String debtorNameCard = "已退群";
            try{
                debtorNameCard = yuq.getGroups().get(group).getMembers().get(debtorQq).nameCardOrName();
            }catch (Exception e){}

            String strDebtorDetail = debtorNameCard+"("+debtorQq+")  "+((message!=null)?message:(ifPaid)?"已交":"未交");

            if(ifPaid)
                paidDebtors+="\n"+strDebtorDetail;
            else
                unpaidDebtors+="\n"+strDebtorDetail;
        }
        paidDebtors+=(paidDebtors.length()>4)?"":"\n无";
        unpaidDebtors+=(unpaidDebtors.length()>4)?"":"\n无";
        response+=paidDebtors+unpaidDebtors;
        sendMessage(event, response);
    }

    //添加交肾记录
    private void addKidneyPaidHistory(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms, JSONObject groupKidneyStatus){
        String formNameAndMessage = strMessageText.split("(添加交肾记录|已交|已肾)\\s*")[1];
        String formName = formNameAndMessage;
        String message = null;
        try {
            formName = formNameAndMessage.split("\\s+")[0];
            message = formNameAndMessage.split("\\s+")[1];
        }catch (Exception e){}

        JSONObject toPay = findFormByName(forms,formName);
        if(toPay==null){
            sendMessage(event, "肾表不存在，请检查肾表名是否准确");
            return;
        }
        JSONArray jsaDebtors = toPay.getJSONArray("debtors");
        boolean found = false;
        int paid = 0;
        for(int i = 0; i<jsaDebtors.size(); i++){
            JSONObject jsoDebtor = jsaDebtors.getJSONObject(i);
            if(jsoDebtor.getLong("qq")==qq) {
                found = true;
                sendMessage(event, jsoDebtor.getBoolean("paid")?"你已经交过肾了":"感谢交肾！");
                jsoDebtor.put("paid", true);
                jsoDebtor.put("message", (message==null)?"已交，无备注":message);
                uis.save();
            }
            if(jsoDebtor.getBoolean("paid"))
                paid++;
        }

        //如果交齐则存档肾表
        if(paid==jsaDebtors.size()&&toPay.getBoolean("auto-delete")){
            groupKidneyStatus.getJSONArray("archived-forms").add(toPay.clone());
            sendKidneyCollectingMessage(toPay, group);
            forms.remove(toPay);
            uis.save();
        }

        if(!found)
            sendMessage(event, "这份肾表不需要您交肾的！");
    }

    //收肾列表
    private void formsList(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms){
        if (forms.size()==0){
            sendMessage(event, "本群没有在用小助手收肾的肾表哟");
            return;
        }
        String response = "本群正在收肾的肾表有：\n";
        for (int i = 0; i < forms.size(); i++){
            response+= forms.getJSONObject(i).getString("name")+"\n";
        }
        response+="您可以使用肾表详情指令查看肾表详细信息";
        sendMessage(event,response);
    }

    //肾表详情
    private void formDetail(long group, long qq, String strMessageText, GroupMessageEvent event, JSONArray forms){
        String formName = strMessageText.substring(4).replaceAll("\\s","");
        JSONObject forDetail = findFormByName(forms,formName);
        if(forDetail==null){
            sendMessage(event, "肾表不存在，请检查肾表名是否准确");
            return;
        }
        String imagePath = ListAndAddressHandeler.getImagePath()+"kidney_forms/"+forDetail.getString("image")+".png";
        Message message = new Message();
        sendMessage(event, message.plus(Router.getInstance().getMif().imageByFile(new File(imagePath))));
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        return
                // 添加新肾表，需要发信人是管理员
                (KeyboardToolBox.getAdminLevel(event)>0 && strMessageText.equals("添加新肾表")) ||
                //管理员删除肾表
                (KeyboardToolBox.getAdminLevel(event)>0 && strMessageText.matches("删除肾表(.)+"))||
                //管理员设定肾表属性
//                (KeyboardToolBox.getAdminLevel(event)>0 && strMessageText.matches("设定肾表属性.+\\s[^\\s]+"))||
                //管理员发送一键催肾
                (KeyboardToolBox.getAdminLevel(event)>0 && strMessageText.matches("催肾(.)+"))||
                //管理员查看收肾情况
                (KeyboardToolBox.getAdminLevel(event)>0 && strMessageText.matches("交肾情况(.)+"))||
                //群成员查看收肾列表
                strMessageText.contains("收肾列表")||
                //群成员查看肾表详情
                strMessageText.matches("肾表详情(.)+")||
                //群成员交肾
                strMessageText.matches("(添加交肾记录|已交|已肾)\\s+\\S.+")
                //群成员上传已有记录的截图凭证
//                ||strMessageText.matches("上传截图凭证(.)+")
                ;

    }
}

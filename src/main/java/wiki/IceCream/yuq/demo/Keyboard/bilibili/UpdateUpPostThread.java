package wiki.IceCream.yuq.demo.Keyboard.bilibili;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class UpdateUpPostThread implements Runnable{
    private ArrayList<Long> serviceGroups;
    private Thread _thread;
    private boolean _stopped = false;
    private ArrayList<Up> _ups = new ArrayList<>();
    private File _upsInfoFile;
    YuQ _yuQ;
    MessageItemFactory _mif;

    public UpdateUpPostThread(YuQ yuQ, MessageItemFactory mif){
        serviceGroups= ListAndAddressHandeler.getServiceGroupList();
        _yuQ = yuQ;
        _mif = mif;
        _upsInfoFile = new File(ListAndAddressHandeler.getDependenciesFolderPath()+"infos/up infos.txt");
        //read info from file
        try{
            String json = KeyboardToolBox.readAllBytesJava7(_upsInfoFile.getPath());
            JSONObject  jsonObject = JSON.parseObject(json);
            JSONArray uplist = jsonObject.getJSONArray("uplist");
            for (int i = 0; i<uplist.size(); i++){
                Map<String, Object> pageMap = (Map) uplist.get(i);
                String mid =  (String) pageMap.get("mid");
                String lastvid = (String) pageMap.get("lastvid");
                JSONArray groupsArray = (JSONArray) pageMap.get("groups");
                ArrayList<Long> groups = new ArrayList<>();
                for (int j = 0; j<groupsArray.size(); j++){
                    Map<String, Object> group = (Map) groupsArray.get(j);
                    long a = Long.parseLong(""+group.get("group"));
                    if (serviceGroups.contains(a)){
                        groups.add(a);
                    }
                }
                _ups.add(new Up(mid, lastvid, groups));
            }
            System.out.println("扫包成功，共有"+_ups.size()+"个up");
        } catch (Exception e){
            System.out.println("read file failed");
            KeyboardToolBox.writeToFile(_upsInfoFile.getPath(), "");
        }
    }

    public void start () {
        if (_thread == null) {
            _thread = new Thread (this, "update up post thread");
            _thread.start ();
        }
    }

    public void run(){
        int failedInARow = 0;
        while (true) {
            while (!_stopped) {
                int size = _ups.size();
                for (int i = 0; i < size; i++) {
                    try {
                        if (failedInARow >= 3){
                            try{_yuQ.getFriends().get(172924001L).sendMessage("获取up信息出错了！");}catch(Exception ee){System.out.println("获取up信息出错了！");}
                            _thread.sleep(62*60*1000);
                        }
                        Up up = _ups.get(i);
                        String oldLastVid = up.lastvid;
                        String[] upLastPostInfo = BilibiliGrabber.getUpVideosInfos(up.mid)[0];
                        String nowLastVid = upLastPostInfo[1];
                        failedInARow = 0;
                        if(!oldLastVid.equals(nowLastVid)){
                            //update info
                            up.setLastvid(nowLastVid);
                            storeInfo();
                            String upName = BilibiliGrabber.getUpInfos(up.mid)[0];
                            Message message = new Message().plus(upName+"发新视频啦！\n"+
                                    "标题：" + upLastPostInfo[0]+"\n"+
                                    "链接：https://www.bilibili.com/video/" + nowLastVid+"\n").plus(_mif.imageByUrl(upLastPostInfo[3]));
                            for (long group : up.groups){
                                try{
                                    _yuQ.getGroups().get(group).sendMessage(message);
                                    _thread.sleep(5000);
                                } catch (Exception ee){}
                            }
                        }
                    } catch (Exception e) {failedInARow ++;}
                    try{
                        _thread.sleep(8000);
                    } catch (Exception e){}
                }
            }
            try{
                Thread.sleep(5000);
            } catch (Exception e){}
        }
    }

    public void pauseResume(){
        _stopped=!_stopped;
    }

    public boolean addUp(String mid, long group){
        if (getUp(mid)==null) {
            ArrayList<Long> groups = new ArrayList<>();
            groups.add(group);
            _ups.add(new Up(mid, BilibiliGrabber.getUpVideosInfos(mid)[0][1], groups));
        }
        else{
            Up up = getUp(mid);
            if(up.groups.contains(group)){
                return false;
            }
            up.groups.add(group);
        }
        storeInfo();
        return  true;
    }

    public boolean removeUp(String mid, long group){
        Up up = getUp(mid);
        if (up==null) return false;
        up.groups.remove(group);
        if(up.groups.size()<1) _ups.remove(up);
        return true;
    }

    public String followList(long group){
        String list = "";
        for(Up up:_ups){
            for(long g : up.groups){
                if (g==group){
                    String[] info = BilibiliGrabber.getUpInfos(up.mid);
                    list += (info!=null)?("\n"+ BilibiliGrabber.getUpInfos(up.mid)[0]):"";
                }
            }
        }
        return (list.equals("")) ?list = "该群还没有关注的up" : list.substring(1);
    }

    public void storeInfo(){
        JSONObject root = new JSONObject();
        JSONArray uplist = new JSONArray();
        for (Up up : _ups){
            JSONObject upObject = new JSONObject();
            upObject.put("mid",up.mid);
            upObject.put("lastvid", up.lastvid);
            JSONArray groups = new JSONArray();
            System.out.println(up.groups);
            for(long group : up.groups){
                JSONObject groupObj = new JSONObject();
                groupObj.put("group",group);
                groups.add(groupObj);
            }
            upObject.put("groups", groups);
            uplist.add(upObject);
        }
        root.put("uplist", uplist);
        System.out.println("store called\n"+root.toJSONString());
        KeyboardToolBox.writeToFile(_upsInfoFile.getPath(), root.toJSONString());
    }

    private Up getUp(String mid){
        for (Up up : _ups){
            if (up.mid.replaceAll("[^0-9]","").equals(mid.replaceAll("[^0-9]",""))) return up;
        }
        return null;
    }

}

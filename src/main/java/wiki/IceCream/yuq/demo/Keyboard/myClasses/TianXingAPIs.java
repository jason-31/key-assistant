package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.*;

public class TianXingAPIs {

    private static String key = "6c377ef6130639ddbc648e2946cbd674";


    public static String getHuangLiInfo(int offSet){
        HashMap<String, Object> huangliMap = new HashMap<>();
        huangliMap.put("key", key);
        huangliMap.put("date", getDateInTianXingFormat(offSet));
        try {
            String json = ApiInfoGrabber.httpGetRequest("http://api.tianapi.com/lunar/index", huangliMap);
            JSONObject root = JSON.parseObject(json);
            JSONObject newslist = root.getJSONArray("newslist").getJSONObject(0);
            String lubarmonth = newslist.getString("lubarmonth");
            String lunarday = newslist.getString("lunarday");
            String fitness = newslist.getString("fitness");
            String taboo = newslist.getString("taboo");
            String lunar_festival = newslist.getString("lunar_festival");
            String festival = newslist.getString("festival");
            return "今天是农历"+lubarmonth+lunarday+
                    ((lunar_festival.equals(""))?"":"，"+lunar_festival)+
                    "\n宜："+fitness.replaceAll("\\.","，")+((fitness.contains("\\."))?"":"，摸鱼")+
                    "\n忌："+taboo.replaceAll("\\.", "，")+((taboo.contains("\\."))?"":"，奋斗");
        } catch (Exception e){e.printStackTrace(); return "请求错误";}
    }

    private static String getDateInTianXingFormat(int offsetDay){
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT+8"));
        Date date = DateUtil.offset(DateUtil.date(calendar), DateField.DAY_OF_MONTH, offsetDay);
        String format = DateUtil.format(date, "yyyy-MM-dd");
        return format;
    }

    public static String getTodayYunShi(String input){
        long sum = 0L;
        long date = Long.parseLong(getDateInTianXingFormat(0).replaceAll("[^0-9]",""));
        for (int i = 0; i < input.length(); i++){
            sum+= (int)input.charAt(i);
        }
        return getYunShi(date+sum);
    }

    public static String getTodayYunShi(long qq){
        long date = Long.parseLong(getDateInTianXingFormat(0).replaceAll("[^0-9]",""));
        return getYunShi(date+qq);
    }

    public static String getYunShi(long code){
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("number", code);
        //get today to Halloween
        String strHalloween = DateUtil.year(DateUtil.date())+"-10-31";
        Date halloween = DateUtil.parse(strHalloween, "yyyy-MM-dd");
        String strToday = KeyboardToolBox.getBeijingDate();
        Date today = DateUtil.parse(strToday, "yyyy年MM月dd日");
        long todayToHalloween = Math.abs(DateUtil.between(halloween, today, DateUnit.DAY));

        try {
            String json = ApiInfoGrabber.httpGetRequest("http://api.tianapi.com/jixiong/index", map);
            JSONObject root = JSON.parseObject(json);
            JSONObject newslist = root.getJSONArray("newslist").getJSONObject(0);
            String result = newslist.getString("result");
            String conclusion = newslist.getString("conclusion");
            if(todayToHalloween<3) {
                result = result.replaceAll("凶", "寄");
            }
            return result+"\n"+conclusion;
        } catch (Exception e){e.printStackTrace(); return "请求错误";}
    }

    public static String getWeather(String city){
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("city", city);
        try {
            String json = ApiInfoGrabber.httpGetRequest("http://api.tianapi.com/tianqi/index", map);
            //System.out.println(json);
            JSONObject root = JSON.parseObject(json);
            JSONArray newslist = root.getJSONArray("newslist");
            String toReturn = city+"的天气为：";
            for (int i = 0; i<3; i++){
                JSONObject dayWeather = newslist.getJSONObject(i);
                String date = dayWeather.getString("date");
                String week = dayWeather.getString("week");
                String weather = dayWeather.getString("weather");
                String lowest = dayWeather.getString("lowest");
                String highest = dayWeather.getString("highest");
                String tips = dayWeather.getString("tips").replaceAll("疫情防控不松懈，出门请佩戴口罩。","");
                toReturn+="\n"+date+" "+week+"\n"+lowest+" - "+highest+"\n"+tips;
            }
            return toReturn;
        } catch (Exception e){e.printStackTrace(); return "请求错误";}
    }

    public static String fishermanGuide(){
        return fishermanGuide(0);
    }

    public static String fishermanGuide(int offset){
        String date = getDateInTianXingFormat(offset);
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        map.put("date", date);
        try {
            String json = ApiInfoGrabber.httpGetRequest("http://api.tianapi.com/jiejiari/index", map);
            JSONObject root = JSON.parseObject(json);
            JSONObject newslist = root.getJSONArray("newslist").getJSONObject(0);
            int daycode = newslist.getInteger("daycode");
            if(daycode==0) return "是工作日，好好摸鱼吧";
            if(daycode==2) return "是双休日，好好瘫着吧";
            if(daycode==3) return "是调休日，收假回来总该好好摸鱼了吧";
            String cnweekday = newslist.getString("cnweekday");
            String name = newslist.getString("name");
            int now = newslist.getInteger("now")+1;
            int end = newslist.getInteger("end")+1;
            String tip = newslist.getString("tip");
            return "是节假日，"+cnweekday+"，正在放"+name+"假！"+"\n假期已经过去了"+now+"/"+end+"天"+"\n给您的建议是："+tip;
        } catch (Exception e){e.printStackTrace(); return "请求错误";}
    }

    public static String story(int type){
        HashMap<String, Object> map = new HashMap<>();
        map.put("key", key);
        if(type>0&&type<5) {
            map.put("type", type);
            map.put("num",10);
        }
        try {
            String json = ApiInfoGrabber.httpGetRequest("http://api.tianapi.com/story/index", map);
            JSONObject root = JSON.parseObject(json);
            JSONArray jsonArray = root.getJSONArray("newslist");
            JSONObject newslist = jsonArray.getJSONObject(Math.min(jsonArray.size()-1, (int)(Math.random()*10)));
            String title = newslist.getString("title");
            String content = newslist.getString("content");
            return "小助手开始广播啦！故事的名字叫："+title+"\n"+content;
        } catch (Exception e){e.printStackTrace(); return "请求错误";}
    }
}

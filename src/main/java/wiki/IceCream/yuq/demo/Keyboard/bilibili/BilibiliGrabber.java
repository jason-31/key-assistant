package wiki.IceCream.yuq.demo.Keyboard.bilibili;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ApiInfoGrabber;

import java.util.HashMap;
import java.util.Map;

public class BilibiliGrabber {

    public static String[][] getUpVideosInfos(String mid){
        String json = getUpVideosJson(mid);
        JSONObject  jsonObject = JSON.parseObject(json);
        if(jsonObject.getInteger("code")!=0) return null;
        JSONObject jsonData = jsonObject.getJSONObject("data").getJSONObject("list");
        JSONArray jsonArray = jsonData.getJSONArray("vlist");
        String[][] videosInfos= new String[jsonArray.size()][5];
        for (int i = 0; i< videosInfos.length; i++){
            String[] videoInfos = videosInfos[i];
            Map<String, Object> pageMap = (Map) jsonArray.get(i);
            //title
            videoInfos[0] = (String) pageMap.get("title");
            //bvid
            videoInfos[1] = (String) pageMap.get("bvid");
            //description
            videoInfos[2] = (String) pageMap.get("description");
            //pic
            videoInfos[3] = (String) pageMap.get("pic");
            //主要up
            videoInfos[4] = (String) pageMap.get("author");
        }
        return videosInfos;
    }

    public static String[] getUpInfos(String mid){
        String json = getUpInfosJson(mid);
        JSONObject  jsonObject = JSON.parseObject(json);
        if(jsonObject.getInteger("code")!=0) return null;
        JSONObject jsonData = jsonObject.getJSONObject("data");
        String[] upInfo = {
                //name
                jsonData.getString("name"),
                //sign
                jsonData.getString("sign"),
                //sex
                jsonData.getString("sex"),
                //face
                jsonData.getString("face")
        };
        return upInfo;
    }

    public static String[][] searchUp(String keyword){
        String json = getSearchUpJson(keyword);
        JSONObject  jsonObject = JSON.parseObject(json);
        if(jsonObject.getInteger("code")!=0) return null;
        JSONObject jsonData = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonData.getJSONArray("result");
        int resultAmount = Math.min(jsonArray.size(), 5);
        String[][] usersInfo= new String[resultAmount][5];
        for(int i = 0; i<resultAmount; i++){
            Map<String, Object> pageMap = (Map) jsonArray.get(i);
            String[] userInfo = usersInfo[i];
            //name
            userInfo[0] = (String) pageMap.get("uname");
            //sign
            userInfo[1] = ""+pageMap.get("usign");
            //fans
            userInfo[2] = ""+pageMap.get("fans");
            //uid
            userInfo[3] = ""+pageMap.get("mid");
            //pic
            userInfo[4] = "http:"+pageMap.get("upic");
        }
        return usersInfo;
    }

    public static String[][] searchVideo(String keyword){
        String json = getSearchVideoJson(keyword);
        JSONObject  jsonObject = JSON.parseObject(json);
        if(jsonObject.getInteger("code")!=0) return null;
        JSONObject jsonData = jsonObject.getJSONObject("data");
        JSONArray jsonArray = jsonData.getJSONArray("result");
        int resultAmount = Math.min(jsonArray.size(), 5);
        String[][] videosInfo= new String[resultAmount][5];
        for(int i = 0; i<resultAmount; i++){
            Map<String, Object> pageMap = (Map) jsonArray.get(i);
            String[] videoInfo = videosInfo[i];
            //title
            videoInfo[0] = (""+pageMap.get("title")).replaceAll("(<em class=\\\"keyword\\\">)|</em>","");
            //author
            videoInfo[1] = ""+pageMap.get("author");
            //bvid
            videoInfo[2] = ""+pageMap.get("bvid");
            //description
            videoInfo[3] = ""+pageMap.get("description");
            //pic
            videoInfo[4] = "http:"+pageMap.get("pic");
        }
        return videosInfo;
    }

    public static String getSearchUpJson(String keywoard){
        //接口地址
        String requestUrl = "http://api.bilibili.com/x/web-interface/search/type";
        //params用于存储请求数据的参数
        Map params = new HashMap();
        params.put("search_type", "bili_user");
        params.put("keyword", keywoard);

        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        String json = ApiInfoGrabber.httpGetRequest(requestUrl, params);
        return json;
    }

    public static String getSearchVideoJson(String keywoard){
        //接口地址
        String requestUrl = "http://api.bilibili.com/x/web-interface/search/type";
        //params用于存储请求数据的参数
        Map params = new HashMap();
        params.put("search_type", "video");
        params.put("keyword", keywoard);

        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        String json = ApiInfoGrabber.httpGetRequest(requestUrl, params);
        return json;
    }

    public static String getUpInfosJson(String mid){
        //接口地址
        String requestUrl = "http://api.bilibili.com/x/space/acc/info";
        //params用于存储请求数据的参数
        Map params = new HashMap();
        params.put("mid", mid);

        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        String json = ApiInfoGrabber.httpGetRequest(requestUrl, params);
        return json;
    }


    public static String getUpVideosJson(String mid) {
        //接口地址
        String requestUrl = "http://api.bilibili.com/x/space/arc/search";
        //params用于存储请求数据的参数
        Map params = new HashMap();
        params.put("mid", mid);
        params.put("order", "pubdate");
        params.put("pn", "1");
        params.put("ps", "20");

        //调用httpRequest方法，这个方法主要用于请求地址，并加上请求参数
        String json = ApiInfoGrabber.httpGetRequest(requestUrl, params);
        return json;
    }
}

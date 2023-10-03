package wiki.IceCream.yuq.demo.Keyboard.mercari;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ApiInfoGrabber;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MercariInfoGetter {
    private static MercariInfoGetter instance = null;
    private String filePath = ListAndAddressHandeler.getDependenciesFolderPath() +"infos/dpops.json";
    private String searchDpop;
    private String itemDpop;

    public MercariInfoGetter(){
        updateDpop();
    }

    public static MercariInfoGetter getInstance(){
        if(instance==null){
            instance=new MercariInfoGetter();
        }
        return instance;
    }

    private void updateDpop(){
        //read the newest dpop from file
        JSONObject root = JSONObject.parseObject(KeyboardToolBox.readAllBytesJava7(filePath));
        if(System.currentTimeMillis()-root.getLong("last_edit")<24*60*60*1000) {
            searchDpop = root.getString("search");
            itemDpop = root.getString("item");
        }
    }

    public String getItemInfoJsonFromMercari(String url){
        String requestUrl = "https://api.mercari.jp/items/get";
        String id = "m"+url.replaceAll("[^0-9]","");
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        Map<String, Object> headers = new HashMap<>();
        headers.put("dpop", itemDpop);
        headers.put("x-platform", "web");
        return ApiInfoGrabber.httpGetRequest(requestUrl, params,headers);
    }

    public  String[] getItemInfoFromMercari(String url){
        try {
            String json = getItemInfoJsonFromMercari(url);
            JSONObject data = JSONObject.parseObject(json).getJSONObject("data");
            String price = data.getString("price");
            String name = data.getString("name");
            String photoUrl = "https://static.mercdn.net/item/detail/orig/photos"+data.getJSONArray("thumbnails").getString(0).split("photos")[1];;
            String ifSold = (!json.contains("status\":\"on_sale"))?"已SOLD":"未SOLD";
            String[] strArrReturn = {price, name, photoUrl, ifSold};
            return  strArrReturn;
        } catch(Exception e) {e.printStackTrace(); return getItemInfoFromMercariByBrowser(url);}
    }

    private String[] getItemInfoFromMercariByBrowser(String url){
        String html = getHtmlFromMercari(url);
        String price = "";
        String name = "";
        String photoUrl="";
        String ifSold = (html.contains("sticker=\"sold\""))?"已SOLD":"未SOLD";
        //获取价格
        //获取标题
        try {
            price = html.split("\"price\":")[1].split(",\"priceCurrency")[0];
            name = html.split("name\":\"")[1].split("\",\"description")[0];
            photoUrl = html.split("\"image\":\\[\"")[1].split("\"],\"name\"")[0];
        }catch (Exception e){
            e.printStackTrace();
            return null;}
        String[] strArrReturn = {price, name, photoUrl, ifSold};
        return strArrReturn;
    }

    //search

    public String getSearchJsonFromMercari(String keyword){
        String requestUrl = "https://api.mercari.jp/v2/entities:search";
        Map<String, Object> params = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        headers.put("dpop", searchDpop);
        headers.put("x-platform", "web");
        String toSend = "{\"userId\":\"\",\"pageSize\":40,\"pageToken\":\"\",\"searchSessionId\":\"9aa8d5f8096de6b8107d0f2c2d01c5c8\",\"indexRouting\":\"INDEX_ROUTING_UNSPECIFIED\",\"thumbnailTypes\":[],\"searchCondition\":{\"keyword\":\""+
                keyword+
                "\",\"excludeKeyword\":\"\",\"sort\":\"SORT_SCORE\",\"order\":\"ORDER_DESC\",\"status\":[\"STATUS_ON_SALE\"],\"sizeId\":[],\"categoryId\":[],\"brandId\":[],\"sellerId\":[],\"priceMin\":0,\"priceMax\":0,\"itemConditionId\":[],\"shippingPayerId\":[],\"shippingFromArea\":[],\"shippingMethod\":[],\"colorId\":[],\"hasCoupon\":false,\"attributes\":[],\"itemTypes\":[]},\"defaultDatasets\":[],\"serviceFrom\":\"suruga\"}";
        return ApiInfoGrabber.httpPostRequest(requestUrl, params,headers, toSend);
    }

    public String[][] searchOnMercari(String keyword){
        try{
            String json = getSearchJsonFromMercari(keyword);
            //System.out.println(json);
            JSONObject root = JSON.parseObject(json);
            JSONArray items = root.getJSONArray("items");
            String[][] itemInfos = new String[Math.min(items.size(),4)][4];
            for(int i = 0; i < itemInfos.length; i++){
                JSONObject item = items.getJSONObject(i);
                String[] itemInfo = itemInfos[i];
                //name
                itemInfo[0] = item.getString("name");
                //price
                itemInfo[1] = item.getString("price");
                //photo url
                itemInfo[2] = "https://static.mercdn.net/item/detail/orig/photos"+item.getJSONArray("thumbnails").getString(0).split("photos")[1];
                //编号
                itemInfo[3] = item.getString("id").substring(1);
            }
            return itemInfos;
        } catch (Exception e){return searchOnMercariByBrowser(keyword);}
    }


    public String[][] searchOnMercariByBrowser(String keyword){
        System.out.println("browser method called");
        String url = "https://jp.mercari.com/search?keyword="+keyword+"&status=on_sale&page=1";
        String html = getHtmlFromMercari(url);
        String[] blocks = html.split("</mer-item-thumbnail>");
        if(blocks.length<=1) return null;
        ArrayList<String> items = new ArrayList<>();
        for(int i = 0; i<((blocks.length>=5)?4:blocks.length-1); i++){
            items.add(blocks[i].split("<mer-item-thumbnail")[1]);
        }
        String[][] itemInfos = new String[(blocks.length>=5)?4:blocks.length-1][4];
        int i = 0;
        try{
            for(String[] itemInfo : itemInfos){
                String item = items.get(i);
                //name
                itemInfo[0] = item.split("item-name=\"")[1].split("\"")[0];
                //price
                itemInfo[1] = item.split("price=\"")[1].split("\"")[0];
                //photo url
                itemInfo[2] = item.split("src=\"")[1].split("\"")[0];
                //编号
                itemInfo[3] = itemInfo[2].split("photos/m")[1].split("_")[0];
                i++;
            }
        } catch(Exception e){return null;}
        return itemInfos;
    }

    public String getHtmlFromMercari(String url){
        // 设置 chromedirver 的存放位置
        System.getProperties().setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver");
        // 设置无头浏览器
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments("--headless");
        ChromeDriver webDriver;
        Thread t =  Thread.currentThread();
        ClassLoader ccl = t.getContextClassLoader();
        t.setContextClassLoader(ChromeDriver.class.getClassLoader());
        try {
            webDriver = new ChromeDriver(chromeOptions);
        } finally {
            t.setContextClassLoader(ccl);
        }
//        ChromeDriver webDriver = new ChromeDriver(chromeOptions);
        webDriver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
        webDriver.get(url);
        //设置浏览器的宽高
        Dimension dimension = new Dimension(1920, 1080);
        webDriver.manage().window().setSize(dimension);
        String html = "";
        JavascriptExecutor driver_js= ((JavascriptExecutor) webDriver);
        try{Thread.sleep(5*1000);}catch (Exception e){}
        html = (String)driver_js.executeScript("return document.documentElement.outerHTML");
        webDriver.close();
        return html;
    }

}

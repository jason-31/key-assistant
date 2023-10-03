package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class ApiInfoGrabber {

    public static String httpGetRequest(String requestUrl, Map params){
        return httpGetRequest(requestUrl, params, null);
    }


    public static String httpGetRequest(String requestUrl, Map params, Map<String, Object> headers) {
        //buffer用于接受返回的json数据
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(requestUrl+"?"+urlencode(params));
            //打开http连接
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();//连接
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setRequestMethod("GET");
            if(headers!=null) {
                for(String key : headers.keySet())
                httpUrlConnection.setRequestProperty(key, headers.get(key).toString());
            }
            httpUrlConnection.connect();

            //获得输入
            InputStream inputStream = httpUrlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"utf-8");//编码
            BufferedReader bufferedReader =  new BufferedReader(inputStreamReader);

            //将bufferReader的值给放到str里
            String str = null;
            while((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            //关闭bufferReader和输入流
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            // inputStream = null;

            //断开连接
            httpUrlConnection.disconnect();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //返回字符串
        return buffer.toString();
    }

    public static String httpPostRequest(String requestUrl, Map params, String requestBody) {
        return httpPostRequest(requestUrl,params,null,requestBody);
    }

    public static String httpPostRequest(String requestUrl, Map params, Map<String, Object> headers, String toPost) {
        //buffer用于接受返回的json数据
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(requestUrl+((params!=null)?("?"+urlencode(params)):""));
            //打开http连接
            HttpURLConnection httpUrlConnection = (HttpURLConnection) url.openConnection();//连接
            httpUrlConnection.setDoInput(true);
            httpUrlConnection.setRequestMethod("POST");
            if(headers!=null) {
                for(String key : headers.keySet())
                    httpUrlConnection.setRequestProperty(key, headers.get(key).toString());
            }
            httpUrlConnection.setDoOutput(true);
            try(OutputStream os = httpUrlConnection.getOutputStream()) {
                byte[] input = toPost.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            httpUrlConnection.connect();

            //获得输入
            InputStream inputStream = httpUrlConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream,"utf-8");//编码
            BufferedReader bufferedReader =  new BufferedReader(inputStreamReader);

            //将bufferReader的值给放到str里
            String str = null;
            while((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }

            //关闭bufferReader和输入流
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            // inputStream = null;

            //断开连接
            httpUrlConnection.disconnect();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
        //返回字符串
        return buffer.toString();
    }


    public static String urlencode(Map<String, Object>data) {

        //将map里的参数变成像 showapi_appid=###&showapi_sign=###&的样子
        StringBuilder sb = new StringBuilder();
        for(Map.Entry i : data.entrySet()) {
            try {
                sb.append(i.getKey()).append("=").append(URLEncoder.encode(i.getValue() + "", "UTF-8")).append("&");
            } catch (UnsupportedEncodingException ex) {
                ex.printStackTrace();
            }
        }
        return sb.toString();
    }
}

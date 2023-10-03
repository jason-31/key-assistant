package wiki.IceCream.yuq.demo.Keyboard.router.functions.other;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ApiInfoGrabber;
import wiki.IceCream.yuq.demo.Keyboard.router.functions.Function;

import java.util.HashMap;
import java.util.Map;

public class TalkToChatGPT extends Function {

    public TalkToChatGPT(){
        super("chatgpt对话",
                "talk-chatgpt",
                "可以用这个功能方便地与chatgpt对话",
                1);
    }

    @Override
    public void action(long group, long qq, String strMessageText, GroupMessageEvent event) {
        sendMessage(event,getChatgptResponse(parsePromptFromMessage(strMessageText)));
    }

    private String getChatgptResponse(String prompt){
        try {
            String chatgptResponseJson = getChatgptResponseJson(prompt);
            String chatgptResponseContent = parseChatgptResponseJson(chatgptResponseJson);
            return chatgptResponseContent==null?"请求错误，请联系管理员":chatgptResponseContent;
        } catch (Exception e){return "请求错误，请联系管理员";}
    }

    private String parseChatgptResponseJson(String json){
        String chatgptResponseContent = null;
        JSONObject root = JSONObject.parseObject(json);

        //get the wanted message
        JSONArray choices = root.getJSONArray("choices");

        //get the assistant response content
        for(int i = 0; i<choices.size(); i++){
            if(choices.getJSONObject(i).getJSONObject("message").getString("role").equals("assistant")) {
                chatgptResponseContent = choices.getJSONObject(i).getJSONObject("message").getString("content");
                break;
            }
        }

        return chatgptResponseContent;
    }

    private String getChatgptResponseJson(String prompt){
        return ApiInfoGrabber.httpPostRequest("https://api.openai.com/v1/chat/completions",
                null,
                constructRequestHeaders(),
                constructBody(prompt));
    }

    private Map<String, Object> constructRequestHeaders(){
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization","Bearer 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");
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private String constructBody(String prompt){
        return "{\n" +
                "  \"model\": \"gpt-3.5-turbo\",\n" +
                "  \"max_tokens\": 500,\n" +
                "  \"messages\": [\n" +
                "    {\"role\":\"system\",\n" +
                "     \"content\":\"don‘t explain unless asked. Speak the language of the question\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"role\": \"user\",\n" +
                "      \"content\": \""+prompt+"\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n";
    }

    @Override
    protected boolean checkMessage(String strMessageText, GroupMessageEvent event) {
        System.out.println(event.getMessage().getBody().size()==1 &&
                strMessageText.startsWith("-chat"));
        System.out.println(strMessageText);
        return strMessageText.startsWith("-chat");
    }

    private String parsePromptFromMessage(String strMessageText){
        String prompt = strMessageText.replaceFirst("-chat(\\s)*","");
        return prompt;
    }
}

package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.message.*;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import java.io.File;

public class MessageLocalStorageProcessor {
    private static MessageLocalStorageProcessor _instance = null;
    String messageStorageImageFolderPath;
    UltimateInfoStorage uis;
    JSONObject root;

    //singleton
    public static MessageLocalStorageProcessor getInstance(){
        if(_instance == null)
            _instance = new MessageLocalStorageProcessor();
        return _instance;
    }

    private MessageLocalStorageProcessor(){
        //获取图片文件夹地址
        messageStorageImageFolderPath = ListAndAddressHandeler.getImagePath()+"message_image_storage/";
        //获取超级存储器实例
        uis = UltimateInfoStorage.getInstance();
        //get the root object of local_message_storage
        root = uis.getJsonObject("local_message_storage", 1);
    }

    public long storeMessage(Message message){
        long id = System.currentTimeMillis();
        String strId = "lms-"+id;
        //code meanings:
        //-1 - unsupported message item(s) included
        //-2 - unknown error
        //check if all message items are in the list of supported message Item: text, image, at
        for(MessageItem messageItem: message.getBody()){
            //check if messageItem is supported
            if(!(messageItem instanceof Text||
                messageItem instanceof Image||
                messageItem instanceof  At)){
                    return -1;
            }
        }
        //make a jsonArray and each messageItem would be a jsonObject
        JSONArray jsonItems = new JSONArray();
        //store all the messageItems
        try{
            for(MessageItem messageItem : message.getBody()){
                String type = (messageItem instanceof Text)?"Text":(messageItem instanceof Image)?"Image":"At";
                String content=(type.equals("Text")?storeTextObject(messageItem):
                        type.equals("Image")?storeImageObject(messageItem):storeAtObject(messageItem));
                //put the type and content into a new json object
                JSONObject jsonItem = new JSONObject();
                jsonItem.put("type", type);
                jsonItem.put("content", content);
                //store that object in the array
                jsonItems.add(jsonItem);
            }
        } catch(Exception e){return -2;}
        JSONObject jobjMessage = new JSONObject();
        jobjMessage.put("items", jsonItems);
        jobjMessage.put("storage-date", KeyboardToolBox.getBeijingDate());
        root.put(strId, jobjMessage);
        uis.save();
        return id;
    }

    public Message getMessageByID(long id, MessageItemFactory mif){
        return constructMessage(findMessageJsonById(id),mif);
    }

    private JSONObject findMessageJsonById(long id){
        //many pathes never go through the path manager and this is a crime, fuck
        return root.getJSONObject("lms-"+id);
    }

    private Message constructMessage(JSONObject jobjMessage, MessageItemFactory mif){
        if(jobjMessage == null)
            return new Message().plus("消息不存在或已被删除");
        Message message = new Message();
        JSONArray items = jobjMessage.getJSONArray("items");
        for(int i = 0; i< items.size(); i++){
            JSONObject item = items.getJSONObject(i);
            String type = item.getString("type");
            String content = item.getString("content");
            switch (type){
                case "Image":
                    message.plus(constructImageObject(content, mif));
                    break;
                case "At":
                    message.plus(constructAtObject(content, mif));
                    break;
                default:
                    message.plus(constructTextObject(content));
                    break;
            }
        }
        return message;
    }

    private Message constructTextObject(String content){
        return new Message().plus(content);
    }

    private String storeTextObject(MessageItem text){
        return ((Text)text).getText();
    }

    private Message constructImageObject(String fileName, MessageItemFactory mif){
        File image = new File(messageStorageImageFolderPath+fileName);
        if(!image.exists())
            image = new File(messageStorageImageFolderPath+"default-image.png");
        return new Message().plus(mif.imageByFile(image));
    }

    private String storeImageObject(MessageItem image){
        String imageUrl = ((Image)image).getUrl();
        String fileName = "ms-"+System.currentTimeMillis()+".png";
        String filePath = messageStorageImageFolderPath+fileName;
        return (KeyboardToolBox.getImageFileFromUrl(imageUrl, filePath)==null)?"-failed":fileName;
    }

    private Message constructAtObject(String content, MessageItemFactory mif){
        return new Message().plus(mif.at(Long.parseLong(content)));
    }

    private String storeAtObject(MessageItem at){
        long qq = ((At)at).getUser();
        return ""+qq;
    }

}

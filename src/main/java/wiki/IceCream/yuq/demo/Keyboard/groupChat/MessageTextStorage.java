package wiki.IceCream.yuq.demo.Keyboard.groupChat;

import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class MessageTextStorage {
    private static MessageTextStorage instance = null;
    private HashMap<Long, String> messages = new HashMap<>();
    private ArrayList<Long> serviceGroupList = ListAndAddressHandeler.getServiceGroupList();
    private long lastSavedTime = 0;

    public static MessageTextStorage getInstance(){
        if(instance==null){
            instance=new MessageTextStorage();
        }
        return instance;
    }

    public MessageTextStorage(){
        for (long serviceGroup : serviceGroupList){
            try{
                File file = new File(ListAndAddressHandeler.getDependenciesFolderPath()+"word cloud/"+serviceGroup+".txt");
                file.createNewFile();
                messages.put(serviceGroup, readAllBytesJava7(file.getPath()));
            } catch (Exception e){}
        }
    }

    public void add(long group, String messageText){
        String a = messages.get(group) + "\n" + messageText;
        messages.replace(group, a);
        if(System.currentTimeMillis()-lastSavedTime>5*60*1000) {
            lastSavedTime=System.currentTimeMillis();
            save();
        }
    }

    public String get(long group){
        return messages.get(group);
    }

    public void save(){
        for (long serviceGroup : serviceGroupList){
            try{
                File file = new File(ListAndAddressHandeler.getDependenciesFolderPath()+"word cloud/"+serviceGroup+".txt");
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write(messages.get(serviceGroup));
                writer.close();
            } catch (Exception e){}
        }
    }

    public void clear(){
        for (long serviceGroup : serviceGroupList){
            messages.replace(serviceGroup,"");
        }
        save();
    }

    public void clear(long serviceGroup){
        messages.replace(serviceGroup,"");
        save();
    }

    private static String readAllBytesJava7(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (Exception e) {}
        return content;
    }
}
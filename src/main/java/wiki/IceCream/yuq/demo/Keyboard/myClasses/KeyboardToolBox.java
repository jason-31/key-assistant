package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import com.IceCreamQAQ.Yu.util.IO;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.event.GroupMessageEvent;
import com.icecreamqaq.yuq.message.*;
import sun.font.FontDesignMetrics;
import wiki.IceCream.yuq.demo.Keyboard.groupChat.LoveLevelProcessor;
import wiki.IceCream.yuq.demo.Keyboard.ultimateInfoProcessor.UltimateInfoStorage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.List;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.AttributedString;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyboardToolBox {

    private static String imagePath = ListAndAddressHandeler.getImagePath();

    private static boolean containsEmoji(String toCheck){
        return toCheck.length()>1&&toCheck.matches("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+");
    }

    public static String readAllBytesJava7(String filePath)
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        }
        catch (Exception e) {}
        return content;
    }

    public static void writeToFile (String filePath, String content){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(content);
            writer.close();
        } catch (IOException ioException){}
    }

    public static String getBeijingDate(){
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        DateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return formatter.format(calendar.getTime());
    }
    public static BufferedImage getBufferedImageFromUrl(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        BufferedImage bufferedImage = ImageIO.read(url);
        return bufferedImage;
    }

    public static File saveBufferedImage(BufferedImage bufferedImage) throws IOException {
        String filePath = imagePath+"temp/bi_"+System.currentTimeMillis()+".png";
        return saveBufferedImage(bufferedImage, filePath,0);
    }

    public static File saveBufferedImage(BufferedImage bufferedImage, String folderPath)throws IOException {
        String filePath = folderPath+"bi_"+System.currentTimeMillis()+".png";
        return saveBufferedImage(bufferedImage, filePath, 0);
    }

    public static File saveBufferedImage(BufferedImage bufferedImage, String filePath, int useless) throws IOException {
        File savedFile = new File (filePath);
        savedFile.getParentFile().mkdirs();
        ImageIO.write(bufferedImage, "png", savedFile);
        return savedFile;
    }

    public static File getImageFileFromUrl(String imageUrl){
        try{
            return saveBufferedImage(getBufferedImageFromUrl(imageUrl));
        }catch (Exception e){return null;}
    }

    public static File getImageFileFromUrl(String imageUrl, String filePath){
        try{
            return saveBufferedImage(getBufferedImageFromUrl(imageUrl),filePath,0);
        }catch (Exception e){return null;}
    }

    public static BufferedImage convertFileToBufferedImage(File file){
        try {
            return ImageIO.read(file);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static boolean ifLongArrayContains(long[] toCheck, long target){
        return getIndexInLongArray(toCheck,target)!=-1;
    }

    public static int getIndexInLongArray(long[] toCheck, long target){
        for(int i = 0; i< toCheck.length; i++){
            if (toCheck[i]== target) return i;
        }
        return -1;
    }

    public static boolean partialMatch(String toCheck, String regex){
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(toCheck);
        return (m.find());
    }

    public static void sendMessage(long group, YuQ yuq, MessageItemFactory mif, Object input){
        sendMessage(yuq.getGroups().get(group), mif, input);
    }

    public static void sendMessage(GroupMessageEvent event, MessageItemFactory mif, Object input){
        LoveLevelProcessor.getInstance().addOrSubtractLoveLevel(event.getSender().getId(),1);
        sendMessage(event.getGroup(), mif, input);
    }

    public static void sendMessage(Group group, MessageItemFactory mif, Object input){
        Message message = (input instanceof Message)? (Message)input : new Message().plus(input.toString());
        //将过长的消息转化为图片
        int maxMessageTextLength = 60;
        //遍历所有messageitem
        for(int i = 0; i<message.getBody().size(); i++){
            MessageItem messageItem = message.get(i);
            //检测过长text类
            if (messageItem instanceof Text&&messageItem.toString().length()>maxMessageTextLength){
                //创建新消息
                Message newMessage = new Message();
                for(int j = 0; j<message.getBody().size(); j++){
                    //将不需要替换的messageItem加回进新构建消息
                    if(j!=i)
                        newMessage.plus(message.get(j));
                    //仅将当前messageItem替换为图片
                    else{
                        File photo = stringToImage(messageItem.toString(),
                                500,
                                35,
                                new Font("KaiTi", 0,35),
                                Color.WHITE,
                                new Color(42,130, 176)
                                );
                        newMessage.plus(mif.imageByFile(photo));
                    }
                }
                //将之前的回复对象加回新构建的message
                newMessage.setReply(message.getReply());
                //替换message为新message
                message=newMessage;
            }
        }

        //等待
        int a = (int)(Math.random()*1000);
        System.out.print("等待"+String.format(".3d", (a+1000)/1000.0f)+"秒\n");
        try {Thread.sleep(1000+a);} catch (Exception e){}

        //尝试两次发送
        for(int i=0; i<2; i++){
            try{
                System.out.println("sending message...\n"+message);
                group.sendMessage(message);
                break;
            }
            //忽略 IllegalStateException
            catch(IllegalStateException illegalStateException){}
        }
    }

    public static File generateXiBao(String input) throws IOException {
        String path = imagePath +  "xibao_background.jpg";
        boolean ifContainsChinese = partialMatch(input, "[\\u4e00-\\u9fa5]");
        String[] a = (ifContainsChinese)?input.split("(?<=\\G............)|\\n"):input.replaceAll("((?:[^\\s]*\\s){2}[^\\s]*)\\s", "$1\n").split("\n");
        BufferedImage image = ImageIO.read(new File(path));
        Graphics g = image.getGraphics();
        Font font = (ifContainsChinese)? new Font("STSong", Font.BOLD, 50) : new Font("Courier New", Font.BOLD, 60);
        FontMetrics metrics = g.getFontMetrics(font);
        int positionY = (image.getHeight() - metrics.getHeight()*a.length) / 2 + metrics.getAscent();
        for (String text : a) {
            if (image.getWidth() - metrics.stringWidth(text)<10) {
                Font localFont = new Font(font.getName(), Font.BOLD, font.getSize());
                while (image.getWidth() - metrics.stringWidth(text)<30) {
                    localFont = new Font(font.getName(), Font.BOLD,localFont.getSize()-2);
                    metrics = g.getFontMetrics(localFont);
                }
                font = localFont;
            }
            metrics = g.getFontMetrics(font);
            int positionX = (image.getWidth() - metrics.stringWidth(text)) / 2;
            AttributedString attributedText = new AttributedString(text);
            attributedText.addAttribute(TextAttribute.FONT, font);
            attributedText.addAttribute(TextAttribute.BACKGROUND, Color.RED);
            g.drawString(attributedText.getIterator(), positionX, positionY);
            positionY += metrics.getHeight();
        }
        String address = imagePath + "temp/xibao_" + System.currentTimeMillis() + ".png";
        File file = new File(address);
        ImageIO.write(image, "png", file);
        return file;
    }

    public static File stringToImage(String text){
        Font defaultFont = new Font("KaiTi", Font.BOLD, 25);
        int defaultWidth = 400;
        int defaultLineHeight = 25;
        Color defaultBackgroundColor = Color.WHITE;
        Color defaultTextColor = Color.BLACK;
        return stringToImage(text, defaultWidth, defaultLineHeight, defaultFont, defaultBackgroundColor, defaultTextColor);
    }

    public static File stringToImage(String text, int textWidth, int lineHeight, Font font, Color backgroundColor, Color textColor){
        String filePath = ListAndAddressHandeler.getImagePath()+"/temp/ci_"+System.currentTimeMillis()+".png";
        File outFile = new File(filePath);
        FontMetrics fm = FontDesignMetrics.getMetrics(font);
        int fullCharWidth = fm.charWidth('字');// 标点符号也算一个字
        //确定emoji字体
        int maxEmojiSize = 2*font.getSize(), emojiSize=maxEmojiSize;
        while(FontDesignMetrics.getMetrics(new Font("Segoe UI Emoji", Font.PLAIN, emojiSize--)).stringWidth("\uD83D\uDE02")>fullCharWidth
                &&emojiSize>1){}
        Font emojiFont = new Font("Segoe UI Emoji", 0, emojiSize);
        System.out.println(emojiSize);
        //将String转为list
        ArrayList<String> strList = new ArrayList<>();
        String buffer = "";
        //遍历所有char
        int count = 0;
        for(char reading : text.toCharArray()) {
            buffer+=(reading=='\n')?"":reading;
            if(++count==text.length()
                    ||(fm.stringWidth(buffer)>=textWidth-fullCharWidth&&!containsEmoji("" + reading + text.charAt(count)))
                    ||reading=='\n'){
                strList.add(buffer);
                buffer = "";
            }
        }

        //计算图片的高度，多预留一行
        int imageHeight = (strList.size()+1) * lineHeight;

        // 创建图片  宽度多预留一点
        BufferedImage image = new BufferedImage(textWidth + 20, imageHeight,
                BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, textWidth + 20, imageHeight);
        g.setColor(backgroundColor); // 背景色
        g.fillRect(0, 0, textWidth + 20, imageHeight);
        g.setColor(textColor);//  字体颜色

        for (int i = 0; i < strList.size();) { //逐行绘制
            String lineToDraw = strList.get(i);
            System.out.println(lineToDraw);
            int x = 10, y=lineHeight * (++i);
            //逐字绘制
            for(int j = 0; j<lineToDraw.length();j++){
                String drawing = ""+lineToDraw.charAt(j), nextTwo = drawing+((j!=lineToDraw.length()-1)?lineToDraw.charAt(j+1):"");
//                System.out.println(drawing);
                System.out.print(nextTwo);
                //System.out.println(nextTwo.length());
                boolean ifEmoji = containsEmoji(nextTwo);
                System.out.println(", "+ifEmoji);
                if(ifEmoji) {
                    drawing = nextTwo;
                    j++;
                }
                Font using = (ifEmoji)?emojiFont:font;
                g.setFont(using);// 设置画笔字体
                g.drawString(drawing,x,y);
                x+=fm.stringWidth(drawing);
            }
        }
        g.dispose();
        try {
            ImageIO.write(image, "png", outFile);// 输出png图片
        } catch (Exception e){}
        return outFile;
    }

//    public static File messageToImage(Message message){
//        Font defaultFont = new Font("KaiTi", Font.BOLD, 25);
//        int defaultWidth = 400;
//        int defaultLineHeight = 25;
//        Color defaultBackgroundColor = Color.WHITE;
//        Color defaultTextColor = Color.BLACK;
//        return messageToImage(message, defaultWidth, defaultLineHeight, defaultFont, defaultBackgroundColor, defaultTextColor);
//    }
//
////    public static File messageToImage(Message message, int textWidth, int lineHeight, Font font, Color backgroundColor, Color textColor){
////        //获取message的body
////        ArrayList<String> body = new ArrayList<>();
////        for(int i = 0; i<message.getBody().size();i++){
////            body.add(message.get(i).toString());
////        }
////    }

    public static boolean stringListContains(ArrayList<String> stringArrayList, String target){
        for(String string : stringArrayList){
            if (string.equals(target)) return true;
        }
        return false;
    }

    public static int getAdminLevel(GroupMessageEvent event){
        return getAdminLevel(event.getGroup(), event.getSender().getId());
    }

    public static int getAdminLevel(long group, long qq, YuQ yuq){
        return getAdminLevel(yuq.getGroups().get(group), qq);
    }

    public static int getAdminLevel(Group group, long qq){
        int level = 0;
        if(ListAndAddressHandeler.getBotAdmins().contains(qq)) return 3;
        level = group.getMembers().get(qq).getPermission();
        int specialLevel = getSpecialAdminLevel(group.getId(), qq);
        return (specialLevel>=0)?specialLevel:level;
    }

    public static int getSpecialAdminLevel(long group, long qq){
        try{
            JSONArray admins = UltimateInfoStorage.getInstance().getObjectFromUserOrGroup("special-admin-level",""+group, 3).getJSONArray("admins");
            for(int i = 0; i<admins.size(); i++){
                JSONObject admin = admins.getJSONObject(i);
                if(admin.getLong("id")==qq){
                    return admin.getInteger("level");
                }
            }
            return -1;
        }catch (Exception e){return -1;}
    }


}

package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import oshi.SystemInfo;

import java.util.ArrayList;
import java.util.Arrays;

public class ListAndAddressHandeler {
    private static ArrayList<Long> serviceGroupList = new ArrayList<Long>(Arrays.asList(
            479955083L, //平安祥和
            743186065L, //鸡腿群
            651863303L, //易辙群
            926754397L, //上海偶像部
            518837062L, //开开心心搞无料
            545993596L, //墨玉社
            664176266L, //东代会
            580125641L, //白兰社
            479825009L, //之酱指挥部
            693409005L, //摇滚基金会
            106211189L //阿水群
    ));

    private static ArrayList<Long> numOnlyGroups = new ArrayList<Long>(Arrays.asList(133264448L,570880140L,668340190L));
    private static ArrayList<Long> limitActivaGroups = new ArrayList<Long>(Arrays.asList(716878201L,479955083L));
    private static ArrayList<Long> botAdmins = new ArrayList<Long>(Arrays.asList(1726924001L,1920957511L));
    private static ArrayList<Long> mercariGroupList = new ArrayList<Long>(Arrays.asList(651863303L,664176266L));
    private static String dependenciesFolderPath = (new SystemInfo().getOperatingSystem().toString().contains("Windows"))?"C:/Keyboard Assistant Dependency Folder/" : "/srv/dev-disk-by-uuid-54ed81ad-4b45-4c2e-bce1-6e0349270890/programs/Keyboard Assistant Dependency Folder/";
    private static String imagePath = dependenciesFolderPath+"image/";
    private static String voicePath = dependenciesFolderPath+"voice/";


    public ListAndAddressHandeler(){

    }

    public static ArrayList<Long> getServiceGroupList (){
        System.out.println(serviceGroupList);
        return serviceGroupList;
    }

    public static ArrayList<Long> getNumOnlyGroups(){
        return numOnlyGroups;
    }

    public static ArrayList<Long> getLimitActivaGroups() {
        return limitActivaGroups;
    }

    public static ArrayList<Long> getBotAdmins() {
        return botAdmins;
    }

    public static ArrayList<Long> getMercariGroupList(){
        return mercariGroupList;
    }

    public static String getImagePath(){return imagePath;}

    public static String getVoicePath(){return voicePath;}

    public static String getDependenciesFolderPath(){return dependenciesFolderPath;}
}

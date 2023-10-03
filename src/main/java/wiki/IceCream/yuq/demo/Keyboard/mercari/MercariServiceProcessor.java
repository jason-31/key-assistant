package wiki.IceCream.yuq.demo.Keyboard.mercari;


import com.icecreamqaq.yuq.YuQ;
import com.icecreamqaq.yuq.entity.Contact;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;

import java.util.ArrayList;
import java.util.HashMap;

public class MercariServiceProcessor {

    private static MercariServiceProcessor instance = null;

    private HashMap<Long, long[]> groupsAdmins;
    private HashMap<Long, Integer> exchangeRates;
    private HashMap<Long, long[]> cutGroupsAndCutters;
    private MercariRecordProcessor mercariRecordProcessor;
    private MercariInfoGetter mercariInfoGetter;
    private HashMap<Long, Group> noticeTargetsMap =null;


    public static MercariServiceProcessor getInstance(){
        if (instance==null){
            instance = new MercariServiceProcessor();
        }
        return instance;
    }
    public MercariServiceProcessor(){
        mercariRecordProcessor = MercariRecordProcessor.getInstance();
        mercariInfoGetter = MercariInfoGetter.getInstance();
        //add each group's info
        groupsAdmins = new HashMap<>();
        exchangeRates = new HashMap<>();
        cutGroupsAndCutters = new HashMap<>();
        //易辙团
        long[] adminsYiZhe = {810905592L,1726924001L,2481700992L,639020952L,775206650L};
        long[] cutGroupAndCutterYizhe = {709796752L, 3591574554L};
        groupsAdmins.put(651863303L, adminsYiZhe);
        exchangeRates.put(651863303L, 58);
        cutGroupsAndCutters.put(651863303L,cutGroupAndCutterYizhe);


        //kukka
        long[] adminsKukka = {1726924001L, 627316395L, 759943623L};
        long[] cutGroupAndCutterKukka = {668340190L, 759943623L};
        groupsAdmins.put(664176266L, adminsKukka);
        exchangeRates.put(664176266L, 63);
        cutGroupsAndCutters.put(664176266L,cutGroupAndCutterKukka);
    }

    public int getExchangeRates (long group){
        return exchangeRates.get(group);
    }

    public long[] getCutGroupAndCutter(long group){
        return cutGroupsAndCutters.get(group);
    }

    public Group getNoticeTarget (long group, YuQ yuq){
        initializeNoticeTargetMap(yuq);
        return noticeTargetsMap.get(group);
    }

    public void initializeNoticeTargetMap(YuQ yuq){
        if (noticeTargetsMap == null) {
            noticeTargetsMap = new HashMap<>();
            noticeTargetsMap.put(651863303L, yuq.getGroups().get(133264448L));
            noticeTargetsMap.put(664176266L, yuq.getGroups().get(668340190L));
        }
    }

    public long getMercariGroupFromNoticeGroup(long noticeGroup, YuQ yuq){
        initializeNoticeTargetMap(yuq);
        for (long mercariGroup : noticeTargetsMap.keySet()){
            try {
                if (noticeTargetsMap.get(mercariGroup).getId() == noticeGroup) return mercariGroup;
            } catch (Exception e){}
        }
        return 0;
    }

    public ArrayList<Long> getAdminedGroups(long qq){
        ArrayList<Long> adminedGroups  = new ArrayList();
        for (long group : groupsAdmins.keySet()){
            long[] admins = groupsAdmins.get(group);
            for (long admin : admins){
                if (admin == qq) {
                    adminedGroups.add(group);
                    break;
                }
            }
        }
        return adminedGroups;
    }

    public String getUserAllowance(long userId, long group, YuQ yuq){
        //获取群名片
        String nameCard = "";
        try{
            nameCard = yuq.getGroups().get(group).getMembers().get(userId).nameCardOrName();
            if (nameCard==null||nameCard=="") throw new Exception();
        } catch (Exception e) {
            return "获取群名片出错，请确认是否还在群中";
        }
        return mercariRecordProcessor.getUserAllowance(group, userId, nameCard);
    }

    public String changeUserAllowance(long group, long userId, int newAllowance){
        return mercariRecordProcessor.changeUserAllowance(group, userId,newAllowance);
    }

    public String parseTrustedUsers (long[] infoPiece, YuQ yuq){
        long user = infoPiece[0];
        int allowance = (int)infoPiece[1];
        long group = infoPiece[2];
        String userNameCard = (yuq.getGroups().get(group).getMembers().get(user)==null)?
                "找不到用户":yuq.getGroups().get(group).getMembers().get(user).nameCardOrName();
        String groupName = yuq.getGroups().get(group).getName();
        return  "用户“"+userNameCard+"”， 群“"+groupName+"”， 单日上限："+allowance;
    }

    public String getTrustedUsersString(long qq, YuQ yuq){
        ArrayList<Long> adminedGroups = getAdminedGroups(qq);
        String list = "用户列表：";
        if (adminedGroups.size()==0) return "你没有此项权限";
        ArrayList<long[]> usersInfo = mercariRecordProcessor.getTrustedUsers(adminedGroups);
        for (long[] infoPiece : usersInfo){
            list += "\n"+parseTrustedUsers(infoPiece,yuq);
        }
        return list;
    }

    public String getTrustedUsersString(long qq, long group, YuQ yuq){
        if (!getAdminedGroups(qq).contains(group)) return "你没有此项权限";
        String list = "用户列表：";
        ArrayList<long[]> usersInfo = mercariRecordProcessor.getTrustedUsers(group);
        for (long[] infoPiece : usersInfo){
            list += "\n"+parseTrustedUsers(infoPiece,yuq);
        }
        return list;
    }

    public String getUserHistory (long user, long qq){
        ArrayList<Long> adminedGroup = getAdminedGroups(qq);
        if (adminedGroup.size()==0) return "你没有此权限";
        return mercariRecordProcessor.getUserHistory(user,adminedGroup);
    }

    public String getUserHistory (long user){
        return mercariRecordProcessor.getUserHistory(user);
    }

    public String getCertainDayHistory(long qq, String date){
        ArrayList<Long> adminedGroup = getAdminedGroups(qq);
        if (adminedGroup.size()==0) return "你没有此权限";
        return mercariRecordProcessor.getCertainDayHistory(date, adminedGroup);
    }

    public String getTodayHistory(long qq){
        ArrayList<Long> adminedGroup = getAdminedGroups(qq);
        if (adminedGroup.size()==0) return "你没有此权限";
        return mercariRecordProcessor.getTodayHistroy(adminedGroup);
    }

    public String getRecentHistory(int num, long group){
        return mercariRecordProcessor.getRecentHistory(num, group);
    }

    public String allUploaded(long group){
        System.out.println("called"); return mercariRecordProcessor.allUploaded(group);
    }

    public String cut(String id, long qq, YuQ yuq){
        id=id.replaceAll("[^0-9]","");
        ArrayList<Long> adminedGroup = getAdminedGroups(qq);
        if (adminedGroup.size()==0) return "你没有此权限";
        XSSFRow row= mercariRecordProcessor.getHistoryRowByID(id, adminedGroup);
        if (row==null) return "未找到记录";
        long userId = (long)row.getCell(1).getNumericCellValue();
        try {
            yuq.getFriends().get(userId).sendMessage("您编号为"+row.getCell(0).getStringCellValue()+"的切煤已被记录为切到！");
            Thread.sleep(3000);
        } catch(Exception e){}
        return mercariRecordProcessor.setProperty(row,3,true);
    }

    public String setProperty(long qq, String id, int cellIndex, boolean property ){
        id=id.replaceAll("[^0-9]","");
        ArrayList<Long> adminedGroup = getAdminedGroups(qq);
        if (adminedGroup.size()==0) return "你没有此权限";
        return mercariRecordProcessor.setProperty(id, adminedGroup, cellIndex, property);
    }

    //查询
    public Object browseMercari(String link, MessageItemFactory mif) {
        String strReturn="切煤小助手为您服务";
        long l = 0;
        int maxAllowance = 0;
        int price = 0;
        float rmbPrice = 0.0f;

        //获取网址
        link=link.replaceAll("[^0-9]","");
        if (link.length()!=11){
            strReturn+="\n获取编号失败：不是煤炉编号";
            return strReturn;
        }

        //获取信息
        String[] info = mercariInfoGetter.getItemInfoFromMercari(("https://jp.mercari.com/item/m" + link));
        if(info==null){
            strReturn+=("\n获取信息出错");
            return strReturn;
        }
        strReturn+="\n商品名称为：" + info[1];
        strReturn+="\nsold状态为：" + info[3];
        strReturn+=("\n价格为："+info[0]);
        rmbPrice=Integer.parseInt(info[0])*(float)62/1000;
        strReturn+=("\n"+56+"汇对应为："+String.format("%.2f", rmbPrice)+"\n您忠实的小助手根据sold情况认为它"+((info[3].contains("已"))?"很好价哦！！！\n":"不好价哦！！！\n"));
        return new Message().plus(strReturn).plus(mif.imageByUrl(info[2]));
    }

    public Object searchMercari(Message message, MessageItemFactory mif){
        Message msgReturn = new Message();
        try{
            String keyword = message.getBody().get(0).toString().split("#搜索(\\s)*")[1];
            System.out.println(keyword);
            msgReturn.plus(keyword+"的搜索结果为：");
            String[][] result = mercariInfoGetter.searchOnMercari(keyword);
            if (result==null) throw new Exception("没有有效结果");
            int i=1;
            for(String[] itemInfo : result){
                msgReturn.plus("\n商品"+i+"：\n名称："+itemInfo[0]+"\n价格："+itemInfo[1]+"\n编号："+itemInfo[3]+"\n").plus(mif.imageByUrl(itemInfo[2]));
                i++;
            }
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException){return "关键词为空";}
        catch (Exception exception){return "搜索出错："+exception.getMessage();}
        System.out.println(msgReturn.toLogString());
        return msgReturn;
    }

    public String[] getItemInfoFromMercari (String url){
        return mercariInfoGetter.getItemInfoFromMercari(url);
    }

    public boolean checkIfAlreadyInRecord(String link){
        return mercariRecordProcessor.getHistoryRowByID(link)!=null;
    }

    public boolean checkIfAlreadyInRecord(String link, long group){
        ArrayList<Long> list = new ArrayList<>();
        list.add(group);
        return mercariRecordProcessor.getHistoryRowByID(link,list)!=null;
    }

    public ArrayList<Long> getParticipateGroups(long qq){
        return mercariRecordProcessor.getParticipateGroups(qq);
    }

    public int getUserBalance(long qq, long group){
        return mercariRecordProcessor.getUserBalance(qq, group);
    }

    public boolean setUserBalance(long qq, long group, int newBalance){
        return mercariRecordProcessor.setUserBalance(qq, group, newBalance);
    }

    public boolean saveNewHistory(long group, long qq, int price , String link, String nameCard, String date){
        return mercariRecordProcessor.saveNewHistory(group,qq,price,link,nameCard,date);
    }

    public String addNewUser(long group, long qq, int allowance){
        return mercariRecordProcessor.addNewUser(group, qq, allowance);
    }

    public String removeUser(long group, long qq){
        return mercariRecordProcessor.removeUser(group, qq);
    }

    public String deleteHistroy(String id, long qq, YuQ yuq){
        id=id.replaceAll("[^0-9]","");
        ArrayList<Long> adminedGroups = getAdminedGroups(qq);
        if (adminedGroups.size()==0) return "你没有此权限";
        Object[] result = mercariRecordProcessor.getHistoryRowByID(id);
        if (result == null) return "未找到记录";
        XSSFRow historyRow = (XSSFRow) result[0];
        long group = (Long)result[1];
        if (!adminedGroups.contains(group)) return "未找到记录";
        String date = historyRow.getCell(6).getStringCellValue();
        if (historyRow.getCell(3).getBooleanCellValue()||historyRow.getCell(4).getBooleanCellValue()) return "不能删除已切/已肾记录";
        long userId = (long)historyRow.getCell(1).getNumericCellValue();
        int price = (int)historyRow.getCell(2).getNumericCellValue();
        String mercariId = historyRow.getCell(0).getStringCellValue();
        if(!mercariRecordProcessor.deleteRow(historyRow)) return "移除出错";
        if(date.equals(KeyboardToolBox.getBeijingDate())){
            //将上限扣除部分添加回去
            int balance = getUserBalance(qq,group);
            balance += price;
            if(!mercariRecordProcessor.setUserBalance(qq, group, balance)) return "重设余额出错";
        }
        //通知对方
        try {
            yuq.getFriends().get(userId).sendMessage("您编号为"+mercariId+"的切煤记录已被删除！已将扣除上限撤销。\n请及时自行或联系管理员退肾");
            Thread.sleep(3000);
        } catch(Exception e){}
        return "删除记录成功";
    }


}

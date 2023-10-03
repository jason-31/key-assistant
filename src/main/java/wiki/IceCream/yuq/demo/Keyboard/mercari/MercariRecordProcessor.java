package wiki.IceCream.yuq.demo.Keyboard.mercari;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox;
import wiki.IceCream.yuq.demo.Keyboard.myClasses.ListAndAddressHandeler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static wiki.IceCream.yuq.demo.Keyboard.myClasses.KeyboardToolBox.getBeijingDate;

public class MercariRecordProcessor {
    private static MercariRecordProcessor instance = null;

    private String excelFolderPath = ListAndAddressHandeler.getDependenciesFolderPath()+"Assistant Forms/";
    private HashMap<Long, XSSFSheet> groupsMercariHistorySheetMap;
    private HashMap<Long, XSSFSheet> groupsMercariUsersSheetMap;
    private ArrayList<Long> mercariGroupList;

    MercariRecordProcessor(){
        groupsMercariUsersSheetMap = new HashMap<>();
        groupsMercariHistorySheetMap = new HashMap<>();
        mercariGroupList = ListAndAddressHandeler.getMercariGroupList();
        //put the Wb and Sheets into the HashMap
        for (long group : mercariGroupList) {
            try {
                //users
                String path = excelFolderPath +"mercariUsers/"+group+ ".xlsx";
                XSSFWorkbook buffer = new XSSFWorkbook(new FileInputStream(path));
                groupsMercariUsersSheetMap.put(group,buffer.getSheet("Sheet1"));
                //history
                path = excelFolderPath +"mercariHistory/"+group+ ".xlsx";
                buffer = new XSSFWorkbook(new FileInputStream(path));
                groupsMercariHistorySheetMap.put(group,buffer.getSheet("Sheet1"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public static MercariRecordProcessor getInstance(){
        if(instance==null){
            instance=new MercariRecordProcessor();
        }
        return instance;
    }


    public boolean ifAlreadySent(long group, String mercariId){return true;}

    public void save() throws FileNotFoundException, IOException {
        for (long group : mercariGroupList) {
            //user
            String path = excelFolderPath +"mercariUsers/"+group+ ".xlsx";
            groupsMercariUsersSheetMap.get(group).getWorkbook().write(new FileOutputStream(path));
            path = excelFolderPath +"mercariHistory/"+group+ ".xlsx";
            groupsMercariHistorySheetMap.get(group).getWorkbook().write(new FileOutputStream(path));
        }
    }

    public Object[] getHistoryRowByID (String id){
        for (long group : mercariGroupList){
            XSSFSheet mercariHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
            int lastRowNum  = mercariHistoryWorkbookSheet.getLastRowNum();
            for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
                XSSFRow row = mercariHistoryWorkbookSheet.getRow(rowNum);
                if (id.equals(row.getCell(0).getStringCellValue())){
                    Object[] info = {row, group};
                    return info;
                }
            }
        }
        return null;
    }

    public XSSFRow getHistoryRowByID (String id, ArrayList<Long> groupsCanBeView){
        for (long group : groupsCanBeView){
            XSSFSheet mercariHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
            int lastRowNum  = mercariHistoryWorkbookSheet.getLastRowNum();
            for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
                XSSFRow row = mercariHistoryWorkbookSheet.getRow(rowNum);
                if (id.equals(row.getCell(0).getStringCellValue())){
                    return row;
                }
            }
        }
        return null;
    }

    public ArrayList<Long> getParticipateGroups(long id){
        ArrayList<Long> groups = new ArrayList<>();
        for (long group : mercariGroupList){
            XSSFSheet mercariUsersWorkbookSheet = groupsMercariUsersSheetMap.get(group);
            int lastRowNum  = mercariUsersWorkbookSheet.getLastRowNum();
            for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
                XSSFRow row = mercariUsersWorkbookSheet.getRow(rowNum);
                if (id==(long)row.getCell(0).getNumericCellValue()){
                    groups.add(group);
                }
            }
        }
        return groups;
    }



    public XSSFRow getUserRowByID(long id, long group) {
        XSSFSheet mercariUsersWorkbookSheet = groupsMercariUsersSheetMap.get(group);
        int lastRowNum  = mercariUsersWorkbookSheet.getLastRowNum();
        for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
            XSSFRow row = mercariUsersWorkbookSheet.getRow(rowNum);
            if (id==(long)row.getCell(0).getNumericCellValue()){
                return row;
            }
        }
        return null;
    }

    public void updateUserBalance(XSSFRow row){
        if (row==null) return;
        if (!row.getCell(3).getStringCellValue().equals(getBeijingDate())){
            row.getCell(2).setCellValue(row.getCell(1).getNumericCellValue());
            try{save();} catch(Exception e){}
        }
    }

    public boolean saveNewHistory (long group, long qq, int price, String link, String nameCard){
        return saveNewHistory(group, qq, price, link, nameCard, getBeijingDate());
    }
    public boolean saveNewHistory (long group, long qq, int price, String link, String nameCard, String date){
        XSSFSheet xssfSheet = groupsMercariHistorySheetMap.get(group);
        XSSFRow newRow = xssfSheet.createRow(xssfSheet.getLastRowNum() + 1);
        newRow.createCell(0, CellType.STRING);
        newRow.getCell(0).setCellValue(link);
        newRow.createCell(1, CellType.NUMERIC);
        newRow.getCell(1).setCellValue(qq);
        newRow.createCell(2, CellType.NUMERIC);
        newRow.getCell(2).setCellValue(price);
        newRow.createCell(3, CellType.BOOLEAN);
        newRow.createCell(4, CellType.BOOLEAN);
        newRow.createCell(5, CellType.STRING);
        newRow.getCell(5).setCellValue(nameCard);
        newRow.createCell(6, CellType.STRING);
        newRow.getCell(6).setCellValue(date);
        newRow.createCell(7, CellType.BOOLEAN);
        try {
            save();
        }catch(Exception e){
            return false;
        }
        return true;
    }

    public String addNewUser(long group, long userId, int allowance){
        try{
            if (getUserRowByID(userId, group)!=null) return "该用户已存在";
            XSSFSheet mercariUsersWorkbookSheet = groupsMercariUsersSheetMap.get(group);
            XSSFRow newRow =mercariUsersWorkbookSheet.createRow(mercariUsersWorkbookSheet.getLastRowNum()+1);
            newRow.createCell(0,CellType.NUMERIC);
            newRow.getCell(0).setCellValue(userId);
            newRow.createCell(1,CellType.NUMERIC);
            newRow.getCell(1).setCellValue(allowance);
            newRow.createCell(2,CellType.NUMERIC);
            newRow.getCell(2).setCellValue(allowance);
            newRow.createCell(3,CellType.STRING);
            newRow.getCell(3).setCellValue(getBeijingDate());
            save();
            return "添加成功";
        } catch(Exception e){
            e.printStackTrace();
            return "添加出错，请联系管理员";
        }
    }

    public String removeUser(long group, long userId){
        //find the right row
        XSSFRow row = getUserRowByID(userId, group);
        if (row == null) return "未找到用户";
        return (deleteRow(row))?"移除成功":"移除出错，请联系管理员";
    }

    public String allUploaded(long group){
        try {
        XSSFSheet mercariHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
        int lastRowNum = mercariHistoryWorkbookSheet.getLastRowNum();
        for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
            XSSFRow row = mercariHistoryWorkbookSheet.getRow(rowNum);
            if (row.getCell(3).getBooleanCellValue())
                row.getCell(7).setCellValue(true);
        }
        save();
            return "记录成功";
        }catch(Exception e){
            return "切煤记录保存出错，请联系管理员";
        }
    }

    public String getUserAllowance(long group, long userId, String nameCard){
        XSSFRow row = getUserRowByID(userId, group);
        if (row == null)
            return "用户不存在";
        int allowance = (int)row.getCell(1).getNumericCellValue();
        updateUserBalance(row);
        int balance = (int)row.getCell(2).getNumericCellValue();
        return nameCard+"的单日切煤上限为："+allowance+"\n今日剩余为："+balance;
    }

    public String changeUserAllowance(long group, long userId, int newAllowance){
        try{
            XSSFRow row = getUserRowByID(userId, group);
            if (row == null) return "未找到用户";
            int oldAllowance = (int)row.getCell(1).getNumericCellValue();
            int oldBalance = (int)row.getCell(2).getNumericCellValue();
            int newBalance = newAllowance-oldAllowance+oldBalance;
            if (newBalance<0) newBalance=0;
            row.getCell(1).setCellValue(newAllowance);
            row.getCell(2).setCellValue(newBalance);
            save();
        }
        catch(Exception e){
            return "保存出错，请联系管理员";
        }
        return "更改成功";
    }

    public ArrayList<long[]> getTrustedUsers(ArrayList<Long> adminedGroups){
        ArrayList<long[]> trustedUsers = new ArrayList<>();
        for (long group : adminedGroups){
            trustedUsers.addAll(getTrustedUsers(group));
        }
        return trustedUsers;
    }

    public ArrayList<long[]> getTrustedUsers(long group){
        ArrayList<long[]> trustedUsers = new ArrayList<>();
        XSSFSheet mercariUsersWorkbookSheet = groupsMercariUsersSheetMap.get(group);
        int lastRowNum  = mercariUsersWorkbookSheet.getLastRowNum();
        for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
            XSSFRow row = mercariUsersWorkbookSheet.getRow(rowNum);
            long user = (long)row.getCell(0).getNumericCellValue();
            int allowance = (int)row.getCell(1).getNumericCellValue();
            long[] buffer = {user, allowance,group};
            trustedUsers.add(buffer);
        }

        return trustedUsers;
    }

    public String getUserHistory(long user, ArrayList<Long> groupsCanView){
        String strReturn = "用户记录：";
        //遍历每个表
        for (long group : groupsCanView){
            XSSFSheet mercuriHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
            int lastRowNum = mercuriHistoryWorkbookSheet.getLastRowNum();
            for(int rowNum = 1;rowNum <= lastRowNum;rowNum++){
                XSSFRow row = mercuriHistoryWorkbookSheet.getRow(rowNum);
                if (user==(long)row.getCell(1).getNumericCellValue()){
                    String date = row.getCell(6).getStringCellValue();
                    String mercariId = row.getCell(0).getStringCellValue();
                    int price = (int)row.getCell(2).getNumericCellValue();
                    boolean ifCut = row.getCell(3).getBooleanCellValue();
                    boolean ifKidneyed = row.getCell(4).getBooleanCellValue();
                    strReturn+="\n"+date+" "+mercariId+" "+price+" "+
                            (ifCut?"已切":"未切")+" "+(ifKidneyed?"已肾":"未肾");
                }
            }
        }
        return strReturn;
    }

    public String getUserHistory(long user){
        return getUserHistory(user, mercariGroupList);
    }

    public String getCertainDayHistory(String date, ArrayList<Long> groupsCanView){
        String strReturn = "当日记录：";
        boolean starts = false;
        for (long group : groupsCanView) {
            XSSFSheet mercuriHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
            int lastRowNum = mercuriHistoryWorkbookSheet.getLastRowNum();
            for (int rowNum = 1; rowNum <= lastRowNum; rowNum++) {
                XSSFRow row = mercuriHistoryWorkbookSheet.getRow(rowNum);
                if (date.equals(row.getCell(6).getStringCellValue())) {
                    String userNameCard = row.getCell(5).getStringCellValue();
                    String mercariId = row.getCell(0).getStringCellValue();
                    int price = (int) row.getCell(2).getNumericCellValue();
                    boolean ifCut = row.getCell(3).getBooleanCellValue();
                    boolean ifKidneyed = row.getCell(4).getBooleanCellValue();
                    boolean ifUploaded = row.getCell(7).getBooleanCellValue();
                    strReturn += "\n" + userNameCard + " " + mercariId + " " + price + " " +
                            (ifCut ? "已切" : "未切") + " " + (ifKidneyed ? "已肾" : "未肾") + " " +
                            (ifUploaded ? "已传" : "未传") + "群："+group;
                    starts = true;
                } else {
                    if (starts) break;
                }
            }
        }
        return strReturn;
    }

    public String getTodayHistroy(ArrayList<Long> groupsCanView){
        return getCertainDayHistory(getBeijingDate(), groupsCanView);
    }

    public String getRecentHistory (int num, long group){
        String strReturn = "最近记录：";
        XSSFSheet mercuriHistoryWorkbookSheet = groupsMercariHistorySheetMap.get(group);
        int lastRowNum = mercuriHistoryWorkbookSheet.getLastRowNum();
        if(num>lastRowNum) {
            strReturn+= "表格中没有足够条数！！已显示所有条目，共"+lastRowNum+"条";
            num=lastRowNum;
        }
        for(int rowNum = lastRowNum; rowNum > lastRowNum-num;rowNum--){
            XSSFRow row = mercuriHistoryWorkbookSheet.getRow(rowNum);
            String nameCard = row.getCell(5).getStringCellValue();
            String date = row.getCell(6).getStringCellValue();
            String mercariId = row.getCell(0).getStringCellValue();
            int price = (int)row.getCell(2).getNumericCellValue();
            boolean ifCut = row.getCell(3).getBooleanCellValue();
            boolean ifKidneyed = row.getCell(4).getBooleanCellValue();
            boolean ifUploaded = row.getCell(7).getBooleanCellValue();
            strReturn+="\n"+nameCard+" "+date+" "+mercariId+" "+price+" "+(ifCut?"已切":"未切")+" "+(ifKidneyed?"已肾":"未肾")+" "+(ifUploaded?"已传":"未传");
        }
        if (strReturn=="") strReturn=" 未找到记录";
        return strReturn;
    }

    public String setProperty(String id, ArrayList<Long> groupsCanBeViewed, int cellIndex, boolean property){
        XSSFRow row= getHistoryRowByID(id, groupsCanBeViewed);
        if (row==null) return "未找到记录";
        return setProperty(row, cellIndex,property);
    }

    public String setProperty(XSSFRow row, int cellIndex, boolean property){
        try{
            row.getCell(cellIndex).setCellValue(property);
            save();
        }
        catch (Exception e) {
            return e.getMessage();
        }
        return "记录成功";
    }

    public int getUserBalance(long qq, long group){
        XSSFRow userRow = getUserRowByID(qq, group);
        updateUserBalance(userRow);
        int balance = (int)userRow.getCell(2).getNumericCellValue();
        return balance;
    }

    public boolean setUserBalance(long qq, long group, int newBalance){
        try {
            XSSFRow userRow = getUserRowByID(qq, group);
            updateUserBalance(userRow);
            userRow.getCell(2).setCellValue(newBalance);
            userRow.getCell(3).setCellValue(KeyboardToolBox.getBeijingDate());
            int balance = (int) userRow.getCell(2).getNumericCellValue();
            return true;
        }
        catch(Exception e){return false;}
    }
    public boolean deleteRow(XSSFRow row){
        //删除记录
        try{
        int rowIndex = row.getRowNum();
        int lastRowNum=row.getSheet().getLastRowNum();
        if(rowIndex>=0&&rowIndex<lastRowNum){
            row.getSheet().shiftRows(rowIndex+1,lastRowNum, -1);
        }
        if(rowIndex==lastRowNum){
            row.getSheet().removeRow(row);
        }
        save();
        return true;
        } catch(Exception e){
            return false;
        }
    }
}

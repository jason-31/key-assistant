package wiki.IceCream.yuq.demo.Keyboard.bilibili;

import java.util.ArrayList;

public class Up {
    public String mid;
    public String lastvid;
    public ArrayList<Long> groups;

    public Up(String mid, String lastvid, ArrayList<Long> groups){
        this.mid =mid;
        this.lastvid = lastvid;
        this.groups = groups;
    }

    public void setLastvid(String lastvid){
        this.lastvid = lastvid;
    }
}

package wiki.IceCream.yuq.demo.Keyboard.groupChat;

public class WordBanner {
    private String _word;
    private long _endTime;
    private int _banLength;

    public WordBanner(String word, int banLength){
        _word = word;
        _banLength = banLength;
        _endTime = System.currentTimeMillis()+banLength*60*1000;
    }

    public String getWord(){
        return _word;
    }

    public long getEndTime(){
        return _endTime;
    }

    public int getBanLength(){
        return _banLength;
    }

}

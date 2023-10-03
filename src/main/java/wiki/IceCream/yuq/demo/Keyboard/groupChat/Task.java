package wiki.IceCream.yuq.demo.Keyboard.groupChat;

public class Task {
    private long _group;
    private long _from;
    private String _task;

    public Task(long group, long from, String task){
        _group = group;
        _from = from;
        _task = task;
    }

    public long get_from() {
        return _from;
    }

    public long get_group() {
        return _group;
    }

    public String get_task(){
        return _task;
    }

    public String toString(){
        return "from:" + _from + " in group: " + _group + " task:" + _task;
    }

}

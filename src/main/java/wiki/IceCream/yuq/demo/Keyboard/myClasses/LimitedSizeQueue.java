package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import java.util.ArrayList;

public class LimitedSizeQueue<K> extends ArrayList<K> {

    private int maxSize;

    public LimitedSizeQueue(int size){
        this.maxSize = size;
    }

    public boolean push(K k){
        boolean r = super.add(k);
        if (size() > maxSize){
            removeRange(0, size() - maxSize);
        }
        return r;
    }

    public K getYoungest() {
        return get(size() - 1);
    }

    public K getOldest() {
        return get(0);
    }

    @Override
    public boolean contains(Object object){
        for (K a : this){
            if(a==object) return true;
        }
        return false;
    }
}

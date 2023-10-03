package wiki.IceCream.yuq.demo.Keyboard.myClasses;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.net.URL;

public class GettingBufferedImageThread implements Runnable{
    private Thread _thread;
    private String _threadName;
    private BufferedImage _toLoad;
    private String _url;
    private boolean _isReady = false;

    public GettingBufferedImageThread(String url, String threadname){
        _url = url;
        _threadName = threadname;
    }
    public void run(){
        try{
            URL url = new URL(_url);
            _toLoad = ImageIO.read(url);
            _isReady = true;
            System.out.println(_threadName+" is finished");
        } catch (Exception e){}
    }

    public boolean isReady(){
        return _isReady;
    }

    public BufferedImage getLoaded(){return _toLoad;}

    public void close(){
        _threadName = null;
        _toLoad = null;
        _url = null;
        _thread = null;
    }

    public void start () {
        if (_thread == null) {
            _thread = new Thread (this, _threadName);
            _thread.start ();
        }
    }
}

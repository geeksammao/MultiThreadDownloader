package geeksammao.bingyan.net.mydownloader.network.task;

/**
 * Created by Geeksammao on 11/15/15.
 */
public abstract class BaseTask extends Thread {
    @Override
    public void run() {
        super.run();

        startTask();
    }

    abstract void startTask();
}

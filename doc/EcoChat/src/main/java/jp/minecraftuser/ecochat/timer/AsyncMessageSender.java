package jp.minecraftuser.ecochat.timer;

import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.async.AsyncFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

public class AsyncMessageSender extends AsyncFrame {
    long parentNum = 0;
    long childNum = 0;
    public AsyncMessageSender(PluginFrame plg_) {
        super(plg_);
    }

    @Override
    protected AsyncFrame clone() {
        return new AsyncMessageSender(plg);
    }

    @Override
    protected void parentRun() {
        //log.info("parent:"+parentNum);
        parentNum++;
    }

    @Override
    protected void childRun() {
        for (childNum = 0; ; childNum++) {
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(TestTimer.class.getName()).log(Level.SEVERE, null, ex);
//            }
            //log.info("child:"+childNum);
        }
    }
}

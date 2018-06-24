
package jp.minecraftuser.ecochat.thread;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.async.AsyncFrame;
import jp.minecraftuser.ecoframework.async.MessageAsyncFrame;
import jp.minecraftuser.ecoframework.async.MessagePayload;

/**
 * 非同期コマンド実行クラス
 * @author ecolight
 */
public class AsyncCommandExecutor extends MessageAsyncFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ フレーム名
     */
    public AsyncCommandExecutor(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * 子スレッドインスタンス返却
     * @return 
     */
    @Override
    protected AsyncFrame clone() {
        return new AsyncCommandExecutor(plg, name);
    }

    /**
     * メッセージの処理
     * @param msg 処理メッセージデータ
     */
    @Override
    protected void executeProcess(MessagePayload msg) {
        // AsyncCommandPayloadでなければ破棄する
        if (!(msg instanceof AsyncCommandPayload)) return;

        // 
        
    }


}

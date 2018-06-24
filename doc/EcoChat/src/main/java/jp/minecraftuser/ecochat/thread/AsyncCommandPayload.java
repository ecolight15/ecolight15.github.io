
package jp.minecraftuser.ecochat.thread;

import jp.minecraftuser.ecochat.type.CmdType;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.async.MessagePayload;
import org.bukkit.command.CommandSender;

/**
 * 非同期コマンドデータクラス
 * @author ecolight
 */
public class AsyncCommandPayload extends MessagePayload {
    private final CmdType type; 
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param type_ コマンド種別
     * @param sender_ 送信者
     * @param target_ 送信先
     * @param msg_ 送信メッセージ
     */
    public AsyncCommandPayload(PluginFrame plg_, CmdType type_, CommandSender sender_, CommandSender target_, String msg_) {
        super(plg_, sender_, target_, msg_);
        type = type_;
    }

    /**
     * コマンド種別取得
     * @return コマンド種別
     */
    public CmdType getType() {
        return type;
    }
}

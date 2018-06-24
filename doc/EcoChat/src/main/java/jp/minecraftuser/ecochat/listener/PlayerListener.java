
package jp.minecraftuser.ecochat.listener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochat.UserConfig;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecochat.config.EcoChatConfig;
import jp.minecraftuser.ecochat.db.DataManager;
import jp.minecraftuser.ecochat.m;
import jp.minecraftuser.ecoframework.ListenerFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

/**
 * プレイヤーイベント処理リスナークラス
 * @author ecolight
 */
public class PlayerListener extends ListenerFrame {
    private static EcoChatConfig eccConf = null;
    
    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     */
    public PlayerListener(PluginFrame plg_, String name_) {
        super(plg_, name_);
        eccConf = (EcoChatConfig)conf;
    }

    /**
     * PlayerJoinイベント処理
     * プレイヤーログイン後のイベントについて実装する
     * @param event JOINイベント情報
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        try {
            ((DataManager)manager).joinServer(event.getPlayer());
        } catch (DatabaseControlException ex) {
            log.warning(ex.getLocalizedMessage());
            SimpleDateFormat sdf = new SimpleDateFormat(" [MM/dd_HH:mm] ");
            event.getPlayer().sendMessage("ログイン処理時にエラーが発生しました。管理者に次のメッセージ出力内容と発生時刻を伝えて対応を仰いでください。" + sdf.format(new Date()) + ex.getLocalizedMessage());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void PlayerChat(AsyncPlayerChatEvent chat) {
        // Muteプレイヤーチェック
        try {
            UserConfig conf = plg.getChannelList().getUserConfig(chat.getPlayer().getName());
            // チャンネルにメッセージ送出
            String active = plg.getChannelList().getActive(chat.getPlayer().getName());
            plg.getChannelList().sendMessage(active, chat.getPlayer().getName(), chat.getMessage());
        } catch (Exception ex) {
            chat.getPlayer().sendMessage(m.plg(ex.getMessage()));
        }
        // メッセージキャンセル
        chat.setCancelled(true);
    }
     
}


package jp.minecraftuser.ecochat;

import java.util.logging.Logger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author ecolight
 */
public class EcoChatEventListener implements Listener {

    private static Logger log = null;
    private static EcoChat plg = null;

    public EcoChatEventListener(EcoChat plugin) {
        plg = plugin;
        log = plugin.getLogger();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerJoin(PlayerJoinEvent event) throws Exception {
//        if (event.getResult() == Result.ALLOWED)
        plg.joinServer(event.getPlayer().getName());
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void PlayerQuit(PlayerQuitEvent event) {
        try {
            if (!event.getPlayer().getName().equals(plg.getConsoleUser())) {
                plg.leaveServer(event.getPlayer().getName());
            } else {
                m.info("コンソールユーザーに設定されているため、ログアウト時のデータアンロードをキャンセルしました("+plg.getConsoleUser()+")");
            }
        } catch (Exception ex) {
            m.info("ユーザー情報無しでログアウトイベントキャッチ["+event.getPlayer().getName()+"]");
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

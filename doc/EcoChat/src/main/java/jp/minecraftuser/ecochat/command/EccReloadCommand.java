
package jp.minecraftuser.ecochat.command;

import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.CommandFrame;
import org.bukkit.command.CommandSender;

/**
 * リロードコマンドクラス
 * @author ecolight
 */
public class EccReloadCommand extends CommandFrame {

    /**
     * コンストラクタ
     * @param plg_ プラグインインスタンス
     * @param name_ コマンド名
     */
    public EccReloadCommand(PluginFrame plg_, String name_) {
        super(plg_, name_);
    }

    /**
     * コンソールユーザー実行可否設定
     * @return 実行可否
     */
    @Override
    public boolean canConsole() {
        return true;
    }

    /**
     * コマンド権限文字列設定
     * @return 権限文字列
     */
    @Override
    public String getPermissionString() {
        return "ecochat.reload";
    }

    /**
     * 処理実行部
     * @param sender コマンド送信者
     * @param args パラメタ
     * @return コマンド処理成否
     */
    @Override
    public boolean worker(CommandSender sender, String[] args) {
        // リロード
        conf.reload();
        sender.sendMessage("[" + plg.getName()+"] コンフィグリロード");
        return true;
    }
    
}

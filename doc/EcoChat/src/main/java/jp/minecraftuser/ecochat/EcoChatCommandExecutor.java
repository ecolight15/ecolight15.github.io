
package jp.minecraftuser.ecochat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class EcoChatCommandExecutor implements CommandExecutor {
    
    private static EcoChat plg = null;
    private int paracnt = 0;
    private CommandEcc cmdEcc = null;
    
    public EcoChatCommandExecutor(EcoChat plugin) {
        plg = plugin;
        cmdEcc = new CommandEcc(plg);
        plugin.getCommand("cc").setExecutor(this);
        plugin.getCommand("ecc").setExecutor(this);
        plugin.getCommand("join").setExecutor(this);
        plugin.getCommand("passjoin").setExecutor(this);
        plugin.getCommand("leave").setExecutor(this);
        plugin.getCommand("delete").setExecutor(this);
        plugin.getCommand("set").setExecutor(this);
        plugin.getCommand("add").setExecutor(this);
        plugin.getCommand("conf").setExecutor(this);
        plugin.getCommand("channel").setExecutor(this);
        plugin.getCommand("pm").setExecutor(this);
        plugin.getCommand("rs").setExecutor(this);
        plugin.getCommand("history").setExecutor(this);
        plugin.getCommand("pmhistory").setExecutor(this);
        plugin.getCommand("dice").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
        paracnt = 0;
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        // 各処理関数に振り分け
        if (cmd.getName().equalsIgnoreCase("join")) { cmdEcc.exec(player, "join", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("passjoin")) { cmdEcc.exec(player, "passjoin", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("leave")) { cmdEcc.exec(player, "leave", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("delete")) { cmdEcc.exec(player, "delete", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("set")) { cmdEcc.exec(player, "set", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("add")) { cmdEcc.exec(player, "add", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("conf")) { cmdEcc.exec(player, "conf", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("channel")) { cmdEcc.exec(player, "channel", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("pm")) { cmdEcc.exec(player, "pm", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("rs")) { cmdEcc.exec(player, "rs", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("history")) { cmdEcc.exec(player, "history", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("pmhistory")) { cmdEcc.exec(player, "pmhistory", args, paracnt); }
        else if (cmd.getName().equalsIgnoreCase("dice")) { cmdEcc.exec(player, "dice", args, paracnt); }
        else if((cmd.getName().equalsIgnoreCase("ecc")) || (cmd.getName().equalsIgnoreCase("cc"))) {
            if (args.length == 0) {
                if (player == null){
                    m.info("EcoChatCommand parameter is not enough.");
                } else {
                    player.sendMessage(m.get("cmd_param_fewer"));
                }
                return false;
            }
            paracnt = 1;
            if (args[0].equalsIgnoreCase("join")) { cmdEcc.exec(player, "join", args, paracnt); }
            else if (args[0].equalsIgnoreCase("passjoin")) { cmdEcc.exec(player, "passjoin", args, paracnt); }
            else if (args[0].equalsIgnoreCase("leave")) { cmdEcc.exec(player, "leave", args, paracnt); }
            else if (args[0].equalsIgnoreCase("delete")) { cmdEcc.exec(player, "delete", args, paracnt); }
            else if (args[0].equalsIgnoreCase("set")) { cmdEcc.exec(player, "set", args, paracnt); }
            else if (args[0].equalsIgnoreCase("add")) { cmdEcc.exec(player, "add", args, paracnt); }
            else if (args[0].equalsIgnoreCase("conf")) { cmdEcc.exec(player, "conf", args, paracnt); }
            else if (args[0].equalsIgnoreCase("channel")) { cmdEcc.exec(player, "channel", args, paracnt); }
            else if (args[0].equalsIgnoreCase("kick")) { cmdEcc.exec(player, "kick", args, paracnt); }
            else if (args[0].equalsIgnoreCase("list")) { cmdEcc.exec(player, "list", args, paracnt); }
            else if (args[0].equalsIgnoreCase("who")) { cmdEcc.exec(player, "who", args, paracnt); }
            else if (args[0].equalsIgnoreCase("info")) { cmdEcc.exec(player, "info", args, paracnt); }
            else if (args[0].equalsIgnoreCase("pm")) { cmdEcc.exec(player, "pm", args, paracnt); }
            else if (args[0].equalsIgnoreCase("rs")) { cmdEcc.exec(player, "rs", args, paracnt); }
            else if (args[0].equalsIgnoreCase("history")) { cmdEcc.exec(player, "history", args, paracnt); }
            else if (args[0].equalsIgnoreCase("pmhistory")) { cmdEcc.exec(player, "pmhistory", args, paracnt); }
            else if (args[0].equalsIgnoreCase("console")) { cmdEcc.exec(player, "console", args, paracnt); }
            else if (args[0].equalsIgnoreCase("spychat")) { cmdEcc.exec(player, "spychat", args, paracnt); }
            else if (args[0].equalsIgnoreCase("spypm")) { cmdEcc.exec(player, "spypm", args, paracnt); }
            else if (args[0].equalsIgnoreCase("dice")) { cmdEcc.exec(player, "dice", args, paracnt); }
            else if (args[0].equalsIgnoreCase("silent")) {
                if (player != null) {
                    if (!player.isOp()) {
                        m.info("[" + player.getName() + "]" + "EcoChat not Permissions : op");
                        player.sendMessage(m.get("cmd_notperm","EcoChat silent"));
                        return false;
                    }
                }
                if (plg.isSilent(player.getName())) {
                    plg.clearSilent();
                    player.sendMessage("エコチャット表示抑止条件をクリア");
                } else {
                    plg.setSilent(player.getName());
                    player.sendMessage("エコチャット表示抑止条件設定");
                }
                return true;
            } else if (args[0].equalsIgnoreCase("reload")) {
                // コンソール許可＋パーミッションチェック
                if (player != null) {
                    if (!player.isOp()) {
                        m.info("[" + player.getName() + "]" + "EcoChat not Permissions : op");
                        player.sendMessage(m.get("cmd_notperm","EcoChat reload"));
                        return false;
                    }
                }

                // パラメタ個数チェック
                if (args.length >= 2) {
                    if (player == null){
                        m.info("EcoChat many parameter");
                    } else {
                        m.info("[" + player.getName() + "]" + "EcoChat many parameter");
                        player.sendMessage(m.get("cmd_param_many"));
                    }
                    return false;
                }

                // 設定ファイルのリロード
                plg.setConfig();
                if (player == null){
                    m.info("EcoChat config reloaded.");
                } else {
                    m.info("[" + player.getName() + "]" + "EcoChat config reloaded.");
                    player.sendMessage(m.get("plg_reload"));
                }
                return true;
            } else {
                // パラメーターがひとつの場合はチャンネルjoinかset
                if (args.length == 1) {
                    try {
                        boolean b = false;
                        if (player == null) {
                           b = plg.getChannelList().isChannelUser(args[paracnt - 1], plg.getConsoleUser());
                        } else {
                           b = plg.getChannelList().isChannelUser(args[paracnt - 1], player.getName());
                        }
                        if (b) {   
                            // チャンネル加入済みならSET
                            cmdEcc.exec(player, "set", args, paracnt - 1);
                        } else {
                            // チャンネル未加入であればJOIN
                            cmdEcc.exec(player, "join", args, paracnt - 1);                     
                        }
                    // パラメータがふたつ以上の場合には、1つ目をチャンネル、以降をメッセージと認識する
                    } catch (Exception ex) {
                        m.info(m.plg(ex.getMessage()));
                    }
                } else if (args.length > 1) {
                    cmdEcc.exec(player, "send", args, paracnt - 1);
                } else {
                    return false;
                }
            }
	}
        return true;
    }
    
}

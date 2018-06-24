
package jp.minecraftuser.ecochat;

import jp.minecraftuser.ecochat.type.ChatType;
import java.sql.SQLException;
import jp.minecraftuser.ecochat.db.log.MsgLoggerDB;
import jp.minecraftuser.ecochat.db.chat.EcoChatDB_;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochat.command.AddCommand;
import jp.minecraftuser.ecochat.command.CcCommand;
import jp.minecraftuser.ecochat.command.ChannelCommand;
import jp.minecraftuser.ecochat.command.ConfCommand;
import jp.minecraftuser.ecochat.command.DeleteCommand;
import jp.minecraftuser.ecochat.command.DiceCommand;
import jp.minecraftuser.ecochat.command.EccCommand;
import jp.minecraftuser.ecochat.command.EccInfoCommand;
import jp.minecraftuser.ecochat.command.EccKickCommand;
import jp.minecraftuser.ecochat.command.EccListCommand;
import jp.minecraftuser.ecochat.command.EccReloadCommand;
import jp.minecraftuser.ecochat.command.EccSilentCommand;
import jp.minecraftuser.ecochat.command.EccSpychatCommand;
import jp.minecraftuser.ecochat.command.EccSpypmCommand;
import jp.minecraftuser.ecochat.command.EccWhoCommand;
import jp.minecraftuser.ecochat.command.HistoryCommand;
import jp.minecraftuser.ecochat.command.JoinCommand;
import jp.minecraftuser.ecochat.command.LeaveCommand;
import jp.minecraftuser.ecochat.command.PassJoinCommand;
import jp.minecraftuser.ecochat.command.PmCommand;
import jp.minecraftuser.ecochat.command.PmHistoryCommand;
import jp.minecraftuser.ecochat.command.RsCommand;
import jp.minecraftuser.ecochat.command.SetCommand;
import jp.minecraftuser.ecochat.config.DefaultChannelConfig;
import jp.minecraftuser.ecochat.config.EcoChatConfig;
import jp.minecraftuser.ecochat.db.DataManager;
import jp.minecraftuser.ecochat.listener.PlayerListener;
import jp.minecraftuser.ecoframework.CommandFrame;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecousermanager.EcoUserManager;
import jp.minecraftuser.ecousermanager.db.EcoUserUUIDStore;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author ecolight
 */
public class EcoChat extends PluginFrame {
    private static EcoChatConfig eccConf = null;
    
    @Override
    public void onEnable(){
        initialize();
        eccConf = (EcoChatConfig)getDefaultConfig();
        
        try {
            // マネージャー起動
            manager = new DataManager(this);
        } catch (Exception ex) {
            Logger.getLogger(EcoChat.class.getName()).log(Level.SEVERE, null, ex);
            disable();
            return;
        }
        getLogger().info(getName()+" Enable");
    }
    
    @Override
    public void onDisable(){
        disable();
        getLogger().info(getName()+" Disable");
    }

    public void setConfig(){

        reloadConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // メッセージ読み込み
        m.msgLoad(null, this);

        chatdb = new EcoChatDB_(this);

        // チャットチャンネルロード
        this.channelList = new LoaderChannel(this);
        this.msgdb = new MsgLoggerDB(this);
        try {
            //String channel, String joinuser, String password, boolean force, ChatType type, String join, String leave, boolean def, boolean listed, boolean addperm
            channelList.joinChannel("G", null, null, true, ChatType.global, "{p1}さん、全体チャットチャンネル[G](初期)へようこそ！チャットの詳しい使い方はサーバーwikiを参照ください", null, true, true, false);
            channelList.joinChannel("I", null, null, true, ChatType.global, "{p2}チャンネルは挨拶用です。挨拶以外の会話はご遠慮下さい。", null, true, true, false);
            channelList.joinChannel("W", null, null, true, ChatType.world, "{p2}チャンネルはワールド内向けチャットです。他のワールドへは発言が通知されません", null, true, true, false);
            channelList.joinChannel("L", null, null, true, ChatType.local, "{p2}チャンネルは近距離用チャンネルです。設定で受信範囲を変更できます。", null, true, true, false);
            channelList.joinChannel("FRN", null, null, true, ChatType.global, "{p2}チャンネルは国際用途のチャンネルです。日本語英語以外の言語向けになります。", null, true, true, true);

            channelList.joinChannel("F", null, null, true, ChatType.global, "雑談用チャンネルへようこそ！", null, false, true, false);
            channelList.joinChannel("H", null, null, true, ChatType.global, "ここは・・・アダルトな・・・・チャンネル・・・", null, false, false, false);
            channelList.joinChannel("T", null, null, true, ChatType.global, "トレード用チャンネルへようこそ！", null, false, true, false);
            channelList.joinChannel("P", null, null, true, ChatType.global, "ゲームマップ向けチャンネルへようこそ！", null, false, true, false);
            channelList.joinChannel("DEF", null, null, true, ChatType.global, "DEFチャンネルは参加や公開が制限されています", null, false, false, false);
            channelList.joinChannel("B", null, null, true, ChatType.global, "ぼっちやで", null, false, true, false);
            channelList.joinChannel("LIVE", null, null, true, ChatType.global, "各種生放送等向けのライブチャンネルです", null, false, true, true);
            channelList.joinChannel("ONI", null, null, true, ChatType.global, "鬼ごっこ用チャンネルへようこそ！", null, false, true, true);
            if (!channelList.isPasswordChannel("DEF")) channelList.setChannelPass("DEF", "fsaba-def");
        } catch (Exception ex) {
            Logger.getLogger(EcoChat.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    public EcoUserManager getUman() {
        return man;
    } 
    public boolean isHawk() {
        return this.hawk;
    }
    public LoaderChannel getChannelList() {return this.channelList;}
    
    public MsgLoggerDB getLoggerDB() {
        return this.msgdb;
    }

    public void joinServer(String username) throws Exception {

        // ユーザーデータの存在確認
        if (channelList.isExistUserData(username)) return;
        
        // ユーザーデータが無い場合、初期値を追加
        channelList.addUserData(username, "G");
        m.info("初期ユーザーデータ追加:"+username);
        
        // 初期チャンネルに加入
        for (String ch : channelList.getDefaultChannel()) {
            channelList.joinChannel(ch, username, null, false, null, null, null, false, false, false);
        }
    }
    public void leaveServer(String username) throws Exception {
        return;
    }

    @Override
    public void initializeConfig() {
        ConfigFrame f = new EcoChatConfig(this);
        f.registerString("default-channel");
        f.registerString("spychat");
        f.registerString("spypm");
        confMap.put("config", f);
        f = new DefaultChannelConfig(this, "default.yml");
        f.registerSectionString("Channels");
        confMap.put("default", f);
    }

    @Override
    public void initializeCommand() {
        cmdMap.put("add", new AddCommand(this, "add"));
        cmdMap.put("channel", new ChannelCommand(this, "channel"));
        cmdMap.put("conf", new ConfCommand(this, "conf"));
        cmdMap.put("delete", new DeleteCommand(this, "delete"));
        cmdMap.put("dice", new DiceCommand(this, "dice"));
        cmdMap.put("history", new HistoryCommand(this, "history"));
        cmdMap.put("join", new JoinCommand(this, "join"));
        cmdMap.put("leave", new LeaveCommand(this, "leave"));
        cmdMap.put("passjoin", new PassJoinCommand(this, "passjoin"));
        cmdMap.put("pm", new PmCommand(this, "pm"));
        cmdMap.put("pmhistory", new PmHistoryCommand(this, "pmhistory"));
        cmdMap.put("rs", new RsCommand(this, "rs"));
        cmdMap.put("set", new SetCommand(this, "set"));
        CommandFrame frame = new EccCommand(this, "ecc");
        for (CommandFrame f : cmdMap.values()) {
            frame.addCommand(f.getName(), f);
        }
        frame.addCommand("reload", new EccReloadCommand(this, "reload"));
        frame.addCommand("info", new EccInfoCommand(this, "info"));
        frame.addCommand("kick", new EccKickCommand(this, "kick"));
        frame.addCommand("list", new EccListCommand(this, "list"));
        frame.addCommand("silent", new EccSilentCommand(this, "silent"));
        frame.addCommand("spychat", new EccSpychatCommand(this, "spychat"));
        frame.addCommand("spypm", new EccSpypmCommand(this, "spypm"));
        frame.addCommand("who", new EccWhoCommand(this, "who"));
        CommandFrame cc = new CcCommand(this, "cc");
        for (CommandFrame f : frame.getCommandList()) {
            cc.addCommand(f.getName(), f);
        }
        cmdMap.put("ecc", frame);
        cmdMap.put("cc", cc);
        
    }

    @Override
    public void initializeListener() {
        listenerMap.put("player", new PlayerListener(this));
    }

    @Override
    public void initializeTimer() {
    }


}

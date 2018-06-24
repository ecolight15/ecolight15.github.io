
package jp.minecraftuser.ecochat.db;

import jp.minecraftuser.ecochat.db.log.MsgLoggerDB;
import jp.minecraftuser.ecochat.db.chat.EcoChatDB_;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import jp.minecraftuser.ecochat.type.ChatType;
import jp.minecraftuser.ecochat.config.DefaultChannelConfig;
import jp.minecraftuser.ecochat.db.chat.ChannelConfData;
import jp.minecraftuser.ecochat.db.chat.ChannelData;
import jp.minecraftuser.ecochat.db.chat.ChannelPassData;
import jp.minecraftuser.ecochat.db.chat.ChannelUserData;
import jp.minecraftuser.ecochat.db.chat.EcoChatDB;
import jp.minecraftuser.ecochat.db.chat.EcoChatDBInsert;
import jp.minecraftuser.ecochat.db.chat.FellowConfigData;
import jp.minecraftuser.ecochat.db.chat.FellowNgData;
import jp.minecraftuser.ecochat.db.chat.UserConfigData;
import jp.minecraftuser.ecochat.db.chat.UserData;
import jp.minecraftuser.ecoframework.ConfigFrame;
import jp.minecraftuser.ecoframework.ManagerFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class DataManager extends ManagerFrame {
    private MsgLoggerDB msgdb = null;
    private EcoChatDB chatdb = null;
    private EcoChatDBInsert insertdb = null;

    // 常時展開
    private Map<String, ChannelData> channels;
    private ArrayList<ChannelData> defChannels;
    private Map<Long, ChannelConfData> channelConf;
    private Map<Long, Map<UUID, ChannelUserData>> users;
    private Map<Long, ChannelPassData> pass;

    // オンラインユーザーデータ
    private Map<UUID, UserData> userData;
    private Map<Long, Map<Long, UserConfigData>> userChannelConf;
    private Map<Long, Map<Long, FellowConfigData>> fellowConf;
    private Map<Long, Map<Long, FellowNgData>> fellowNg;

    public DataManager(PluginFrame plg_) throws ClassNotFoundException, SQLException, DatabaseControlException {
        super(plg_);
        // データベース接続
        chatdb = new EcoChatDB(plg);
        insertdb = new EcoChatDBInsert(chatdb);
        msgdb = new MsgLoggerDB(plg); 
        
        // チャンネル情報の全ロード
        channels = chatdb.loadMap("CHANNEL", "TAG", String.class, ChannelData.class);
        channelConf = chatdb.loadMap("CHCONF", "ID", Long.class, ChannelConfData.class);
        users = chatdb.loadChannelUsers();
        pass = chatdb.loadMap("CHPASS", "ID", Long.class, ChannelPassData.class);
        
        // 固定チャンネル定義のオーバーライド
        DefaultChannelConfig def = (DefaultChannelConfig) plg.getPluginConfig("default");
        Set<String> list = def.getSectionList("Channels");
        if (list != null) {
            log.info("Start register default defined channels.");
            for (String s : list) {
                // ロードチャンネル定義ごとに処理を行う
                // チャンネル定義が存在するか？
                if (channels.containsKey(s.toLowerCase())) {
                    // あればなにもしない
                    continue;
                }
                // 無ければ設定ファイルの値で書き出す
                def.registerString("Channels." + s + ".name");
                def.registerString("Channels." + s + ".type");
                def.registerBoolean("Channels." + s + ".default");
                def.registerBoolean("Channels." + s + ".listed");
                def.registerBoolean("Channels." + s + ".activate");
                def.registerBoolean("Channels." + s + ".addperm");
                def.registerBoolean("Channels." + s + ".password.enable");
                def.registerString("Channels." + s + ".password.key");
                def.registerString("Channels." + s + ".font.color");
                def.registerBoolean("Channels." + s + ".font.bold");
                def.registerBoolean("Channels." + s + ".font.italic");
                def.registerBoolean("Channels." + s + ".font.line");
                def.registerBoolean("Channels." + s + ".font.strike");
                def.registerString("Channels." + s + ".msg.join");
                def.registerString("Channels." + s + ".msg.leave");
                ChannelData data1 = new ChannelData(
                                0, // 自動採番なので0にしておく
                                s,
                                def.getString("Channels." + s + ".name"),
                                ChatType.valueOf(def.getString("Channels." + s + ".type")).getId(),
                                def.getString("Channels." + s + ".msg.join"),
                                def.getString("Channels." + s + ".msg.leave"),
                                def.getBoolean("Channels." + s + ".default"),
                                def.getBoolean("Channels." + s + ".listed"),
                                def.getBoolean("Channels." + s + ".addperm"),
                                def.getBoolean("Channels." + s + ".activate"),
                                new Date().getTime());
                long id = insertdb.insertChannel(data1);
                data1.setId(id);
                channels.put(data1.getTag().toLowerCase(), data1);
                // 装飾定義は初期値でなければDBに書き込む
                ChannelConfData data2 = new ChannelConfData(
                                id, // 採番した値をもらってくる
                                def.getString("Channels." + s + ".font.color"),
                                def.getBoolean("Channels." + s + ".font.bold"),
                                def.getBoolean("Channels." + s + ".font.italic"),
                                def.getBoolean("Channels." + s + ".font.line"),
                                def.getBoolean("Channels." + s + ".font.strike"));
                if ((!data2.getColor().equalsIgnoreCase("white")) ||
                    (data2.isBold()) ||
                    (data2.isItalic()) ||
                    (data2.isLine()) ||
                    (data2.isStrike())) {
                    insertdb.insertChannelConf(data2);
                    channelConf.put(id, data2);
                }
                
                if (def.getBoolean("Channels." + s + ".password.enable")) {
                    ChannelPassData data3 = new ChannelPassData(
                                    id, // 採番した値をもらってくる
                                    def.getString("Channels." + s + ".password.key"));
                    insertdb.insertChannelPass(data3);
                    pass.put(id, data3);
                }
                // 追加したチャンネルがデフォルト参加チャンネルの場合、過去にさかのぼって参加させる。
                if (data1.isAuto()) {
                    log.info("Migration exist user data.[" + data1.getTag() + "]");
                    Map<UUID, UserData> all = chatdb.loadAllUsers();
                    insertdb.insertChannelUsers(all.keySet().toArray(new UUID[0]), data1, false);
                }
                log.info("Register default defined channel.[" + data1.getTag() + "]");
            }
            users = chatdb.loadChannelUsers();
        }
       
        // デフォルトチャンネルの抽出
        defChannels = new ArrayList<>();
        for (ChannelData ch : channels.values()) {
            if (ch.isAuto()) {
                defChannels.add(ch);
                log.info("Detect auto join channel.[" + ch.getTag() + "]");
            }
        }
        
        // 現時点のオンラインユーザーデータロード
        log.info("Online user data loading...");
        // UUIDリスト作成
        List<UUID> uuids = new ArrayList<>();
        for (Player p : plg.getServer().getOnlinePlayers()) {
            uuids.add(p.getUniqueId());
        }
        userData = chatdb.loadUsers(uuids.toArray(new UUID[0]));
        // userIdList作成
        List<Long> uids = new ArrayList<>();
        for (UserData d : userData.values()) {
            uids.add(d.getId());
        }
        Long[] uids_array = uids.toArray(new Long[0]);
        userChannelConf = chatdb.loadUsersChannelConf(uids_array);
        fellowConf = chatdb.loadUserFellowConf(uids_array);
        fellowNg = chatdb.loadUsersNgConf(uids_array);
        
        log.info("Online user data loaded.(size:" + userData.size() + ")");
    }
コマンドとリスナーの見直し
    @Override
    protected void close() {
        chatdb.close();
        msgdb.finalize();
    }
    
    /**
     * サーバー接続時ユーザーデータ読み込み処理
     * @param pl プレイヤーインスタンス 
     * @throws jp.minecraftuser.ecoframework.exception.DatabaseControlException 
     */
    public void joinServer(Player pl) throws DatabaseControlException {

        // メモリ上のユーザーデータの存在確認
        if (userData.containsKey(pl.getName())) return;
        
        // DBからのロードを試みる
        UserData data = chatdb.loadUser(pl.getUniqueId());
        
        // DBからロードできたら他のプレイヤー情報も含めて展開する
        if (data != null) {
            userData.put(pl.getUniqueId(), data);
            Map<Long, UserConfigData> conf = chatdb.loadUserChannelConf(data.getId());
            if (conf != null) userChannelConf.put(data.getId(), conf);
            Map<Long, FellowConfigData> fconf = chatdb.loadUserFellowConf(data.getId());
            if (fconf != null) fellowConf.put(data.getId(), fconf);
            Map<Long, FellowNgData> ng = chatdb.loadUserNgConf(data.getId());
            if (ng != null) fellowNg.put(data.getId(), ng);
        } 
        // DBからロードできなかった場合は新規作成する
        else {
            // ユーザーデータ追加
            insertdb.insertUser(pl, conf.getString("default-channel"));
            // ユーザーデータ反映
            userData.put(pl.getUniqueId(), chatdb.loadUser(pl.getUniqueId()));
            log.info("初期ユーザーデータ追加:"+pl.getName());
            // 初期チャンネルに加入
            // DB登録済みチャンネルなので諸々のチェックは不要
            for (ChannelData ch : defChannels) {
                insertdb.insertChannelUser(pl.getUniqueId(), ch, false);
            }
            // チャンネルユーザーデータ反映
            users = chatdb.loadChannelUsers();
        }
    }
}


package jp.minecraftuser.ecochat.db.chat;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;

/**
 *
 * @author ecolight
 */
public class EcoChatDB extends DatabaseFrame {

    public EcoChatDB(PluginFrame plg_) throws ClassNotFoundException, SQLException {
        super(plg_, "chat.db", "chat");
    }

    /**
     * データベース移行処理
     * 内部処理からトランザクション開始済みの状態で呼ばれる
     * @throws SQLException
     */
    @Override
    protected void migrationData() throws SQLException {
        // version 1 の場合、新規作成もしくは旧バージョンのデータベース引き継ぎの場合を検討する
        if (dbversion == 1) {
            if (justCreated) {
                // 新規作成の場合、テーブル定義のみ作成して終わり
                executeStatement("CREATE TABLE IF NOT EXISTS CHANNEL(ID INTEGER PRIMARY KEY AUTOINCREMENT, TAG TEXT NOT NULL UNIQUE, NAME TEXT NOT NULL UNIQUE, TYPE INTEGER DEFAULT 0, ENTERMSG TEXT DEFAULT '{p1}さん、ようこそ{p2}チャンネルへ！', LEAVEMSG TEXT DEFAULT '{p1}さん、{p2}チャンネルのご利用ありがとうございました。', AUTOJOIN BOOLEAN DEFAULT 0, LISTED BOOLEAN DEFAULT 0, ADDPERM BOOLEAN DEFAULT 0, ACTIVATE BOOLEAN DEFAULT 1, SINCE INTEGER DEFAULT 0);");
                log.info("DataBase CHANNEL table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS CHCONF(ID INTEGER PRIMARY KEY, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
                log.info("DataBase CHCONF table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS CHUSERS(ID INTEGER NOT NULL, MOSTUUID INTEGER NOT NULL, LEASTUUID INTEGER NOT NULL, OWNER BOOLEAN DEFAULT 0, JOINDATE INTEGER NOT NULL, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
                log.info("DataBase CHUSERS table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS CHPASS(ID INTEGER NOT NULL, PASS TEXT NOT NULL, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
                log.info("DataBase CHPASS table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS USERS(USERID INTEGER PRIMARY KEY AUTOINCREMENT, MOSTUUID INTEGER NOT NULL, LEASTUUID INTEGER NOT NULL, ACTIVE INTEGER NOT NULL, MUTE BOOLEAN DEFAULT 0, LOCAL INTEGER DEFAULT 50, INFO BOOLEAN DEFAULT 0, NGVIEW BOOLEAN DEFAULT 0, RANGE BOOLEAN DEFAULT 1, RSWARN BOOLEAN DEFAULT 1, UNIQUE(MOSTUUID,LEASTUUID));");
                log.info("DataBase USERS table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS USCHCONF(USERID INTEGER NOT NULL, ID INTEGER NOT NULL, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, UNIQUE(USERID,ID), FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE, FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                log.info("DataBase USCHCONF table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS USUSCONF(USERID INTEGER NOT NULL, TARGET INTEGER NOT NULL, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                log.info("DataBase USUSCONF table checked.");
                executeStatement("CREATE TABLE IF NOT EXISTS USNGCONF(USERID INTEGER NOT NULL, TARGET INTEGER NOT NULL, NG BOOLEAN DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                log.info("DataBase USNGCONF table checked.");
                // データベースバージョンは最新版数に設定する
                log.info("create " + name + " version 2");
                updateSettingsVersion(2);
                return;
            } else {
                // 既存DB引き継ぎの場合は新規作成と同レベルの状態にする必要がある
                // 1 -> 2版の変更内容
                // - CHANNELテーブルにACTIVATEカラム追加
                // - USNGCONFテーブル新規作成 : USUSCONFからNG設定分離
                // - USUSCONFからNG削除, TARGETのDELETE CASCADE指定を追加
                log.info("convert " + name + " version 1 -> 2 startz");
                
                // カラム追加
                addLongColumn("CHANNEL", "ACTIVATE", 1L);
                
                // USNGCONFテーブルに移行
                executeStatement("CREATE TABLE IF NOT EXISTS USNGCONF(USERID INTEGER NOT NULL, TARGET INTEGER NOT NULL, NG BOOLEAN DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                ResultSet rs = executeQuery("SELECT * FROM USUSCONF WHERE NG = 1");
                PreparedStatement prep = con.prepareStatement("INSERT INTO USNGCONF VALUES(? ? ?);");
                while (rs.next()) {
                    prep.setLong(1, rs.getLong("USERID"));
                    prep.setLong(2, rs.getLong("TARGET"));
                    prep.setBoolean(3, rs.getBoolean("NG"));
                    prep.executeUpdate();
                }
                prep.close();
                rs.close();
                
                // USUSCONFの再作成用テーブル作成＋全項目移行
                executeStatement("CREATE TABLE IF NOT EXISTS BUF(USERID INTEGER NOT NULL, TARGET INTEGER NOT NULL, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE, FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");
                rs = executeQuery("SELECT * FROM USUSCONF");
                prep = con.prepareStatement("INSERT INTO USUSCONF VALUES(? ? ? ? ? ? ?);");
                while (rs.next()) {
                    prep.setLong(1, rs.getLong("USERID"));
                    prep.setLong(2, rs.getLong("TARGET"));
                    prep.setString(3, rs.getString("COLOR"));
                    prep.setBoolean(4, rs.getBoolean("BOLD"));
                    prep.setBoolean(5, rs.getBoolean("ITALIC"));
                    prep.setBoolean(6, rs.getBoolean("LINE"));
                    prep.setBoolean(7, rs.getBoolean("STRIKE"));
                    prep.executeUpdate();
                }
                prep.close();
                rs.close();
                
                // 既存のテーブルを置き換える
                dropTable("USUSCONF");
                renameTable("BUF", "USUSCONF");
                
                // データベースバージョンは次版にする
                updateSettingsVersion();
                
                log.info("convert " + name + " version 1 -> 2 complete");
            }
        }
    }
    public Map loadMap(String name, String key, Class<?> keyclass, Class<?> valclass) {
        HashMap<Object, Object> ret = new HashMap<>();
        try {
            // コンストラクタパラメータ型定義
            Class<?>[] types = { EcoChatDB.class, ResultSet.class };
            // コンストラクタ取得
            // NoSuchMethodException | SecurityException
            Constructor<?> constructor = valclass.getConstructor(types);
            
            log.log(Level.INFO, "loading table {0}(count:{1})", new Object[]{name, count(name)});
            ResultSet rs = executeQuery("SELECT * FROM " + name);
            while (rs.next()) {
                // ResultSetをコンストラクタのパラメタで受け取るclassnameのクラスをnewしてmapに格納
                // InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                ret.put(rs.getObject(key, keyclass), constructor.newInstance(this, rs));
            }
            rs.close();
            commit();
        } catch (SQLException |
                 NoSuchMethodException | SecurityException |
                 InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded ChannelData(size:" + ret.size() + ")");
        return ret;
    }

    public Map<Long, Map<UUID, ChannelUserData>> loadChannelUsers() {
        HashMap<Long, Map<UUID, ChannelUserData>> ret = new HashMap<>();
        long size = 0;
        try {
            log.info("loading ChannelUserData(count:" + count("CHUSERS") + ")");
            ResultSet rs = executeQuery("SELECT * FROM CHUSERS");
            while (rs.next()) {
                // チャンネルのMAPがあれば流用する
                Map<UUID, ChannelUserData> user;
                if (ret.containsKey(rs.getLong("ID"))) {
                    user = ret.get(rs.getLong("ID"));
                } else {
                    user = new HashMap<>();
                    ret.put(rs.getLong("ID"), user);
                }
                user.put(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")), 
                         new ChannelUserData(this, rs.getLong("ID"), rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID"), rs.getBoolean("OWNER"), rs.getLong("JOINDATE")));
                size++;
            }
            rs.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded ChannelUserData(size:" + size + ")");
        return ret;
    } 

    public UserData loadUser(UUID uid) {
        UserData ret = null;
        long size = 0;
        try {
            log.info("loading UserData(uid:" + uid.toString() + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            ResultSet rs = prep.executeQuery();
            if (rs.next()) {
                ret = new UserData(this, rs.getLong("USERID"), rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID"), rs.getLong("ACTIVE"), rs.getBoolean("MUTE"), rs.getLong("LOCAL"), rs.getBoolean("INFO"), rs.getBoolean("NGVIEW"), rs.getBoolean("RANGE"), rs.getBoolean("RSWARN"));
            }
            rs.close();
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UserData(uid:" + uid.toString() + ")");
        return ret;
    } 
    public Map<UUID, UserData> loadUsers(UUID[] uids) {
        HashMap<UUID, UserData> ret = new HashMap<>();
        try {
            log.info("loading UsersData(length:" + uids.length + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            for (UUID uid : uids) {
                prep.setLong(1, uid.getMostSignificantBits());
                prep.setLong(2, uid.getLeastSignificantBits());
                ResultSet rs = prep.executeQuery();
                if (rs.next()) {
                    ret.put(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")),
                            new UserData(this, rs.getLong("USERID"), rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID"), rs.getLong("ACTIVE"), rs.getBoolean("MUTE"), rs.getLong("LOCAL"), rs.getBoolean("INFO"), rs.getBoolean("NGVIEW"), rs.getBoolean("RANGE"), rs.getBoolean("RSWARN")));
                }
                rs.close();
            }
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UsersData(size:" + ret.size() + ")");
        return ret;
    } 
    public Map<UUID, UserData> loadAllUsers() {
        HashMap<UUID, UserData> ret = new HashMap<>();
        try {
            log.info("loading AllUsersData(length:" + count("USERS")+ ")");
            
            ResultSet rs =  executeQuery("SELECT * FROM USERS;");
            while (rs.next()) {
                ret.put(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")),
                        new UserData(this, rs.getLong("USERID"), rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID"), rs.getLong("ACTIVE"), rs.getBoolean("MUTE"), rs.getLong("LOCAL"), rs.getBoolean("INFO"), rs.getBoolean("NGVIEW"), rs.getBoolean("RANGE"), rs.getBoolean("RSWARN")));
            }
            rs.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded AllUsersData(size:" + ret.size() + ")");
        return ret;
    } 
    public Map<Long, UserConfigData> loadUserChannelConf(long userid) {
        Map<Long, UserConfigData> ret = new HashMap<>();
        long size = 0;
        try {
            log.info("loading UserChannelConfigData(id:" + userid + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USCHCONF WHERE USERID = ?;");
            prep.setLong(1, userid);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                ret.put(rs.getLong("ID"), new UserConfigData(this, rs.getLong("USERID"), rs.getLong("ID"), rs.getString("COLOR"), rs.getBoolean("BOLD"), rs.getBoolean("ITALIC"), rs.getBoolean("LINE"), rs.getBoolean("STRIKE")));
            }
            rs.close();
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UserChannelConfigData(id:" + userid + ")");
        return ret;
    } 
    public Map<Long, Map<Long, UserConfigData>> loadUsersChannelConf(Long[] userids) {
        HashMap<Long, Map<Long, UserConfigData>> ret = new HashMap<>();
        try {
            log.info("loading UsersChannelConfigData(length:" + userids.length + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USCHCONF WHERE USERID = ?;");
            for (long userid : userids) {
                prep.setLong(1, userid);
                ResultSet rs = prep.executeQuery();
                if (rs.next()) {
                    // ユーザーのMAPがあれば流用する
                    Map<Long, UserConfigData> user;
                    if (ret.containsKey(userid)) {
                        user = ret.get(userid);
                    } else {
                        user = new HashMap<>();
                        ret.put(userid, user);
                    }
                    user.put(rs.getLong("ID"), new UserConfigData(this, rs.getLong("USERID"), rs.getLong("ID"), rs.getString("COLOR"), rs.getBoolean("BOLD"), rs.getBoolean("ITALIC"), rs.getBoolean("LINE"), rs.getBoolean("STRIKE")));
                }
                rs.close();
            }
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UsersChannelConfigData(size:" + ret.size() + ")");
        return ret;
    } 
    public Map<Long, FellowConfigData> loadUserFellowConf(long userid) {
        Map<Long, FellowConfigData> ret = new HashMap<>();
        long size = 0;
        try {
            log.info("loading FellowConfigData(id:" + userid + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ?;");
            prep.setLong(1, userid);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                ret.put(rs.getLong("TARGET"), new FellowConfigData(this, rs.getLong("USERID"), rs.getLong("TARGET"), rs.getString("COLOR"), rs.getBoolean("BOLD"), rs.getBoolean("ITALIC"), rs.getBoolean("LINE"), rs.getBoolean("STRIKE")));
            }
            rs.close();
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded FellowConfigData(id:" + userid + ")");
        return ret;
    } 
    public Map<Long, Map<Long, FellowConfigData>> loadUserFellowConf(Long[] userids) {
        HashMap<Long, Map<Long, FellowConfigData>> ret = new HashMap<>();
        try {
            log.info("loading UserFellowConfigData(length:" + userids.length + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ?;");
            for (long userid : userids) {
                prep.setLong(1, userid);
                ResultSet rs = prep.executeQuery();
                if (rs.next()) {
                    // ユーザーのMAPがあれば流用する
                    Map<Long, FellowConfigData> user;
                    if (ret.containsKey(userid)) {
                        user = ret.get(userid);
                    } else {
                        user = new HashMap<>();
                        ret.put(userid, user);
                    }
                    user.put(rs.getLong("TARGET"), new FellowConfigData(this, rs.getLong("USERID"), rs.getLong("TARGET"), rs.getString("COLOR"), rs.getBoolean("BOLD"), rs.getBoolean("ITALIC"), rs.getBoolean("LINE"), rs.getBoolean("STRIKE")));
                }
                rs.close();
            }
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UserFellowConfigData(size:" + ret.size() + ")");
        return ret;
    } 
    public Map<Long, FellowNgData> loadUserNgConf(long userid) {
        Map<Long, FellowNgData> ret = new HashMap<>();
        long size = 0;
        try {
            log.info("loading FellowNgData(id:" + userid + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USNGCONF WHERE USERID = ?;");
            prep.setLong(1, userid);
            ResultSet rs = prep.executeQuery();
            while (rs.next()) {
                ret.put(rs.getLong("TARGET"), new FellowNgData(this, rs.getLong("USERID"), rs.getLong("TARGET"), rs.getBoolean("NG")));
            }
            rs.close();
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded FellowNgData(id:" + userid + ")");
        return ret;
    } 
    public Map<Long, Map<Long, FellowNgData>> loadUsersNgConf(Long[] userids) {
        HashMap<Long, Map<Long, FellowNgData>> ret = new HashMap<>();
        try {
            log.info("loading UserFellowNgData(length:" + userids.length + ")");
            PreparedStatement prep = con.prepareStatement("SELECT * FROM USNGCONF WHERE USERID = ?;");
            for (long userid : userids) {
                prep.setLong(1, userid);
                ResultSet rs = prep.executeQuery();
                if (rs.next()) {
                    // ユーザーのMAPがあれば流用する
                    Map<Long, FellowNgData> user;
                    if (ret.containsKey(userid)) {
                        user = ret.get(userid);
                    } else {
                        user = new HashMap<>();
                        ret.put(userid, user);
                    }
                    user.put(rs.getLong("TARGET"), new FellowNgData(this, rs.getLong("USERID"), rs.getLong("TARGET"), rs.getBoolean("NG")));
                }
                rs.close();
            }
            prep.close();
            commit();
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex);
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1);
            }
        }
        log.info("loaded UserFellowNgData(size:" + ret.size() + ")");
        return ret;
    } 

}

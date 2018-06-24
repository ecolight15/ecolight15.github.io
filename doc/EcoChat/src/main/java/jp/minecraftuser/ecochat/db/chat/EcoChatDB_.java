/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.minecraftuser.ecochat.db.chat;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochat.ChatConf;
import jp.minecraftuser.ecochat.type.ChatType;
import jp.minecraftuser.ecochat.EcoChat;
import jp.minecraftuser.ecochat.UserConfig;
import jp.minecraftuser.ecochat.m;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecousermanager.db.EcoUserUUIDStore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class EcoChatDB_ {

    private PluginFrame plg = null;
    private Connection con = null;

    public EcoChatDB_(PluginFrame plg) {
        this.plg = plg;
        String msgDBpath = plg.getDataFolder().getPath()+"/chat.db";
        try {
            m.info("Start connecting database.");
            Class.forName("org.sqlite.JDBC");
            Properties p = new Properties();
            p.setProperty("foreign_keys", "true");
            con = DriverManager.getConnection("jdbc:sqlite:"+msgDBpath, p);
//            con.prepareStatement("pragma foreign_keys=true;").executeQuery();
            con.setAutoCommit(false);
            m.info("database settings complete.");
            
            // 必要テーブルの追加
            
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CHANNEL(ID INTEGER PRIMARY KEY AUTOINCREMENT, TAG TEXT NOT NULL UNIQUE, NAME TEXT NOT NULL UNIQUE, TYPE INTEGER DEFAULT 0, ENTERMSG TEXT DEFAULT '{p1}さん、ようこそ{p2}チャンネルへ！', LEAVEMSG TEXT DEFAULT '{p1}さん、{p2}チャンネルのご利用ありがとうございました。', AUTOJOIN BOOLEAN DEFAULT 0, LISTED BOOLEAN DEFAULT 0, ADDPERM BOOLEAN DEFAULT 0, SINCE INTEGER DEFAULT 0);");
            m.info("DataBase CHANNEL table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CHCONF(ID INTEGER PRIMARY KEY, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
            m.info("DataBase CHCONF table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CHUSERS(ID INTEGER NOT NULL, MOSTUUID INTEGER NOT NULL, LEASTUUID INTEGER NOT NULL, OWNER BOOLEAN DEFAULT 0, JOINDATE INTEGER NOT NULL, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
            m.info("DataBase CHUSERS table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS CHPASS(ID INTEGER NOT NULL, PASS TEXT NOT NULL, FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
            m.info("DataBase CHPASS table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS USERS(USERID INTEGER PRIMARY KEY AUTOINCREMENT, MOSTUUID INTEGER NOT NULL, LEASTUUID INTEGER NOT NULL, ACTIVE INTEGER NOT NULL, MUTE BOOLEAN DEFAULT 0, LOCAL INTEGER DEFAULT 50, INFO BOOLEAN DEFAULT 0, NGVIEW BOOLEAN DEFAULT 0, RANGE BOOLEAN DEFAULT 1, RSWARN BOOLEAN DEFAULT 1, UNIQUE(MOSTUUID,LEASTUUID));");
            m.info("DataBase USERS table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS USCHCONF(USERID INTEGER NOT NULL, ID INTEGER NOT NULL, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, UNIQUE(USERID,ID), FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE, FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");
            m.info("DataBase USCHCONF table checked.");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS USUSCONF(USERID INTEGER NOT NULL, TARGET INTEGER NOT NULL, NG BOOLEAN DEFAULT 0, COLOR TEXT DEFAULT 'WHITE', BOLD BOOLEAN DEFAULT 0, ITALIC BOOLEAN DEFAULT 0, LINE BOOLEAN DEFAULT 0, STRIKE BOOLEAN DEFAULT 0, UNIQUE(USERID,TARGET), FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");
            m.info("DataBase USUSCONF table checked.");
            stmt.close();
            m.info("EcoChat database connected.");
        } catch (SQLException ex) {
            m.Warn("コネクション失敗："+ex.getMessage());
        } catch (ClassNotFoundException ex) {
            m.Warn("DBシステム異常："+ex.getMessage());
        }
    }
    public void EcoBansDB_() {
        finalize();
    }
    public void finalize() {
        if (con == null) {return;}
        try {
            con.close();
            con = null;
        } catch (SQLException ex) {
            m.Warn("DBシステム異常："+ex.getMessage());
        }
    }
/*    public void test() {
        if (con == null) {
            m.Warn("★★★★未ロード★★★★");
            return;
        }
        PreparedStatement prep = null;
        try {
            // UUIDテーブルに現在の名前を登録
            prep = con.prepareStatement("INSERT INTO CHANNEL(TAG, NAME) VALUES (?, ?);");
            prep.setString(1, "testtag");
            prep.setString(2, "testname");
            prep.executeUpdate();
            con.commit();
            prep.close();

            prep = con.prepareStatement("INSERT INTO CHCONF(ID) VALUES (?);");
            prep.setInt(1, 1);
            prep.executeUpdate();
            con.commit();
            prep.close();

            prep = con.prepareStatement("DELETE FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, 1);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;

    }
    */
    public int getChannelId(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelID DB取得できません★★★★");
            return -1;
        }
        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            prep = con.prepareStatement("SELECT ID FROM CHANNEL WHERE TAG LIKE ?;");
            prep.setString(1, tag);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getInt("ID");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public boolean isExistChannel(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelID DB取得できません★★★★");
            return false;
        }
        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            prep = con.prepareStatement("SELECT ID FROM CHANNEL WHERE TAG LIKE ?;");
            prep.setString(1, tag);
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public boolean isExistChannelUser(String tag, String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }
        
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[isExistChannelUser]指定ユーザーのUUID情報の取得に失敗しました");

        return isExistChannelUser(tag, uid);
    }
    public boolean isExistChannelUser(String tag, UUID uid) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        
        // UUID取得
        if (uid == null) throw new Exception("[isExistChannelUser]指定ユーザーのUUID情報の取得に失敗しました");

        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT OWNER FROM CHUSERS WHERE ID = ? AND MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setInt(1, id);
            prep.setLong(2, uid.getMostSignificantBits());
            prep.setLong(3, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public int getActiveChannel(String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return -1;
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[getActiveChannel]指定ユーザーのUUID情報の取得に失敗しました");

        int id = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT ACTIVE FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                id = rs.getInt("ACTIVE");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return id;
    }
    public boolean isActiveChannel(String tag, String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[isActiveChannel]指定ユーザーのUUID情報の取得に失敗しました");

        // アクティブチャンネル取得
        int id = getActiveChannel(name);
        return (getChannelTag(id).equals(tag));
    }
    public boolean isChannelOwner(String tag, String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return false; }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[isChannelOwner]指定ユーザーのUUID情報の取得に失敗しました");
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT OWNER FROM CHUSERS WHERE ID = ? AND OWNER = ? AND MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setInt(1, id);
            prep.setBoolean(2, true);
            prep.setLong(3, uid.getMostSignificantBits());
            prep.setLong(4, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                result = rs.getBoolean("OWNER");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public boolean isExistChannelPass(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return false; }

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT PASS FROM CHPASS WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public boolean isExistUserData(String name) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) return false;

        return isExistUserData(uid);
    }
    public boolean isExistUserData(UUID uid) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }
        if (uid == null) return false;

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public int getUserID(String name) throws Exception {
        if (con == null) {
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }
        
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[getUserID]指定ユーザーのUUID情報取得に失敗しました");

        return getUserID(uid);
    }
    public int getUserID(UUID uid) throws Exception {
        if (con == null) {
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }
        
        if (uid == null) throw new Exception("[getUserID]指定ユーザーのUUID情報取得に失敗しました");

        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT USERID FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getInt("USERID");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (ret == -1) throw new Exception("指定ユーザーのチャンネルチャット情報が見つかりませんでした");
        return ret;
    }
    public UUID getUserUUID(int id) throws Exception {
        if (con == null) {
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }
        
        UUID ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT MOSTUUID,LEASTUUID FROM USERS WHERE USERID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new UUID(rs.getLong("MOSTUUID"),rs.getLong("LEASTUUID"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (ret == null) throw new Exception("[getUserUUID]指定ユーザーのUUIDが見つかりませんでした");
        return ret;
    }
    public boolean isChannelListed(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return false; }

        boolean ret = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT LISTED FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getBoolean("LISTED");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public boolean isChannelAutoJoin(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return false; }

        boolean ret = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT AUTOJOIN FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getBoolean("AUTOJOIN");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public boolean isChannelAddPerm(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return false;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return false; }

        boolean ret = false;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT ADDPERM FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getBoolean("ADDPERM");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public ChatType getChannelType(String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBが取得できませんでした");
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルが存在しませんでした");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できませんでした"); }
        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT TYPE FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getInt("TYPE");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (ret == -1) throw new Exception("指定チャンネルのチャンネルタイプが取得できませんでした");
        ChatType type = null;
        if (ret == 0) type = ChatType.global;
        else if (ret == 1) type = ChatType.local;
        else if (ret == 2) type = ChatType.world;
        else throw new Exception("取得したチャンネルタイプが無効な値です");
        return type;
    }
    public String getChannelPass(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return null;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return null; }

        String ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT PASS FROM CHPASS WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("PASS");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        return ret;
    }

    public void addChannel(String tag, String name, ChatType type, String enter, String leave, boolean auto, boolean list, boolean addperm) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 既に登録されている旨で返却
        if (isExistChannel(tag)) {
            throw new Exception("既にチャンネルが存在します");
        }
        PreparedStatement prep = null;
        try {
            // チャンネルテーブルに登録
            StringBuilder node = new StringBuilder();
            StringBuilder set = new StringBuilder();
            if (type != null) { set.append(",?"); node.append(",TYPE"); }
            if (enter != null) { set.append(",?"); node.append(",ENTERMSG"); }
            if (leave != null) { set.append(",?"); node.append(",LEAVEMSG"); }
            prep = con.prepareStatement("INSERT INTO CHANNEL(TAG, NAME, AUTOJOIN, LISTED, ADDPERM, SINCE" + node.toString() + ") VALUES (?, ?, ?, ?, ?, ?" + set.toString() + ");");
            prep.setString(1, tag);
            prep.setString(2, name);
            prep.setBoolean(3, auto);
            prep.setBoolean(4, list);
            prep.setBoolean(5, addperm);
            prep.setLong(6, new Date().getTime());
            int cnt = 7;
            if (type!=null) { prep.setInt(cnt, type.getId()); cnt++; }
            if (enter!=null) { prep.setString(cnt, enter); cnt++; }
            if (leave!=null) { prep.setString(cnt, leave); cnt++; }
            prep.executeUpdate();
            con.commit();
            prep.close();
            
            prep = con.prepareStatement("INSERT INTO CHCONF(ID) VALUES (?);");
            prep.setInt(1, getChannelId(tag));
            prep.executeUpdate();
            con.commit();
            prep.close();

        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void addChannelUser(String tag, String name, boolean owner) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 既に登録されている旨で返却
        if (isExistChannelUser(tag, name)) {
            throw new Exception("既にこのユーザーはチャンネルに所属しています");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できませんでした"); }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[addChannelUser]指定ユーザーのUUID情報の取得に失敗しました");

        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("INSERT INTO CHUSERS(ID, MOSTUUID, LEASTUUID, OWNER, JOINDATE) VALUES (?, ?, ?, ?, ?);");
            prep.setInt(1, id);
            prep.setLong(2, uid.getMostSignificantBits());
            prep.setLong(3, uid.getLeastSignificantBits());
            prep.setBoolean(4, owner);
            prep.setLong(5, new Date().getTime());
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void addChannelPass(String tag, String pass) throws Exception {
        // パスワード指定が無ければ何もしないで終了
        if (pass == null) return;
        
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 既に登録されている旨で返却
        if (isExistChannelPass(tag)) {
            throw new Exception("既にこのチャンネルは登録されています");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("INSERT INTO CHPASS(ID, PASS) VALUES (?, ?);");
            prep.setInt(1, id);
            prep.setString(2, pass);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void delChannel(String tag) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("DELETE FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void delChannelUser(String tag, String name) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[delChannelUser]指定ユーザーのUUID情報の取得に失敗しました");

        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("DELETE FROM CHUSERS WHERE ID = ? AND MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setInt(1, id);
            prep.setLong(2, uid.getMostSignificantBits());
            prep.setLong(3, uid.getLeastSignificantBits());
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void delChannelPass(String tag) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        // パスワード
        if (!isExistChannelPass(tag)) throw new Exception("チャンネルのパスワードが設定されていません");
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("DELETE FROM CHPASS WHERE ID = ?;");
            prep.setInt(1, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelOwner(String tag, String name, boolean owner) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        if (store == null) throw new Exception("[updateChannelOwner]なぜかstoreがnull");
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[updateChannelOwner]指定ユーザーのUUID情報の取得に失敗しました");

        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHUSERS SET OWNER = ? WHERE ID = ? AND MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setBoolean(1, owner);
            prep.setInt(2, id);
            prep.setLong(3, uid.getMostSignificantBits());
            prep.setLong(4, uid.getLeastSignificantBits());
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelTag(String tag, String newtag) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET TAG = ? WHERE ID = ?;");
            prep.setString(1, newtag);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateJoinMsg(String tag, String msg) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET JOINMSG = ? WHERE ID = ?;");
            prep.setString(1, msg);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateLeaveMsg(String tag, String msg) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET LEAVEMSG = ? WHERE ID = ?;");
            prep.setString(1, msg);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelName(String tag, String name) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET NAME = ? WHERE ID = ?;");
            prep.setString(1, name);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelType(String tag, String type) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        
        // チャンネルタイプ
        ChatType t = ChatType.valueOf(type);
        if (t == null) throw new Exception("チャンネルタイプの判定に失敗しました");
        
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET TYPE = ? WHERE ID = ?;");
            prep.setInt(1, t.getId());
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelAutoJoin(String tag, boolean auto) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET AUTOJOIN = ? WHERE ID = ?;");
            prep.setBoolean(1, auto);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelListed(String tag, boolean list) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET LISTED = ? WHERE ID = ?;");
            prep.setBoolean(1, list);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateChannelAddPerm(String tag, boolean perm) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できませんでした"); }
        
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE CHANNEL SET ADDPERM = ? WHERE ID = ?;");
            prep.setBoolean(1, perm);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public void updateActiveChannel(String tag, String name) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            m.Warn("★★★★addPlayer DB取得できません★★★★");
            throw new Exception("データベースの取得に失敗しました");
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルは存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDの取得に失敗しました"); }
        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[updateActiveChannel]指定ユーザーのUUID情報の取得に失敗しました");

        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("UPDATE USERS SET ACTIVE = ? WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setInt(1, id);
            prep.setLong(2, uid.getMostSignificantBits());
            prep.setLong(3, uid.getLeastSignificantBits());
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public int countChannelUsers(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return -1;
        }
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return -1;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return -1; }

        int ret = -1;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
           // ユーザー検索
            prep = con.prepareStatement("SELECT COUNT(MOSTUUID) FROM CHUSERS WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getInt(1);
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        return ret;
    }
    public ArrayList<String> getChannelList(String name, int page, int count) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        ArrayList<String> tags = getUserChannels(name);
        ArrayList<String> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // LISTEDのタグ取得既に存在する場合はスキップ
            prep = con.prepareStatement("SELECT TAG FROM CHANNEL WHERE LISTED = ?;");
            prep.setBoolean(1, true);
            rs = prep.executeQuery();
            while(rs.next()) {
                String tag = rs.getString("TAG");
                if (tags.contains(tag)) continue;
                tags.add(tag);
            }
            rs.close();
            prep.close();

            // トータル件数取得
            int total = tags.size();
            if (total == 0) throw new Exception("表示可能なチャンネルがありませんでした");

            // 指定件数タグ取得
            int startcount = ((page*count)-count);
            int endcount = (page*count);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (String s : tags) {
                if (!first) {
                    sb.append(" OR ");
                }
                sb.append("TAG = ?");
                first = false;
            }
            prep = con.prepareStatement("SELECT * FROM CHANNEL WHERE " + sb.toString() + " ORDER BY TAG ASC LIMIT ? OFFSET ?;");
            int tagcnt = 1; 
            for (; tagcnt <= total; tagcnt++) {
                prep.setString(tagcnt, tags.get(tagcnt-1));
            }
            prep.setInt(tagcnt, count); tagcnt++;
            prep.setInt(tagcnt, startcount);
            rs = prep.executeQuery();

            sb = new StringBuilder();
            ChatType t = ChatType.global;
            first = true;
            while (rs.next()) {
                if (!first) {
                    sb.append(ChatColor.WHITE+",");
                }
                String tag = rs.getString("TAG");
                if (isExistChannelUser(tag, name)) {
                    sb.append(ChatColor.WHITE);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                if (isActiveChannel(tag, name)) {
                    sb.append(ChatColor.UNDERLINE);
                }
                sb.append(tag);
                String tagname = getChannelName(tag);
                if (!tagname.equals(tag)) {
                    sb.append(":"+tagname);
                }
                first = false;
            }
            sb.append(ChatColor.RESET);
            // 成型して返却
            if (total < endcount) {endcount = total;}
            if (startcount > endcount) {endcount = 0; startcount = 0; }
            list.add("===  "+page+"ページ目：全"+total+"件中"+(startcount+1)+"～"+endcount+"件目 ===");
            list.add(sb.toString());
            list.add("===================================================");
            rs.close();
            prep.close();
        
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return list;
    }
    public ArrayList<String> getChannelUsers(String tag, int page, int count) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return null;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return null; }

        ArrayList<String> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // トータル件数取得
            int total = 0;
            prep = con.prepareStatement("SELECT COUNT(*) FROM CHUSERS WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                total = rs.getInt(1);
            }
            rs.close();
            prep.close();

            // 指定件数タグ取得
            int startcount = ((page*count)-count);
            int endcount = (page*count);
            prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE ID = ? ORDER BY MOSTUUID ASC LIMIT ? OFFSET ?;");
            prep.setInt(1, id);
            prep.setInt(2, count);
            prep.setInt(3, startcount);
            rs = prep.executeQuery();

            // 成型して返却
            StringBuilder sb = new StringBuilder();
            EcoUserUUIDStore store = plg.getUman().getStore();
            if (total < endcount) {endcount = total;}
            if (startcount > endcount) {endcount = 0; startcount = 0; }
            boolean first = true;
            ArrayList<String> onlines = new ArrayList<>();
            for (Player p : plg.getServer().getOnlinePlayers()) {
                onlines.add(p.getName());
            }
            while (rs.next()) {
                if (!first) {
                    sb.append(ChatColor.WHITE+",");
                }
                String name = store.latestName(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")));
                if (onlines.contains(name)) {
                    sb.append(ChatColor.WHITE);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                sb.append(name);
                first = false;
            }
            list.add("--- ["+tag+"]チャンネル参加者リスト "+page+"ページ目：全"+total+"人中"+(startcount+1)+"～"+endcount+"人目 ---");
            list.add(sb.toString());
            list.add("---------------------------------------------------------");
            rs.close();
            prep.close();
        
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return list;
    }
    public ArrayList<String> getChannelUsers(String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBが読み込めませんでした");
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定したチャンネルが存在しませんでした");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できませんでした"); }

        ArrayList<String> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // 指定件数タグ取得
            prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();

            // 成型して返却
            EcoUserUUIDStore store = plg.getUman().getStore();
            while (rs.next()) {
                String name = store.latestName(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")));
                list.add(name);
            }
            rs.close();
            prep.close();
        
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return list;
    }
    public String getChannelOwner(String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBが取得できません");
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定チャンネルが存在しません");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できません"); }

        PreparedStatement prep = null;
        ResultSet rs = null;
        StringBuilder sb = new StringBuilder();
        try {
            // 指定件数タグ取得
            prep = con.prepareStatement("SELECT * FROM CHUSERS WHERE ID = ? AND OWNER = ? ORDER BY MOSTUUID;");
            prep.setInt(1, id);
            prep.setBoolean(2, true);
            rs = prep.executeQuery();

            // 成型して返却
            EcoUserUUIDStore store = plg.getUman().getStore();
            boolean first = true;
            ArrayList<String> onlines = new ArrayList<>();
            for (Player p : plg.getServer().getOnlinePlayers()) {
                onlines.add(p.getName());
            }
            while (rs.next()) {
                if (!first) {
                    sb.append(ChatColor.WHITE+",");
                }
                String name = store.latestName(new UUID(rs.getLong("MOSTUUID"), rs.getLong("LEASTUUID")));
                if (onlines.contains(name)) {
                    sb.append(ChatColor.WHITE);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                sb.append(name);
                first = false;
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return sb.toString();
    }
    public ArrayList<String> getChannelInfo(String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBが取得できません");
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("指定チャンネルが存在しませんでした");
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("チャンネルIDが取得できませんでした"); }

        ArrayList<String> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        boolean pass = isExistChannelPass(tag);
        try {
            // 基本情報取得
            prep = con.prepareStatement("SELECT * FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            ChatType a = ChatType.global;
            SimpleDateFormat sdf = new SimpleDateFormat("[YYYY-MM-dd HH:mm:ss] ");
            if (result) {
                Date date = new Date(rs.getLong("SINCE"));
                list.add("============ ["+rs.getString("TAG")+":"+rs.getString("NAME")+"]チャンネル情報 =============");
                list.add("作成者グループ:"+getChannelOwner(tag));
                list.add("作成日:"+sdf.format(date));
                list.add("チャットタイプ:"+a.getName(rs.getInt("TYPE")));
                list.add("デフォルト加入:"+m.b(rs.getBoolean("AUTOJOIN")));
                list.add("リスト表示:"+m.b(rs.getBoolean("LISTED")));
                list.add("パスワード:"+m.b(pass));
                list.add("参加時メッセージ:"+rs.getString("ENTERMSG"));
                list.add("離脱時メッセージ:"+rs.getString("LEAVEMSG"));
            }
            rs.close();
            prep.close();
            
            // チャネル設定取得
            ChatConf conf = getChannelConf(tag);
            if (conf != null) {
                list.add("チャンネルカラー:"+m.cnvSTR2COLOR(conf.getColor())+m.cnvSTR2COLOR(conf.getColor()).name()+ChatColor.RESET);
                list.add("太字]"+m.b(conf.isBold())+" 斜体:"+m.b(conf.isItalic())+" 下線:"+m.b(conf.isLine())+" 抹消線:"+m.b(conf.isStrike()));
                list.add("==================================================");
            }
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return list;
    }
    public String getChannelJoinMsg(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return "join msg 読み込み失敗";
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return "join msg 読み込み失敗";
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return "join msg 読み込み失敗"; }

        String ret = "join msg 読み込み失敗";
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT ENTERMSG FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("ENTERMSG");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public String getChannelLeaveMsg(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return "leave msg 読み込み失敗";
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return "leave msg 読み込み失敗";
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return "leave msg 読み込み失敗"; }

        String ret = "leave msg 読み込み失敗";
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT LEAVEMSG FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("LEAVEMSG");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public String getChannelName(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return "";
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return "";
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return ""; }

        String ret = "";
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT NAME FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("NAME");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public String getChannelTag(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return "";
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return "";
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return ""; }

        String ret = "";
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT TAG FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("TAG");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public String getChannelTag(int id) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return "";
        }
        
        String ret = "";
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT TAG FROM CHANNEL WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = rs.getString("TAG");
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public ChatConf getChannelConf(String tag) {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return null;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { return null; }
        
        ChatConf ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM CHCONF WHERE ID = ?;");
            prep.setInt(1, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new ChatConf();
                ret.setColor(rs.getString("COLOR"));
                ret.setBold(rs.getBoolean("BOLD"));
                ret.setItalic(rs.getBoolean("ITALIC"));
                ret.setLine(rs.getBoolean("LINE"));
                ret.setStrike(rs.getBoolean("STRIKE"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public void setChannelConf(String tag, ChatConf conf) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの読み込みに失敗しました");
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("[setChannelConf]指定チャンネルが存在しません:"+tag);
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) { throw new Exception("指定チャンネルのID取得に失敗しました"); }
        
        ChatConf ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("UPDATE CHCONF SET COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE ID = ?;");
            prep.setString(1, conf.getColor());
            prep.setBoolean(2, conf.isBold());
            prep.setBoolean(3, conf.isItalic());
            prep.setBoolean(4, conf.isLine());
            prep.setBoolean(5, conf.isStrike());
            prep.setInt(6, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return;
    }
    public void addUserData(String name, String active) throws Exception {
        // 登録に失敗(DB異常)した旨で返却
        if (con == null) {
            throw new Exception("データベースの取得に失敗しました");
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[addUserData]指定ユーザーのUUID情報の取得に失敗しました");

        // 既に登録されている旨で返却
        if (isExistChannelUser(active, name)) {
            throw new Exception("既にユーザーデータが登録されています");
        }
        // チャンネルIDを取得
        int id = getChannelId(active);
        if (id == -1) { throw new Exception("チャンネルID取得に失敗しました"); }
        
        PreparedStatement prep = null;
        try {
            // チャンネルユーザーテーブルに登録
            prep = con.prepareStatement("INSERT INTO USERS(MOSTUUID, LEASTUUID, ACTIVE) VALUES (?, ?, ?);");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            prep.setInt(3, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        // 正常完了の旨を返却
        return;
    }
    public ArrayList<String> getDefaultChannel() {
        ArrayList<String> ret = new ArrayList<>();
        if (con == null) {
            m.Warn("★★★★ChannelID DB取得できません★★★★");
            return ret;
        }
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            prep = con.prepareStatement("SELECT * FROM CHANNEL;");
            rs = prep.executeQuery();
            while(rs.next()) {
                if (rs.getBoolean("AUTOJOIN")) {
                    ret.add(rs.getString("TAG"));
                }
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public ArrayList<String> getUserChannels(String name) throws Exception {
        ArrayList<String> ret = new ArrayList<>();
        if (con == null) {
            m.Warn("★★★★ChannelID DB取得できません★★★★");
            return ret;
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[getUserChannels]指定ユーザーのUUID情報の取得に失敗しました");

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            prep = con.prepareStatement("SELECT ID FROM CHUSERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            while(rs.next()) {
                int id = rs.getInt("ID");
                ret.add(getChannelTag(id));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public boolean isExistUserChannelConf(String name, String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }

        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            return false;
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) throw new Exception("指定チャンネルのID取得に失敗しました");
        
        // ユーザーID取得
        if (!isExistUserData(name)) return false;
        int sid = getUserID(name);

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USCHCONF WHERE USERID = ? AND ID = ?;");
            prep.setInt(1, sid);
            prep.setInt(2, id);
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public ChatConf getUserChannelConf(String name, String tag) throws Exception{
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("[getUserChannelConf]指定チャンネルが存在しません:"+tag);
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) throw new Exception("指定チャンネルのID取得に失敗しました");
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[getUserChannelConf]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        ChatConf ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USCHCONF WHERE USERID = ? AND ID = ?;");
            prep.setInt(1, sid);
            prep.setInt(2, id);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new ChatConf();
                ret.setColor(rs.getString("COLOR"));
                ret.setBold(rs.getBoolean("BOLD"));
                ret.setItalic(rs.getBoolean("ITALIC"));
                ret.setLine(rs.getBoolean("LINE"));
                ret.setStrike(rs.getBoolean("STRIKE"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public void setUserChannelConf(String name, String tag, ChatConf conf) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの読み込みに失敗しました");
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("[setUserChannelConf]指定チャンネルが存在しません:"+tag);
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) throw new Exception("指定チャンネルのID取得に失敗しました");
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[setUserChannelConf]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        // confの有無をチェック
        if (!isExistUserChannelConf(name, tag)) throw new Exception("指定ユーザー、チャンネルの設定データが見つかりませんでした");
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("UPDATE USCHCONF SET COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE USERID = ? AND ID = ?;");
            prep.setString(1, conf.getColor());
            prep.setBoolean(2, conf.isBold());
            prep.setBoolean(3, conf.isItalic());
            prep.setBoolean(4, conf.isLine());
            prep.setBoolean(5, conf.isStrike());
            prep.setInt(6, sid);
            prep.setInt(7, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return;
    }
    public void addUserChannelConf(String name, String tag) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの読み込みに失敗しました");
        }
        
        // 存在しない旨で返却
        if (!isExistChannel(tag)) {
            throw new Exception("[addUserChannelConf]指定チャンネルが存在しません:"+tag);
        }
        // チャンネルIDを取得
        int id = getChannelId(tag);
        if (id == -1) throw new Exception("指定チャンネルのID取得に失敗しました");
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[addUserChannelConf]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        // confの有無をチェック
        if (isExistUserChannelConf(name, tag)) throw new Exception("既に指定ユーザー、チャンネルの設定データが存在します");
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("INSERT INTO USCHCONF(USERID, ID) VALUES (?,?);");
            prep.setInt(1, sid);
            prep.setInt(2, id);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return;
    }

    public boolean isExistUserUserConf(String name, String target) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの取得に失敗しました");
        }

        // ユーザーID取得
        if (!isExistUserData(name)) return false;
        int sid = getUserID(name);

        // ターゲットユーザーID取得
        if (!isExistUserData(target)) return false;
        int tid = getUserID(target);

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ? AND TARGET = ?;");
            prep.setInt(1, sid);
            prep.setInt(2, tid);
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return result;
    }
    public ChatConf getUserUserConf(UUID uid, UUID target) throws Exception{
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        
        // ユーザーID取得
        if (!isExistUserData(uid)) throw new Exception("[getUserUserConf_1]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(uid);
        
        // ターゲットユーザーID取得
        if (!isExistUserData(target)) throw new Exception("[getUserUserConf_2]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int tid = getUserID(target);

        ChatConf ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ? AND TARGET = ?;");
            prep.setInt(1, sid);
            prep.setInt(2, tid);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new ChatConf();
                ret.setNg(rs.getBoolean("NG"));
                ret.setColor(rs.getString("COLOR"));
                ret.setBold(rs.getBoolean("BOLD"));
                ret.setItalic(rs.getBoolean("ITALIC"));
                ret.setLine(rs.getBoolean("LINE"));
                ret.setStrike(rs.getBoolean("STRIKE"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public ChatConf getUserUserConf(String name, String target) throws Exception{
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[getUserUserConf_1]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        // ターゲットユーザーID取得
        if (!isExistUserData(target)) throw new Exception("[getUserUserConf_2]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int tid = getUserID(target);

        ChatConf ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ? AND TARGET = ?;");
            prep.setInt(1, sid);
            prep.setInt(2, tid);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new ChatConf();
                ret.setNg(rs.getBoolean("NG"));
                ret.setColor(rs.getString("COLOR"));
                ret.setBold(rs.getBoolean("BOLD"));
                ret.setItalic(rs.getBoolean("ITALIC"));
                ret.setLine(rs.getBoolean("LINE"));
                ret.setStrike(rs.getBoolean("STRIKE"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return ret;
    }
    public void setUserUserConf(String name, String target, ChatConf conf) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの読み込みに失敗しました");
        }
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[setUserUserConf_1]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        // ターゲットユーザーID取得
        if (!isExistUserData(target)) throw new Exception("[setUserUserConf_2]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int tid = getUserID(target);

        // confの有無をチェック
        if (!isExistUserUserConf(name, target)) throw new Exception("指定ユーザー、チャンネルの設定データが見つかりませんでした");
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("UPDATE USUSCONF SET NG = ?, COLOR = ?, BOLD = ?, ITALIC = ?, LINE = ?, STRIKE = ? WHERE USERID = ? AND TARGET = ?;");
            prep.setBoolean(1, conf.isNg());
            prep.setString(2, conf.getColor());
            prep.setBoolean(3, conf.isBold());
            prep.setBoolean(4, conf.isItalic());
            prep.setBoolean(5, conf.isLine());
            prep.setBoolean(6, conf.isStrike());
            prep.setInt(7, sid);
            prep.setInt(8, tid);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return;
    }
    public void addUserUserConf(String name, String target) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBの読み込みに失敗しました");
        }
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[addUserUserConf_1]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        // ターゲットユーザーID取得
        if (!isExistUserData(target)) throw new Exception("[addUserUserConf_2]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int tid = getUserID(target);

        // confの有無をチェック
        if (isExistUserUserConf(name, target)) throw new Exception("既に指定ユーザー、チャンネルの設定データが存在します");
        
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("INSERT INTO USUSCONF(USERID, TARGET) VALUES (?,?);");
            prep.setInt(1, sid);
            prep.setInt(2, tid);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return;
    }
    public ArrayList<String> getUserNGList(String name, int page, int count) throws Exception{
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }
        
        // ユーザーID取得
        if (!isExistUserData(name)) throw new Exception("[getUserNGList]指定ユーザーのチャンネルチャット情報が取得できませんでした");
        int sid = getUserID(name);
        
        ArrayList<String> list = new ArrayList<>();
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // トータル件数取得
            int total = 0;
            prep = con.prepareStatement("SELECT COUNT(*) FROM USUSCONF WHERE USERID = ? AND NG = ?;");
            prep.setInt(1, sid);
            prep.setBoolean(2, true);
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                total = rs.getInt(1);
            }
            rs.close();
            prep.close();

            // ユーザー検索
            int startcount = ((page*count)-count);
            int endcount = (page*count);
            prep = con.prepareStatement("SELECT * FROM USUSCONF WHERE USERID = ? AND NG = ? ORDER BY TARGET ASC LIMIT ? OFFSET ?;");
            prep.setInt(1, sid);
            prep.setBoolean(2, true);
            prep.setInt(3, count);
            prep.setInt(4, startcount);
            rs = prep.executeQuery();

            StringBuilder sb = new StringBuilder();
            EcoUserUUIDStore store = plg.getUman().getStore();
            if (total < endcount) {endcount = total;}
            if (startcount > endcount) {endcount = 0; startcount = 0; }
            boolean first = true;
            ArrayList<String> onlines = new ArrayList<>();
            for (Player p : plg.getServer().getOnlinePlayers()) {
                onlines.add(p.getName());
            }
            while (rs.next()) {
                if (!first) {
                    sb.append(ChatColor.WHITE+",");
                }
                UUID uuid = getUserUUID(rs.getInt("TARGET"));
                String uname = store.latestName(uuid);
                if (onlines.contains(uname)) {
                    sb.append(ChatColor.WHITE);
                } else {
                    sb.append(ChatColor.GRAY);
                }
                sb.append(uname);
                first = false;
            }
            list.add("--- ["+name+"]NGユーザーリスト "+page+"ページ目：全"+total+"人中"+(startcount+1)+"～"+endcount+"人目 ---");
            list.add(sb.toString());
            list.add("---------------------------------------------------------");
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }

        return list;
    }
    public boolean isExistUserConf(String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDbが取得できませんでした");
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[isExistUserConf]指定ユーザーのUUID情報取得に失敗しました");

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        return result;
    }
    public UserConfig getUserConf(String name) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            return null;
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[getUserConf]指定ユーザーのUUID情報取得に失敗しました");

        UserConfig ret = null;
        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("SELECT * FROM USERS WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setLong(1, uid.getMostSignificantBits());
            prep.setLong(2, uid.getLeastSignificantBits());
            rs = prep.executeQuery();
            result = rs.next();
            if (result) {
                ret = new UserConfig();
                ret.setMute(rs.getBoolean("MUTE"));
                ret.setLocal(rs.getInt("LOCAL"));
                ret.setInfo(rs.getBoolean("INFO"));
                ret.setRange(rs.getBoolean("RANGE"));
                ret.setNgView(rs.getBoolean("NGVIEW"));
                ret.setRsWarn(rs.getBoolean("RSWARN"));
            }
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        if (ret == null) throw new Exception("ユーザー情報が取得できませんでした");
        return ret;
    }
    public void updateUserConf(String name, UserConfig conf) throws Exception {
        if (con == null) {
            m.Warn("★★★★ChannelUserが取得できません★★★★");
            throw new Exception("チャンネルチャットDBが取得できませんでした");
        }

        // UUID取得
        EcoUserUUIDStore store = plg.getUman().getStore();
        UUID uid = store.latestUUID(name);
        if (uid == null) throw new Exception("[updateUserConf]指定ユーザーのUUID情報取得に失敗しました");

        PreparedStatement prep = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            // ユーザー検索
            prep = con.prepareStatement("UPDATE USERS SET MUTE = ?, LOCAL = ?, INFO = ?, NGVIEW = ?, RANGE = ?, RSWARN = ? WHERE MOSTUUID = ? AND LEASTUUID = ?;");
            prep.setBoolean(1, conf.isMute());
            prep.setInt(2, conf.getLocal());
            prep.setBoolean(3, conf.isInfo());
            prep.setBoolean(4, conf.isNgView());
            prep.setBoolean(5, conf.isRange());
            prep.setBoolean(6, conf.isRsWarn());
            prep.setLong(7, uid.getMostSignificantBits());
            prep.setLong(8, uid.getLeastSignificantBits());
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            m.info(ex.getLocalizedMessage());
            m.info(ex.getMessage());
            m.info(ex.getSQLState());
            if (rs != null) try { rs.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
            if (prep != null) try { prep.close(); } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDB_.class.getName()).log(Level.SEVERE, null, ex1); }
        }
        return;
    }

}


package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochat.m;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;
import org.bukkit.entity.Player;

/**
 * エコチャットDBインサート処理
 * @author ecolight
 */
public class EcoChatDBUpdate extends DatabaseFrame {
    private EcoChatDB db = null;
    public EcoChatDBUpdate(EcoChatDB frame_) {
        super(frame_);
        db = frame_;
    }
    
    public long updateChannel(ChannelData ch) throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネルを追加
            PreparedStatement prep = con.prepareStatement("UPDATE CHANNEL SET NAME = ?, TYPE = ?, ENTERMSG = ?, LEAVEMSG = ?, AUTOJOIN = ?, LISTED = ?, ADDPERM = ?, ACTIVATE = ? WHERE TAG = ?;");
            prep.setString(1, ch.getName());
            prep.setLong(2, ch.getType());
            prep.setString(3, ch.getEnter());
            prep.setString(4, ch.getLeave());
            prep.setBoolean(5, ch.isAuto());
            prep.setBoolean(6, ch.isListed());
            prep.setBoolean(7, ch.isAdd());
            prep.setBoolean(8, ch.isActivate());
            prep.setString(9, ch.getTag());
            prep.executeUpdate();
            commit();
            prep.close();
            ret = getLongByString("CHANNEL", "TAG", ch.getTag(), "ID");
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("チャンネルデータ追加に失敗しました");
        }
        return ret;
    }
    public long insertChannelConf(ChannelConfData conf) throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネルを追加
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHCONF(ID, COLOR, BOLD, ITALIC, LINE, STRIKE) VALUES (?, ?, ?, ?, ?, ?);");
            prep.setLong(1, conf.getId());
            prep.setString(2, conf.getColor());
            prep.setBoolean(3, conf.isBold());
            prep.setBoolean(4, conf.isItalic());
            prep.setBoolean(5, conf.isLine());
            prep.setBoolean(6, conf.isStrike());
            prep.executeUpdate();
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("チャンネル設定データ追加に失敗しました");
        }
        return ret;
    }
    public long insertChannelPass(ChannelPassData pass) throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネルを追加
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHPASS(ID, PASS) VALUES (?, ?);");
            prep.setLong(1, pass.getId());
            prep.setString(2, pass.getPass());
            prep.executeUpdate();
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("チャンネルパスワードデータ追加に失敗しました");
        }
        return ret;
    }
    public void insertUser(Player pl, String active) throws DatabaseControlException {
        try {
            // 指定したタグのチャンネルIDを取得する
            long id;
            try {
                id = getLongByString("CHANNEL", "TAG", active, "ID");
            } catch (SQLException ex) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
                throw new DatabaseControlException("指定したチャンネルが見つかりませんでした");
            }
            
            // チャンネルユーザーを追加
            PreparedStatement prep = con.prepareStatement("INSERT INTO USERS(MOSTUUID, LEASTUUID, ACTIVE) VALUES (?, ?, ?);");
            prep.setLong(1, pl.getUniqueId().getMostSignificantBits());
            prep.setLong(2, pl.getUniqueId().getLeastSignificantBits());
            prep.setLong(3, id);
            prep.executeUpdate();
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("プレイヤーのチャットユーザーデータ追加に失敗しました");
        }
    }
    
    public void insertChannelUser(UUID uuid, ChannelData ch, boolean owner) throws DatabaseControlException {
        try {
            // チャンネルユーザーを追加
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHUSERS(ID, MOSTUUID, LEASTUUID, OWNER, JOINDATE) VALUES (?, ?, ?, ?, ?);");
            prep.setLong(1, ch.getId());
            prep.setLong(2, uuid.getMostSignificantBits());
            prep.setLong(3, uuid.getLeastSignificantBits());
            prep.setBoolean(4, owner);
            prep.setLong(5, new Date().getTime());
            prep.executeUpdate();
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("プレイヤーのチャットユーザーデータ追加に失敗しました");
        }
    }
    public void insertChannelUsers(UUID[] uuids, ChannelData ch, boolean owner) throws DatabaseControlException {
        try {
            // チャンネルユーザーを追加
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHUSERS(ID, MOSTUUID, LEASTUUID, OWNER, JOINDATE) VALUES (?, ?, ?, ?, ?);");
            for (UUID uuid : uuids) {
                prep.setLong(1, ch.getId());
                prep.setLong(2, uuid.getMostSignificantBits());
                prep.setLong(3, uuid.getLeastSignificantBits());
                prep.setBoolean(4, owner);
                prep.setLong(5, new Date().getTime());
                prep.executeUpdate();
            }
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBUpdate.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("プレイヤーのチャットユーザーデータ追加に失敗しました");
        }
    }

}

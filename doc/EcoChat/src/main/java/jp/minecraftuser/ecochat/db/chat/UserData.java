
package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;
import org.bukkit.entity.Player;

/**
 *
 * @author ecolight
 */
public class UserData extends DatabaseFrame{
    private EcoChatDB db = null;
//USERS(
//ユーザーID USERID INTEGER PRIMARY KEY AUTOINCREMENT,
//ユーザーUUID上位 MOSTUUID INTEGER NOT NULL,
//ユーザーUUID下位 LEASTUUID INTEGER NOT NULL,
//発言先設定 ACTIVE INTEGER NOT NULL,
//MUTE指定 MUTE BOOLEAN DEFAULT 0,
//ローカル受信範囲 LOCAL INTEGER DEFAULT 50,
//情報表示設定 INFO BOOLEAN DEFAULT 0,
//NGユーザー表示設定 NGVIEW BOOLEAN DEFAULT 0,
//ローカル送信結果表示設定 RANGE BOOLEAN DEFAULT 1,
//RSコマンドの誤爆防止チェック RSWARN BOOLEAN DEFAULT 1,
//UUIDが固有 UNIQUE(MOSTUUID,LEASTUUID));");
    
    private long id = 0;
    private long most = 0;
    private long least = 0;
    private long active = 0;
    private boolean mute = false;
    private long local = 50;
    private boolean info = false;
    private boolean ng = false;
    private boolean range = true;
    private boolean rs = true;
    
    public void setId(long id_) { id = id_; }
    public void setMost(long most_) { most = most_; }
    public void setLeast(long least_) { least = least_; }
    public void setUUID(UUID uuid_) { most = uuid_.getMostSignificantBits(); least = uuid_.getLeastSignificantBits(); }
    public void setActive(long active_) { active = active_; }
    public void setMute(boolean mute_) { mute = mute_; }
    public void setLocal(long local_) { local = local_; }
    public void setInfo(boolean info_) { info = info_; }
    public void setNg(boolean ng_) { ng = ng_; }
    public void setRange(boolean range_) { range = range_; }
    public void setRs(boolean rs_) { rs = rs_; }
    
    public long getId() { return id; }
    public long getMost() { return most; }
    public long getLeast() { return least; }
    public UUID getUUID() { return new UUID(most, least); }
    public long getActive() { return active; }
    public boolean isMute() { return mute; }
    public long getLocal() { return local; }
    public boolean isInfo() { return info; }
    public boolean isNg() { return ng; }
    public boolean isRange() { return range; }
    public boolean isRs() { return rs; }

    public UserData(EcoChatDB db_, long id_, long most_, long least_, long active_, boolean mute_,
                    long local_, boolean info_, boolean ng_, boolean range_, boolean rs_) {
        super(db_);
        db = db_;
        id = id_;
        most = most_;
        least = least_;
        active = active_;
        mute = mute_;
        local = local_;
        info = info_;
        ng = ng_;
        range = range_;
        rs = rs_;
    }
    public UserData(EcoChatDB db_, Player p_, String active_) throws DatabaseControlException {
        super(db_);
        db = db_;
        most = p_.getUniqueId().getMostSignificantBits();
        least = p_.getUniqueId().getLeastSignificantBits();

        // 指定したタグのチャンネルIDを取得する
        long id;
        try {
            id = getLongByString("CHANNEL", "TAG", active_, "ID");
        } catch (SQLException ex) {
            Logger.getLogger(EcoChatDBInsert.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("指定したチャンネルが見つかりませんでした");
        }
        active = id;
        
    }
    public void insert() throws DatabaseControlException {
        try {
            // エコチャットユーザーを追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが
            // 自動インクリメントのID以外は全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO USERS(MOSTUUID, LEASTUUID, ACTIVE, MUTE, LOCAL, INFO, NGVIEW, RANGE, RSWARN) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);");
            prep.setLong(1, most);
            prep.setLong(2, least);
            prep.setLong(3, id);
            prep.setBoolean(4, mute);
            prep.setLong(5, local);
            prep.setBoolean(6, info);
            prep.setBoolean(7, ng);
            prep.setBoolean(8, range);
            prep.setBoolean(9, rs);
            prep.executeUpdate();
            commit();
            prep.close();
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBInsert.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBInsert.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("プレイヤーのチャットユーザーデータ追加に失敗しました");
        }
    }

}

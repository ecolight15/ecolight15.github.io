
package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
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
public class ChannelUserData extends DatabaseFrame{
    private EcoChatDB db = null;
//CHUSERS(
//チャンネルID ID INTEGER NOT NULL,
//ユーザーUUID上位 MOSTUUID INTEGER NOT NULL,
//ユーザーUUID下位 LEASTUUID INTEGER NOT NULL,
//チャンネルオーナー OWNER BOOLEAN DEFAULT 0,
//チャンネル参加日 JOINDATE INTEGER NOT NULL,
//チャンネルIDに応じて消す FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");
    
    private long id = 0;
    private long most = 0;
    private long least = 0;
    private boolean owner = false;
    private long joindate = 0;
    
    public void setId(long id_) { id = id_; }
    public void setMost(long most_) { most = most_; }
    public void setLeast(long least_) { least = least_; }
    public void setUUID(UUID uuid) { most = uuid.getMostSignificantBits(); least = uuid.getLeastSignificantBits(); }
    public void setOwner(boolean owner_) { owner = owner_; }
    public void setJoinDate(long joindate_) { joindate = joindate_; }
    
    public long getId() { return id; }
    public long getMost() { return most; }
    public long getLeast() { return least; }
    public UUID getUUID() { return new UUID(most, least); }
    public boolean getOwner() { return owner; }
    public long getJoinDate() { return joindate; }
    
    public ChannelUserData(EcoChatDB db_, long id_, long most_, long least_, boolean owner_, long joindate_) {
        super(db_);
        db = db_;
        id = id_;
        most = most_;
        least = least_;
        owner = owner_;
        joindate = joindate_;
    }
    public ChannelUserData(EcoChatDB db_, ChannelData ch_, Player p_, boolean owner_) {
        super(db_);
        db = db_;
        id = ch_.getId();
        most = p_.getUniqueId().getMostSignificantBits();
        least = p_.getUniqueId().getLeastSignificantBits();
        owner = owner_;
    }
    public void insert() throws DatabaseControlException {
        try {
            // チャンネルユーザー設定を追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHUSERS(ID, MOSTUUID, LEASTUUID, OWNER, JOINDATE) VALUES (?, ?, ?, ?, ?);");
            prep.setLong(1, id);
            prep.setLong(2, most);
            prep.setLong(3, least);
            prep.setBoolean(4, owner);
            prep.setLong(5, new Date().getTime());
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

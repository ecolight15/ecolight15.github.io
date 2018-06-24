
package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;

/**
 *
 * @author ecolight
 */
public class FellowNgData extends DatabaseFrame{
    private EcoChatDB db = null;
//USNGCONF(
//ユーザーID USERID INTEGER NOT NULL,
//相手ユーザーID TARGET INTEGER NOT NULL,
//NG指定 NG BOOLEAN DEFAULT 0,
//ユーザーIDと相手ユーザーIDの対が固有 UNIQUE(USERID,TARGET),
//ユーザーIDに応じて消す FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE),
//ターゲットIDに応じて消す FOREIGN KEY(TARGET) REFERENCES USERS(USERID) ON DELETE CASCADE);");

    private long uid = 0;
    private long tid = 0;
    private boolean ng = false;
    
    public void setUId (long uid_) { uid = uid_; }
    public void setTId (long tid_) { tid = tid_; }
    public void setNg (boolean ng_) { ng = ng_; }
    
    public long getUId() { return uid; }
    public long getTId() { return tid; }
    public boolean isNg() { return ng; }

    public FellowNgData(EcoChatDB db_, long uid_, long tid_, boolean ng_) {
        super(db_);
        db = db_;
        uid = uid_;
        tid = tid_;
        ng = ng_;
    }
    public void insert() throws DatabaseControlException {
        try {
            // プレイヤー別NGユーザーを追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO USNGCONF(USERID, TARGET, NG) VALUES (?, ?, ?);");
            prep.setLong(1, uid);
            prep.setLong(2, tid);
            prep.setBoolean(3, ng);
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
            throw new DatabaseControlException("プレイヤーのNGユーザー設定に失敗しました");
        }
    }
}

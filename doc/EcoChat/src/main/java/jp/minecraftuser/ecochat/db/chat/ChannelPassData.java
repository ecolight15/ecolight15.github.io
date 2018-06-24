
package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;

/**
 *
 * @author ecolight
 */
public class ChannelPassData extends DatabaseFrame{
    private EcoChatDB db = null;
//CHPASS(
//チャンネルID ID INTEGER NOT NULL,
//チャンネルパスワード PASS TEXT NOT NULL,
//チャンネルIDに応じて消す FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");

    private long id = 0;
    private String pass = "";
    
    public void setId(long id_) { id = id_; }
    public void setPass(String pass_) { pass = pass_; }
    
    public long getId() { return id; }
    public String getPass() { return pass; }

    public ChannelPassData(EcoChatDB db_, long id_, String pass_) {
        super(db_);
        db = db_;
        id = id_;
        pass = pass_;
    }
    public ChannelPassData(EcoChatDB db_, ResultSet rs_) throws SQLException {
        super(db_);
        db = db_;
        id = rs_.getLong("ID");
        pass = rs_.getString("PASS");
    }
    public long insert() throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネルパスワードを追加
            // 全カラムNOT NULLで初期値もないため全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHPASS(ID, PASS) VALUES (?, ?);");
            prep.setLong(1, id);
            prep.setString(2, pass);
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
            throw new DatabaseControlException("チャンネルパスワードデータ追加に失敗しました");
        }
        return ret;
    }
}

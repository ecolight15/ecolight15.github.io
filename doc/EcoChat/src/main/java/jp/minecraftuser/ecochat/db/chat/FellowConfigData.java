
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
public class FellowConfigData extends DatabaseFrame{
    private EcoChatDB db = null;
//USUSCONF(
//ユーザーID USERID INTEGER NOT NULL,
//相手ユーザーID TARGET INTEGER NOT NULL,
//対ユーザー指定文字色 COLOR TEXT DEFAULT 'WHITE',
//対ユーザー指定太字 BOLD BOOLEAN DEFAULT 0,
//対ユーザー指定斜体 ITALIC BOOLEAN DEFAULT 0,
//対ユーザー指定下線 LINE BOOLEAN DEFAULT 0,
//対ユーザー指定打消し STRIKE BOOLEAN DEFAULT 0,
//ユーザーIDと相手ユーザーIDの対が固有 UNIQUE(USERID,TARGET),
//ユーザーIDに応じて消す FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");

    private long uid = 0;
    private long tid = 0;
    private String color = "WHITE";
    private boolean bold = false;
    private boolean italic = false;
    private boolean line = false;
    private boolean strike = false;
    
    public void setUId (long uid_) { uid = uid_; }
    public void setTId (long tid_) { tid = tid_; }
    public void setColor (String color_) { color = color_; }
    public void setBold (boolean bold_) { bold = bold_; }
    public void setItalic (boolean italic_) { italic = italic_; }
    public void setLine (boolean line_) { line = line_; }
    public void setStrike (boolean strike_) { strike = strike_; }
    
    public long getUId() { return uid; }
    public long getTId() { return tid; }
    public String getColor() { return color; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isLine() { return line; }
    public boolean isStrike() { return strike; }

    public FellowConfigData(EcoChatDB db_, long uid_, long tid_,  String color_, boolean bold_,
                             boolean italic_, boolean line_, boolean strike_) {
        super(db_);
        db = db_;
        uid = uid_;
        tid = tid_;
        color = color_;
        bold = bold_;
        italic = italic_;
        line = line_;
        strike = strike_;
    }
    public void insert() throws DatabaseControlException {
        try {
            // プレイヤー別対人設定データを追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO USUSCONF(USERID, TARGET, COLOR, BOLD, ITALIC, LINE, STRIKE) VALUES (?, ?, ?, ?, ?, ?, ?);");
            prep.setLong(1, uid);
            prep.setLong(2, tid);
            prep.setString(3, color);
            prep.setBoolean(4, bold);
            prep.setBoolean(5, italic);
            prep.setBoolean(6, line);
            prep.setBoolean(7, strike);
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
            throw new DatabaseControlException("プレイヤーのユーザー文字種設定に失敗しました");
        }
    }
}

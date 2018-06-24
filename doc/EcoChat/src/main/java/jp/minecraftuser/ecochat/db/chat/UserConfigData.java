
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
public class UserConfigData extends DatabaseFrame{
    private EcoChatDB db = null;
//USCHCONF(
//ユーザーID USERID INTEGER NOT NULL,
//チャンネルID ID INTEGER NOT NULL,
//ユーザー指定チャンネル文字色 COLOR TEXT DEFAULT 'WHITE',
//ユーザー指定チャンネル太字 BOLD BOOLEAN DEFAULT 0,
//ユーザー指定チャンネル斜体 ITALIC BOOLEAN DEFAULT 0,
//ユーザー指定チャンネル下線 LINE BOOLEAN DEFAULT 0,
//ユーザー指定チャンネル打消し STRIKE BOOLEAN DEFAULT 0,
//ユーザーIDとチャンネルIDの対が固有 UNIQUE(USERID,ID),
//チャンネルIDに応じて消す FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE,
//ユーザーIDに応じて消す FOREIGN KEY(USERID) REFERENCES USERS(USERID) ON DELETE CASCADE);");

    private long uid = 0;
    private long id = 0;
    private String color = "WHITE";
    private boolean bold = false;
    private boolean italic = false;
    private boolean line = false;
    private boolean strike = false;
    
    public void setUId (long uid_) { uid = uid_; }
    public void setId (long id_) { id = id_; }
    public void setColor (String color_) { color = color_; }
    public void setBold (boolean bold_) { bold = bold_; }
    public void setItalic (boolean italic_) { italic = italic_; }
    public void setLine (boolean line_) { line = line_; }
    public void setStrike (boolean strike_) { strike = strike_; }
    
    public long getUId() { return uid; }
    public long getId() { return id; }
    public String getColoe() { return color; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isLine() { return line; }
    public boolean isStrike() { return strike; }

    public UserConfigData(EcoChatDB db_, long uid_, long id_, String color_, boolean bold_,
                             boolean italic_, boolean line_, boolean strike_) {
        super(db_);
        db = db_;
        uid = uid_;
        id = id_;
        color = color_;
        bold = bold_;
        italic = italic_;
        line = line_;
        strike = strike_;
    }
    public void insert() throws DatabaseControlException {
        try {
            // プレイヤー別対チャンネル設定を追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO USUSCONF(USERID, ID, COLOR, BOLD, ITALIC, LINE, STRIKE) VALUES (?, ?, ?, ?, ?, ?, ?);");
            prep.setLong(1, uid);
            prep.setLong(2, id);
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

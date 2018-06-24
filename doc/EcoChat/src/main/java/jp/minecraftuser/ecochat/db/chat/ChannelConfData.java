
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
public class ChannelConfData extends DatabaseFrame{
    private EcoChatDB db = null;
//CHCONF(
//チャンネルID ID INTEGER PRIMARY KEY,
//チャンネル文字色 COLOR TEXT DEFAULT 'WHITE',
//チャンネル太字 BOLD BOOLEAN DEFAULT 0,
//チャンネル斜体 ITALIC BOOLEAN DEFAULT 0,
//チャンネル下線 LINE BOOLEAN DEFAULT 0,
//チャンネル打消し STRIKE BOOLEAN DEFAULT 0,
//チャンネルIDに応じて消す FOREIGN KEY(ID) REFERENCES CHANNEL(ID) ON DELETE CASCADE);");

    private long id = 0;
    private String color = "WHITE";
    private boolean bold = false;
    private boolean italic = false;
    private boolean line = false;
    private boolean strike = false;

    public void setId (long id_) { id = id_; }
    public void setColor (String color_) { color = color_; }
    public void setBold (boolean bold_) { bold = bold_; }
    public void setItalic (boolean italic_) { italic = italic_; }
    public void setLine (boolean line_) { line = line_; }
    public void setStrike (boolean strike_) { strike = strike_; }
    
    public long getId() { return id; }
    public String getColor() { return color; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isLine() { return line; }
    public boolean isStrike() { return strike; }

    public ChannelConfData(EcoChatDB db_, long id_, String color_, boolean bold_,
                             boolean italic_, boolean line_, boolean strike_) {
        super(db_);
        db = db_;
        id = id_;
        color = color_;
        bold = bold_;
        italic = italic_;
        line = line_;
        strike = strike_;
    }
    public ChannelConfData(EcoChatDB db_, ResultSet rs_) throws SQLException {
        super(db_);
        db = db_;
        id = rs_.getLong("ID");
        color = rs_.getString("COLOR");
        bold = rs_.getBoolean("BOLD");
        italic = rs_.getBoolean("ITALIC");
        line = rs_.getBoolean("LINE");
        strike = rs_.getBoolean("STRIKE");
    }
    
    public long insert() throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネル設定データを追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHCONF(ID, COLOR, BOLD, ITALIC, LINE, STRIKE) VALUES (?, ?, ?, ?, ?, ?);");
            prep.setLong(1, id);
            prep.setString(2, color);
            prep.setBoolean(3, bold);
            prep.setBoolean(4, italic);
            prep.setBoolean(5, line);
            prep.setBoolean(6, strike);
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
            throw new DatabaseControlException("チャンネル設定データ追加に失敗しました");
        }
        return ret;
    }
}

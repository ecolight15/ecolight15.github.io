
package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;

/**
 *
 * @author ecolight
 */
public class ChannelData extends DatabaseFrame{
    private EcoChatDB db = null;
//CHANNEL(
//チャンネルID ID INTEGER PRIMARY KEY AUTOINCREMENT,
//チャンネルタグ TAG TEXT NOT NULL UNIQUE,
//チェンネル名 NAME TEXT NOT NULL UNIQUE,
//チャンネルタイプ TYPE INTEGER DEFAULT 0,
//参加メッセージ ENTERMSG TEXT DEFAULT '{p1}さん、ようこそ{p2}チャンネルへ！',
//退出メッセージ LEAVEMSG TEXT DEFAULT '{p1}さん、{p2}チャンネルのご利用ありがとうございました。',
//デフォルト参加チャンネル AUTOJOIN BOOLEAN DEFAULT 0,
//リスト表示有無 LISTED BOOLEAN DEFAULT 0,
//他ユーザー追加可否 ADDPERM BOOLEAN DEFAULT 0,
//作成日 SINCE INTEGER DEFAULT 0);");
  
    public long id = 0;
    private String tag = "";
    private String name = "";
    private long type = 0;
    private String enter = "{p1}さん、ようこそ{p2}チャンネルへ！";
    private String leave = "{p1}さん、{p2}チャンネルのご利用ありがとうございました。";
    private boolean auto = false;
    private boolean listed = false;
    private boolean add = false;
    private boolean activate = false;
    private long since = 0;
    
    public void setId (long id_) { id = id_; } 
    public void setTag (String tag_) { tag = tag_; } 
    public void setName (String name_) { name = name_; } 
    public void setType (long type_) { type = type_; } 
    public void setEnter (String enter_) { enter = enter_; } 
    public void setLeave (String leave_) { leave = leave_; } 
    public void setAuto (boolean auto_) { auto = auto_; } 
    public void setListed (boolean listed_) { listed = listed_; } 
    public void setAdd (boolean add_) { add = add_; } 
    public void setActivate (boolean activate_) { activate = activate_; } 
    public void setSince (long since_) { since = since_; } 

    public long getId () { return id; } 
    public String getTag () { return tag; } 
    public String getName () { return name; } 
    public long getType () { return type; } 
    public String getEnter () { return enter; } 
    public String getLeave () { return leave; } 
    public boolean isAuto () { return auto; } 
    public boolean isListed () { return listed; } 
    public boolean isAdd () { return add; } 
    public boolean isActivate () { return activate; } 
    public long getSince () { return since; } 

    public ChannelData(EcoChatDB db_, long id_, String tag_, String name_, long type_,
                         String enter_, String leave_, boolean auto_, boolean listed_, boolean add_, boolean activate_, long since_) {
        super(db_);
        db = db_;
        id = id_;
        tag = tag_;
        name = name_;
        type = type_;
        enter = enter_;
        leave = leave_;
        auto = auto_;
        listed = listed_;
        add = add_;
        activate = activate_;
        since = since_;
    }
    public ChannelData(EcoChatDB db_, String tag_) {
        super(db_);
        db = db_;
        tag = tag_;
        // 名前なしなのでタグと同じ名称にしておく
        name = tag_;
    }
    
    public ChannelData(EcoChatDB db_, ResultSet rs_) throws SQLException {
        super(db_);
        db = db_;
        id = rs_.getLong("ID");
        tag = rs_.getString("TAG");
        name = rs_.getString("NAME");
        type = rs_.getLong("TYPE");
        enter = rs_.getString("ENTERMSG");
        leave = rs_.getString("LEAVEMSG");
        auto = rs_.getBoolean("AUTOJOIN");
        listed = rs_.getBoolean("LISTED");
        add = rs_.getBoolean("ADDPERM");
        activate = rs_.getBoolean("ACTIVATE");
        since = rs_.getLong("SINCE");
    }

    public long insert() throws DatabaseControlException {
        long ret = 0;
        try {
            // チャンネルを追加
            // クラスのメンバ変数も初期値に合わせてあるので、SQLでdefault値はあるが
            // 自動インクリメントのID以外は全挿入する
            PreparedStatement prep = con.prepareStatement("INSERT INTO CHANNEL(TAG, NAME, ADDPERM, ACTIVATE, SINCE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
            prep.setString(1, tag);
            prep.setString(2, name);
            prep.setLong(3, type);
            prep.setString(4, enter);
            prep.setString(5, leave);
            prep.setBoolean(6, auto);
            prep.setBoolean(7, listed);
            prep.setBoolean(8, add);
            prep.setBoolean(9, activate);
            prep.setLong(10, new Date().getTime());
            prep.executeUpdate();
            commit();
            prep.close();
            ret = getLongByString("CHANNEL", "TAG", tag, "ID");
        } catch (SQLException ex) {
            try {
                rollback();
            } catch (SQLException ex1) {
                Logger.getLogger(EcoChatDBInsert.class.getName()).log(Level.SEVERE, null, ex1);
            }
            Logger.getLogger(EcoChatDBInsert.class.getName()).log(Level.SEVERE, null, ex);
            throw new DatabaseControlException("チャンネルデータ追加に失敗しました");
        }
        return ret;
    }
}

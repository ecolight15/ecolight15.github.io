
package jp.minecraftuser.ecochat.db.log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import jp.minecraftuser.ecochat.EcoChat;
import jp.minecraftuser.ecochat.m;
import jp.minecraftuser.ecoframework.PluginFrame;
import org.bukkit.ChatColor;

public class MsgLoggerDB {
    private Connection con = null;
    
    public MsgLoggerDB(PluginFrame plg) {
        String msgDBpath = plg.getDataFolder().getPath()+"/msg.db";
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:"+msgDBpath);
            con.setAutoCommit(false);
            
            // 必要テーブルの追加
            Statement stmt = con.createStatement();
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS MSGDB(TIME INTEGER, CHANNEL TEXT, PLAYER TEXT, MSG TEXT, PRIMARY KEY(TIME, CHANNEL));");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS PMDB(TIME INTEGER, PLAYER TEXT, RECEIVER TEXT, MSG TEXT, PRIMARY KEY(TIME, PLAYER));");
            stmt.close();
        } catch (SQLException ex) {
            m.Warn("コネクション失敗："+ex.getMessage());
        } catch (ClassNotFoundException ex) {
            m.Warn("DBシステム異常："+ex.getMessage());
        }
    }
    public void MsgLoggerDB_() {
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
    public void msgLogging(String channel, String player, String msg) {
        if (con == null) {return;}
        try {
            Date date = new Date();
            PreparedStatement prep = con.prepareStatement("INSERT INTO MSGDB VALUES (?, ?, ?, ?);");
            prep.setLong(1, date.getTime());
            prep.setString(2, channel.toLowerCase());
            prep.setString(3, player);
            prep.setString(4, msg);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
        }
    }
    public void pmLogging(String player, String receiver, String msg) {
        if (con == null) {return;}
        try {
            Date date = new Date();
            PreparedStatement prep = con.prepareStatement("INSERT INTO PMDB VALUES (?, ?, ?, ?);");
            prep.setLong(1, date.getTime());
            prep.setString(2, player);
            prep.setString(3, receiver);
            prep.setString(4, msg);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
            m.Warn(ex.getMessage());
        }
    }
    public ArrayList<String> msgSearch(String channel, Date start, Date end, int page, int count) {
        if (con == null) {return null;}
        ArrayList<String> msglist = null;
        try {
            // トータル件数取得
            int total = 0;
            PreparedStatement prep = con.prepareStatement("SELECT COUNT(*) FROM MSGDB WHERE CHANNEL = ? AND TIME >= ? AND TIME <= ?;");
            prep.setString(1, channel.toLowerCase());
            prep.setLong(2, start.getTime());
            prep.setLong(3, end.getTime());
            ResultSet rs = prep.executeQuery();
            rs.next();
            total = rs.getInt(1);
            rs.close();
            prep.close();
            // 指定期間の実データを取得
            int startcount = ((page*count)-count);
            int endcount = (page*count);
            prep = con.prepareStatement("SELECT TIME,MSG,PLAYER FROM MSGDB WHERE CHANNEL = ? AND TIME >= ? AND TIME <= ? ORDER BY TIME ASC LIMIT ? OFFSET ?;");
            prep.setString(1, channel.toLowerCase());
            prep.setLong(2, start.getTime());
            prep.setLong(3, end.getTime());
            prep.setInt(4, count);
            prep.setInt(5, startcount);
            rs = prep.executeQuery();
            // 成型して返却
            msglist = new ArrayList<>();
            if (total < endcount) {endcount = total;}
            if (startcount > endcount) {endcount = 0; startcount = 0; }
            msglist.add("========== ["+channel+"] "+page+"ページ目：全"+total+"件中"+(startcount+1)+"～"+endcount+"件目 ==========");
            SimpleDateFormat sdf = new SimpleDateFormat("[MM/dd_HH:mm] ");
            while (rs.next()) {
                Date date = new Date(rs.getLong("TIME"));
                msglist.add(m.repColor(sdf.format(date)+"<"+rs.getString("PLAYER")+"> "+rs.getString("MSG")+ChatColor.RESET));
            }
            msglist.add("===================================================");
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには乗せない
        }
        return msglist;
    }
    public ArrayList<String> pmSearch(String player, Date start, Date end, int page, int count) {
        if (con == null) {return null;}
        ArrayList<String> msglist = null;
        try {
            // トータル件数取得
            int total = 0;
            PreparedStatement prep = con.prepareStatement("SELECT COUNT(*) FROM PMDB WHERE (PLAYER = ? OR RECEIVER = ?) AND TIME >= ? AND TIME <= ?;");
            prep.setString(1, player);
            prep.setString(2, player);
            prep.setLong(3, start.getTime());
            prep.setLong(4, end.getTime());
            ResultSet rs = prep.executeQuery();
            rs.next();
            total = rs.getInt(1);
            rs.close();
            prep.close();
            // 指定期間の実データを取得
            int startcount = ((page*count)-count);
            int endcount = (page*count);
            prep = con.prepareStatement("SELECT PLAYER,RECEIVER,TIME,MSG FROM PMDB WHERE (PLAYER = ? OR RECEIVER = ?) AND TIME >= ? AND TIME <= ? ORDER BY TIME ASC LIMIT ? OFFSET ?;");
            prep.setString(1, player);
            prep.setString(2, player);
            prep.setLong(3, start.getTime());
            prep.setLong(4, end.getTime());
            prep.setInt(5, count);
            prep.setInt(6, startcount);
            rs = prep.executeQuery();
            // 成型して返却
            msglist = new ArrayList<>();
            if (total < endcount) {endcount = total;}
            if (startcount > endcount) {endcount = 0; startcount = 0; }
            msglist.add("========== "+page+"ページ目：全"+total+"件中"+(startcount+1)+"～"+endcount+"件目 ==========");
            SimpleDateFormat sdf = new SimpleDateFormat("[MM/dd_HH:mm] ");
            while (rs.next()) {
                Date date = new Date(rs.getLong("TIME"));
                msglist.add(m.repColor(sdf.format(date)+rs.getString("PLAYER")+" -> "+rs.getString("RECEIVER")+": "+rs.getString("MSG")+ChatColor.RESET));
            }
            msglist.add("===================================================");
            rs.close();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには乗せない
            m.Warn(ex.getMessage());
        }
        return msglist;
    }
    public void msgDelete(String channel) {
        if (con == null) {return;}
        try {
            PreparedStatement prep = con.prepareStatement("DELETE FROM MSGDB WHERE CHANNEL = ?;");
            prep.setString(1, channel);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
        }
    }
    public void msgDeletePlayer(String player) {
        if (con == null) {return;}
        try {
            PreparedStatement prep = con.prepareStatement("DELETE FROM MSGDB WHERE PLAYER = ?;");
            prep.setString(1, player);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
        }
    }
    public void pmDelete(String player) {
        if (con == null) {return;}
        try {
            PreparedStatement prep = con.prepareStatement("DELETE FROM PMDB WHERE PLAYER = ?;");
            prep.setString(1, player);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
        }
    }
    public void pmDeleteReceiver(String receiver) {
        if (con == null) {return;}
        try {
            PreparedStatement prep = con.prepareStatement("DELETE FROM PMDB WHERE RECEIVER = ?;");
            prep.setString(1, receiver);
            prep.executeUpdate();
            con.commit();
            prep.close();
        } catch (SQLException ex) {
            // サーバーログには残さない
        }
    }
}

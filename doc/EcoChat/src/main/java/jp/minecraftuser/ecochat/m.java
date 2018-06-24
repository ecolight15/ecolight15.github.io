
package jp.minecraftuser.ecochat;

import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.ChatColor;

public final class m {
    private static HashMap<String, String> msgMap = null;
    private static EcoChat plg = null;
    private static Logger logger = null;
    public static String repColor(String msg) {
        return repSpace(msg.replaceAll("&([0-9A-Fa-flLmMnNoOkKrR])", "§$1"));
    }
    public static String removeColor(String msg) {
        return msg.replaceAll("&([0-9A-Fa-flLmMnNoOkKrR])", "");
    }
    public static String repSpace(String msg) {
        return msg.replaceAll("　", "  ");
    }
    public static String get(String format){
        if (msgMap == null) return "";
        String msgbuf = msgMap.get(format);
        if (msgbuf == null) return "MessageLoadErr";
        return "§d[情報]§f" + msgParser(msgbuf);
    }
    public static String get(String format, String p1){
        if (msgMap == null) return "";
        String msgbuf = msgMap.get(format);
        if (msgbuf == null) return "MessageLoadErr";
        return "§d[情報]§f" + msgParser(msgbuf, p1);
    }
    public static String get(String format, String p1, String p2){
        if (msgMap == null) return "";
        String msgbuf = msgMap.get(format);
        if (msgbuf == null) return "MessageLoadErr";
        return "§d[情報]§f" + msgParser(msgbuf, p1, p2);
    }
    public static String repInfo(String msg, String player, String channel) {
        return msg.replace("{p1}", player).replace("{p2}", channel);
    }
    private static String msgParser(String msg){
        return msg;
    }
    private static String msgParser(String msg, String p1){
        return msg.replace("{p1}", p1);
    }
    private static String msgParser(String msg, String p1, String p2){
        return msg.replace("{p1}", p1).replace("{p2}", p2);
    }

    public static boolean msgLoad(String loc, EcoChat plugin){
        if (msgMap == null) msgMap = new HashMap<String, String>();
        if (plg == null) plg = plugin;
        if (logger == null) logger = plugin.getLogger();
        msgMap.clear();
        // そのうちファイル読み込み実装

        // システム系
        msgMap.put("plg_reload", "設定ファイルを読み込み直しました");
        msgMap.put("cmd_reject_notperm", "コマンドの実行権限がありません({p1})");
        msgMap.put("cmd_reject_console", "このコマンドはコンソールから実行できません");
        msgMap.put("cmd_reject_usernotofound", "指定したユーザーは見つかりませんでした");
        msgMap.put("cmd_reject_useroffline", "指定したユーザーは現在ログインしていません");
        msgMap.put("cmd_param_invalid", "コマンドパラメータの指定が不正です");
        msgMap.put("cmd_param_many", "コマンドパラメータが多いです");
        msgMap.put("cmd_param_fewer", "コマンドパラメータが不足しています");

        // コマンド実行結果系
        msgMap.put("cmd_result_setspawn", "ワールド[{p1}]のスポーン位置を設定しました");
        msgMap.put("cmd_result_spawn", "ワールド[{p1}]のスポーン位置へテレポートしました");
        msgMap.put("cmd_result_tp", "ユーザー[{p1}]の位置にテレポートしました");

        return true;
    }
    
    public static Logger log(){
        return logger;
    }
    public static void info(String msg){
        logger.info(msg);
    }
    public static void Warn(String msg){
        logger.warning(msg);
    }
    public static String plg(String msg) {
        return "§d[EcoChat] "+msg;
    }
    
    public static ChatColor cnvSTR2COLOR(String color) {
        ChatColor result = ChatColor.WHITE;
        if (color.equalsIgnoreCase("black")) {result = ChatColor.BLACK;} 
        else if(color.equalsIgnoreCase("dark_blue")) {result = ChatColor.DARK_BLUE;}
        else if(color.equalsIgnoreCase("dark_green")) {result = ChatColor.DARK_GREEN;}
        else if(color.equalsIgnoreCase("dark_aqua")) {result = ChatColor.DARK_AQUA;}
        else if(color.equalsIgnoreCase("dark_red")) {result = ChatColor.DARK_RED;}
        else if(color.equalsIgnoreCase("dark_purple")) {result = ChatColor.DARK_PURPLE;}
        else if(color.equalsIgnoreCase("gold")) {result = ChatColor.GOLD;}
        else if(color.equalsIgnoreCase("gray")) {result = ChatColor.GRAY;}
        else if(color.equalsIgnoreCase("dark_gray")) {result = ChatColor.DARK_GRAY;}
        else if(color.equalsIgnoreCase("blue")) {result = ChatColor.BLUE;}
        else if(color.equalsIgnoreCase("green")) {result = ChatColor.GREEN;}
        else if(color.equalsIgnoreCase("aqua")) {result = ChatColor.AQUA;}
        else if(color.equalsIgnoreCase("red")) {result = ChatColor.RED;}
        else if(color.equalsIgnoreCase("light_purple")) {result = ChatColor.LIGHT_PURPLE;}
        else if(color.equalsIgnoreCase("yellow")) {result = ChatColor.YELLOW;}
        else if(color.equalsIgnoreCase("white")) {result = ChatColor.WHITE;}
        
        else if(color.equalsIgnoreCase("0")) {result = ChatColor.BLACK;} 
        else if(color.equalsIgnoreCase("1")) {result = ChatColor.DARK_BLUE;}
        else if(color.equalsIgnoreCase("2")) {result = ChatColor.DARK_GREEN;}
        else if(color.equalsIgnoreCase("3")) {result = ChatColor.DARK_AQUA;}
        else if(color.equalsIgnoreCase("4")) {result = ChatColor.DARK_RED;}
        else if(color.equalsIgnoreCase("5")) {result = ChatColor.DARK_PURPLE;}
        else if(color.equalsIgnoreCase("6")) {result = ChatColor.GOLD;}
        else if(color.equalsIgnoreCase("7")) {result = ChatColor.GRAY;}
        else if(color.equalsIgnoreCase("8")) {result = ChatColor.DARK_GRAY;}
        else if(color.equalsIgnoreCase("9")) {result = ChatColor.BLUE;}
        else if(color.equalsIgnoreCase("a")) {result = ChatColor.GREEN;}
        else if(color.equalsIgnoreCase("b")) {result = ChatColor.AQUA;}
        else if(color.equalsIgnoreCase("c")) {result = ChatColor.RED;}
        else if(color.equalsIgnoreCase("d")) {result = ChatColor.LIGHT_PURPLE;}
        else if(color.equalsIgnoreCase("e")) {result = ChatColor.YELLOW;}
        else if(color.equalsIgnoreCase("f")) {result = ChatColor.WHITE;}
        return result;
    }
    public static String b(boolean flag) {
        if (flag) return "有効";
        return "無効";
    }
    public static String cnvCOLOR2STR(ChatColor color) {
        String result = "WHITE";
        switch (color) {
            case BLACK: result = "BLACK"; break;
            case DARK_BLUE: result = "DARK_BLUE"; break;
            case DARK_GREEN: result = "DARK_GREEN"; break;
            case DARK_AQUA: result = "DARK_AQUA"; break;
            case DARK_RED: result = "DARK_RED"; break;
            case DARK_PURPLE: result = "DARK_PURPLE"; break;
            case GOLD: result = "GOLD"; break;
            case GRAY: result = "GRAY"; break;
            case DARK_GRAY: result = "DARK_GRAY"; break;
            case BLUE: result = "BLUE"; break;
            case GREEN: result = "GREEN"; break;
            case AQUA: result = "AQUA"; break;
            case RED: result = "RED"; break;
            case LIGHT_PURPLE: result = "LIGHT_PURPLE"; break;
            case YELLOW: result = "YELLOW"; break;
            case WHITE: result = "WHITE"; break;
        }
        return result;
    }
    
}

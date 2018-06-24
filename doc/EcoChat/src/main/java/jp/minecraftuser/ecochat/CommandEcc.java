
package jp.minecraftuser.ecochat;


import jp.minecraftuser.ecochat.type.ChatType;
import jp.minecraftuser.ecochat.type.CmdType;
import jp.minecraftuser.ecochat.db.log.MsgLoggerDB;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
import jp.minecraftuser.ecousermanager.EcoUserManager;
import jp.minecraftuser.ecousermanager.db.EcoUserUUIDStore;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class CommandEcc {
    private static EcoChat plg = null;
    private static HashMap<String, String> lastTimeResponse = null;
    private static HashMap<String, String> timeBeforeResponse = null;
    private String spypm = ""    EcoUserManager uman = null;
//    EcoUserUUIDStore store = null;

    public void setSpyPM(String user) {
        this.spypm = user;
        FileConfiguration cnf = plg.getConfig();
        cnf.addDefault("spypm", user);
        cnf.set("spypm", user);
        cnf.options().copyDefaults(true);
        plg.saveConfig();
    }
    public boolean isSpyPM() {
        if (this.spypm.equals("")) return false;
        return true;
    }
    public CommandEcc(EcoChat plg) {
        this.plg = plg;
        this.lastTimeResponse = new HashMap<>();
        this.timeBeforeResponse = new HashMap<>();
        if (plg.getConfig().getString("spypm") != null) setSpyPM(plg.getConfig().getString("spypm"));
        this.uman = plg.getUman();
//        this.store = uman.getStore();
    }

    private String getConsole(Player player) {
        if (player == null) {
            if (plg.getConsoleUser().equals("")) {
                m.info("コマンド実行に必要なコンソールユーザーが設定されていません");
                return null;
            } else {
                return plg.getConsoleUser();
            }
        } else {
            return player.getName();
        }
    }
    private void sendMessage(Player player, String msg) {
        String name = getConsole(player);
        if (name == null) {
            m.info("[CONSOLE_USER]"+msg);
        } else {
            if (player == null) {
                m.info("["+name+"]"+msg);
            } else {
                player.sendMessage(msg);
            }
        }
    }
    public void checkPermissions(Player player, String channel, boolean op, boolean owner) throws Exception {
        // チャンネルマネージャー呼び出し
        LoaderChannel chlist = plg.getChannelList();
        if (player == null) return;
        if (owner) {
            try {
                if (chlist.isChannelOwner(channel, player.getName())) return;
            } catch (Exception ex) {} // エラーは捨てる
        }
        if (op) {
            if (player.isOp()) return;
        }
        if (op && !owner) {
            throw new Exception("このコマンドは管理者だけ使用可能です");
        } else if (!op && owner) {
            throw new Exception("このコマンドはチャンネル作成者だけ使用可能です");
        } else if (op && owner) {
            throw new Exception("このコマンドは管理者またはチャンネル作成者だけ使用可能です");
        }
        return;
    }
    public void notify(String name, String msg) {
        if (plg.getServer().getOfflinePlayer(name).isOnline()) {
            plg.getServer().getPlayer(name).sendMessage(m.plg(msg));
        } else {
            m.info(m.plg("["+name+"] "+msg));
        }
    }
    public void notify(Player player, String msg) {
        if (player == null) {
            m.info(m.plg("[console+] "+msg));
        } else {
            player.sendMessage(m.plg(msg));
        }
    }
    public void sm(Player player, String msg) {
        if (player == null) {
            m.info(m.plg("[console+] "+msg));
        } else {
            player.sendMessage(msg);
        }
    }
    private String joinMsg(String pname, String tag, LoaderChannel chlist) throws Exception {
            return ChatColor.YELLOW+"["+chlist.getTag(tag)+"] "+
                    m.repInfo(chlist.getJoinMsg(tag), pname, chlist.getName(tag));
    }
    private String leaveMsg(String pname, String tag, LoaderChannel chlist) throws Exception {
            return ChatColor.YELLOW+"["+chlist.getTag(tag)+"] "+
                    m.repInfo(chlist.getLeaveMsg(tag), pname, chlist.getName(tag));
    }
    public boolean exec(Player player, String cmd, String[] args, int paracnt) {
        // チャンネルマネージャー呼び出し
        LoaderChannel chlist = plg.getChannelList();
        
        // プレイヤー名取得
        String pname = getConsole(player);

        // 実行部
        int count = 0;
        switch (CmdType.toCmd(cmd)) {
            case JOIN:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                    // コマンドより後の文字列は全て個別のチャンネル名として判定する
                    count = -1;
                    for (String chname: args) {
                        count++;
                        if (paracnt > count) {
                            continue;
                        }
                        try {
                            chlist.joinChannel(chname, pname, null, false, ChatType.global, null, null, false, false, false);
                            // チャンネル加入成功メッセージ、当該チャンネル内の通知ユーザーも走査する
                            chlist.sendInformation(chname, pname, null, pname+" がチャンネル["+chlist.getName(chname) +"]に参加しました");
                            if (player == null) m.info(joinMsg(pname, chname, chlist));
                            else player.sendMessage(joinMsg(pname, chname, chlist));
                        } catch (Exception ex) {
                            notify(pname, ex.getMessage());
                        }
                    }
                }
                break;
            case PASSJOIN:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                    // コマンドの次の文字列をチャンネル名、以降をパスワードとして判定する
                    count = -1;
                    String channel = args[paracnt];
                    StringBuilder password = new StringBuilder();
                    for (String pass: args) {
                        count++;
                        if (paracnt + 1 > count) {
                            continue;
                        }
                        if (paracnt + 1 == count) {
                            password.append(pass);
                        } else {
                            password.append(" " + pass);
                        }
                    }
                    try {
                        chlist.joinChannel(channel, pname, password.toString(), false, ChatType.global, null, null, false, false, false);
                        // チャンネル加入成功メッセージ、当該チャンネル内の通知ユーザーも走査する
                        chlist.sendInformation(channel, pname, null, pname+" がチャンネル["+channel+"]に参加しました");
                            if (player == null) m.info(joinMsg(pname, channel, chlist));
                            else player.sendMessage(joinMsg(pname, channel, chlist));
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case LEAVE:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // leaveの後に指定が無い場合はアクティブチャンネルを離脱
                    if (args.length - paracnt == 0) {
                        try {
                            String channel = chlist.getActive(pname);
                            // チャンネル離脱メッセージ
                            if (!chlist.isChannelUser(channel, pname)) throw new Exception("["+channel+"]チャンネルに所属していません");
                            chlist.sendInformation(channel, pname, null, pname+" がチャンネル["+chlist.getName(channel) +"]から離脱しました");
                            if (player == null) m.info(leaveMsg(pname, channel, chlist));
                            else player.sendMessage(leaveMsg(pname, channel, chlist));
                            if (player != null) chlist.delChannelUser(channel, pname);
                            if (!chlist.existChannel(channel)) notify(player, "チャンネルが削除されました");
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    }
                    // leaveの直後のコマンドがALLの場合には全て離脱
                    else if ((args.length - paracnt == 1) &&
                        (args[paracnt].equalsIgnoreCase("all"))) {
                        try {
                            ArrayList<String> chs = chlist.getUserChannels(pname);
                            if (chs.isEmpty()) throw new Exception("チャンネルに所属していません");
                            for (String chname: chs.toArray(new String[0])) {
                                if (!chlist.isChannelUser(chname, pname)) throw new Exception("["+chname+"]チャンネルに所属していません");
                                // チャンネル離脱メッセージ
                                chlist.sendInformation(chname, pname, null, pname+" がチャンネル["+chlist.getName(chname) +"]から離脱しました");
                                sm(player, leaveMsg(pname, chname, chlist));
                                chlist.delChannelUser(chname, pname);
                                if (!chlist.existChannel(chname)) notify(player, "チャンネルが削除されました");
                            }
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    }
                    // leaveの直後の指定がALL以外の場合、続くチャンネル指定を全てLEAVE
                    else {
                        // コマンドの次以降をチャンネル名として判定し全てleaveする
                        count = -1;
                        for (String chname: args) {
                            count++;
                            if (paracnt > count) {
                                continue;
                            }
                            try {
                                // チャンネル離脱メッセージ
                                if (!chlist.isChannelUser(chname, pname)) throw new Exception("["+chname+"]チャンネルに所属していません");
                                chlist.sendInformation(chname, pname, null, pname+" がチャンネル["+chlist.getName(chname) +"]から離脱しました");
                                sm(player, leaveMsg(pname, chname, chlist));
                                chlist.delChannelUser(chname, pname);
                                if (!chlist.existChannel(chname)) notify(player, "チャンネルが削除されました");
                            } catch (Exception ex) {
                                notify(player,ex.getMessage());
                            }
                        }
                    }
                }
                break;
            case DELETE:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt != 0) {notify(player,"パラメータが多すぎます"); return false;}
                    // 第一パラメータ分岐
                    try {
                        String act = chlist.getActive(pname);
                        // 作成者、管理者チェック
                        checkPermissions(player, act, true, true);

                        // チャンネル削除メッセージ
                        chlist.sendInformation(act, pname, null, pname +" によりチャンネル["+act+":"+chlist.getName(act) +"]が削除されました");
                        chlist.delChannel(act);
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case SET:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // set 直後のパラメタをチャンネル名としてアクティブチャンネルに設定する
                    if (args.length - paracnt == 1) {
                        try {
                            if (!chlist.isChannelUser(args[paracnt], pname)) throw new Exception("指定したチャンネルに参加していません");
                            chlist.setActive(pname, args[paracnt]);
                            notify(player,"発言先チャンネルを["+chlist.getName(args[paracnt])+"]に設定しました");
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    } else {
                        // パラメタ多すぎ
                        notify(player,"パラメータが多すぎます"); 
                        return false;
                    }
                }
                break;
            case ADD:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                    try {
                        // アクティブチャンネルを取得
                        String channel = chlist.getActive(pname);
                        if (channel == null) {
                            notify(player,"発言先チャンネルが設定されていません");
                            return true;
                        }
                        // 実行権限を確認
                        if (!chlist.isChannelAddPerm(channel) && (player != null) && (!player.isOp())) {
                            if (!chlist.isChannelOwner(channel, pname)) throw new Exception(chlist.getName("channel")+"チャンネルへの他ユーザー追加権限がありません");
                        }
                        // コマンドの次の文字列以降をユーザー名として判定する
                        count = -1;
                        for (String username: args) {
                            count++;
                            if (paracnt > count) {
                                continue;
                            }
                            // 指定ユーザーをチャンネル追加
                            try {
//                                OfflinePlayer pl = plg.getServer().getOfflinePlayer(username);
//                                if (!pl.isOnline()) throw new Exception("名前の指定が誤っているか、対象のプレイヤーがオフラインのため追加できませんでした");
                                // オンラインプレイヤーなら追加する
                                if (chlist.isPasswordChannel(channel)) {
                                    chlist.joinChannel(channel, username, chlist.getChannelPassword(channel), false, null, null, null, false, false, false);
                                } else {
                                    chlist.joinChannel(channel, username, null, false, null, null, null, false, false, false);
                                }
                                sm(player, joinMsg(username, channel, chlist));
                                chlist.sendInformation(channel, pname, username, username+" が "+ pname +" によりチャンネル["+chlist.getName(channel) +"]に追加されました");
                                // チャンネル加入メッセージ
                            } catch (Exception ex) {
                                notify(player,ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case DICE:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    try {
                        // アクティブチャンネルを取得
                        String channel = chlist.getActive(pname);
                        if (channel == null) {
                            notify(player,"発言先チャンネルが設定されていません");
                            return true;
                        }

                        // パラメタチェック
                        Random r = new Random();
                        if (args.length - paracnt < 1) {
                            // ﾊﾟﾗﾒﾀなし
                            chlist.sendInformation(channel, pname, null, pname+" が 6 面サイコロを転がしました。コロコロ...["+(r.nextInt(6)+1)+"]");
                        } else {
                            // ﾊﾟﾗﾒﾀはサイコロの母数
                            int i = Integer.parseInt(args[paracnt]);
                            if (i < 1) throw new Exception("サイコロの目の指定が小さすぎます");
                            chlist.sendInformation(channel, pname, null, pname+" が "+ i +" 面サイコロを転がしました。コロコロ...["+(r.nextInt(i)+1)+"]");
                        }
                    } catch (Exception ex) {
                        notify(player,"サイコロ転がし失敗"+ex.getMessage());
                    }
                }
                break;
            case CONF:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                    // 第一パラメータ分岐
                    if (args[paracnt].equalsIgnoreCase("channel")) {
                        // パラメータチェック
                        if (args.length - paracnt < 3) {notify(player,"パラメータが不足しています"); return false;}
                        String channel = args[paracnt+1];
                        try {
                            if (!chlist.isExistUserChannelConf(pname, channel)) {
                                chlist.addUserChannelConf(pname, channel);
                            }
                            ChatConf cnf = chlist.getUserChannelConf(pname, channel);
                            // ユーザーチャンネル設定取得
                            if (args[paracnt+2].equalsIgnoreCase("bold")) {
                                if (cnf.isBold()) {cnf.setBold(false); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の太字指定を無効化しました");}
                                else {cnf.setBold(true); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の太字指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("italic")) {
                                if (cnf.isItalic()) {cnf.setItalic(false); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の斜体指定を無効化しました");}
                                else {cnf.setItalic(true); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の斜体指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("line")) {
                                if (cnf.isLine()) {cnf.setLine(false); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の下線指定を無効化しました");}
                                else {cnf.setLine(true); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の下線指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("strike")) {
                                if (cnf.isStrike()) {cnf.setStrike(false); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の抹消線指定を無効化しました");}
                                else {cnf.setStrike(true); chlist.setUserChannelConf(pname, channel, cnf); notify(player,"個人設定：チャンネル["+channel+"]の抹消線指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("color")) {
                                // パラメータチェック
                                if (args.length - paracnt < 4) {notify(player,"パラメータが不足しています"); return false;}
                                cnf.setColor(args[paracnt+3]); chlist.setUserChannelConf(pname, channel, cnf);
                                // 色設定有効
                                notify(player,"個人設定：チャンネル["+chlist.getName(channel)+"]に色["+cnf.getColor()+"]を設定しました");
                            } else {
                                // パラメタエラー
                                notify(player,"不明な設定値が指定されました");
                            }
                        } catch (Exception ex) {
                            // 失敗;
                            notify(player,ex.getMessage());
                        }
                    } else if (args[paracnt].equalsIgnoreCase("player")) {
                        // パラメータチェック
                        if (args.length - paracnt < 3) {notify(player,"パラメータが不足しています"); return false;}
                        try {
                            if (!chlist.isExistUserUserConf(pname, args[paracnt+1])) {
                                chlist.addUserUserConf(pname, args[paracnt+1]);
                            }
                            ChatConf cnf = chlist.getUserUserConf(pname, args[paracnt+1]);

                            // ユーザー設定ファイルの有無を確認
                            if (args[paracnt+2].equalsIgnoreCase("bold")) {
                                if (cnf.isBold()) {cnf.setBold(false); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の太字指定を無効化しました");}
                                else {cnf.setBold(true); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の太字指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("italic")) {
                                if (cnf.isItalic()) {cnf.setItalic(false); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の斜体指定を無効化しました");}
                                else {cnf.setItalic(true); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の斜体指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("line")) {
                                if (cnf.isLine()) {cnf.setLine(false); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の下線指定を無効化しました");}
                                else {cnf.setLine(true); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の下線指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("strike")) {
                                if (cnf.isStrike()) {cnf.setStrike(false); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の抹消線指定を無効化しました");}
                                else {cnf.setStrike(true); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]の抹消線指定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("color")) {
                                // パラメータチェック
                                if (args.length - paracnt < 4) {notify(player,"パラメータが不足しています"); return false;}
                                cnf.setColor(args[paracnt+3]); chlist.setUserUserConf(pname, args[paracnt+1], cnf);
                                // 色指定成功メッセージ
                                notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]に色["+cnf.getColor()+"]を設定しました");
                            } else if (args[paracnt+2].equalsIgnoreCase("ng")) {
                                if (cnf.isNg()) {cnf.setNg(false); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]のNGユーザー設定を無効化しました");}
                                else {cnf.setNg(true); chlist.setUserUserConf(pname, args[paracnt+1], cnf); notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]のNGユーザー設定を有効化しました");}
                            } else if (args[paracnt+2].equalsIgnoreCase("listng")) {
                                notify(player,"個人設定：プレイヤー["+args[paracnt+1]+"]のNGユーザーリストを表示します");
                                ArrayList<String> list = null;
                                if (args.length - paracnt == 3) {
                                    list = chlist.getUserNGList(args[paracnt+1], 1, 100);
                                } else if (args.length - paracnt == 4) {
                                    list = chlist.getUserNGList(args[paracnt+1], Integer.parseInt(args[paracnt+3]), 100);
                                } else if (args.length - paracnt == 5) {
                                    list = chlist.getUserNGList(args[paracnt+1], Integer.parseInt(args[paracnt+3]), Integer.parseInt(args[paracnt+4]));
                                } else {
                                    throw new Exception("指定パラメータが多すぎます");
                                }
                                for (String s: list) {
                                    sm(player, s);
                                }
                            } else {
                                // パラメタエラー
                                notify(player,"不明な設定値が指定されました");
                            }
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    } else if (args[paracnt].equalsIgnoreCase("range")) {
                        // パラメータチェック
                        if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                        if (args[paracnt+1].equalsIgnoreCase("local")) {
                            if (args.length - paracnt < 3) {notify(player,"パラメータが不足しています"); return false;}
                            try {
                                UserConfig cnf = chlist.getUserConfig(pname);
                                cnf.setLocal(Integer.parseInt(args[paracnt+2]));
                                chlist.setUserConfig(pname, cnf);
                                notify(player,"個人設定：近距離用(ローカル)チャンネルの受信範囲を"+Integer.parseInt(args[paracnt+2])+"に設定しました");
                            } catch (Exception ex) {
                                // エラー
                                notify(player,ex.getMessage());
                            }
                        } else {
                            // パラメタエラー
                            notify(player,"不明な設定値が指定されました");
                        }
                    } else if (args[paracnt].equalsIgnoreCase("flag")) {
                        // パラメータチェック
                        if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                        try {
                            UserConfig cnf = chlist.getUserConfig(pname);
                            if (args[paracnt+1].equalsIgnoreCase("info")) {
                                if (cnf.isInfo()) {
                                    cnf.setInfo(false); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：他プレイヤーのチャンネル入退出表示を無効にしました");
                                } else {
                                    cnf.setInfo(true); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：他プレイヤーのチャンネル入退出表示を有効にしました");
                                }
                            } else if (args[paracnt+1].equalsIgnoreCase("nguser")) {
                                if (cnf.isNgView()) {
                                    cnf.setNgView(false); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：NGプレイヤーの発言の表示を無効にしました");
                                } else {
                                    cnf.setNgView(true); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：NGプレイヤーの発言の表示を有効にしました");
                                }
                            } else if (args[paracnt+1].equalsIgnoreCase("range")) {
                                if (cnf.isRange()) {
                                    cnf.setRange(false); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：近距離用(ローカル)チャンネル発言時の伝達人数表示を無効にしました");
                                } else {
                                    cnf.setRange(true); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：近距離用(ローカル)チャンネル発言時の伝達人数表示を有効にしました");
                                }
                            } else if (args[paracnt+1].equalsIgnoreCase("rs")) {
                                if (cnf.isRsWarn()) {
                                    cnf.setRsWarn(false); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：RSコマンドの誤爆防止チェックを無効にしました");
                                } else {
                                    cnf.setRsWarn(true); chlist.setUserConfig(pname, cnf);
                                    notify(player,"個人設定：RSコマンドの誤爆防止チェックを有効にしました");
                                }
                            } else {
                                // パラメタエラー
                                notify(player,"不明な設定値が指定されました");
                            }
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    } else if (args[paracnt].equalsIgnoreCase("mute")) {
                        // パラメータチェック
                        if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                        try {
                            // 管理者チェック
                            checkPermissions(player, null, true, false);
                            // パラメタ取得、MUTE設定
                            UserConfig cnf = chlist.getUserConfig(args[paracnt+1]);
                            if (cnf.isMute()) {
                                cnf.setMute(false); chlist.setUserConfig(pname, cnf);
                                notify(player,"管理者設定：ユーザー["+args[paracnt+1]+"]の発言をMUTE解除しました");
                            } else {
                                cnf.setMute(true); chlist.setUserConfig(pname, cnf);
                                notify(player,"管理者設定：ユーザー["+args[paracnt+1]+"]の発言をMUTE状態にしました");
                            }
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    } else {
                        // パラメタエラー
                        notify(player,"不明な設定値が指定されました");
                    }
                }
                break;
            case CHANNEL:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                    // 第一パラメータ分岐
                    try {
                        String ch = chlist.getActive(pname);
                        ChatConf cnf = chlist.getChannelConf(ch);
                        // 作成者、管理者チェック
                        checkPermissions(player, chlist.getActive(pname), true, true);
                        // オプション別
                        if (args[paracnt].equalsIgnoreCase("bold")) {
                            if (cnf.isBold()) {cnf.setBold(false); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の太字指定を無効化しました");}
                            else {cnf.setBold(true); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の太字指定を有効化しました");}
                        } else if (args[paracnt].equalsIgnoreCase("italic")) {
                            if (cnf.isItalic()) {cnf.setItalic(false); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の斜体指定を無効化しました");}
                            else {cnf.setItalic(true); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の斜体指定を有効化しました");}
                        } else if (args[paracnt].equalsIgnoreCase("line")) {
                            if (cnf.isLine()) {cnf.setLine(false); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の下線指定を無効化しました");}
                            else {cnf.setLine(true); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の下線指定を有効化しました");}
                        } else if (args[paracnt].equalsIgnoreCase("strike")) {
                            if (cnf.isStrike()) {cnf.setStrike(false); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の抹消線指定を無効化しました");}
                            else {cnf.setStrike(true); chlist.setChannelConf(ch, cnf); notify(player,"チャンネル設定：チャンネル["+ch+"]の抹消線指定を有効化しました");}
                        } else if (args[paracnt].equalsIgnoreCase("color")) {
                            // パラメータチェック
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            cnf.setColor(args[paracnt+1]); chlist.setChannelConf(ch, cnf);
                            // 色指定完了メッセージ
                            notify(player,"チャンネル設定：チャンネル["+ch+"]に色["+cnf.getColor()+"]を設定しました");
                        } else if (args[paracnt].equalsIgnoreCase("name")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            chlist.setName(ch, args[paracnt+1]);
                            notify(player,"チャンネル設定：チャンネル["+ch+"]の名称を["+chlist.getName(ch)+"]に変更しました");
                        } else if (args[paracnt].equalsIgnoreCase("type")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            //String buf = cnf.getType().toString();
                            chlist.setChannelType(ch, args[paracnt+1]);
                            notify(player,"チャンネル設定：チャンネル["+ch+"]のチャットタイプを["+args[paracnt+1]+"]に変更しました");
                        } else if (args[paracnt].equalsIgnoreCase("def")) {
                            // 作成者は禁止
                            checkPermissions(player, null, true, false);
                            if (chlist.isChannelAutoJoin(ch)) {
                                chlist.setChannelAutoJoin(ch, false);
                                notify(player,"管理者設定：チャンネル["+ch+"]のデフォルト加入設定を無効化しました");
                            } else {
                                chlist.setChannelAutoJoin(ch, true);
                                notify(player,"管理者設定：チャンネル["+ch+"]のデフォルト加入設定を有効化しました");
                            }
                        } else if (args[paracnt].equalsIgnoreCase("list")) {
                            if (chlist.isChannelListed(ch)) {
                                chlist.setChannelListed(ch, false);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のチャンネル一覧掲載を無効化しました");
                            } else {
                                chlist.setChannelListed(ch, true);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のチャンネル一覧掲載を有効化しました");
                            }
                        } else if (args[paracnt].equalsIgnoreCase("passenable")) {
                            if (chlist.isPasswordChannel(ch)) {
                                chlist.delChannelPass(ch);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のパスワード設定を無効化しました");
                            } else {
                                if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                                chlist.setChannelPass(ch, args[paracnt+1]);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のパスワードを設定しました");
                            }
                        } else if (args[paracnt].equalsIgnoreCase("perm")) {
                            if (chlist.isChannelAddPerm(ch)) {
                                chlist.setChannelAddPerm(ch, false);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のADD一般ユーザー実行権限を無効化しました");
                            } else {
                                chlist.setChannelAddPerm(ch, true);
                                notify(player,"チャンネル設定：チャンネル["+ch+"]のADD一般ユーザー実行権限を有効化しました");
                            }
                        } else if (args[paracnt].equalsIgnoreCase("join")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            // コマンドの次の文字列をチャンネル名、以降をパスワードとして判定する
                            count = -1;
                            StringBuilder msg = new StringBuilder();
                            for (String part: args) {
                                count++;
                                if (paracnt + 1 > count) {
                                    continue;
                                }
                                if (paracnt + 1 == count) {
                                    msg.append(part);
                                } else {
                                    msg.append(" " + part);
                                }
                            }
                            chlist.setJoinMsg(ch, msg.toString());
                            notify(player,"チャンネル設定：チャンネル["+ch+"]の加入時表示メッセージを設定しました");
                        } else if (args[paracnt].equalsIgnoreCase("leave")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            // コマンドの次の文字列をチャンネル名、以降をパスワードとして判定する
                            count = -1;
                            StringBuilder msg = new StringBuilder();
                            for (String part: args) {
                                count++;
                                if (paracnt + 1 > count) {
                                    continue;
                                }
                                if (paracnt + 1 == count) {
                                    msg.append(part);
                                } else {
                                    msg.append(" " + part);
                                }
                            }
                            chlist.setLeaveMsg(ch, msg.toString());
                            notify(player,"チャンネル設定：チャンネル["+ch+"]の離脱時表示メッセージを設定しました");
                        } else if (args[paracnt].equalsIgnoreCase("owner")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return true;}
                            EcoUserUUIDStore store = plg.getUman().getStore();
                            UUID uid = store.latestUUID(args[paracnt+1]);
                            if (uid == null) {
                                notify(player,"指定ユーザーのUUID情報が見つかりませんでした");
                                return true;
                            }
                            m.info("4:"+ch+"/"+args[paracnt+1]);
                            if (chlist.isChannelOwner(ch, args[paracnt+1])) {
                                m.info("7");
                                chlist.delChannelOwner(ch, args[paracnt+1]);
                                m.info("8");
                                notify(player,"チャンネル設定：チャンネル["+ch+"]の作成者グループからプレイヤー["+args[paracnt+1]+"]を削除しました");
                            } else {
                                m.info("5");
                                chlist.addChannelOwner(ch, args[paracnt+1]);
                                m.info("6");
                                if (chlist.isChannelOwner(ch, args[paracnt+1])) {
                                    notify(player,"チャンネル設定：チャンネル["+ch+"]の作成者グループにプレイヤー["+args[paracnt+1]+"]を追加しました");
                                } else {
                                    notify(player,"チャンネル設定：チャンネル["+ch+"]の作成者グループにプレイヤー["+args[paracnt+1]+"]が追加できませんでした");
                                }
                            }
                        } else if (args[paracnt].equalsIgnoreCase("tag")) {
                            if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                            // 作成可能かチェック
                            if (chlist.existChannel(args[paracnt+1])) {
                                if (!chlist.getTag(args[paracnt+1]).equalsIgnoreCase(ch)) {
                                    throw new Exception("指定したチャンネルは既に存在します");
                                }
                            }
                            chlist.checkNgCreateName(args[paracnt+1]);
                            chlist.checkNgJoinName(args[paracnt+1]);
                            
                            // 変更
                            chlist.changeChannelTag(ch, args[paracnt+1]);
                            chlist.sendInformation(args[paracnt+1], pname, null, pname +" によりチャンネル["+ch+":"+chlist.getName(args[paracnt+1]) +"]の識別タグが["+args[paracnt+1]+"]に変更されました");
                        } else {
                            // パラメタエラー
                            notify(player,"不明な設定値が指定されました");
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                        m.info(ex.getLocalizedMessage());
                        m.info(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                break;
            case KICK:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                    try {
                        // アクティブチャンネルを取得
                        String channel = chlist.getActive(pname);
                        // OP権限持ち、または指定チャンネルの作成者グループに所属しているか確認する
                        checkPermissions(player, channel, true, true);

                        // コマンドの次の文字列以降をユーザー名として判定する
                        count = -1;
                        for (String username: args) {
                            count++;
                            if (paracnt > count) {
                                continue;
                            }
                            // 指定ユーザーをアクティブチャンネルからKICKする
                            try {
                                // キックメッセージ
                                chlist.sendInformation(channel, pname, username, username+" が "+ pname +" によりチャンネル["+chlist.getName(channel) +"]から強制退出させられました");
                                chlist.delChannelUser(channel, username);
                                if (!chlist.existChannel(channel)) notify(player, "チャンネルが削除されました");
                            } catch (Exception ex) {
                                notify(player,ex.getMessage());
                            }
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case LIST:                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    try {
                        // パラメタチェック
                        if (args.length - paracnt > 2) {notify(player,"パラメータが多すぎます"); return false;}
                        // チャンネルリストを取得
                        ArrayList<String> list = null;
                        if (args.length - paracnt == 1) {
                            list = chlist.getChannelList(pname, Integer.parseInt(args[paracnt]),100);
                        } else if (args.length - paracnt == 2) {
                            list = chlist.getChannelList(pname, Integer.parseInt(args[paracnt]),Integer.parseInt(args[paracnt+1]));
                        } else {
                            list = chlist.getChannelList(pname, 1,100);
                        }
                        for (String s : list) {
                            sm(player, s);
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case WHO:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    try {
                        // whoの後に指定が無い場合はアクティブチャンネルのユーザーをリスト表示
                        String channel = "";
                        int page = 1;
                        int cnt = 100;
                        if (args.length - paracnt == 0) {
                            // アクティブチャンネルを取得
                            channel = chlist.getActive(pname);
                            if (!chlist.isChannelUser(channel, pname)) throw new Exception("指定したチャンネルに所属していません");
                        }
                        // whoの直後の指定がひとつならチャンネル名と判断し指定チャンネルユーザーをリスト表示
                        else if (args.length - paracnt == 1) {
                            channel = args[paracnt];
                        }
                        // さらにパラメタ＋１でページ指定
                        else if (args.length - paracnt == 2) {
                            channel = args[paracnt];
                            page = Integer.parseInt(args[paracnt+1]);
                        }
                        else if (args.length - paracnt == 3) {
                            channel = args[paracnt];
                            page = Integer.parseInt(args[paracnt+1]);
                            cnt = Integer.parseInt(args[paracnt+2]);
                        }
                        // who以降に2個以上パラメタが付いてたらエラー
                        else {
                            notify(player,"パラメータが多すぎます"); 
                            return true;
                        }
                        if (!chlist.existChannel(channel)) throw new Exception("指定したチャンネルが見つかりませんでした");
                        for (String s : chlist.getChannelUsers(channel, page, cnt)) {
                            sm(player, s);
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case INFO:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // whoの後に指定が無い場合はアクティブチャンネルのユーザーをリスト表示
                    String channel = "";
                    if (args.length - paracnt == 0) {
                        try {
                            // アクティブチャンネルを取得
                            channel = chlist.getActive(pname);
                            player.sendMessage(chlist.getChannelInfo(channel).toArray(new String[0]));
                        } catch (Exception ex) {
                            notify(player,ex.getMessage());
                        }
                    }
                }
                break;
            case PM:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                    // パラメタの一つ目はユーザー名
                    String user = args[paracnt];
                    // 相手ユーザーの存在チェック
                    if ((!plg.getServer().getOfflinePlayer(user).isOnline()) &&
                        (!plg.getConsoleUser().equals(user))) {
                        notify(player,"発言先のプレイヤーが見つかりませんでした");
                        return false;
                    }

                    // 指定ユーザーに発言送出
                    StringBuilder sb = new StringBuilder();
                    count = -1;
                    for (String msg: args) {
                        count++;
                        if (paracnt + 1 > count) {
                            continue;
                        }
                        if (paracnt + 1 < count) sb.append(" ");
                        sb.append(args[count]);
                    }
                    String sender_msg = ""+ChatColor.GOLD+ChatColor.UNDERLINE+pname+" -> "+user+ChatColor.RESET+": "+m.repColor(sb.toString());
                    String reciever_msg = ""+ChatColor.YELLOW+ChatColor.UNDERLINE+pname+" -> "+user+ChatColor.RESET+": "+m.repColor(sb.toString());
                    try {
                        plg.getLoggerDB().pmLogging(pname, user, m.repColor(sb.toString()));
                        if (plg.getServer().getOfflinePlayer(user).isOnline()) {
                            // 相手がオンラインプレイヤー
                            plg.getServer().getPlayer(user).sendMessage(reciever_msg);
                            // 自分がオンラインプレイヤー
                            if (player != null) player.sendMessage(sender_msg);
                            // 自分がコンソールユーザー
                            else m.info(sender_msg);
                        } else {
                            // 相手がコンソールユーザー
                            m.info(reciever_msg);
                            // 自分がオンラインユーザー
                            if (player != null) player.sendMessage(sender_msg);
                        }
                        // 受信履歴の更新
                        if (this.timeBeforeResponse.containsKey(user.toLowerCase())) {
                            this.timeBeforeResponse.remove(user.toLowerCase());
                        }
                        if (this.lastTimeResponse.containsKey(user.toLowerCase())) {
                            this.timeBeforeResponse.put(user.toLowerCase(), this.lastTimeResponse.get(user.toLowerCase()));
                            this.lastTimeResponse.remove(user.toLowerCase());
                        }
                        this.lastTimeResponse.put(user.toLowerCase(), pname.toLowerCase());
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                    // スパイメッセージ
                    if (!this.spypm.equals("")) {
                        if (plg.getServer().getOfflinePlayer(this.spypm).isOnline()) {
                            if ((!this.spypm.equals(pname)) &&
                                (!this.spypm.equals(user))) {
                                plg.getServer().getPlayer(this.spypm).sendMessage(ChatColor.GREEN+"[SpyPM] "+ChatColor.RESET+sender_msg);
                            }
                        }
                    }
                }
                break;
            case RS:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    // パラメタチェック
                    if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                    String sender_msg = null;
                    try {
                        // 返信ユーザーチェック
                        
                        // 前回受信ユーザーが居ない場合
                        if (!this.lastTimeResponse.containsKey(pname.toLowerCase())) throw new Exception("返信先ユーザーが見つかりませんでした");
                        
                        // 前回受信ユーザーがオフラインの場合
                        if ((!plg.getServer().getOfflinePlayer(this.lastTimeResponse.get(pname.toLowerCase())).isOnline()) &&
                            (!plg.getConsoleUser().equals(this.lastTimeResponse.get(pname.toLowerCase())))) {
                            notify(player,"返信先ユーザーが見つかりませんでした");
                            return false;
                        }
                        
                        // 前回受信ユーザー取得
                        String user = this.lastTimeResponse.get(pname.toLowerCase());

                        // 前回ユーザーと前々回ユーザーが異なる場合
                        UserConfig cnf = chlist.getUserConfig(pname);
                        if (cnf.isRsWarn()) {
                            if (this.timeBeforeResponse.containsKey(pname.toLowerCase())) {
                                if (!user.equals(this.timeBeforeResponse.get(pname.toLowerCase()))) {
                                    this.timeBeforeResponse.remove(pname.toLowerCase());
                                    throw new Exception("前回受信と前々回受信ユーザーが異なります。<"+user+">に返信する場合にはコマンド履歴から再度操作を実行して下さい");
                                }
                            }
                        }

                        // 指定ユーザーに発言送出
                        StringBuilder sb = new StringBuilder();
                        count = -1;
                        for (String ms: args) {
                            count++;
                            if (paracnt > count) {
                                continue;
                            }
                            if (paracnt < count) sb.append(" ");
                            sb.append(args[count]);
                        }
                        sender_msg = ""+ChatColor.GOLD+ChatColor.UNDERLINE+pname+" -> "+user+ChatColor.RESET+": "+m.repColor(sb.toString());
                        String reciever_msg = ""+ChatColor.YELLOW+ChatColor.UNDERLINE+pname+" -> "+user+ChatColor.RESET+": "+m.repColor(sb.toString());
                        plg.getLoggerDB().pmLogging(pname, user, m.repColor(sb.toString()));
                        if (plg.getServer().getOfflinePlayer(user).isOnline()) {
                            // 相手がオンラインプレイヤー
                            plg.getServer().getPlayer(user).sendMessage(reciever_msg);
                            // 自分がオンラインプレイヤー
                            if (player != null) player.sendMessage(sender_msg);
                            // 自分がコンソールユーザー
                            else m.info(sender_msg);
                        } else if (plg.getConsoleUser().equals(user)) {
                            // 相手がコンソールユーザー
                            m.info(reciever_msg);
                            // 自分がオンラインユーザー
                            if (player != null) player.sendMessage(sender_msg);
                        }
                        // 受信履歴の更新
                        if (this.timeBeforeResponse.containsKey(user.toLowerCase())) {
                            this.timeBeforeResponse.remove(user.toLowerCase());
                        }
                        if (this.lastTimeResponse.containsKey(user.toLowerCase())) {
                            this.timeBeforeResponse.put(user.toLowerCase(), this.lastTimeResponse.get(user.toLowerCase()));
                            this.lastTimeResponse.remove(user.toLowerCase());
                        }
                        this.lastTimeResponse.put(user.toLowerCase(), pname.toLowerCase());
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                    // スパイメッセージ
                    if (!this.spypm.equals("")) {
                        if (sender_msg != null) {
                            if (plg.getServer().getOfflinePlayer(this.spypm).isOnline()) {
                                if ((!this.spypm.equals(pname)) &&
                                    (!this.spypm.equals(this.lastTimeResponse.get(pname.toLowerCase())))) {
                                    plg.getServer().getPlayer(this.spypm).sendMessage(ChatColor.GREEN+"[SpyPM] "+ChatColor.RESET+sender_msg);
                                }
                            }
                        }
                    }
                }
                break;
            case HISTORY:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    MsgLoggerDB db = plg.getLoggerDB();
                    Date start = null;
                    Date end = null;
                    int page = 1;
                    
                    // オプション無しの場合直近24時間
                    try {
                        if (args.length - paracnt == 0) {
                            end = new Date();
                            start = new Date(end.getTime()-1000*60*60*24);
                        }
                        // オプションが一つの場合にはページ番号と判断する
                        // 取得は直近24時間
                        else if (args.length - paracnt == 1) {
                            end = new Date();
                            start = new Date(end.getTime()-1000*60*60*24);
                            // ページ番号指定
                            page = Integer.parseInt(args[paracnt]);
                        }
                        // オプションが二つなら二つ目は日付と判定する
                        // 取得は指定日時から24時間
                        // ページ指定必須
                        else if (args.length - paracnt == 2) {
                            start = CalendarFormatter.toDate(args[paracnt+1]);
                            end = new Date(start.getTime() + 1000*60*60*24);
                            // ページ番号指定
                            page = Integer.parseInt(args[paracnt]);
                        }
                        // 上記以外はエラー
                        else {
                            // エラー
                            notify(player,"パラメータが多すぎます"); 
                        }
                    } catch (Exception ex) {
                        // 変換失敗
                        notify(player,"パラメータの解析に失敗しました:"+ex.getMessage());
                    }
                        
                    try {
                        String act = chlist.getActive(pname);
                        if (!chlist.isChannelUser(act, pname)) throw new Exception("指定したチャンネルに所属していません");
                        ArrayList<String> list = db.msgSearch(act, start, end, page, 10);
                        if (player == null) {
                            m.info(list.toString());
                        } else {
                            for (String msg: list) {
                                player.sendMessage(msg);
                            }
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case PMHISTORY:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    MsgLoggerDB db = plg.getLoggerDB();
                    Date start = null;
                    Date end = null;
                    int page = 1;
                    
                    // オプション無しの場合直近24時間
                    try {
                        if (args.length - paracnt == 0) {
                            end = new Date();
                            start = new Date(end.getTime()-1000*60*60*24);
                        }
                        // オプションが一つの場合にはページ番号と判断する
                        // 取得は直近24時間
                        else if (args.length - paracnt == 1) {
                            end = new Date();
                            start = new Date(end.getTime()-1000*60*60*24);
                            // ページ番号指定
                            page = Integer.parseInt(args[paracnt]);
                        }
                        // オプションが二つなら二つ目は日付と判定する
                        // 取得は指定日時から24時間
                        // ページ指定必須
                        else if (args.length - paracnt == 2) {
                            start = CalendarFormatter.toDate(args[paracnt+1]);
                            end = new Date(start.getTime() + 1000*60*60*24);
                            // ページ番号指定
                            page = Integer.parseInt(args[paracnt]);
                        }
                        // 上記以外はエラー
                        else {
                            // エラー
                            notify(player,"パラメータが多すぎます"); 
                        }
                    } catch (Exception ex) {
                        // 変換失敗
                        notify(player,"パラメータの解析に失敗しました:"+ex.getMessage());
                    }
                        
                    try {
                        ArrayList<String> list = db.pmSearch(pname, start, end, page, 10);
                        if (player == null) {
                            m.info(list.toString());
                        } else {
                            for (String msg: list) {
                                player.sendMessage(msg);
                            }
                        }
                    } catch (Exception ex) {
                        notify(player,ex.getMessage());
                    }
                }
                break;
            case SPYCHAT:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    try {
                        // OP権限持ち、または指定チャンネルの作成者グループに所属しているか確認する
                        checkPermissions(player, null, true, false);
                        if (chlist.isSpyChat()) {
                            chlist.setSpyChat("");
                            notify(pname, "SpyChat無効化");
                        } else {
                            chlist.setSpyChat(pname);
                            notify(pname, "SpyChat有効化");
                        }
                    } catch (Exception ex) {
                        
                    }
                }
                break;
            case SPYPM:
                // コンソールユーザーチェック(不明ユーザー＆コンソールユーザー未設定)
                if (pname != null) {
                    try {
                        // OP権限持ち、または指定チャンネルの作成者グループに所属しているか確認する
                        checkPermissions(player, null, true, false);
                        if (isSpyPM()) {
                            setSpyPM("");
                            notify(pname, "SpyPM無効化");
                        } else {
                            setSpyPM(pname);
                            notify(pname, "SpyPM有効化");
                        }
                    } catch (Exception ex) {
                        
                    }
                }
                break;
            case CONSOLE:
                // パラメタチェック
                if (args.length - paracnt < 1) {notify(player,"パラメータが不足しています"); return false;}
                String consoleUserBuf = args[paracnt];
                if (player == null) {
                    try {
                        plg.setConsoleUser("");
                        if (consoleUserBuf.equals("")) {
                            notify(player,"サーバー設定：コンソールユーザーをクリアしました");
                            m.info("サーバー設定：コンソールユーザーをクリアしました");
                        } else {
                            plg.setConsoleUser(consoleUserBuf);
                            plg.joinServer(plg.getConsoleUser());
                            notify(player,"サーバー設定：コンソールユーザーを["+plg.getConsoleUser()+"]に設定しました");
                            m.info("サーバー設定：コンソールユーザーを["+plg.getConsoleUser()+"]に設定しました");
                        }
                    } catch (Exception ex) {
                        notify("内部エラー: コンソールユーザー参照 "+player,ex.getMessage());
                    }
                }
                break;
                
            case SEND:
                // パラメタチェック
                if (args.length - paracnt < 2) {notify(player,"パラメータが不足しています"); return false;}
                // パラメタの一つ目はチャンネル名
                String channel = args[0];
                // 指定チャンネルに発言送出
                StringBuilder sb = new StringBuilder();
                count = -1;
                for (String msg: args) {
                    count++;
                    if (paracnt + 1 > count) {
                        continue;
                    }
                    if (paracnt + 1 < count ) sb.append(" ");
                    sb.append(args[count]);
                }
                try {
                    chlist.sendMessage(channel, pname, sb.toString());
                } catch (Exception ex) {
                    notify(player,ex.getMessage());
                }
                break;
             default:
                m.Warn("NotSupportCmd:"+cmd);
                notify(player,"不明なコマンドです");
                break;
        }
        return true;
    }
}

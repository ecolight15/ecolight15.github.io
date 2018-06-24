
package jp.minecraftuser.ecochat;

import jp.minecraftuser.ecochat.type.ChatType;
import jp.minecraftuser.ecochat.db.chat.EcoChatDB_;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.UUID;
import jp.minecraftuser.ecousermanager.db.EcoUserUUIDStore;


public class LoaderChannel {
    private static EcoChatDB_ db = null;
    private EcoChat plg = null;
    private String spychat = "";
    private static EcoUserUUIDStore store = null;

    public void setSpyChat(String user) {
        this.spychat = user;
        FileConfiguration cnf = plg.getConfig();
        cnf.set("spychat", user);
        plg.saveConfig();
    }
    public boolean isSpyChat() {
        if (this.spychat.equals("")) return false;
        return true;
    }
    public LoaderChannel(EcoChat plg) {
        this.plg = plg;
        this.db = plg.getDB();
        this.store = plg.getUman().getStore();
        if (plg.getConfig().getString("spychat") != null) setSpyChat(plg.getConfig().getString("spychat"));
        // 起動時のチャンネル読み込みは廃止
    }
    private void addChannel(String channel, String password, ChatType type, String join, String leave, boolean def, boolean listed, boolean addperm) throws Exception {
        // DBへの追加へ変更
        db.addChannel(channel, channel, type, join, leave, def, listed, addperm);

        // パスワード付ならパスワードテーブルに保存
        db.addChannelPass(channel, password);
    }
    public String getJoinMsg(String tag) {
        return db.getChannelJoinMsg(tag);
    }
    public String getLeaveMsg(String tag) {
        return db.getChannelLeaveMsg(tag);
    }
    public void setJoinMsg(String tag, String msg) throws Exception {
        db.updateJoinMsg(tag, msg);
    }
    public void setLeaveMsg(String tag, String msg) throws Exception {
        db.updateLeaveMsg(tag, msg);
    }
    public String getName(String tag) {
        return db.getChannelName(tag);
    }
    public String getTag(String tag) {
        return db.getChannelTag(tag);
    }
    public boolean isChannelOwner(String tag, String name) throws Exception {
        return db.isChannelOwner(tag, name);
    }
    public boolean isExistUserData(String user) {
        return db.isExistUserData(user);
    }
    public void addUserData(String user, String active) throws Exception {
        db.addUserData(user, active);
    }

    public void checkNgJoinName(String channel) throws Exception {
        boolean reject = false;
        // 予約語のチャンネルは作成禁止
        if (channel.equalsIgnoreCase("join")) reject = true;
        if (channel.equalsIgnoreCase("passjoin")) reject = true;
        if (channel.equalsIgnoreCase("leave")) reject = true;
        if (channel.equalsIgnoreCase("delete ")) reject = true;
        if (channel.equalsIgnoreCase("set")) reject = true;
        if (channel.equalsIgnoreCase("add")) reject = true;
        if (channel.equalsIgnoreCase("conf")) reject = true;
        if (channel.equalsIgnoreCase("channel")) reject = true;
        if (channel.equalsIgnoreCase("kick")) reject = true;
        if (channel.equalsIgnoreCase("list")) reject = true;
        if (channel.equalsIgnoreCase("who")) reject = true;
        if (channel.equalsIgnoreCase("pm")) reject = true;
        if (channel.equalsIgnoreCase("rs")) reject = true;
        if (channel.equalsIgnoreCase("history")) reject = true;
        if (channel.equalsIgnoreCase("pmhistory")) reject = true;
        if (channel.equalsIgnoreCase("console")) reject = true;
        if (channel.equalsIgnoreCase("reload")) reject = true;
        if (channel.equalsIgnoreCase("all")) reject = true;
        if (channel.equalsIgnoreCase("cc")) reject = true;
        if (channel.equalsIgnoreCase("ecc")) reject = true;
        if (channel.equalsIgnoreCase("temp")) reject = true;
        if (channel.equalsIgnoreCase("dice")) reject = true;
        if (reject) throw new Exception("指定したチャンネル["+channel+"]は作成/所属禁止名に指定されています");
    }
    public void checkNgCreateName(String channel) throws Exception {
        boolean reject = false;
        // 予約語のチャンネルは作成禁止
        if (channel.matches("[a-zA-Z]")) throw new Exception("指定したチャンネル["+channel+"]は作成禁止名に指定されています");
    }
    public boolean existChannel(String channel) {
        return db.isExistChannel(channel);
    }
    public void joinChannel(String channel, String joinuser, String password, boolean force, ChatType type, String join, String leave, boolean def, boolean listed, boolean addperm) throws Exception {
        // JOIN可能なチャンネル名か確認する
        if (!force) checkNgJoinName(channel);
        // チャンネルが存在しなければ作成する
        if (!db.isExistChannel(channel)) {
            // 作成可能なチャンネル名か確認する
            if (!force) checkNgCreateName(channel);
            // パスワード付きチャンネル追加
            addChannel(channel, password, type, join, leave, def, listed, addperm);
            
            // チャンネルにユーザー追加
            if (joinuser != null) addChannelUser(channel, joinuser, password, true);
        } else {
            // パスワード有りでチャンネルにユーザー追加
            if (joinuser != null) addChannelUser(channel, joinuser, password, false);
        }
    }
    private void addChannelUser(String channel, String name, String password, boolean owner) throws Exception {
        // チャンネルの存在チェック
        if (!db.isExistChannel(channel)) {throw new Exception("指定したチャンネル["+channel+"]は存在しません");}
        LoaderChannel ch = plg.getChannelList();
        // ユーザーの照会
        if (db.isExistChannelUser(channel, name)) {
            throw new Exception("既にチャンネル["+ch.getName(channel) +"]に参加しています");
        }

        // チャンネルがパスワード付きの場合はパスワード照会
        if (db.isExistChannelPass(channel)) {
            // パスワード指定無しの場合参加不可
            if (password == null) {
                throw new Exception("チャンネル["+ch.getName(channel)+"]に参加するためにはパスワードが必要です");
            }
            // パスワード不一致
            if (!db.getChannelPass(channel).equals(password)) {
                throw new Exception("チャンネル["+ch.getName(channel)+"]に参加するためのパスワードが一致しません、この記録は管理者に通知されます");
            }
        }
        // 問題なければ追加
        db.addChannelUser(channel, name, owner);
    }
    public void delChannelOwner(String channel, String name) throws Exception {
        // チャンネルの存在チェック
        if (!db.isExistChannel(channel)) {throw new Exception("指定したチャンネル["+channel+"]は存在しません");}
        LoaderChannel ch = plg.getChannelList();

        // ユーザーの照会
        if (!db.isChannelOwner(channel, name)) {
            throw new Exception("チャンネル["+ch.getName(channel)+"]の作成者グループに参加していません");
        }

        // 問題なければ削除
        db.updateChannelOwner(channel, name, false);
    }
    public void delChannelUser(String channel, String name) throws Exception {
        // チャンネルの存在チェック
        if (!db.isExistChannel(channel)) {throw new Exception("指定したチャンネル["+channel+"]は存在しません");}
        LoaderChannel ch = plg.getChannelList();

        // ユーザーの照会
        if (!db.isExistChannelUser(channel, name)) {
            throw new Exception("チャンネル["+ch.getName(channel)+"]に参加していません");
        }

        // 問題なければ削除
        db.delChannelUser(channel, name);
        
        // チャンネル所属ユーザーが居なくなったらチャンネル削除
        if (db.countChannelUsers(channel) <= 0) {
            db.delChannel(channel);
        }
    }
    public ArrayList<String> getChannelList(String name, int page, int count) throws Exception {
        return db.getChannelList(name, page, count);
    }
    public ArrayList<String> getChannelUsers(String tag, int page, int count) {
        return db.getChannelUsers(tag, page, count);
    }
    public ArrayList<String> getChannelUsers(String tag) throws Exception {
        return db.getChannelUsers(tag);
    }
    public ArrayList<String> getChannelInfo(String tag) throws Exception {
        return db.getChannelInfo(tag);
    }
    public boolean isJoin(String tag, String name) throws Exception {
        return db.isExistChannelUser(tag, name);
    }
    public boolean isActive(String tag, String name) throws Exception {
        return db.isActiveChannel(tag, name);
    }
    public String getActive(String name) throws Exception {
        return db.getChannelTag(db.getActiveChannel(name));
    }
    public void setActive(String name, String tag) throws Exception {
        db.updateActiveChannel(tag, name);
    }
    public void delChannel(String tag) throws Exception {
        db.delChannel(tag);
    }
    public boolean isPasswordChannel(String tag) {
        return db.isExistChannelPass(tag);
    }
    public ChatConf getChannelConf(String tag) {
        return db.getChannelConf(tag);
    }
    public void setName(String tag, String name) throws Exception {
       db.updateChannelName(tag, name);
    }
    public String getChannelPassword(String tag) {
        return db.getChannelPass(tag);
    }
    public void setChannelType(String tag, String type) throws Exception {
        db.updateChannelType(tag, type);
    }
    public ChatType getChannelType(String tag) throws Exception {
        return db.getChannelType(tag);
    }
    public boolean isChannelAutoJoin(String tag) {
        return db.isChannelAutoJoin(tag);
    }
    public boolean isChannelListed(String tag) {
        return db.isChannelListed(tag);
    }
    public boolean isChannelAddPerm(String tag) {
        return db.isChannelAddPerm(tag);
    }
    public void setChannelAutoJoin(String tag, boolean flag) throws Exception {
        db.updateChannelAutoJoin(tag, flag);
    }
    public void setChannelListed(String tag, boolean flag) throws Exception {
        db.updateChannelListed(tag, flag);
    }
    public void setChannelAddPerm(String tag, boolean flag) throws Exception {
        db.updateChannelAddPerm(tag, flag);
    }
    public void delChannelPass(String tag) throws Exception {
        db.delChannelPass(tag);
    }
    public void setChannelPass(String tag, String pass) throws Exception {
        db.addChannelPass(tag, pass);
    }
    public void addChannelOwner(String tag, String name) throws Exception {
        // チャンネルの存在チェック
        if (!db.isExistChannel(tag)) {throw new Exception("指定したチャンネル["+tag+"]は存在しません");}
        LoaderChannel ch = plg.getChannelList();
        
        // ユーザーの照会
        if (db.isChannelOwner(tag, name)) {
            throw new Exception("既にチャンネル["+ch.getName(tag)+"]の作成者グループに参加しています");
        }

        // ついか
        db.updateChannelOwner(tag, name, true);
    }
    public void changeChannelTag(String tag, String newtag) throws Exception {
        db.updateChannelTag(tag, newtag);
    }
    public boolean isChannelUser(String tag, String name) throws Exception {
        return db.isExistChannelUser(tag, name);
    }
    public boolean isChannelUser(String tag, UUID uid) throws Exception {
        return db.isExistChannelUser(tag, uid);
    }
    public ArrayList<String> getDefaultChannel() {
        return db.getDefaultChannel();
    }
    public void setChannelConf(String tag, ChatConf conf) throws Exception {
        db.setChannelConf(tag, conf);
    }
    public boolean isExistUserChannelConf(String name, String tag) throws Exception {
        return db.isExistUserChannelConf(name, tag);
    }
    public ChatConf getUserChannelConf(String name, String tag) throws Exception {
        return db.getUserChannelConf(name, tag);
    }
    public void setUserChannelConf(String name, String tag, ChatConf conf) throws Exception {
        db.setUserChannelConf(name, tag, conf);
    }
    public void addUserChannelConf(String name, String tag) throws Exception {
        db.addUserChannelConf(name, tag);
    }
    public boolean isExistUserUserConf(String name, String target) throws Exception {
        return db.isExistUserUserConf(name, target);
    }
    public ChatConf getUserUserConf(String name, String tag) throws Exception {
        return db.getUserUserConf(name, tag);
    }
    public ChatConf getUserUserConf(UUID name, UUID tag) throws Exception {
        return db.getUserUserConf(name, tag);
    }
    public void setUserUserConf(String name, String tag, ChatConf conf) throws Exception {
        db.setUserUserConf(name, tag, conf);
    }
    public void addUserUserConf(String name, String user) throws Exception {
        db.addUserUserConf(name, user);
    }
    public ArrayList<String> getUserNGList(String name, int page, int count) throws Exception {
        return db.getUserNGList(name,page,count);
    }
    public ArrayList<String> getUserChannels(String name) throws Exception {
        return db.getUserChannels(name);
    }
    public UserConfig getUserConfig(String name) throws Exception {
        return db.getUserConf(name);
    }
    public void setUserConfig(String name, UserConfig conf) throws Exception {
        db.updateUserConf(name, conf);
    }
    public boolean isExistUserConf(String name) throws Exception {
        return db.isExistUserConf(name);
    }

    public void sendMessage(String channel, String username, String msg) throws Exception {
        UserConfig conf = null;
        if (isExistUserConf(username)) {
            conf = getUserConfig(username);
        } else {
            conf = new UserConfig();
        }
        ChatType type = getChannelType(channel);
        boolean hit = false;
        // cunlockコマンド誤タイプ抑止
        if (msg.toLowerCase().indexOf("unlock") != -1) {
            msg = "<<system:unlockコマンド誤タイプの可能性があるため発言が伏せられました>>";
        }
        // チャンネル存在確認
        if (!existChannel(channel)) throw new Exception("指定したチャンネル["+channel+"]は存在しません");
        // 発言者が該当チャンネルに参加しているか
        if (!isChannelUser(channel, username)) throw new Exception("指定したチャンネルには参加していません");
       
        // 発言者のMUTE設定判定
        if (conf.isMute()) throw new Exception("現在サーバー管理者により発言禁止設定されています");
        
        // チャンネルの設定取得
        ChatConf chconf = getChannelConf(channel);

        int sendcounter = 0;
        StringBuilder localsb = null;
        // 指定チャンネルのユーザーにメッセージ送出
        for (Player pl: plg.getServer().getOnlinePlayers()) {
            if (plg.isSilent(pl.getName())) {
                if (!this.spychat.equals("")) {
                    if (plg.isSilent(this.spychat)) {
                        hit = true;
                    }
                }
                continue;
            }
            try {
                // ユーザーのチャンネル所属
                if (!isChannelUser(channel, pl.getName())) continue;
                
                // 対象ユーザーの設定を取得
                UserConfig plconf = null;
                if (isExistUserConf(pl.getName())) {
                    plconf = getUserConfig(pl.getName());
                } else {
                    plconf = new UserConfig();
                }
                ChatConf ususconf = null;
                if (isExistUserUserConf(pl.getName(), username)) {
                    ususconf = getUserUserConf(pl.getName(), username);
                } else {
                    ususconf = new ChatConf();
                }
                ChatConf uschconf = null;
                if (isExistUserChannelConf(pl.getName(), channel)) {
                    uschconf = getUserChannelConf(pl.getName(), channel);
                } else {
                    uschconf = new ChatConf();
                }

                // 送出先ユーザーのNGユーザー判定
                if ((ususconf != null) && (!plconf.isNgView()) && (ususconf.isNg())) continue;

                // ワールド指定の場合は同じワールドのユーザーのみに伝達する
                // 発言元がオンラインかつワールド指定かつ違うワールドの場合には次のユーザーの処理へ
                if ((plg.getServer().getOfflinePlayer(username).isOnline()) &&
                    (type == ChatType.world) &&
                    (plg.getServer().getPlayer(username)).getWorld() != pl.getWorld()) {
                    continue;
                }

                // ローカル指定の場合は、受信側が設定した距離内であれば伝達する
                // プレイヤーがオンラインの場合
                if ((plg.getServer().getOfflinePlayer(username).isOnline()) &&
                    (type == ChatType.local)){
                    Location loc = plg.getServer().getPlayer(username).getLocation();
                    Location loc2 = pl.getLocation();
                    // 違うワールドなら次ユーザー判定へ
                    if (loc.getWorld() != loc2.getWorld()) {
                        continue;
                    }
                    // 受信側の設定距離よりも二者間の距離が離れていれば次のユーザー処理へ
                    if (plconf.getLocal() * 10 < loc.distanceSquared(loc2)) {
                        continue;
                    }
                }

                //------------------------------------------------------------------
                // メッセージ整形
                //------------------------------------------------------------------
                StringBuilder sb = new StringBuilder();
                // チャンネルタグ部(チャンネル設定から取得)
                sb.append(m.cnvSTR2COLOR(chconf.getColor()));
                if (chconf.isBold()) sb.append(ChatColor.BOLD);
                if (chconf.isItalic()) sb.append(ChatColor.ITALIC);
                if (chconf.isLine()) sb.append(ChatColor.UNDERLINE);
                if (chconf.isStrike()) sb.append(ChatColor.STRIKETHROUGH);
                sb.append("[");
                sb.append(getTag(channel));
                sb.append("] ");
                sb.append(ChatColor.RESET);

                // ユーザー名部(送信先ユーザー設定から取得)
                if (ususconf != null) {
                    sb.append(m.cnvSTR2COLOR(ususconf.getColor()));
                    if (ususconf.isBold()) sb.append(ChatColor.BOLD);
                    if (ususconf.isItalic()) sb.append(ChatColor.ITALIC);
                    if (ususconf.isLine()) sb.append(ChatColor.UNDERLINE);
                    if (ususconf.isStrike()) sb.append(ChatColor.STRIKETHROUGH);
                    sb.append("<");
                    sb.append(username);
                    sb.append("> ");
                    sb.append(ChatColor.RESET);
                } else {
                    sb.append("<");
                    sb.append(username);
                    sb.append("> ");
                }
                
                // メッセージ部(送信先ユーザー設定から取得)
                if (uschconf != null) {
                    sb.append(m.cnvSTR2COLOR(uschconf.getColor()));
                    if (uschconf.isBold()) sb.append(ChatColor.BOLD);
                    if (uschconf.isItalic()) sb.append(ChatColor.ITALIC);
                    if (uschconf.isLine()) sb.append(ChatColor.UNDERLINE);
                    if (uschconf.isStrike()) sb.append(ChatColor.STRIKETHROUGH);
                    sb.append(msg);
                    sb.append(ChatColor.RESET);
                } else {
                    sb.append(msg);
                }

                // スパイチャット登録者がHITしたか
                if (this.spychat.equals(pl.getName())) hit=true;

                // ローカル指定の場合には自分自身は後で抄出する(人数付き表示設定あり)
                // 表示用に文字列データは退避しておく
                if ((pl.getName().equals(username)) && (conf.isRange()) && (type == ChatType.local)) {
                    localsb = sb;
                    continue; // 後で表示する
                }

                // プレイヤー取得、送信
                pl.sendMessage(m.repColor(sb.toString()));
                //plg.getServer().getPlayer(user).sendMessage(m.repColor(sbt.toString()));
                sendcounter++;
            } catch (Exception ex) {
                m.Warn(ex.getMessage());
            }
        }
        // ローカル指定の場合には自分自身は後で出力する(人数付き表示設定あり)
        if ((conf.isRange()) && (type == ChatType.local)) {
            if (plg.getServer().getOfflinePlayer(username).isOnline()) {
                plg.getServer().getPlayer(username).sendMessage(m.repColor("(+"+sendcounter+")"+localsb.toString()));
            }
        }
        
        // スパイメッセージ
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        sb.append(getTag(channel));
        sb.append("] ");
        sb.append("<");
        sb.append(username);
        sb.append("> ");
        sb.append(msg);
        if (!this.spychat.equals("")) {
            if (!hit) {
                if (plg.getServer().getOfflinePlayer(this.spychat).isOnline()) {
                    plg.getServer().getPlayer(this.spychat).sendMessage(ChatColor.GREEN+"[SpyChat] "+ChatColor.RESET+m.repColor(sb.toString()));
                }
            }
        }
        // Hawkeyeへログ
        if ((plg.getServer().getOfflinePlayer(username).isOnline()) &&
            (plg.isHawk())){
            Player pl = plg.getServer().getPlayer(username);
            //HawkEyeAPI.addCustomEntry(plg,"Chat",pl,pl.getLocation(),sb.toString());
        }
        m.info(sb.toString());
        plg.getLoggerDB().msgLogging(channel, username, msg);
    }
    public void sendInformation(String channel, String username, String target, String msg) throws Exception {
        LoaderChannel ch = plg.getChannelList();

        // チャンネル存在確認
        if (!ch.existChannel(channel)) throw new Exception("指定したチャンネル["+channel+"]は存在しません");

        //------------------------------------------------------------------
        // メッセージ整形
        //------------------------------------------------------------------
        StringBuilder sb = new StringBuilder();
        // チャンネルタグ部(チャンネル設定から取得)
        sb.append(ChatColor.YELLOW);
        sb.append("["+getTag(channel)+"] ");
        //sb.append(ChatColor.RESET);

        // メッセージ部(送信先ユーザー設定から取得)
        sb.append(msg);
        
        sendFreeMessage(channel, username, target, sb.toString() );
    }
    public void sendFreeMessage(String channel, String username, String target, String msg) throws Exception {
        LoaderChannel ch = plg.getChannelList();

        // チャンネル存在確認
        if (!ch.existChannel(channel)) throw new Exception("指定したチャンネル["+channel+"]は存在しません");

        //------------------------------------------------------------------
        // メッセージ整形
        //------------------------------------------------------------------
        StringBuilder sb = new StringBuilder();

        // メッセージ部(送信先ユーザー設定から取得)
        sb.append(msg);
        m.info(sb.toString());

        for (Player pl: plg.getServer().getOnlinePlayers()) {
            if (plg.isSilent(pl.getName())) {
                continue;
            }
            try {
                // ユーザーのチャンネル所属
                if (!ch.isChannelUser(channel, pl.getName())) continue;
                
                // 対象ユーザーの設定を取得
                UserConfig plconf = null;
                if (isExistUserConf(pl.getName())) {
                    plconf = getUserConfig(pl.getName());
                } else {
                    plconf = new UserConfig();
                }
                ChatConf ususconf = null;
                if (isExistUserUserConf(pl.getName(), username)) {
                    ususconf = getUserUserConf(pl.getName(), username);
                } else {
                    ususconf = new ChatConf();
                }
                ChatConf uschconf = null;
                if (isExistUserChannelConf(pl.getName(), channel)) {
                    uschconf = getUserChannelConf(pl.getName(), channel);
                } else {
                    uschconf = new ChatConf();
                }

                // 送出先ユーザーのNGユーザー判定
                if ((ususconf != null) && (!plconf.isNgView()) && (ususconf.isNg())) continue;

                // 送出先ユーザーの情報受信判定
                if (!plconf.isInfo()) continue;

                // 指定ユーザーへは必ず通知するため設定に関係なく後で表示させる
                // よって個々ではパス
                if ((username != null) && (pl.getName().equals(username))) continue; // 後で表示する

                // ﾀｰｹﾞｯﾄユーザー指定の場合は、同じく後で通知する
                if ((target != null) && (pl.getName().equals(target))) continue;

                // プレイヤー取得、送信
                pl.sendMessage(m.repColor(sb.toString()));
            } catch (Exception ex) {
                m.Warn(ex.getMessage());
            }
        }
        if (username != null) if (plg.getServer().getOfflinePlayer(username).isOnline()) plg.getServer().getPlayer(username).sendMessage(m.repColor(sb.toString()));
        if (target != null) if (plg.getServer().getOfflinePlayer(target).isOnline()) plg.getServer().getPlayer(target).sendMessage(m.repColor(sb.toString()));

        plg.getLoggerDB().msgLogging(channel, "Information", msg);
    }

}


package jp.minecraftuser.ecochat.type;

/**
 * コマンドタイプ列挙体
 * @author ecolight
 */
public enum CmdType {
    JOIN("join"),
    PASSJOIN("passjoin"),
    LEAVE("leave"),
    DELETE("delete"),
    SET("set"),
    ADD("add"),
    CONF("conf"),
    CHANNEL("channel"),
    KICK("kick"),
    LIST("list"),
    WHO("who"),
    INFO("info"),
    PM("pm"),
    RS("rs"),
    HISTORY("history"),
    PMHISTORY("pmhistory"),
    SPYCHAT("spychat"),
    SPYPM("spypm"),
    CONSOLE("console"),
    SEND("send"),
    DICE("dice"),
    NOTSUPPORT("");
    private final String name;
    
    /**
     * コンストラクタ
     * @param name 
     */
    private CmdType(String name) {
        this.name = name;
    }
    
    /**
     * 文字列返還
     * @return 列挙体の値を文字列で返却
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * 文字列指定を列挙体の値に変換して返却
     * @param name 種別文字列指定
     * @return 列挙体値返却
     */
    public static CmdType toCmd(String name) {
        CmdType result = null;
        for (CmdType cmd : values()) {
            if (cmd.toString().equals(name)) {
                result = cmd;
                break;
            }
        }
        return result != null ? result : NOTSUPPORT;
    }
}

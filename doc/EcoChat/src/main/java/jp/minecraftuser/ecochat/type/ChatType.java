
package jp.minecraftuser.ecochat.type;

/**
 * チャット種別列挙体
 * @author ecolight
 */
public enum ChatType {
    global("global"),
    local("local"),
    world("world");
    
    private final String name;
    /**
     * コンストラクタ
     * @param name 
     */
    private ChatType(String name) {
        this.name = name;
    }
    
    /**
     * 文字列変換
     * @return 列挙体値を文字列返還した値
     */
    @Override
    public String toString() {
        return name;
    }
    
    /**
     * 列挙体値に振られた番号を返す
     * @return 
     */
    public int getId() {
        if (this.toString().equals("global")) return 0;
        if (this.toString().equals("local")) return 1;
        if (this.toString().equals("world")) return 2;
        return -1;
    }

    /**
     * 指定された番号から列挙体値に変換して返却
     * @param id 列挙体値を示す番号
     * @return 列挙体値
     */
    public String getName(int id) {
        if (id == 0) return("global");
        if (id == 1) return("local");
        if (id == 2) return("world");
        return "err";
    }

}


package jp.minecraftuser.ecochat.db.chat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jp.minecraftuser.ecochat.m;
import jp.minecraftuser.ecoframework.DatabaseFrame;
import jp.minecraftuser.ecoframework.PluginFrame;
import jp.minecraftuser.ecoframework.exception.DatabaseControlException;
import org.bukkit.entity.Player;

/**
 * エコチャットDBインサート処理
 * @author ecolight
 */
public class EcoChatDBInsert extends DatabaseFrame {
    private EcoChatDB db = null;
    public EcoChatDBInsert(EcoChatDB frame_) {
        super(frame_);
        db = frame_;
    }
    




    


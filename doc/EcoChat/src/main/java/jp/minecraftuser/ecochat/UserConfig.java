/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.minecraftuser.ecochat;

/**
 *
 * @author ecolight
 */
public class UserConfig {
    private boolean mute = false;
    private int local = 50;
    private boolean info = false;
    private boolean ngview = false;
    private boolean range = true;
    private boolean rswarn = true;
    public boolean isMute() { return this.mute; }
    public int getLocal() { return this.local; }
    public boolean isInfo() { return this.info; }
    public boolean isNgView() { return this.ngview; }
    public boolean isRange() { return this.range; }
    public boolean isRsWarn() { return this.rswarn; }
    public void setMute(boolean flag) { this.mute = flag; }
    public void setLocal(int flag) { this.local = flag; }
    public void setInfo(boolean flag) { this.info = flag; }
    public void setNgView(boolean flag) { this.ngview = flag; }
    public void setRange(boolean flag) { this.range = flag; }
    public void setRsWarn(boolean flag) { this.rswarn = flag; }
}

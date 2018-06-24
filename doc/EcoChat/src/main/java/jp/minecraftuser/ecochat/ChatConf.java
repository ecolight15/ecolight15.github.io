
package jp.minecraftuser.ecochat;

/**
 *
 * @author ecolight
 */
public class ChatConf {
    private String color = "WHITE";
    private boolean bold = false;
    private boolean italic = false;
    private boolean line = false;
    private boolean strike = false;
    private boolean ng = false;
    public ChatConf() {
        
    }
    public String getColor() { return color; }
    public boolean isBold() { return bold; }
    public boolean isItalic() { return italic; }
    public boolean isLine() { return line; }
    public boolean isStrike() { return strike; }
    public boolean isNg() { return ng; }
    public void setColor(String color) { this.color = color; }
    public void setBold(boolean bold) { this.bold = bold; }
    public void setItalic(boolean italic) { this.italic = italic; }
    public void setLine(boolean line) { this.line = line; }
    public void setStrike(boolean strike) { this.strike = strike; }
    public void setNg(boolean ng) { this.ng = ng; }
}

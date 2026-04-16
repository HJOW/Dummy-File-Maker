package org.duckdns.hjow.dfm;

/** GUI 환경에서 콤보박스 사용을 위함 */
public class ComboBoxItem {
    protected String key, name;
    public ComboBoxItem() {}
    public ComboBoxItem(String key, String name) {
        super();
        this.key = key;
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }
}

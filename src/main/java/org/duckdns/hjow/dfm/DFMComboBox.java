package org.duckdns.hjow.dfm;

import java.util.Vector;

import javax.swing.JComboBox;

/** JComboBox 의 Warning (자바7로 오면서 제네릭이 추가됨으로 인함) 제거용 상속체 */
public class DFMComboBox extends JComboBox<ComboBoxItem> {
    private static final long serialVersionUID = 7972564005423012085L;
    public DFMComboBox() {
        super();
    }
    
    public DFMComboBox(Vector<ComboBoxItem> items) {
        super(items);
    }
}

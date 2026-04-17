package org.duckdns.hjow.dfm.ui;

/*
Copyright 2026 HJOW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.duckdns.hjow.dfm.stringtable.DFMStringTableManager;

/** 설정 대화상자 */
public class DFMOptionDialog {
    protected JDialog dialog;
    
    protected Vector<ComboBoxItem> vCharset = new Vector<ComboBoxItem>();
    
    protected DFMComboBox cbxCharset;
    protected JSpinner spBufferSize;
    protected JCheckBox chkDynamicBuffer;
    
    protected GUIDummyFileMaker parent;
    
    public DFMOptionDialog(GUIDummyFileMaker parent) {
        this.parent = parent;
        
        dialog = new JDialog(parent.getFrame(), true);
        dialog.setSize(400, 300);
        dialog.setTitle(DFMStringTableManager.t("Option"));
        dialog.setLayout(new BorderLayout());
        GUIDummyFileMaker.centerWindow(dialog);
        
        JPanel pnMain, pnCenter, pnDown, pnCenterContents;
        JPanel[] pns;
        JLabel[] lbs;
        
        pnMain = new JPanel();
        pnMain.setLayout(new BorderLayout());
        dialog.add(pnMain, BorderLayout.CENTER);
        
        pnCenter = new JPanel();
        pnCenter.setLayout(new BorderLayout());
        pnMain.add(pnCenter, BorderLayout.CENTER);
        
        pnCenter.add(new JPanel(), BorderLayout.SOUTH);
        
        pnCenterContents = new JPanel();
        pnCenter.add(pnCenterContents, BorderLayout.CENTER);
        pns = new JPanel[6];
        lbs = new JLabel[pns.length];
        pnCenterContents.setLayout(new GridLayout(pns.length, 1));
        for(int idx=0; idx<pns.length; idx++) {
            pns[idx] = new JPanel();
            pns[idx].setLayout(new FlowLayout(FlowLayout.LEFT));
            lbs[idx] = new JLabel();
            pns[idx].add(lbs[idx]);
            pnCenterContents.add(pns[idx]);
        }
                
        lbs[0].setText(DFMStringTableManager.t("Charset"));
        vCharset.add(new ComboBoxItem("ISO-8859-1"));
        vCharset.add(new ComboBoxItem("EUC-KR"));
        vCharset.add(new ComboBoxItem("UTF-8"));
        vCharset.add(new ComboBoxItem("UTF-16"));
        cbxCharset = new DFMComboBox(vCharset);
        pns[0].add(cbxCharset);
        
        lbs[1].setText(DFMStringTableManager.t("Buffer size"));
        SpinnerNumberModel modelNumber = new SpinnerNumberModel(8192, 128, 262144, 1);
        spBufferSize = new JSpinner(modelNumber);
        pns[1].add(spBufferSize);
        
        lbs[2].setText(DFMStringTableManager.t("Dynamic buffer"));
        chkDynamicBuffer = new JCheckBox(DFMStringTableManager.t("Use"));
        pns[2].add(chkDynamicBuffer);
        
        pnDown = new JPanel();
        pnDown.setLayout(new FlowLayout());
        pnMain.add(pnDown, BorderLayout.SOUTH);
        
        JButton btn;
        btn = new JButton(DFMStringTableManager.t("Apply"));
        btn.addActionListener(new ActionListener() {   
            @Override
            public void actionPerformed(ActionEvent e) {
                processApply();
                close();
            }
        });
        pnDown.add(btn);
        
        
    }
    
    /** 설정 적용 */
    protected void processApply() {
        parent.setCharset(((ComboBoxItem) cbxCharset.getSelectedItem()).getKey());
        parent.setBufferSize(((Number) spBufferSize.getValue()).intValue());
        parent.setUseDynamicBuffer(chkDynamicBuffer.isSelected());
    }
    
    /** 부모 객체로부터 현재 설정을 불러와 화면에 적용 */
    protected void loadOption() {
        cbxCharset.setSelectedIndex(0);
        spBufferSize.setValue(8192);
        chkDynamicBuffer.setSelected(false);
        
        if(parent != null) {
            for(ComboBoxItem charsetOne : vCharset) {
                if(parent.getCharset().equals(charsetOne.getKey())) {
                    cbxCharset.setSelectedItem(charsetOne);
                }
            }
            
            spBufferSize.setValue(parent.getBufferSize());
            chkDynamicBuffer.setSelected(parent.isUseDynamicBuffer());
        }
    }
    
    /** 설정 대화상자 열기 */
    public void open()  {
        loadOption();
        dialog.setVisible(true); 
    }
    
    /** 설정 대화상자 닫기 */
    public void close() {
        if(dialog != null) dialog.setVisible(false);
    }
    
    /** 설정 대화상자 사용 종료 (대화상자 닫기 포함) */
    public void dispose() {
        close();
        parent = null;
    }
}

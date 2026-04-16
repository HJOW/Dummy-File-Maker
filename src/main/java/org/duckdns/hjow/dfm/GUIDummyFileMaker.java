package org.duckdns.hjow.dfm;

import org.duckdns.hjow.commons.ui.extend.HComboBox;
import org.duckdns.hjow.commons.util.DataUtil;
import org.duckdns.hjow.commons.util.GUIUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.math.BigInteger;
import java.util.Map;
import java.util.Vector;

public class GUIDummyFileMaker extends DummyFileMaker {
    private JFrame frame;
    private JFileChooser chooser;

    protected JProgressBar progBar;
    protected JTextField tfDest;
    protected JSpinner spSize;
    protected HComboBox cbxSizeUnit, cbxPattern;
    protected JButton btnRun;

    protected JTextArea taArea;
    protected JTextField tfStat;

    private String strErrorMessage = "";
    private BigInteger progressDivides = BigInteger.ONE;
    private int progressTotal, progressWritten;

    public GUIDummyFileMaker() {
        try {
            UIManager.LookAndFeelInfo[] looAndFeels = UIManager.getInstalledLookAndFeels();
            for(UIManager.LookAndFeelInfo lookAndFeelOne : looAndFeels) {
                if(lookAndFeelOne.getName().equalsIgnoreCase("Nimbus")) {
                    UIManager.setLookAndFeel(lookAndFeelOne.getClassName());
                }
            }
        } catch(Exception ex) { ex.printStackTrace(); }


        frame = new JFrame();
        frame.setSize(500, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setTitle("Dummy File Maker");

        GUIUtil.centerWindow(frame);

        chooser = new JFileChooser();

        JPanel pnMain = new JPanel();
        pnMain.setLayout(new BorderLayout());
        frame.add(pnMain, BorderLayout.CENTER);

        JPanel pnUp1, pnCenter1;
        JPanel pnUp21, pnUp22, pnUp23;
        JPanel pnUp31;

        pnUp1 = new JPanel();
        pnUp1.setLayout(new BorderLayout());
        pnMain.add(pnUp1, BorderLayout.NORTH);

        pnUp21 = new JPanel();
        pnUp22 = new JPanel();
        pnUp23 = new JPanel();
        pnUp21.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnUp22.setLayout(new FlowLayout(FlowLayout.LEFT));
        pnUp23.setLayout(new BorderLayout());
        pnUp1.add(pnUp21, BorderLayout.NORTH);
        pnUp1.add(pnUp22, BorderLayout.CENTER);
        pnUp1.add(pnUp23, BorderLayout.SOUTH);

        JLabel lb;
        JButton btn;

        lb = new JLabel("Dest");
        tfDest = new JTextField(35);
        btn = new JButton("...");
        pnUp21.add(lb);
        pnUp21.add(tfDest);
        pnUp21.add(btn);

        btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = chooser.showSaveDialog(frame);
                if(sel == JFileChooser.APPROVE_OPTION) {
                    tfDest.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        lb = new JLabel("Size");
        SpinnerNumberModel md = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        spSize = new JSpinner(md);
        pnUp22.add(lb);
        pnUp22.add(spSize);

        Vector<ComboBoxItem> vSizeUnits = new Vector<ComboBoxItem>();
        vSizeUnits.add(new ComboBoxItem("", "bytes"));
        vSizeUnits.add(new ComboBoxItem("K", "KiB"));
        vSizeUnits.add(new ComboBoxItem("M", "MiB"));
        vSizeUnits.add(new ComboBoxItem("G", "GiB"));
        vSizeUnits.add(new ComboBoxItem("T", "TiB"));
        cbxSizeUnit = new HComboBox(vSizeUnits);
        pnUp22.add(cbxSizeUnit);

        Vector<ComboBoxItem> vPattern = new Vector<ComboBoxItem>();
        vPattern.add(new ComboBoxItem("0", "Fill 0"));
        vPattern.add(new ComboBoxItem("1", "Fill Space"));
        vPattern.add(new ComboBoxItem("2", "Rotate Bytes"));
        vPattern.add(new ComboBoxItem("11", "Rotate 0~9"));
        vPattern.add(new ComboBoxItem("98", "Random Bytes"));
        vPattern.add(new ComboBoxItem("99", "Random 0~9"));
        cbxPattern = new HComboBox(vPattern);
        pnUp22.add(cbxPattern);

        btnRun = new JButton("Make");
        btnRun.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { start(); }
        });
        pnUp22.add(btnRun);

        progBar = new JProgressBar();
        pnUp23.add(progBar, BorderLayout.SOUTH);

        pnUp31 = new JPanel();
        pnUp31.setLayout(new BorderLayout());
        pnUp23.add(pnUp31, BorderLayout.CENTER);

        tfStat = new JTextField();
        tfStat.setEditable(false);
        pnUp31.add(tfStat, BorderLayout.CENTER);

        pnCenter1 = new JPanel();
        pnCenter1.setLayout(new BorderLayout());
        pnMain.add(pnCenter1, BorderLayout.CENTER);

        taArea = new JTextArea();
        taArea.setEditable(false);
        pnCenter1.add(new JScrollPane(taArea), BorderLayout.CENTER);
    }

    /** 확인 창으로 메시지 띄우기 */
    public void alert(String msg) {
        JOptionPane.showMessageDialog(frame, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    /** 로그 출력 */
    @Override
    public void log(String msg) {
        super.log(msg);
        taArea.append(msg + "\n");
        taArea.setCaretPosition(taArea.getDocument().getLength());
    }

    /** 작업 수행 (프레임 띄우고 중단) */
    public void run(Map<String, String> argMap) throws Exception {
        frame.setVisible(true);

        String strSize = argMap.get("--size");
        if(DataUtil.isNotEmpty(strSize)) {
            strSize = strSize.replace(",", "").replace(" ", "").trim();
            String strSizeUnit = "";
            if(strSize.endsWith("K") || strSize.endsWith("M") || strSize.endsWith("G") || strSize.endsWith("T")) {
                strSizeUnit = strSize.substring(strSize.length()-1).toUpperCase();
                strSize = strSize.substring(0, strSize.length()-1);
            }

            spSize.setValue(new Integer(strSize));
        }

        String strDest = argMap.get("--dest");
        if(DataUtil.isNotEmpty(strDest)) {
            tfDest.setText(strDest.trim());
        }
    }

    /** 작업 시작 */
    protected void start() {
        btnRun.setEnabled(false);
        progBar.setIndeterminate(true);
        taArea.setText("");
        tfStat.setText("Job started !");
        log("Job started !");

        new Thread(new Runnable() {
            @Override
            public void run() {
                onThread();
            }
        }).start();
    }

    /** 쓰레드 내 작업 */
    protected void onThread() {
        strErrorMessage = "";
        try {
            // 입력값 및 선택값 꺼내기, 유효성 검사
            String strSize = String.valueOf(spSize.getValue());
            if(DataUtil.isEmpty(strSize)) {
                throw new RuntimeException("Please input the file sizes !");
            }

            ComboBoxItem itemSizeUnit = (ComboBoxItem) cbxSizeUnit.getSelectedItem();
            ComboBoxItem itemPattern  = (ComboBoxItem) cbxPattern.getSelectedItem();

            if(itemSizeUnit == null) throw new RuntimeException("Please select the size unit !");
            if(itemPattern == null) throw new RuntimeException("Please select the pattern !");

            strSize = strSize.trim() + itemSizeUnit.getKey();
            String strPattern = itemPattern.getKey();

            String strDest = tfDest.getText().trim();
            if(DataUtil.isEmpty(strDest)) throw new RuntimeException("Please select or input where to save !");

            // 진행바 상태 변경
            progBar.setIndeterminate(false);

            // 크기에 따른 진행상태 나눗셈값 계산 (int 최대크기 오버플로우 방지)
            BigInteger totals = detectRequestedSizes(strSize);
            BigInteger intMax = new BigInteger((Integer.MAX_VALUE - 1) + "");
            if(totals.compareTo(intMax) > 0) {
                progressDivides = totals.divide(intMax).add(new BigInteger("2"));
            } else {
                progressDivides = new BigInteger("2");
            }

            File dest = new File(strDest);
            int buffSize = 8192;

            log("   Size : " + strSize);
            log("   Dest : " + dest.getAbsolutePath());
            log("   Pattern Code : " + strPattern);
            log("   Buffer Size : " + buffSize);
            strSize = null;

            // 작업 호출
            process(dest, totals, Integer.parseInt(strPattern), buffSize, new OnWriteCycle() {
                @Override
                public void onCycle(int cycle, BigInteger written, BigInteger totals) {
                    onOneCycleProcessing(cycle, written, totals);
                }
            });
        } catch(RuntimeException e) {
            strErrorMessage = e.getMessage();
        } catch(Throwable ex) {
            strErrorMessage = "(" + ex.getClass().getSimpleName() + ") " + ex.getMessage();
        }

        if(DataUtil.isNotEmpty(strErrorMessage)) {
            log(strErrorMessage);
            alert(strErrorMessage);
            strErrorMessage = "";
        }

        progBar.setValue(0);
        btnRun.setEnabled(true);
        tfStat.setText("Job finished !");
        log("Job finished !");
    }

    /** 버퍼 하나 작업 당 1회 호출 */
    protected void onOneCycleProcessing(int cycle, BigInteger written, BigInteger totals) {
        if(cycle % 10 == 0) { // 매 회 갱신하면 성능이 떨어질 우려가 있음
            progBar.setMaximum(totals.divide(progressDivides).intValue());
            progBar.setValue(written.divide(progressDivides).intValue());
            tfStat.setText("Progressing ... " + written + " / " + totals);
        }
    }
}

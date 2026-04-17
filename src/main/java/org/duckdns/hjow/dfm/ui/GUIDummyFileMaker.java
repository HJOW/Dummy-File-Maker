package org.duckdns.hjow.dfm.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.math.BigInteger;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.duckdns.hjow.dfm.DFMUtil;
import org.duckdns.hjow.dfm.DummyFileMaker;
import org.duckdns.hjow.dfm.OnWriteCycle;
import org.duckdns.hjow.dfm.stringtable.DFMStringTableManager;

/** GUI 기반 DFM 프로그램 구현체 */
public class GUIDummyFileMaker extends DummyFileMaker {
    private JFrame frame;
    private JFileChooser chooser;

    protected JProgressBar progBar;
    protected JTextField tfDest;
    protected JSpinner spSize;
    protected DFMComboBox cbxSizeUnit, cbxPattern;
    protected JButton btnRun, btnPause, btnStop;
    protected JMenuItem menuRun;

    protected JTextArea taArea;
    protected JTextField tfStat;

    private String strErrorMessage = "";
    private BigInteger progressDivides = BigInteger.ONE;
    
    protected Vector<ComboBoxItem> vSizeUnits = new Vector<ComboBoxItem>();
    protected Vector<ComboBoxItem> vPattern   = new Vector<ComboBoxItem>();

    /** 기본 생성자, UI 초기화를 이 때 진행 */
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
        frame.setSize(430, 350);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setTitle(DFMStringTableManager.t("Dummy File Maker (DFM)"));
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { processExit(); }
        });

        centerWindow(frame);
        
        chooser = new JFileChooser();

        JPanel pnMain = new JPanel();
        pnMain.setLayout(new BorderLayout());
        frame.add(pnMain, BorderLayout.CENTER);

        JPanel pnUp1, pnCenter1;
        JPanel pnUp21, pnUp22, pnUp23;
        JPanel pnUp31, pnUp32;

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

        lb = new JLabel(DFMStringTableManager.t("Destination"));
        tfDest = new JTextField(27);
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

        lb = new JLabel(DFMStringTableManager.t("Size"));
        SpinnerNumberModel md = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
        spSize = new JSpinner(md);
        pnUp22.add(lb);
        pnUp22.add(spSize);

        vSizeUnits.add(new ComboBoxItem("", "bytes"));
        vSizeUnits.add(new ComboBoxItem("K", "KiB"));
        vSizeUnits.add(new ComboBoxItem("M", "MiB"));
        vSizeUnits.add(new ComboBoxItem("G", "GiB"));
        vSizeUnits.add(new ComboBoxItem("T", "TiB"));
        cbxSizeUnit = new DFMComboBox(vSizeUnits);
        pnUp22.add(cbxSizeUnit);

        vPattern.add(new ComboBoxItem("0" , DFMStringTableManager.t("Fill 0")));
        vPattern.add(new ComboBoxItem("1" , DFMStringTableManager.t("Fill Space")));
        vPattern.add(new ComboBoxItem("2" , DFMStringTableManager.t("Rotate Bytes")));
        vPattern.add(new ComboBoxItem("11", DFMStringTableManager.t("Rotate 0~9")));
        vPattern.add(new ComboBoxItem("98", DFMStringTableManager.t("Random Bytes")));
        vPattern.add(new ComboBoxItem("99", DFMStringTableManager.t("Random 0~9")));
        cbxPattern = new DFMComboBox(vPattern);
        pnUp22.add(cbxPattern);

        progBar = new JProgressBar();
        pnUp23.add(progBar, BorderLayout.SOUTH);
        
        pnUp31 = new JPanel();
        pnUp31.setLayout(new FlowLayout(FlowLayout.RIGHT));
        pnUp23.add(pnUp31, BorderLayout.NORTH);
        
        btnRun = new JButton(DFMStringTableManager.t("Start"));
        ActionListener actionRun = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) { start(); }
        };
        btnRun.addActionListener(actionRun);
        pnUp31.add(btnRun);
        
        btnPause = new JButton("||");
        ActionListener actionPause = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flagPause = (! flagPause);
                if(flagPause) btnPause.setText("▶");
                else          btnPause.setText("||");
            }
        };
        btnPause.addActionListener(actionPause);
        btnPause.setVisible(false);
        pnUp31.add(btnPause);
        
        btnStop = new JButton("■");
        ActionListener actionStop = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flagStop = true;
                btnStop.setVisible(false);
            }
        };
        btnStop.addActionListener(actionStop);
        btnStop.setVisible(false);
        pnUp31.add(btnStop);

        pnUp32 = new JPanel();
        pnUp32.setLayout(new BorderLayout());
        pnUp23.add(pnUp32, BorderLayout.CENTER);

        tfStat = new JTextField();
        tfStat.setEditable(false);
        pnUp32.add(tfStat, BorderLayout.CENTER);

        pnCenter1 = new JPanel();
        pnCenter1.setLayout(new BorderLayout());
        pnMain.add(pnCenter1, BorderLayout.CENTER);

        taArea = new JTextArea();
        taArea.setEditable(false);
        pnCenter1.add(new JScrollPane(taArea), BorderLayout.CENTER);
        
        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu(DFMStringTableManager.t("File"));
        menuBar.add(menu);
        
        menuItem = new JMenuItem(DFMStringTableManager.t("Exit"));
        menuItem.addActionListener(new ActionListener() {   
            @Override
            public void actionPerformed(ActionEvent e) {
                processExit();
            }
        });
        menu.add(menuItem);
        
        menu = new JMenu(DFMStringTableManager.t("Run"));
        menuBar.add(menu);
        
        menuRun = new JMenuItem(DFMStringTableManager.t("Start"));
        menuRun.addActionListener(actionRun);
        menu.add(menuRun);
    }

    /** 확인 창으로 메시지 띄우기 */
    public void alert(String msg) {
        JOptionPane.showMessageDialog(frame, msg, DFMStringTableManager.t("Message"), JOptionPane.INFORMATION_MESSAGE);
    }

    /** 로그 출력 */
    @Override
    public void log(String msg) {
        super.log(msg);
        taArea.append(msg + "\n");
        taArea.setCaretPosition(taArea.getDocument().getLength());
    }

    /** 프로그램 실행 시 초기화 작업 후 이 메소드가 호출됨. UI 프레임을 띄우고 이 메소드 역할은 종료됨. */
    public void run(Map<String, String> argMap) throws Exception {
        frame.setVisible(true);

        String strSize = argMap.get("--size");
        if(DFMUtil.isNotEmpty(strSize)) {
            strSize = strSize.replace(",", "").replace(" ", "").trim();
            String strSizeUnit = "";
            if(strSize.endsWith("K") || strSize.endsWith("M") || strSize.endsWith("G") || strSize.endsWith("T")) {
                strSizeUnit = strSize.substring(strSize.length()-1).toUpperCase();
                strSize = strSize.substring(0, strSize.length()-1);
            }

            spSize.setValue(new Integer(strSize));
            
            if(DFMUtil.isEmpty(strSizeUnit)) {
                cbxSizeUnit.setSelectedIndex(0);
            } else {
                for(ComboBoxItem item : vSizeUnits) {
                    if(strSizeUnit.equals(item.getKey())) {
                        cbxSizeUnit.setSelectedItem(item);
                        break;
                    }
                }
            }
        }

        String strDest = argMap.get("--dest");
        if(DFMUtil.isNotEmpty(strDest)) {
            tfDest.setText(strDest.trim());
        }
    }

    /** 작업 시작 */
    protected void start() {
        btnRun.setEnabled(false);
        btnStop.setVisible(true);
        btnPause.setVisible(true);
        btnPause.setText("||");
        progBar.setIndeterminate(true);
        taArea.setText("");
        tfStat.setText(DFMStringTableManager.t("Job started !"));
        log(DFMStringTableManager.t("Job started !"));
        frame.setTitle(DFMStringTableManager.t(new File(tfDest.getText()).getName() + " - Dummy File Maker (DFM)"));

        new Thread(new Runnable() {
            @Override
            public void run() {
                onThread();
            }
        }).start();
    }

    /** 쓰레드 내 작업 정의 */
    protected void onThread() {
        flagStop  = false;
        flagPause = false;
        strErrorMessage = "";
        try {
            // 입력값 및 선택값 꺼내기, 유효성 검사
            String strSize = String.valueOf(spSize.getValue());
            if(DFMUtil.isEmpty(strSize)) {
                throw new RuntimeException(DFMStringTableManager.t("Please input the file sizes !"));
            }

            ComboBoxItem itemSizeUnit = (ComboBoxItem) cbxSizeUnit.getSelectedItem();
            ComboBoxItem itemPattern  = (ComboBoxItem) cbxPattern.getSelectedItem();

            if(itemSizeUnit == null) throw new RuntimeException(DFMStringTableManager.t("Please select the size unit !"));
            if(itemPattern == null) throw new RuntimeException(DFMStringTableManager.t("Please select the pattern !"));

            strSize = strSize.trim() + itemSizeUnit.getKey();
            String strPattern = itemPattern.getKey();

            String strDest = tfDest.getText().trim();
            if(DFMUtil.isEmpty(strDest)) throw new RuntimeException(DFMStringTableManager.t("Please select or input where to save !"));

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
            create(dest, totals, Integer.parseInt(strPattern), buffSize, 20L, false, new OnWriteCycle() {
                @Override
                public void onCycle(File f, int cycle, BigInteger written, BigInteger totals) {
                    onOneCycleProcessing(f, cycle, written, totals);
                }
            });
        } catch(RuntimeException e) {
            strErrorMessage = e.getMessage();
        } catch(Throwable ex) {
            strErrorMessage = DFMStringTableManager.t("Error on DFM") + " (" + ex.getClass().getSimpleName() + ") " + ex.getMessage();
        }

        if(DFMUtil.isNotEmpty(strErrorMessage)) {
            log(strErrorMessage);
            alert(strErrorMessage);
            strErrorMessage = "";
        }
        
        log(DFMStringTableManager.t("Job finished !"));
        tfStat.setText(DFMStringTableManager.t("Job finished !"));
        progBar.setIndeterminate(false);
        progBar.setValue(0);
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                btnStop.setVisible(false);
                btnPause.setVisible(false);
                btnRun.setEnabled(true);
                frame.setTitle(DFMStringTableManager.t("Dummy File Maker (DFM)"));
            }
        });
    }

    /** 버퍼 하나 작업 당 1회 호출 */
    protected void onOneCycleProcessing(File dest, int cycle, BigInteger written, BigInteger totals) {
        if(cycle % 10 == 0) { // 매 회 갱신하면 성능이 떨어질 우려가 있음
            progBar.setMaximum(totals.divide(progressDivides).intValue());
            progBar.setValue(written.divide(progressDivides).intValue());
            tfStat.setText(DFMStringTableManager.t("Processing") + " ... " + written + " / " + totals);
        }
    }
    
    /** 프로그램 종료 요청 시 호출 */
    protected void processExit() {
        flagStop = true;
        if(btnStop  != null) btnStop.setVisible(false);
        if(btnRun   != null) btnRun.setVisible(false);
        if(progBar  != null) progBar.setIndeterminate(true);
        try { Thread.sleep(100L); } catch(InterruptedException ex) {}
        frame.setVisible(false);
        System.exit(0);
    }
    
    /** 창 크기 반환 */
    public static Dimension getScreenSize() {
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
    
    /** 창을 모니터 가운데에 위치시킵니다. */
    public static void centerWindow(Window window) {
        Dimension scrSize = getScreenSize();
        window.setLocation((int)( scrSize.getWidth() / 2 - window.getWidth() / 2 ), (int)( scrSize.getHeight() / 2 - window.getHeight() / 2 ));
    }
}

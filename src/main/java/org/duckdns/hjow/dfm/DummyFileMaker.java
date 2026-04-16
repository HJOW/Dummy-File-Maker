package org.duckdns.hjow.dfm;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.duckdns.hjow.commons.util.ClassUtil;
import org.duckdns.hjow.commons.util.DataUtil;

public class DummyFileMaker {
    public static void main(String[] args) {
        Map<String, String> argMap = ClassUtil.convertAppParams(args);
        if(argMap == null) argMap = new HashMap<String, String>();

        String mode = argMap.get("--mode");
        if(mode == null) mode = "console";

        try {
            if(mode.equalsIgnoreCase("gui")) {
                new GUIDummyFileMaker().run(argMap);
            } else {
                new DummyFileMaker().run(argMap);
            }
        } catch(RuntimeException ex) {
            System.out.println("Error on dfm - " + ex.getMessage());
        } catch(Throwable ex) {
            System.out.println("Error on dfm - (" + ex.getClass().getSimpleName() + ") " + ex.getMessage());
        }

    }

    protected volatile boolean flagPause = false;
    protected volatile boolean flagStop = false;

    /** 기본 생성자, 프로그램 실행을 준비 */
    public DummyFileMaker() { }

    /** 로그 출력 */
    public void log(String msg) { System.out.println(msg); }

    /** 생성할 파일 크기 감지 */
    protected BigInteger detectRequestedSizes(String strSize) {
        BigInteger sizes = BigInteger.ZERO;

        if(DataUtil.isEmpty(strSize)) throw new RuntimeException("Please input '--size' arguments !");
        strSize = strSize.replace(",", "").replace(" ", "").trim();

        String strSizeUnit = "";
        if(strSize.endsWith("K") || strSize.endsWith("M") || strSize.endsWith("G") || strSize.endsWith("T")) {
            strSizeUnit = strSize.substring(strSize.length()-1).toUpperCase();
            strSize = strSize.substring(0, strSize.length()-1);
        }

        sizes = new BigInteger(strSize);
        strSize = null;

        BigInteger big1024 = new BigInteger("1024");
        int multiplies = 0;

        if(strSizeUnit.equals("K")) multiplies = 1;
        if(strSizeUnit.equals("M")) multiplies = 2;
        if(strSizeUnit.equals("G")) multiplies = 3;
        if(strSizeUnit.equals("T")) multiplies = 4;

        for(int idx=0; idx<multiplies; idx++) {
            sizes = sizes.multiply(big1024);
        }

        return sizes;
    }

    /** 프로그램 실행 메인 동작 */
    public void run(Map<String, String> argMap) throws Exception {
        String strSize = argMap.get("--size");
        BigInteger sizes = detectRequestedSizes(strSize);

        String strDest = argMap.get("--dest");
        if(DataUtil.isEmpty(strDest)) throw new RuntimeException("Please input '--dest' arguments !");

        File dest = new File(strDest);
        int pattern = 0;
        int buffSize = 1024;

        String strPattern = argMap.get("--pattern");
        if(DataUtil.isNotEmpty(strPattern)) {
            pattern = Integer.parseInt(strPattern.trim());
        }

        String strBufferSize = argMap.get("--buffersize");
        if(DataUtil.isNotEmpty(strBufferSize)) {
            buffSize = Integer.parseInt(strBufferSize.replace(",", "").trim());
        }

        System.out.println("DFM Start, size : " + sizes + ", dest : " + dest.getAbsolutePath());
        process(dest, sizes, pattern);
        System.out.println("END");
    }

    /** 파일 생성 */
    public void process(File dest, BigInteger size, int pattern) throws Exception {
        process(dest, size, pattern, 8192);
    }

    /** 파일 생성 */
    public void process(File dest, BigInteger size, int pattern, int bufferSize) throws Exception {
        process(dest, size, pattern, bufferSize, null);
    }

    /** 파일 생성 */
    public void process(File dest, BigInteger size, int pattern, int bufferSize, OnWriteCycle oneCycleEventHandler) throws Exception {
        flagPause = false;
        flagStop = false;

        FileOutputStream out1 = null;
        byte[] buffer = new byte[bufferSize];
        int idx;
        Throwable exc = null;

        for(idx=0; idx<bufferSize; idx++) {
            buffer[idx] = (byte) 0;
        }

        try {
            BigInteger written = BigInteger.ZERO;
            BigInteger lefts   = size;
            int cycle, nowWriteSize, rotate;

            cycle = 0;
            rotate = 0;
            out1 = new FileOutputStream(dest);

            byte zeros  = new String("0").getBytes("UTF-8")[0];
            byte spaces = new String(" ").getBytes("UTF-8")[0];

            while(lefts.compareTo(BigInteger.ZERO) > 0) {
                if(flagStop) { log("Stop requested."); break; }
                if(! flagPause) {
                    // 버퍼 준비
                    for(idx=0; idx<bufferSize; idx++) {
                        switch(pattern) {
                            case 1:
                                buffer[idx] = spaces;
                            case 2:
                                buffer[idx] = (byte) rotate;
                                rotate++;
                                if(rotate >= (int) Byte.MAX_VALUE) rotate = 0;
                            case 11:
                                buffer[idx] = String.valueOf(rotate).getBytes("UTF-8")[0];
                                rotate++;
                                if(rotate >= 10) rotate = 0;
                            case 98:
                                buffer[idx] = (byte) (Math.random() * ((int) Byte.MAX_VALUE));
                                break;
                            case 99:
                                buffer[idx] = String.valueOf(Math.random() * 9.9).getBytes("UTF-8")[0];
                                break;
                            default:
                                buffer[idx] = (byte) 0;
                        }
                    }

                    // 남은 양 체크
                    lefts = size.subtract(written);
                    // 지금 차례에 써야 하는 바이트 수 계산
                    if(lefts.compareTo(new BigInteger(bufferSize + "")) >= 0) {
                        nowWriteSize = bufferSize;
                    } else {
                        nowWriteSize = bufferSize - lefts.intValue();
                    }

                    // 쓰기
                    out1.write(buffer, 0, nowWriteSize);

                    // 쓴 용량 갱신
                    written = written.add(new BigInteger(nowWriteSize + ""));
                    lefts = size.subtract(written);
                }

                // 이벤트 지정 시 호출
                if(oneCycleEventHandler != null) oneCycleEventHandler.onCycle(cycle, written, size);

                // 사이클 증가
                cycle++;
                if(cycle % 1000 == 0) Thread.sleep(20L);
                if(cycle >= Integer.MAX_VALUE - 1) cycle = 0;
            }
            out1.close();
            out1 = null;
        } catch(Throwable ex) {
            exc = ex;
        } finally {
            ClassUtil.closeAll(out1);
        }

        if(exc != null) throw new RuntimeException("(" + exc.getClass().getSimpleName() + ") " + exc.getMessage(), exc);
    }
}

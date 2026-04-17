package org.duckdns.hjow.dfm;
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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.duckdns.hjow.dfm.stringtable.DFMStringTableManager;
import org.duckdns.hjow.dfm.ui.GUIDummyFileMaker;

/** 더미 파일 생성기 메인 클래스 */
public class DummyFileMaker {
    /** 진입점 */
    public static void main(String[] args) {
        Map<String, String> argMap = DFMUtil.convertAppParams(args);
        if(argMap == null) argMap = new HashMap<String, String>();

        String mode = argMap.get("--mode");
        if(mode == null) mode = "gui";

        try {
            if(mode.equalsIgnoreCase("gui")) {
                new GUIDummyFileMaker().run(argMap);
            } else {
                new DummyFileMaker().run(argMap);
            }
        } catch(RuntimeException ex) {
            System.out.println(DFMStringTableManager.t("Error on DFM") + " - " + ex.getMessage());
        } catch(Throwable ex) {
            System.out.println(DFMStringTableManager.t("Error on DFM") + " - (" + ex.getClass().getSimpleName() + ") " + ex.getMessage());
        }

    }

    /** true 시 작업 일시 중단 (종료되지 않음) - 쓰레드 내 사이클은 계속 돌아가고, 핵심 처리 코드만 건너뜀 */
    protected volatile boolean flagPause = false;
    /** true 시 작업이 중단 (사이클 루프가 중단됨) */
    protected volatile boolean flagStop = false;
    /** 문자 관련 패턴을 선택한 경우 이 캐릭터셋을 따름 */
    protected String charset = "ISO-8859-1";
    /** 동적 버퍼 사용여부 */
    protected boolean useDynamicBuffer = false;

    /** 기본 생성자, 프로그램 실행을 준비 */
    public DummyFileMaker() { }

    /** 로그 출력 */
    public void log(String msg) { System.out.println(msg); }

    /** 생성할 파일 크기 감지 */
    protected BigInteger detectRequestedSizes(String strSize) {
        BigInteger sizes = BigInteger.ZERO;

        if(DFMUtil.isEmpty(strSize)) throw new RuntimeException(DFMStringTableManager.t("Please input '--size' arguments !"));
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
        if(DFMUtil.isEmpty(strDest)) throw new RuntimeException(DFMStringTableManager.t("Please input '--dest' arguments !"));

        File dest = new File(strDest);
        int pattern = 0;
        int buffSize = 8192;

        String strPattern = argMap.get("--pattern");
        if(DFMUtil.isNotEmpty(strPattern)) {
            pattern = Integer.parseInt(strPattern.trim());
        }

        String strBufferSize = argMap.get("--buffersize");
        if(DFMUtil.isNotEmpty(strBufferSize)) {
            buffSize = Integer.parseInt(strBufferSize.replace(",", "").trim());
        }

        System.out.println("DFM Start, size : " + sizes + ", dest : " + dest.getAbsolutePath());
        create(dest, sizes, pattern, buffSize);
        System.out.println("END");
    }
    
    /** 파일 내용 패턴 - 매 바이트를 0 으로 채우기 (기본값) */
    public static final int PATTERN_FILL_ZERO = 0;
    
    /** 파일 내용 패턴 - 매 바이트를 공백 문자 (띄어쓰기) 로 채우기 (ISO-8859-1 기준) */
    public static final int PATTERN_FILL_SPACE = 1;
    
    /** 파일 내용 패턴 - 매 바이트를 숫자 문자 0 으로 채우기 (ISO-8859-1 기준) */
    public static final int PATTERN_FILL_ZERO_NUMBER = 10;
    
    /** 파일 내용 패턴 - 매 바이트를 순서대로 (0~127) 채우기 */
    public static final int PATTERN_ROTATE_BYTE = 2;
    
    /** 파일 내용 패턴 - 매 바이트를 숫자 문자 순서대로 (0~9) 채우기 (ISO-8859-1 기준) */
    public static final int PATTERN_ROTATE_NUMBER = 11;
    
    /** 파일 내용 패턴 - 매 바이트를 랜덤 값 (0~127) 으로 채우기 */
    public static final int PATTERN_RANDOM_BYTE = 98;
    
    /** 파일 내용 패턴 - 매 바이트를 랜덤 숫자 문자 (0~9) 으로 채우기 (ISO-8859-1 기준) */
    public static final int PATTERN_RANDOM_NUMBER = 99;

    /** 
     * 파일 생성 (이 프로그램의 본 목적 핵심 메소드)
     * 
     * @param dest : 파일이 생성될 경로 및 파일명, 단 확장자가 zip 인 경우 생성된 파일이 압축됨.
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     */
    public void create(File dest, BigInteger size, int pattern) throws Exception {
        create(dest, size, pattern, 8192);
    }

    /** 
     * 파일 생성 (이 프로그램의 본 목적 핵심 메소드)
     * 
     * @param dest : 파일이 생성될 경로 및 파일명, 단 확장자가 zip 인 경우 생성된 파일이 압축됨.
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     * @param bufferSize : 버퍼 크기 (한 사이클에 출력하는 바이트 크기, 클 수록 속도가 빨라지며, 기본값은 8192)
     */
    public void create(File dest, BigInteger size, int pattern, int bufferSize) throws Exception {
        create(dest, size, pattern, bufferSize, 20L);
    }
    
    /** 
     * 파일 생성 (이 프로그램의 본 목적 핵심 메소드)
     * 
     * @param dest : 파일이 생성될 경로 및 파일명, 단 확장자가 zip 인 경우 생성된 파일이 압축됨.
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     * @param bufferSize : 버퍼 크기 (한 사이클에 출력하는 바이트 크기, 클 수록 속도가 빨라지며, 기본값은 8192)
     * @param threadGapTimeMillis : 사이클 간 Sleep 주기, 짧을 수록 속도가 빠르나 시스템이 불안정해질 수 있음. 밀리초 단위. 최소 20 이상을 넣어야 함.
     */
    public void create(File dest, BigInteger size, int pattern, int bufferSize, long threadGapTimeMillis) throws Exception {
        create(dest, size, pattern, bufferSize, threadGapTimeMillis, null);
    }
    
    /** 
     * 파일 생성 (이 프로그램의 본 목적 핵심 메소드)
     * 
     * @param dest : 파일이 생성될 경로 및 파일명, 단 확장자가 zip 인 경우 생성된 파일이 압축됨.
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     * @param bufferSize : 버퍼 크기 (한 사이클에 출력하는 바이트 크기, 클 수록 속도가 빨라지며, 기본값은 8192)
     * @param oneCycleEventHandler : 매 사이클마다 처리할 이벤트 (null 입력 가능)
     */
    public void create(File dest, BigInteger size, int pattern, int bufferSize, long threadGapTimeMillis, OnWriteCycle oneCycleEventHandler) throws Exception {
        String ext = DFMUtil.getFileExt(dest);
        if("zip".equals(ext)) createZipped(dest, size, pattern, bufferSize, threadGapTimeMillis, oneCycleEventHandler);
        else                  createPlain(dest, size, pattern, bufferSize, threadGapTimeMillis, oneCycleEventHandler);
    }

    /** 
     * 일반적인 파일 생성 (이 메소드에서 확장자 검사 안 함)
     * 
     * @param dest : 파일이 생성될 경로 및 파일명, 확장자에 관계없이 해당 이름으로 생성
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     * @param bufferSize : 버퍼 크기 (한 사이클에 출력하는 바이트 크기, 클 수록 속도가 빨라지며, 기본값은 8192)
     * @param oneCycleEventHandler : 매 사이클마다 처리할 이벤트 (null 입력 가능)
     */
    protected void createPlain(final File dest, final BigInteger size, final int pattern, final int bufferSize, final long threadGapTimeMillis, final OnWriteCycle oneCycleEventHandler) throws Exception {
        flagPause = false;
        flagStop = false;

        FileOutputStream  out1 = null;
        OutputStream      out2 = null;
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
            
            if(useDynamicBuffer) out2 = new BufferedOutputStream(out1);
            else                 out2 = out1;

            byte zeros  = new String("0").getBytes(charset)[0];
            byte spaces = new String(" ").getBytes(charset)[0];

            while(lefts.compareTo(BigInteger.ZERO) > 0) {
                if(flagStop) { log(DFMStringTableManager.t("Stop requested.")); break; }
                if(! flagPause) {
                    // 버퍼 준비
                    for(idx=0; idx<bufferSize; idx++) {
                        switch(pattern) {
                            case PATTERN_FILL_SPACE:
                                buffer[idx] = spaces;
                                break;
                            case PATTERN_ROTATE_BYTE:
                                buffer[idx] = (byte) rotate;
                                rotate++;
                                if(rotate >= (int) Byte.MAX_VALUE) rotate = 0;
                                break;
                            case PATTERN_ROTATE_NUMBER:
                                buffer[idx] = String.valueOf(rotate).getBytes(charset)[0];
                                rotate++;
                                if(rotate >= 10) rotate = 0;
                                break;
                            case PATTERN_RANDOM_BYTE:
                                buffer[idx] = (byte) (Math.random() * ((int) Byte.MAX_VALUE));
                                break;
                            case PATTERN_RANDOM_NUMBER:
                                buffer[idx] = String.valueOf(Math.random() * 9.9).getBytes(charset)[0];
                                break;
                            case PATTERN_FILL_ZERO_NUMBER:
                                buffer[idx] = zeros;
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
                    out2.write(buffer, 0, nowWriteSize);

                    // 쓴 용량 갱신
                    written = written.add(new BigInteger(nowWriteSize + ""));
                    lefts = size.subtract(written);
                }

                // 이벤트 지정 시 호출
                if(oneCycleEventHandler != null) oneCycleEventHandler.onCycle(dest, cycle, written, size);

                // 사이클 증가
                cycle++;
                if(cycle % 1000 == 0) Thread.sleep(threadGapTimeMillis);
                if(cycle >= Integer.MAX_VALUE - 1) cycle = 0;
            }
            
            if(out2 != null) {
                out2.close();
                if(out2 == out1) out1 = null;
            }
            out2 = null;
            
            if(out1 != null) out1.close();
            out1 = null;
        } catch(Throwable ex) {
            exc = ex;
        } finally {
            DFMUtil.closeAll(out2, out1);
            out2 = null;
            out1 = null;
        }

        if(exc != null) throw new RuntimeException("(" + exc.getClass().getSimpleName() + ") " + exc.getMessage(), exc);
    }
    
    /** 
     * 압축된 파일 생성 (이 메소드에서 확장자 검사 안 함)
     * 
     * @param dest : 압축 파일이 생성될 경로 및 압축 파일명, 확장자에 관계없이 해당 이름으로 생성되며, 압축 파일 내에 단독 파일이 탑재됨.
     * @param size : 생성할 파일의 크기 (byte 단위)
     * @param pattern : 파일 내용 패턴 코드 (이 클래스에 정의된 상수)
     * @param bufferSize : 버퍼 크기 (한 사이클에 출력하는 바이트 크기, 클 수록 속도가 빨라지며, 기본값은 8192)
     * @param oneCycleEventHandler : 매 사이클마다 처리할 이벤트 (null 입력 가능)
     */
    protected void createZipped(final File dest, final BigInteger size, final int pattern, final int bufferSize, final long threadGapTimeMillis, final OnWriteCycle oneCycleEventHandler) throws Exception {
        flagPause = false;
        flagStop = false;

        FileOutputStream  out1 = null;
        OutputStream      out2 = null;
        ZipOutputStream   out3 = null;
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
            
            if(useDynamicBuffer) out2 = new BufferedOutputStream(out1);
            else                 out2 = out1;
            
            String fileName = dest.getName();
            String nameWithoutExt = fileName.substring(0, (int) (fileName.length() - DFMUtil.getFileExt(fileName).length() - 1));
            ZipEntry entry = new ZipEntry(nameWithoutExt);
            
            out3 = new ZipOutputStream(out2);
            out3.putNextEntry(entry);
            
            byte zeros  = new String("0").getBytes(charset)[0];
            byte spaces = new String(" ").getBytes(charset)[0];

            while(lefts.compareTo(BigInteger.ZERO) > 0) {
                if(flagStop) { log(DFMStringTableManager.t("Stop requested.")); break; }
                if(! flagPause) {
                    // 버퍼 준비
                    for(idx=0; idx<bufferSize; idx++) {
                        switch(pattern) {
                            case PATTERN_FILL_SPACE:
                                buffer[idx] = spaces;
                                break;
                            case PATTERN_ROTATE_BYTE:
                                buffer[idx] = (byte) rotate;
                                rotate++;
                                if(rotate >= (int) Byte.MAX_VALUE) rotate = 0;
                                break;
                            case PATTERN_ROTATE_NUMBER:
                                buffer[idx] = String.valueOf(rotate).getBytes(charset)[0];
                                rotate++;
                                if(rotate >= 10) rotate = 0;
                                break;
                            case PATTERN_RANDOM_BYTE:
                                buffer[idx] = (byte) (Math.random() * ((int) Byte.MAX_VALUE));
                                break;
                            case PATTERN_RANDOM_NUMBER:
                                buffer[idx] = String.valueOf(Math.random() * 9.9).getBytes(charset)[0];
                                break;
                            case PATTERN_FILL_ZERO_NUMBER:
                                buffer[idx] = zeros;
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
                    out3.write(buffer, 0, nowWriteSize);

                    // 쓴 용량 갱신
                    written = written.add(new BigInteger(nowWriteSize + ""));
                    lefts = size.subtract(written);
                }

                // 이벤트 지정 시 호출
                if(oneCycleEventHandler != null) oneCycleEventHandler.onCycle(dest, cycle, written, size);

                // 사이클 증가
                cycle++;
                if(cycle % 1000 == 0) Thread.sleep(threadGapTimeMillis);
                if(cycle >= Integer.MAX_VALUE - 1) cycle = 0;
            }
            
            if(out3 != null) {
                out3.close();
                if(out3 == out2) out2 = null;
            }
            out3 = null;
            
            if(out2 != null) {
                out2.close();
                if(out2 == out1) out1 = null;
            }
            out2 = null;
            
            if(out1 != null) out1.close();
            out1 = null;
        } catch(Throwable ex) {
            exc = ex;
        } finally {
            DFMUtil.closeAll(out3, out2, out1);
            out3 = null;
            out2 = null;
            out1 = null;
        }

        if(exc != null) throw new RuntimeException("(" + exc.getClass().getSimpleName() + ") " + exc.getMessage(), exc);
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public boolean isUseDynamicBuffer() {
        return useDynamicBuffer;
    }

    public void setUseDynamicBuffer(boolean useDynamicBuffer) {
        this.useDynamicBuffer = useDynamicBuffer;
    }
}

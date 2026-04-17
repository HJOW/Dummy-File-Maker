package org.duckdns.hjow.dfm.stringtable;

import java.util.Locale;

/** 프로그램 내 언어 처리 클래스. static 메소드들로 구성되어 있음. */
public class DFMStringTableManager {
    private static boolean initialized = false;
    private static DFMStringTable stringTable = null;
    
    /** 초기화 */
    private static synchronized void init() {
        if(initialized) return;
        
        DFMStringTable tableOne;
        String lang = Locale.getDefault().getLanguage();
        
        tableOne = new DFMStringTableKR();
        if(tableOne.getLang().equals(lang)) stringTable = tableOne;
        
        initialized = true;
    }
    
    /** 스트링 테이블로 번역된 텍스트 반환 */
    public static String t(String english) {
        if(! initialized) init();
        if(stringTable == null) return english;
        if(stringTable.containsKey(english)) return stringTable.get(english);
        return english;
    }
}
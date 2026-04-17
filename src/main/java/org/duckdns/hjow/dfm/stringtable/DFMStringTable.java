package org.duckdns.hjow.dfm.stringtable;

import java.util.HashMap;

/** 프로그램 내 언어 관련 클래스 */
public abstract class DFMStringTable {
    protected final HashMap<String, String> stringTable = new HashMap<String, String>();
    protected DFMStringTable() { init(); }
    
    /** 이 곳에서 언어 내용 입력 */
    protected abstract void init();
    
    /** 지원 언어코드 (2자리) 반환 - Locale.getDefault().getLanguage() 와 일치해야 함 */
    public abstract String getLang();
    
    /** 해당 텍스트가 스트링 테이블에 있는지를 확인 */
    public boolean containsKey(String english) {
        return stringTable.containsKey(english);
    }
    
    /** 스트링 테이블로 번역된 텍스트 반환 */
    public String get(String english) {
        if(stringTable.containsKey(english)) return stringTable.get(english);
        return english;
    }
    
}

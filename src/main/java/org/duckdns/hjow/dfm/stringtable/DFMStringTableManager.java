package org.duckdns.hjow.dfm.stringtable;
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
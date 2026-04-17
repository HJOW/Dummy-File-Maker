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

import java.io.Closeable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** DFM 동작에 필요한 유틸리티 클래스, 여러 static 메소드 제공, HJOW-Libs 프로젝트 참조 */
public class DFMUtil {
    
    /**
     * <p>객체가 비었는지 여부를 반환합니다. null 이면 true 가 반환되고, 빈 텍스트인 경우도 true 가 반환됩니다. 숫자 (java.lang.Number 하위)와 java.lang.Boolean 타입인 경우 값에 관계없이 false 가 반환됩니다.</p>
     * 
     * @param ob : 검사할 텍스트
     * @return 비었는지의 여부
     */
    public static boolean isEmpty(Object ob)
    {
        if(ob == null) return true;
        if(ob instanceof Number)
        {
            return false;
        }
        else if(ob instanceof Boolean)
        {
            return false;
        }
        else if(ob instanceof String)
        {
            if(((String) ob).trim().equals("")) return true;
            else if(((String) ob).trim().equals("null")) return true;
            else return false;
        }
        else if(ob instanceof List<?>)
        {
            return ((List<?>) ob).isEmpty();
        }
        else if(ob instanceof Map<?, ?>)
        {
            return ((Map<?, ?>) ob).isEmpty();
        }
        else if(String.valueOf(ob).trim().equals("null")) return true;
        else if(String.valueOf(ob).trim().equals("")) return true;
        else return isEmpty(String.valueOf(ob));
    }
    
    /**
     * <p>객체가 비었는지 여부를 반환합니다. null 이면 false 가 반환되고, 빈 텍스트인 경우도 false 가 반환됩니다.</p>
     * 
     * @param ob : 검사할 텍스트
     * @return 비었는지의 여부
     */
    public static boolean isNotEmpty(Object ob)
    {
        return ! isEmpty(ob);
    }
    
    /**
     * 문자열 배열에서 매개변수를 읽어냅니다.
     * 
     * @param args 매개변수 입력 값들
     * @return
     */
    public static Map<String, String> convertAppParams(String[] args) {
        if(args == null) return null;
        Map<String, String> resultMap = new HashMap<String, String>();
        
        if(args.length == 0) return resultMap;
        if(args.length == 1) {
            String argOne = args[0].trim();
            if(argOne.startsWith("--")) {
                resultMap.put(argOne.substring("--".length()), "true");
            } else {
                resultMap.put("value", args[0]);
            }
        } else {
            String value = null;
            String keyCurrent = null;
            for(String a : args) {
                String argOne = a.trim();
                if(argOne.startsWith("--")) {
                    if(keyCurrent != null) {
                        keyCurrent = argOne.substring("--".length());
                        resultMap.put(keyCurrent, "true");
                        keyCurrent = null;
                        continue;
                    }
                    keyCurrent = argOne;
                } else {
                    if(keyCurrent == null) {
                        if(value != null) continue;
                        value = argOne;
                        resultMap.put("value", argOne);
                        continue;
                    }
                    resultMap.put(keyCurrent, argOne);
                    keyCurrent = null;
                }
            }
        }
        
        return resultMap;
    }
    
    /** 
     * 
     * 스트림을 순서대로 닫습니다. 하나 이상 예외가 발생하더라도 일단 모두 닫기를 시도합니다. 
     *     Closeable 인터페이스 구현체이거나, java.sql.Connection 객체인 경우 close 메소드를 호출합니다.
     *     그 외의 경우, close 메소드를 리플렉션으로 접근해 호출합니다.
     *     close 메소드가 없는 경우, dispose 메소드를 리플렉션으로 접근해 호출합니다.
     *     이 메소드도 없다면 표준 출력으로 경고가 출력되고 다음으로 넘어갑니다.
     */
    public static void closeAll(Object ... closeables) {
        if(closeables == null) return;
        for(Object c : closeables) {
            if(c == null) continue;
            try { 
                if(c instanceof Closeable          ) { ((Closeable) c).close();            continue; } 
                if(c instanceof java.sql.Connection) { ((java.sql.Connection) c).close();  continue; }
                Method mthd = null;
                
                try {
                    mthd = c.getClass().getMethod("close");
                    mthd.invoke(c);
                } catch(NoSuchMethodException e) {
                    mthd = c.getClass().getMethod("dispose");
                    mthd.invoke(c);
                }
                
            } catch(Throwable t) { System.out.println("Warn ! Exception occured when closing " + c.getClass().getName() + " - ( " + t.getClass().getName() + ") " + t.getMessage()); }
        }
    }
    
    /** 
     * 
     * 스트림을 순서대로 닫습니다. 하나 이상 예외가 발생하더라도 일단 모두 닫기를 시도합니다. 
     *     Closeable 인터페이스 구현체이거나, java.sql.Connection 객체인 경우 close 메소드를 호출합니다.
     *     그 외의 경우, close 메소드를 리플렉션으로 접근해 호출합니다.
     *     close 메소드가 없는 경우, dispose 메소드를 리플렉션으로 접근해 호출합니다.
     *     이 메소드도 없다면 표준 출력으로 경고가 출력되고 다음으로 넘어갑니다.
     */
    public static void closeAll(List<java.io.Closeable> closeables) {
        if(closeables == null) return;
        for(Object c : closeables) {
            if(c == null) continue;
            try {
                if(c instanceof Closeable          ) { ((Closeable) c).close();            continue; } 
                if(c instanceof java.sql.Connection) { ((java.sql.Connection) c).close();  continue; }
                Method mthd = null;
                
                try {
                    mthd = c.getClass().getMethod("close");
                    mthd.invoke(c);
                } catch(NoSuchMethodException e) {
                    mthd = c.getClass().getMethod("dispose");
                    mthd.invoke(c);
                }
                
            } catch(Throwable t) { System.out.println("Warn ! Exception occured when closing " + c.getClass().getName() + " - ( " + t.getClass().getName() + ") " + t.getMessage()); }
        }
    }
}

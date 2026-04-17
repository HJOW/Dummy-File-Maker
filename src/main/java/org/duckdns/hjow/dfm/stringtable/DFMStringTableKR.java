package org.duckdns.hjow.dfm.stringtable;
/** 프로그램 내 한글 언어 관련 클래스 */
public class DFMStringTableKR extends DFMStringTable {
    
    @Override
    public String getLang() {
        return "ko";
    }
    
    @Override
    protected void init() {
        stringTable.put("Dummy File Maker", "Dummy File Maker");
        stringTable.put("Destination"     , "생성 경로");
        stringTable.put("Size"            , "크기");
        stringTable.put("Fill 0"          , "0으로 채우기");
        stringTable.put("Fill Space"      , "공백 문자로 채우기");
        stringTable.put("Rotate Bytes"    , "바이트 돌려쓰기");
        stringTable.put("Rotate 0~9"      , "0~9 돌려쓰기");
        stringTable.put("Random Bytes"    , "랜덤 바이트");
        stringTable.put("Random 0~9"      , "0~9 랜덤 채우기");
        stringTable.put("Start"           , "시작");
        stringTable.put("Error on DFM"    , "DFM 오류");
        stringTable.put("Job started !"   , "작업 시작 !");
        stringTable.put("Job finished !"  , "작업 완료 !");
        stringTable.put("Stop requested." , "작업 중단.");
        stringTable.put("Processing"      , "처리 중");
        stringTable.put("Run"             , "실행");
        stringTable.put("File"            , "파일");
        stringTable.put("Exit"            , "종료");
        stringTable.put("Please select or input where to save !", "파일 생성 경로를 입력 또는 선택하여 주세요.");
        stringTable.put("Please input the file sizes !", "파일 크기를 입력해 주세요.");
        stringTable.put("Please select the size unit !", "파일 크기 단위를 선택해 주세요.");
        stringTable.put("Please select the pattern !", "패턴을 선택해 주세요.");
        stringTable.put("Please input '--size' arguments !", "--size 매개변수 값을 추가해 주세요. (생성할 파일의 크기 지정)");
        stringTable.put("Please input '--dest' arguments !", "--dest 매개변수 값을 추가해 주세요. (생성할 파일의 경로와 이름 지정)");
    }
}
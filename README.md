Dummy-File-Maker
=============
지정된 용량의 더미 파일을 만드는 간단한 프로그램입니다. 특정 용량의 파일을 만들어 전송 테스트 등에 활용할 수 있습니다.

# How to use
## 공통사항

이 프로그램은 자바 설치 후 이용하실 수 있습니다.    
JRE, JDK 모두 가능하며, 환경 변수 PATH 등록이 필요합니다.    
(설치형 프로그램으로 설치 시 자동으로 설정됩니다.)    
    
자바 7 이상의 버전이 필요합니다.
    
## GUI Mode

다음 명령어로 이용하실 수 있습니다.    
```
java -jar dfm.jar
```

UI 가 나타나며, 키보드와 마우스 등의 수단으로 조작하여 이용하실 수 있습니다.
    
## Console Mode

다음 명령어로 이용하실 수 있습니다.
```
java -jar dfm.jar --mode console --size 100M --dest D:\temp\test.file --pattern 0
```
    
매개변수 이름 앞에 "--" 를 붙여 쓰고, 한 칸을 띄운 후, 매개변수 값을 입력합니다. 값에 띄어쓰기가 들어가는 경우, 값 전체를 따옴표(") 로 감싸서 입력합니다.
    
매개변수
+ mode : 실행 모드, console, gui 만 이용 가능. (생략 시 기본값 : gui)
+ size : 생성할 파일의 크기, 숫자만 입력 시 byte 단위로 인식하며, K, M, G 단위 사용 가능. (console 모드 시 필수값) 예: 500K, 230M, 1G
+ dest : 생성할 파일의 위치와 이름 (console 모드 시 필수값)
+ pattern : 생성할 파일의 내용의 패턴 코드 (생략 시 기본값은 0)

패턴 코드
+ 0 : 매 바이트를 0 으로 채우기 (기본값)
+ 1 : 매 바이트를 공백 문자 (띄어쓰기) 로 채우기 (ISO-8859-1 기준)
+ 2 : 매 바이트를 순서대로 (0~127) 채우기
+ 10 : 매 바이트를 숫자 문자 0 으로 채우기 (ISO-8859-1 기준)
+ 11 : 매 바이트를 숫자 문자 순서대로 (0~9) 채우기 (ISO-8859-1 기준)
+ 98 : 매 바이트를 랜덤 값 (0~127) 으로 채우기
+ 99 : 매 바이트를 랜덤 숫자 문자 (0~9) 으로 채우기 (ISO-8859-1 기준)

# License
```
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
```


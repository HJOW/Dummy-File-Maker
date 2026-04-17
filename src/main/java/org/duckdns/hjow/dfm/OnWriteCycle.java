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

import java.io.File;
import java.math.BigInteger;

/** 파일 쓰기 작업 매 사이클마다 호출되는 이벤트를 정의 */
public interface OnWriteCycle {
    public void onCycle(File dest, int cycle, BigInteger written, BigInteger totals);
}

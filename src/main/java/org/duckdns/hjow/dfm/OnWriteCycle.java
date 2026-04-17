package org.duckdns.hjow.dfm;

import java.io.File;
import java.math.BigInteger;

/** 파일 쓰기 작업 매 사이클마다 호출되는 이벤트를 정의 */
public interface OnWriteCycle {
    public void onCycle(File dest, int cycle, BigInteger written, BigInteger totals);
}

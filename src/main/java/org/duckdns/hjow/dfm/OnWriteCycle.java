package org.duckdns.hjow.dfm;

import java.math.BigInteger;

public interface OnWriteCycle {
    public void onCycle(int cycle, BigInteger written, BigInteger totals);
}

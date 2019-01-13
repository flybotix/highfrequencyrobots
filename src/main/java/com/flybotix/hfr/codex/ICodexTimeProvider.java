package com.flybotix.hfr.codex;

public interface ICodexTimeProvider {
    /**
     * @return the current time in NANOSECONDS
     */
    public default long getTimestamp() {
        return System.nanoTime();
    }
}
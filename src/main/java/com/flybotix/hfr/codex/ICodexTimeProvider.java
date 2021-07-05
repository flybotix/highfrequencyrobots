package com.flybotix.hfr.codex;

public interface ICodexTimeProvider {
    /**
     * @return the current time in SECONDS
     */
    default double getTimestamp() {
        return (double)System.nanoTime()/1e9;
    }
}
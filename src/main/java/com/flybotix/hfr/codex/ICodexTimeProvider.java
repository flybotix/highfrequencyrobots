package com.flybotix.hfr.codex;

public interface ICodexTimeProvider {
    public default double getTimestamp() {
        return System.nanoTime();
    }
}
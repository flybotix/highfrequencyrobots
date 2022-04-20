package com.flybotix.hfr.db;

import com.flybotix.hfr.codex.RobotCodex;

import java.util.LinkedList;

/**
 * Holds multiple instances of the same RobotCodex. Store a reference to this in Robot.java
 */
public class RobotDBTable<E extends  Enum<E>> {
    private int mNumRows = 1;
    private final Class<E> mEnumClass;

    private LinkedList<RobotCodex<E>> mTableRows;

    public RobotDBTable(Class<E> pEnumClass) {
        this(pEnumClass, 1);
    }

    public RobotDBTable(Class<E> pEnumClass, int pNumRows) {
        mTableRows = new LinkedList<RobotCodex<E>>();
        mEnumClass = pEnumClass;
    }

    public RobotCodex<E> increment() {
        if(mTableRows.size() >= mNumRows) {
            RobotCodex<E> codex = mTableRows.removeLast();
            codex.reset();
            mTableRows.addFirst(codex);
        } else {
            mTableRows.addFirst(new RobotCodex<E>(mEnumClass));
        }

        return mTableRows.getFirst();
    }
}

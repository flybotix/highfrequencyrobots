package com.flybotix.hfr;

import com.flybotix.hfr.codex.RobotCodex;
import org.junit.Assert;
import org.junit.Test;

public class RobotCodexTest {
    private enum Test {
        A,B,C,D,E,F,G
    }

    @org.junit.Test
    public void RobotCodexCopy() {
        RobotCodex<Test> rc = new RobotCodex<>(Test.class);
        for(int i = 0; i < rc.length(); i++) {
            rc.set(i, Math.random());
        }
        Assert.assertEquals(rc, rc.copy());
    }
}

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

    @org.junit.Test
    public void RobotCodexNaN() {
        RobotCodex<Test> rc = new RobotCodex<>(Double.NaN, Test.class);
        rc.reset();
        rc.set(Test.A, Math.random());
        Assert.assertTrue(rc.isSet(Test.A));
        Assert.assertFalse(rc.isNull(Test.A));

        rc.set(Test.B, 0.0);
        Assert.assertTrue(rc.isSet(Test.B));
        Assert.assertFalse(rc.isNull(Test.B));

        rc.set(Test.C, Double.NaN);
        Assert.assertFalse(rc.isSet(Test.C));
        Assert.assertTrue(rc.isNull(Test.C));
        Assert.assertEquals("Safe get 0.0 test", rc.safeGet(Test.C, 0.0), 0.0, 0.000001);

        // Technically these are "set"... except they aren't values. So we expect them to return "not set"
        rc.set(Test.D, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(rc.isSet(Test.D));
        Assert.assertTrue(rc.isNull(Test.D));

        rc.set(Test.E, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(rc.isSet(Test.E));
        Assert.assertTrue(rc.isNull(Test.E));

        rc = new RobotCodex<>(0.0, Test.class);
        rc.reset();
        rc.set(Test.A, Math.random());
        Assert.assertTrue(rc.isSet(Test.A));
        Assert.assertFalse(rc.isNull(Test.A));

        rc.set(Test.B, 0.0);
        Assert.assertFalse(rc.isSet(Test.B));
        Assert.assertTrue(rc.isNull(Test.B));

        rc.set(Test.C, Double.NaN);
        Assert.assertFalse(rc.isSet(Test.C));
        Assert.assertTrue(rc.isNull(Test.C));
        Assert.assertEquals("Safe get 0.0 test", rc.safeGet(Test.C, 0.0), 0.0, 0.000001);

        // Technically these are "set"... except they aren't values. So we expect them to return "not set"
        rc.set(Test.D, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(rc.isSet(Test.D));
        Assert.assertTrue(rc.isNull(Test.D));

        rc.set(Test.E, Double.NEGATIVE_INFINITY);
        Assert.assertFalse(rc.isSet(Test.E));
        Assert.assertTrue(rc.isNull(Test.E));
    }
}

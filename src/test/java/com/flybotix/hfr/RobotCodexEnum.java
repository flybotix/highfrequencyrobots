package com.flybotix.hfr;

import com.flybotix.hfr.codex.RobotCodex;

public enum RobotCodexEnum {
    LEFT_DRIVE_CURRENT,
    RIGHT_DRIVE_CURRENT,
    @RobotCodex.StateCodex(stateEnum = TestDriveModes.class)
    DRIVE_MODE,
    @RobotCodex.FlagCodex
    IS_CURRENT_LIMITING
}

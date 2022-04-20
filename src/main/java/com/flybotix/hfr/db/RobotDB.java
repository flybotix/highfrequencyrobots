package com.flybotix.hfr.db;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds one or more RobotCodexTables.
 *
 * This class serves more as a way to manipulate multiple tables at once. This is uncommon in FRC.
 * Will experiment in the future with 2 default tables: one of doubles and one of floats. This allows us to save RIO
 * memory and network bandwidth for things that do not need high precision doubles.
 */
public class RobotDB {
    private Map<String, RobotDBTable<?>> mTables = new HashMap<>();

    public <E extends Enum<E>> RobotDBTable<E> registerTable(Class<E> pEnumClass) {
        RobotDBTable<E> result = new RobotDBTable<>(pEnumClass);
        mTables.put(pEnumClass.getCanonicalName(), result);
        return result;
    }
}

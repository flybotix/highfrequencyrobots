package com.flybotix.hfr.codex;

import com.flybotix.hfr.util.lang.EnumUtils;
import com.flybotix.hfr.util.lang.IConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class RobotCodex<E extends Enum<E>> {
    public static NumberFormat sGLOBAL_CSV_FORMAT = new DecimalFormat("0.00000");
    private final static String NAN = Double.toString(Double.NaN);
    protected CodexMetadata<E> mMeta;
    protected double[] mData;
    protected IConverter<Double, String>[] mConverters;
    protected double mDefaultValue = Double.NaN;
    protected boolean mHasChanged = false;

    /**
     * Creates a new Codex with the set metadata and default value
     * @param pDefaultValue
     * @param pMeta
     */
    public RobotCodex(double pDefaultValue, CodexMetadata<E> pMeta) {
        mMeta = pMeta;
        mDefaultValue = pDefaultValue;
        int length = EnumUtils.getLength(pMeta.getEnum());
        mData = new double[length];
        Arrays.fill(mData, mDefaultValue);
        mConverters = new IConverter[length];
        IConverter<Double, String> doubleFormat = f -> sGLOBAL_CSV_FORMAT.format(f);
        Arrays.fill(mConverters, doubleFormat);

        // Deal with annotations
        Map<E, StateCodex> states = EnumUtils.getEnumsAnnotatedWith(pMeta.getEnum(), StateCodex.class);
        for(E e : states.keySet()) {
            if(states.get(e).logStateName()) {
                createSimpleEnumConverter(e, states.get(e).stateEnum());
            }
        }

        Map<E, FlagCodex> flags = EnumUtils.getEnumsAnnotatedWith(pMeta.getEnum(), FlagCodex.class);
        for(E e : flags.keySet()) {
            if(flags.get(e).logFlagText()) {
                createSimpleBooleanConverter(e);
            }
        }
    }

    /**
     * Creates a Codex with the set default value and a blank metadata object
     * @param pDefaultValue Default value to set.  May be null
     * @param pEnum Enumeration backing the codex.  May NOT be null.
     */
    public RobotCodex(double pDefaultValue, Class<E> pEnum) {
        this(pDefaultValue, CodexMetadata.empty(pEnum));
    }

    /**
     * Creates a Codex with <code>NaN</code> as the default value
     * @param pEnum Enumeration backing the codex.  May NOT be null.
     */
    public RobotCodex(Class<E> pEnum) {
        this(Double.NaN, pEnum);
    }

    /**
     * @return the metadata
     */
    public CodexMetadata<E> meta() {
        return mMeta;
    }

    /**
     * Overrides the metadata
     * @param pMeta The new metadata
     */
    public void setMetadata(CodexMetadata<E> pMeta) {
        mMeta = pMeta;
    }

    /**
     * @return (effectively) this is E.values().length
     */
    public int length() {
        return mData.length;
    }

    /**
     * Allows the toStrign(E pData) method to return a custom-formatted string. Useful when the number represents a
     * state (such as enumeration or boolean). The CSV output will also use this conversion.
     * @param pData Enumeration that will be converted
     * @param pConversion The converter.
     */
    public void addConverter(E pData, IConverter<Double, String> pConversion) {
        mConverters[pData.ordinal()] = pConversion;
    }

    /**
     * @return a string representing the CSV header. Includes metadata as the first few columns.
     */
    public String getCSVHeader() {
        EnumSet<E> set = EnumSet.allOf(meta().getEnum());
        StringBuilder sb = new StringBuilder();
        sb.append("Codex Name").append(',');
        sb.append("Key").append(',');
        sb.append("Id").append(',');
        sb.append("Time ").append("(s)").append(',');
        for(E e : set) {
            sb.append(e.toString().replaceAll("_", " ")).append(',');
        }
        return sb.toString();
    }

    /**
     * @return A CSV string that represents this instance of the Codex, including metadata; formatted using the global
     *  number format and overriden converters
     */
    public String toFormattedCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(meta().getEnum().getSimpleName()).append(',');
        sb.append(meta().key()).append(',');
        sb.append(meta().id()).append(',');
        sb.append(sGLOBAL_CSV_FORMAT.format(meta().timestamp())).append(',');
        for(int i = 0; i < mData.length; i++) {
            if(isSet(i)) {
                sb.append(toString(i)).append(',');
//                sb.append(sGLOBAL_CSV_FORMAT.format(mData[i])).append(',');
            } else {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * @return A CSV string that represents this instance of the Codex, including metadata
     */
    public String toCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(meta().getEnum().getSimpleName()).append(',');
        sb.append(meta().key()).append(',');
        sb.append(meta().id()).append(',');
        sb.append(meta().timestamp()).append(',');
        for(int i = 0; i < mData.length; i++) {
            if(isSet(i)) {
                sb.append(mData[i]).append(',');
            } else {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * @return a VERY verbose csv string with each enum element name prefixing each codex value. DO NOT call this at
     * a high frequency! This is best used for unit testing.
     */
    public String toVerboseString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KEY="+mMeta.key()).append(", ");
        sb.append("ID="+mMeta.id()).append(", ");
        sb.append("TIME="+mMeta.timestamp()).append(", ");
        for(E e : EnumUtils.getSortedEnums(mMeta.getEnum())) {
            sb.append(e.name()).append("=").append(get(e)).append(", ");
        }
        return sb.toString();
    }

    /**
     * @param pCSV String to parse
     * @return <code>this</code> if successful.  Allows for stream mapping.
     */
    public RobotCodex<E> fillFromCSV(String pCSV) {
        String[] elements = pCSV.split(",");
        if(!elements[0].equalsIgnoreCase(meta().getEnum().getSimpleName())) {
            return this;
        }
        meta().setCompositeKey(Integer.parseInt(elements[1]));
        meta().overrideId(Integer.parseInt(elements[2]));
        meta().setTimestamp(Double.parseDouble(elements[3]));

        for(int i = 0; i < length(); i++) {
            if(elements[i+4].equals(NAN)) {
                set(i, Double.NaN);
            } else {
                set(i, Double.parseDouble(elements[i+4]));
            }
        }

        return this;
    }

    /**
     * Resets the data to the default value (which is usually <code>null</code>
     */
    public void reset() {
        Arrays.fill(mData, mDefaultValue);
        mMeta.next(true);
        mHasChanged = false;
    }

    /**
     * @return whether or not any data in this codex has been set (via <code>set()</code>)
     * since the last time <code>reset()</code> was called.
     */
    public boolean hasChanged() {
        return mHasChanged;
    }

    /**
     * @return the string representation of this codex value. Very useful if converters are used.
     */
    public String toString(E pData) {
        return toString(pData.ordinal());
    }

    /**
     * @return the string representation of this codex value. Very useful if converters are used.
     */
    public String toString(int i) { return mConverters[i].convert(get(i)); }

    public String toString() {
        if(mData == null) return "null";
        return Arrays.toString(mData);
    }

    @SuppressWarnings("unchecked")
    public boolean equals(Object pOther) {
        RobotCodex<E> o = (RobotCodex<E>)pOther;
        return Arrays.equals(o.mData, mData);
    }

    /**
     * Returns an enumeration element based upon the input enum class. This is useful in cases where the numerical
     * robot data must be mixed with state-related (enumerated) data for a subsysytem. This is pretty common in FRC.
     * @param pEnum The enumeration that represents a set of states
     * @param pData The element of this codex which represents a state
     * @param <T> The enumeration Type parameter
     * @return the enumeration, typed to T
     */
    public <T extends Enum<T>> T get(E pData, Class<T> pEnum) {
        return get(pData.ordinal(), pEnum);
    }

    /**
     * Useful for looping functionss
     * @return the value in the array at the ordinal
     * @param pOrdinal The index/ordinal to get
     */
    public <T extends Enum<T>> T get(int pOrdinal, Class<T> pEnum) {
        if(isSet(pOrdinal)) {
            return EnumUtils.getEnums(pEnum, true).get((int) get(pOrdinal));
        } else {
            return null;
        }
    }

    /**
     * Useful for looping functionss
     * @return the value in the array at the ordinal
     * @param pOrdinal The index/ordinal to get
     */
    public double get(int pOrdinal) {
        return mData[pOrdinal];
    }

    /**
     * @return the value in the array at the location of the enum's ordinal
     * @param pData The data piece to get
     */
    public double get(E pData) {
        return get(pData.ordinal());
    }

    /**
     * Provides a way to ensure that a non-null / non-NaN number is returned
     * @param pOrdinal the data piece to get
     * @param pDefault the default value
     * @return the value in the array at the location of the enum's ordinal, or default value if that is null
     */
    public double safeGet(int pOrdinal, double pDefault) {
        if(isNull(pOrdinal)) {
            return pDefault;
        } else {
            return get(pOrdinal);
        }
    }

    /**
     * Provides a way to ensure that a non-null / non-NaN number is returned
     * @param pData the data piece to get
     * @param pDefault the default value
     * @param pClass the class of the state
     * @return the value in the array at the location of the enum's ordinal, or default value if that is null
     */
    public  <T extends Enum<T>> T safeGet(E pData, T pDefault, Class<T> pClass) {
        if(isNull(pData)) {
            return pDefault;
        } else {
            return get(pData, pClass);
        }
    }

    /**
     * Provides a way to ensure that a non-null / non-NaN number is returned
     * @param pData the data piece to get
     * @param pDefault the default value
     * @return the value in the array at the location of the enum's ordinal, or default value if that is null
     */
    public double safeGet(E pData, double pDefault) {
        if(isNull(pData)) {
            return pDefault;
        } else {
            return get(pData);
        }
    }

    /**
     * Utility method to handle flags in a robot context. Sets a value to 1.0 if pValue == true, else the DEFAULT value (Double.NaN, etc)
     * If isSet() is called for this enumeration element, then that call will return TRUE if and only iff pValue is TRUE.
     * @param pData  the data to set
     * @param pValue true/false flag to set
     */
    public void set(E pData, boolean pValue) {
        set(pData.ordinal(), pValue);
    }

    /**
     * Utility method to handle flags in a robot context. Sets a value to 1.0 if pValue == true, else the DEFAULT value (Double.NaN, etc)
     * If isSet() is called for this enumeration element, then that call will return TRUE if and only iff pValue is TRUE.
     * @param pOrdinal  the data to set
     * @param pValue true/false flag to set
     */
    public void set(int pOrdinal, boolean pValue) {
        if(pValue) {
            set(pOrdinal,1.0);
        } else {
            set(pOrdinal,mDefaultValue);
        }
    }

    /**
     * Set some data.
     * @param pData The data to set
     * @param pValue The value of the data
     */
    public void set(E pData, double pValue) {
        set(pData.ordinal(), pValue);
    }

    /**
     * Set some data via a cached (or looped) index/ordinal
     * @param pOrdinal The index of the data (matches E.ordinal())
     * @param pValue The value of the data
     */
    public void set(int pOrdinal, double pValue) {
        mHasChanged = true;
        mData[pOrdinal] = pValue;
    }

    /**
     * Set some data that represents an enumerated state
     * @param pData The data to set
     * @param pState The state value
     * @param <T> The enumeration / set of states
     */
    public <T extends Enum<T>> void set(E pData, T pState) {
        set(pData, (double)pState.ordinal());
    }

    /**
     * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
     * @param pOrdinal Value to check
     * @return whether the value at the enum's location is not null and does not equal the codex's default value.
     */
    public boolean isSet(int pOrdinal) {
        return !isNull(pOrdinal);
    }


    /**
     * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
     * @param pOrdinal Value to check
     * @return whether the value at the enum's location is null or equals the codex's default value.
     */
    public boolean isNull(int pOrdinal) {
        return !Double.isFinite(mData[pOrdinal]) || mData[pOrdinal] == mDefaultValue;
//        return Double.isNaN(mData[pOrdinal]) || mData[pOrdinal] == mDefaultValue;
    }

    /**
     * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
     * @param pEnum Value to check
     * @return whether the value at the enum's location is not null and does not equal the codex's default value.
     */
    public boolean isSet(E pEnum) {
        return isSet(pEnum.ordinal());
    }


    /**
     * This seemingly-useless method helps reduce boilerplate.  It also adds a way to hack booleans/flags into a codex of non-booleans.
     * @param pEnum Value to check
     * @return whether the value at the enum's location is null or equals the codex's default value.
     */
    public boolean isNull(E pEnum) {
        return isNull(pEnum.ordinal());
    }

    /**
     * @return a hash based upon set values.
     */
    public CodexHash hash() {
        CodexHash codex = new CodexHash();
        for(int i = 0; i < mData.length; i++) {
            if(isSet(i)) {
                codex.bs.set(i);
                codex.nonNullCount++;
            }
        }

        // Set this bit so the BitSet length matches the enumeration length
        codex.bs.set(mData.length);
        return codex;
    }

    /**
     * @return a copy of this RobotCodex. The copy should be identical such that RobotCodex.equals() == true
     *  AND RobotMetadata.equals() == true.
     */
    public RobotCodex<E> copy() {
        RobotCodex<E> result = new RobotCodex<>(mMeta.getEnum());
        for(int i = 0; i < mData.length; i++) {
            result.mData[i] = mData[i];
        }
        for(int i = 0; i < mConverters.length; i++) {
            result.mConverters[i] = mConverters[i];
        }
        result.mMeta.setCompositeKey(mMeta.key());
        result.mMeta.setGlobalId(mMeta.gid());
        result.mMeta.setTimestamp(mMeta.timestamp());
        return result;
    }

    /**
     * This will format text output to be true or false
     * @param pData the piece of data represented by a Boolean
     */
    public void createSimpleBooleanConverter(E pData) {
        addConverter(pData, v->Boolean.toString(v == 1));
    }

    /**
     * This will format text output to be the name associated to the enumeration
     * @param pData the piece of data represented by a different enumeration
     * @param pEnum the class of the enumeration that <code>pData</code> represents
     */
    public <T> void createSimpleEnumConverter(E pData, Class<T> pEnum) {
        final List<T> enums = Arrays.asList(pEnum.getEnumConstants());
        addConverter(pData, v -> enums.get((int) Math.round(v)).toString());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface StateCodex {
        Class<?> stateEnum();
        boolean logStateName() default true;

        // Will eventually create a way to log both the text and the ordinal. Ordinals are useful for graphs.
//        boolean logStateOrdinal() default true;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FlagCodex {
        boolean logFlagText() default true;

        // Will eventually create a way to log both the text and the 0/1 flag value. The 0/1 value is useful for graphs.
//        boolean logFlagBool() default true;
    }
}

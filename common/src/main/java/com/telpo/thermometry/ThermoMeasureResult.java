package com.telpo.thermometry;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <code>ThermoMeasureResult</code>
 * <p>
 * 测温算法 {@link ThermoAlgorithm#measureTemperature} 的返回结果
 *
 * @author  gfm
 * @see     ThermoAlgorithm
 * @since   2.0
 */
public class ThermoMeasureResult {
    @IntDef({UNKNOWN, AVERAGE, SNAPSHOT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {}

    /**
     * 结果未知，温度值还没确定。
     * <p>Use with {@link #getType} and {@link #setType}
     */
    public static final int UNKNOWN = -1;

    /**
     * 温度值为平均值，比较准确，但可能确定温度值速度较慢，因为采样需要一定时间。
     * <p>Use with {@link #getType} and {@link #setType}
     */
    public static final int AVERAGE = 1;

    /**
     * 温度值为快照值，确定温度值速度较快，但可能不够准确。
     * <p>Use with {@link #getType} and {@link #setType}
     */
    public static final int SNAPSHOT = 2;

    /** 结果类型 */
    @Type
    private int type = UNKNOWN;

    /** 温度值 */
    private float temperature;

    /** 最近一帧数据 */
    private float[][] latestFrame;

    /**
     * Returns the type for this result.
     *
     * @return One of {@link #UNKNOWN}, {@link #AVERAGE}, or {@link #SNAPSHOT}.
     */
    @Type
    public int getType() {
        return type;
    }

    /** Returns the temperature value for this result. */
    public float getTemperature() {
        return temperature;
    }

    /** Returns the latest frame data for this result. */
    public float[][] getLatestFrame() {
        return latestFrame;
    }

    /**
     * Set the type of this result.
     *
     * @param type One of {@link #UNKNOWN}, {@link #AVERAGE}, or {@link #SNAPSHOT}.
     */
    public void setType(@Type int type) {
        this.type = type;
    }

    /** Set the temperature value of this result. */
    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    /** Set the latest frame data of this result. */
    public void setLatestFrame(float[][] latestFrame) {
        this.latestFrame = latestFrame;
    }

    /** Set the type and temperature value of this result. */
    public void set(@Type int type, float temperature) {
        this.type = type;
        this.temperature = temperature;
    }
}

package com.telpo.thermometry;

import android.graphics.Rect;

/**
 * <code>ThermoAlgorithm</code> 测温算法接口
 *
 * @author  gfm
 * @see     ThermoFactory
 * @see     ThermoMeasureResult
 * @since   2.0
 */
public interface ThermoAlgorithm {

    /**
     * 测温算法工厂接口
     */
    interface Factory {

        /**
         * 创建测温算法
         *
         * @return 返回新创建的测温算法对象
         */
        ThermoAlgorithm createThermoAlgorithm();
    }

    /**
     * 创建默认测温算法工厂
     * <p>
     * 默认实现类是
     * com.telpo.thermometry.ThermoAlgorithmFactory，通过反射来创建对象，异常时返回 null
     *
     * @return 如果异常返回 null, 否则返回新创建的测温算法工厂对象
     */
    static ThermoAlgorithm.Factory getDefaultFactory() {
        try {
            return (ThermoAlgorithm.Factory) Class.forName("com.telpo.thermometry.ThermoAlgorithmFactory").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 测量温度阵列数据帧的温度值
     *
     * @param data 温度阵列数据帧
     * @param result 用于算法输出测量结果
     *
     * @see TemperaturesListener#onTemperaturesReceived
     * @see #measureTemperature(float[][], Rect, ThermoMeasureResult)
     */
    void measureTemperature(float[][] data, ThermoMeasureResult result);

    /**
     * 测量温度阵列数据帧的温度值
     *
     * @param data 温度阵列数据帧
     * @param rect 指定温度阵列数据帧的矩形区域
     * @param result 用于算法输出测量结果
     *
     * @see TemperaturesListener#onTemperaturesReceived
     */
    void measureTemperature(float[][] data, Rect rect, ThermoMeasureResult result);
}

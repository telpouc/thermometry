package com.telpo.thermometry;

/**
 * 调用 {@link Thermometer#getTemperatures} 获取温度数据必须传入 <code>TemperaturesListener</code> 实例参数，
 * <p>
 * 需要实现 <code>TemperaturesListener</code> 接口定义 {@link #onTemperaturesReceived} 方法以接收温度阵列数据帧。
 *
 * @author  gfm
 * @see     Thermometer
 * @since   2.0
 */
public interface TemperaturesListener {
    /**
     * 在 <code>onTemperaturesReceived</code> 接收到数据帧后可以使用 {@link ThermoFactory#createThermoImage} 创建热成像位图，
     * 还可以使用测温算法 {@link ThermoAlgorithm} 统计温度。
     *
     * @param data 温度阵列数据帧，float 二维数组，一般大小为 32 * 32
     * @return 返回 false 继续获取数据帧，返回 true 停止获取数据帧。
     *
     * @see Thermometer#getTemperatures
     * @see ThermoAlgorithm#measureTemperature
     */
    boolean onTemperaturesReceived(float[][] data);
}

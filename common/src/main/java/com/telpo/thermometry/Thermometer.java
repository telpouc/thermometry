package com.telpo.thermometry;

/**
 * <code>Thermometer</code> 测温设备接口
 *
 * @author  gfm
 * @see     ThermoFactory
 * @see     TemperaturesListener
 * @since   2.0
 */
public interface Thermometer {

    /**
     * 测温设备工厂接口
     */
    interface Factory  {

        /**
         * 创建测温设备
         *
         * @return 返回新创建的测温设备对象
         */
        Thermometer createThermometer();
    }

    /**
     * 创建默认测温设备工厂
     * <p>
     * 默认实现类是
     * com.telpo.thermometry.ThermometerFactory，通过反射来创建对象，异常时返回 null
     *
     * @return 如果成功返回新创建的测温设备工厂对象，否则返回 null
     */
    static Thermometer.Factory getDefaultFactory() {
        try {
            return (Thermometer.Factory) Class.forName("com.telpo.thermometry.ThermometerFactory").newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断测温设备是否可用
     * <p>
     * 建议只在 app 初始化的时候判断一次，或者在 {@link #getTemperatures} 抛出异常时需要判断再调用
     *
     * @return 如果测温设备可用返回 true，否则返回 false
     */
    boolean isAvailable();

    /**
     * 获取温度阵列数据帧
     * <p>
     * <code>getTemperatures</code> 不能在主线程调用，因为它可能不会立即返回，正常情况下在它返回之前会一直输出数据帧
     * <p>
     * 如果 <code>getTemperatures<code/> 要返回，可以在 {@link TemperaturesListener#onTemperaturesReceived} 返回 true
     * <p>
     * 在 <code>getTemperatures</code> 返回之前不能重复调用 <code>getTemperatures</code>，否则会抛出 {@link IllegalStateException}
     * <p>
     * 可以在主线程调用 {@link #stop} 来强制停止工作，但不要在多个线程调用 <code>getTemperatures</code>，可以使用单线程的 <code>Executor</code>
     * 或 <code>HandlerThread</code> 来确保 <code>getTemperatures</code> 每次都是工作在同一线程
     *
     * @param listener 监听温度阵列数据帧输出
     *
     * @see Thermometer#stop()
     * @see TemperaturesListener
     */
    void getTemperatures(TemperaturesListener listener) throws Exception;

    /**
     * 停止测温设备工作
     * <p>
     * 目前仅用于停止获取温度阵列数据帧
     *
     * @see Thermometer#getTemperatures(TemperaturesListener)
     */
    void stop();
}

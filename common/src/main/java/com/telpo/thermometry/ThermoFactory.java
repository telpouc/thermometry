package com.telpo.thermometry;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.TypedValue;

import androidx.annotation.Nullable;

import java.io.File;

import dalvik.system.DexClassLoader;


/**
 * <code>ThermoFactory</code> 测温接口方法工厂类
 *
 * @author  gfm
 * @see     Thermometer
 * @see     ThermoAlgorithm
 * @since   2.0
 */
public class ThermoFactory {
    /** Utility class. */
    private ThermoFactory() {
        throw new IllegalStateException("No instances!");
    }

    /** 创建测温算法工厂 */
    public static ThermoAlgorithm.Factory createAlgorithmFactory(String clazzName) throws Exception {
        return (ThermoAlgorithm.Factory) Class.forName(clazzName).newInstance();
    }

    /** 创建测温设备工厂 */
    public static Thermometer.Factory createThermometerFactory(String clazzName) throws Exception {
        return (Thermometer.Factory) Class.forName(clazzName).newInstance();
    }

    /** 创建测温算法工厂 */
    public static ThermoAlgorithm.Factory createAlgorithmFactory(DexClassLoader classLoader, String clazzName) throws Exception {
        return (ThermoAlgorithm.Factory) classLoader.loadClass(clazzName).newInstance();
    }

    /** 创建测温设备工厂 */
    public static Thermometer.Factory createThermometerFactory(DexClassLoader classLoader, String clazzName) throws Exception {
        return (Thermometer.Factory) classLoader.loadClass(clazzName).newInstance();
    }

    /** 用于动态加载外部实现库 */
    public static DexClassLoader getClassLoader(Context context, String dexPath, String librarySearchPath) {
        File codeCacheDir;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            codeCacheDir = context.getCodeCacheDir();
        } else {
            codeCacheDir = context.getDir("code_cache",  Context.MODE_PRIVATE);
        }
        return new DexClassLoader(dexPath, codeCacheDir.getAbsolutePath(), librarySearchPath, context.getClassLoader());
    }

    /**
     * 创建默认测温算法
     *
     * @return 如果发生异常返回 null, 否则返回新创建的测温算法对象
     */
    public static ThermoAlgorithm createDefaultAlgorithm() {
        final ThermoAlgorithm.Factory factory = ThermoAlgorithm.getDefaultFactory();
        if (factory != null) {
            return factory.createThermoAlgorithm();
        }
        return null;
    }

    /**
     * 创建默认测温设备
     *
     * @return 如果发生异常返回 null, 否则返回新创建的测温设备对象
     */
    public static Thermometer createDefaultThermometer() {
        final Thermometer.Factory factory = Thermometer.getDefaultFactory();
        if (factory != null) {
            return factory.createThermometer();
        }
        return null;
    }

    /**
     * 这个方法是对 {@link Thermometer#isAvailable()} 的简单封装
     *
     * @param thermometer 测温设备对象，如果为 null 则返回 false
     * @return 如果测温设备可用返回 true，否则返回 false
     */
    public static boolean isThermometerAvailable(Thermometer thermometer) {
        if (thermometer != null) {
            return thermometer.isAvailable();
        }
        return false;
    }

    /**
     * 创建调色板位图
     * <p>
     * 用于创建热成像时根据温度值从调色板选择颜色
     *
     * @param context 通过该 Context 获取资源
     * @param resId 调色板图片资源 ID
     * @return 如果发生异常返回 null， 否则返回新创建的位图对象
     *
     * @see #createThermoImage
     */
    public static Bitmap createPalette(Context context, int resId) {
        try {
            final Resources resources = context.getResources();
            TypedValue value = new TypedValue();
            resources.openRawResource(resId, value);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inTargetDensity = value.density;
            options.inScaled = false;
            return BitmapFactory.decodeResource(resources, resId, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这个接口用于 {@link #createThermoImage} 基本绘制完成后进行额外的绘制
     */
    public interface ThermoImageExtraDrawer {
        /**
         * 绘制方法
         *
         * @param canvas 画布对象
         * @param paint 用于设置绘制样式、颜色
         * @param width 热成像宽度
         * @param height 热成像高度
         */
        void draw(Canvas canvas, Paint paint, int width, int height);
    }

    /**
     * 实现 {@link #createThermoImage} 基本绘制完成后进行绘制中心框
     */
    public static class ThermoImageCenterRectDrawer implements ThermoImageExtraDrawer {
        final int size;
        final int borderWidth; //
        final int borderColor;

        /**
         * ThermoImageCenterRectDrawer 无参数构造方法
         * <p>
         * 默认矩形大小为 20，边框宽度为 1，边框颜色为 blue
         */
        public ThermoImageCenterRectDrawer() {
            this(20, 1, Color.parseColor("blue"));
        }

        /**
         * ThermoImageCenterRectDrawer 带参数构造方法
         *
         * @param size 矩形大小（边长，矩形为正方形）
         * @param borderWidth 边框宽度
         * @param borderColor 边框颜色
         */
        public ThermoImageCenterRectDrawer(int size, int borderWidth, int borderColor) {
            this.size = size;
            this.borderWidth = borderWidth;
            this.borderColor = borderColor;
        }

        @Override
        public void draw(Canvas canvas, Paint paint, int width, int height) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(borderColor);
            paint.setStrokeWidth(borderWidth);
            canvas.drawRect(new Rect((width - size) / 2 , (height - size) / 2, (width + size) / 2, (height + size) / 2), paint);
        }
    }


    /**
     * 创建热成像位图
     *
     * @param data 温度阵列数据帧
     * @param palette 调色板位图
     * @return 如果发生异常返回 null， 否则返回新创建的位图对象
     *
     * @see ThermoFactory#createPalette
     * @see TemperaturesListener#onTemperaturesReceived
     * @see #createThermoImage(float[][], Bitmap, ThermoImageExtraDrawer)
     */
    public static Bitmap createThermoImage(float[][] data, Bitmap palette) {
        return createThermoImage(data, palette, null);
    }

    /**
     * 创建热成像位图
     *
     * @param data 温度阵列数据帧
     * @param palette 调色板位图
     * @param needCenterRect 是否需要绘制中心框
     * @return 如果发生异常返回 null， 否则返回新创建的位图对象
     *
     * @see ThermoFactory#createPalette
     * @see TemperaturesListener#onTemperaturesReceived
     * @see #createThermoImage(float[][], Bitmap, ThermoImageExtraDrawer)
     * @see ThermoImageCenterRectDrawer
     */
    public static Bitmap createThermoImage(float[][] data, Bitmap palette, boolean needCenterRect) {
        return needCenterRect ? createThermoImage(data, palette, new ThermoImageCenterRectDrawer()) : createThermoImage(data, palette);
    }

    /**
     * 创建热成像位图
     *
     * @param data 温度阵列数据帧
     * @param palette 调色板位图
     * @param extraDrawer 额外绘制接口对象，可以为 null
     * @return 如果发生异常返回 null， 否则返回新创建的位图对象
     *
     * @see ThermoFactory#createPalette
     * @see TemperaturesListener#onTemperaturesReceived
     */
    public static Bitmap createThermoImage(float[][] data, Bitmap palette, @Nullable ThermoImageExtraDrawer extraDrawer) {
        try {
            if (palette == null || data == null || data.length == 0) {
                throw new IllegalArgumentException("data invalid");
            }
            float startTem = 5.0F;
            float endTem = 41.0F;
            int size = (int) Math.sqrt(data.length);
            int width = size * 10;
            int height = size * 10;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(-1);
            Paint paint = new Paint();

            for(int i = 0; i < size; ++i) {
                for(int j = 0; j < size; ++j) {
                    paint.setColor(selectTemColor(palette, data[i][j], startTem, endTem));
                    canvas.drawRect(new Rect(j * 10, i * 10, j * 10 + 10, (i + 1) * 10), paint);
                }
            }

            if (extraDrawer != null) {
                extraDrawer.draw(canvas, paint, width, height);
            }
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 根据温度值从调色板选择颜色
    private static int selectTemColor(Bitmap palette, float temperature, float startTem, float endTem) {
        int width = palette.getWidth();
        int height = palette.getHeight();
        float compensate = 6.0F;

        temperature += compensate;
        int y = (int)(temperature * 10.0F);
        int start = (int)(startTem * 10.0F);
        int end = (int)(endTem * 10.0F);
        if (y <= start) {
            y = start;
        } else if (y > end - 1) {
            y = end - 1;
        }

        y -= start;
        int pixelColor;
        if (width < height) {
            pixelColor = palette.getPixel(0, y);
        } else {
            pixelColor = palette.getPixel(y, 0);
        }

        int r = Color.red(pixelColor);
        int g = Color.green(pixelColor);
        int b = Color.blue(pixelColor);
        String r1 = Integer.toHexString(r);
        if (r1.length() == 1) {
            r1 = "0" + r1;
        }

        String g1 = Integer.toHexString(g);
        if (g1.length() == 1) {
            g1 = "0" + g1;
        }

        String b1 = Integer.toHexString(b);
        if (b1.length() == 1) {
            b1 = "0" + b1;
        }

        String colorStr = "#" + r1 + g1 + b1;
        return Color.parseColor(colorStr);
    }

}

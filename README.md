# 天波人脸识别终端测温库

适用于天波人脸识别终端 TPS967 / TPS980P 机型

![Release](https://jitpack.io/v/telpouc/thermometry.svg)

## 使用
* **添加 JitPack 仓库到项目的根 build.gradle**
<br>Add JitPack repository in your root build.gradle:
```
    allprojects {
        repositories {
            ...
            maven { url 'https://jitpack.io' }
        }
    }
```

* **添加依赖到 app 的 build.gradle**
<br>Add the dependency:
```
    dependencies {
        implementation 'com.github.telpouc:thermometry:2.0.1'
    }
```

* **配置使用 Java 8**
<br>添加 compileOptions 如下:
```
    android {
        ...
        compileOptions {
            sourceCompatibility JavaVersion.VERSION_1_8
            targetCompatibility JavaVersion.VERSION_1_8
        }
    }
```

* **API 调用**
```
    // 创建默认测温设备
    Thermometer thermometer = ThermoFactory.createDefaultThermometer();

    // 创建默认测温算法
    ThermoAlgorithm algorithm = ThermoFactory.createDefaultAlgorithm();

    // 创建热成像调色板位图
    Bitmap palette = ThermoFactory.createPalette(this, R.drawable.palette);

    // 测温算法返回结果
    ThermoMeasureResult result = new ThermoMeasureResult();

    try {
        // getTemperatures 在返回之前会一直输出数据帧，不要在主线程调用
        thermometer.getTemperatures(data -> {
            // 创建热成像位图
            Bitmap image = ThermoFactory.createThermoImage(data, palette);
            // UI 显示热成像
            runOnUiThread(() -> imageView.setImageBitmap(image));

            // 测温算法处理
            algorithm.measureTemperature(data, result);
            if (result.getType() != ThermoMeasureResult.UNKNOWN) {
                Log.d("temperature", String.format("%.1f", result.getTemperature()));
            }
            // 返回 false 继续获取数据帧， 返回 true 停止获取数据帧
            return false; 
        });
    } catch(Exception e) {
        e.printStackTrace();
    }
```

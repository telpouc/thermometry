package com.telpo.thermometry.demo;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

import com.telpo.thermometry.ThermoAlgorithm;
import com.telpo.thermometry.ThermoFactory;
import com.telpo.thermometry.ThermoMeasureResult;
import com.telpo.thermometry.Thermometer;
import com.uber.autodispose.android.lifecycle.AndroidLifecycleScopeProvider;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

import static com.uber.autodispose.AutoDispose.autoDisposable;

@SuppressLint({"DefaultLocale", "AutoDispose"})
public class MainActivity extends AppCompatActivity {
    private TextView tvMachineTemperature;
    private ImageView imageView;
    private Button btnTest;
    private Bitmap palette; // 热成像调色板位图
    private Disposable autoReportDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvMachineTemperature = findViewById(R.id.tvMachineTemperature);
        imageView = findViewById(R.id.imageView);

        // 创建热成像调色板位图
        palette = ThermoFactory.createPalette(this, R.drawable.palette);

        btnTest = findViewById(R.id.btnTest);
        btnTest.setOnClickListener(v -> onTest());
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开始主动上报温度
        startAutoReport();
    }

    @Override
    protected void onStop() {
        // 停止主动上报温度
        stopAutoReport();
        super.onStop();
    }

    private void startAutoReport() {
        // 创建默认测温算法
        ThermoAlgorithm algorithm = ThermoFactory.createDefaultAlgorithm();
        assert algorithm != null;

        ThermoMeasureResult result = new ThermoMeasureResult(); // 测温算法返回结果
        autoReportDisposable = RxThermometer
            .from(Thermometer.getDefaultFactory())
            .getTemperatures()
            .doOnNext(data -> { // 每帧温度阵列输出
                // 创建热成像位图
                Bitmap image = ThermoFactory.createThermoImage(data, palette, false);
                // UI 显示热成像
                runOnUiThread(() -> imageView.setImageBitmap(image));
                // 测温算法处理
                algorithm.measureTemperature(data, result);
            })
            // 过滤未能确定温度的数据帧
            .filter(data -> result.getType() != ThermoMeasureResult.UNKNOWN)
            // 转温度值输出
            .map(data -> String.format("%.1f", result.getTemperature()))
            // 切换到主线程显示机温
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(temperature -> tvMachineTemperature.setText(temperature), Throwable::printStackTrace);
    }

    private void stopAutoReport() {
        if (autoReportDisposable != null && !autoReportDisposable.isDisposed()) {
            autoReportDisposable.dispose();
        }
    }

    //  被动请求温度测试
    private void onTest() {
        btnTest.setText("请稍候...");
        btnTest.setEnabled(false);
        stopAutoReport();

        RxThermometer.from(Thermometer.getDefaultFactory())
            .getMeasureResult()
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterTerminate(this::startAutoReport)  // 被动请求结束后开启主动上报
            .doFinally(() -> {
                btnTest.setText(R.string.getTemperatureTest);
                btnTest.setEnabled(true);
            })
            // 绑定界面生命周期，Activity onStop 时自动停止
            .as(autoDisposable(AndroidLifecycleScopeProvider.from(this, Lifecycle.Event.ON_STOP)))
            .subscribe(res -> {
                // 显示最后一帧热成像
                Bitmap image = ThermoFactory.createThermoImage(res.getLatestFrame(), palette, false);
                imageView.setImageBitmap(image);
                // 显示机温
                String temperature = String.format("%.1f", res.getTemperature());
                tvMachineTemperature.setText(temperature);
                Toast.makeText(this, "获取温度成功：" + temperature, Toast.LENGTH_SHORT).show();
            }, e -> {
                Toast.makeText(this, "获取温度失败", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            });
    }
}

package com.telpo.thermometry.demo;

import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;

import com.telpo.thermometry.TemperaturesListener;
import com.telpo.thermometry.ThermoAlgorithm;
import com.telpo.thermometry.ThermoFactory;
import com.telpo.thermometry.ThermoMeasureResult;
import com.telpo.thermometry.Thermometer;

import java.util.concurrent.Executor;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class RxThermometer {
    private Thermometer.Factory factory;
    private static Executor executor = new Executor() {
        final Handler mHandler;
        {
            HandlerThread thread = new HandlerThread(RxThermometer.class.getSimpleName());
            thread.start();
            mHandler = new Handler(thread.getLooper());
        }

        @Override
        public void execute(@NonNull Runnable command) {
            mHandler.post(command);
        }
    };

    private RxThermometer(Thermometer.Factory factory) {
        this.factory = factory;
    }

    public static RxThermometer from(Thermometer.Factory factory) {
        if (factory == null) {
            factory = Thermometer.getDefaultFactory();
        }
        return new RxThermometer(factory);
    }

    private Thermometer createThermometer() {
        return factory.createThermometer();
    }

    private final class TemperaturesOnSubscribe implements TemperaturesListener, FlowableOnSubscribe<float[][]> {
        FlowableEmitter<float[][]> emitter;

        @Override
        public boolean onTemperaturesReceived(float[][] data) {
            emitter.onNext(data);
            return emitter.isCancelled();
        }

        @Override
        public void subscribe(FlowableEmitter<float[][]> emitter) throws Exception {
            this.emitter = emitter;
            Thermometer thermometer = createThermometer();
            emitter.setCancellable(thermometer::stop);
            thermometer.getTemperatures(this);
        }
    }

    private final class MeasureResultOnSubscribe implements TemperaturesListener, SingleOnSubscribe<ThermoMeasureResult> {
        SingleEmitter<ThermoMeasureResult> emitter;
        ThermoMeasureResult result = new ThermoMeasureResult();
        ThermoAlgorithm algorithm;

        MeasureResultOnSubscribe(ThermoAlgorithm algorithm) {
            this.algorithm = algorithm;
        }

        @Override
        public boolean onTemperaturesReceived(float[][] data) {
            algorithm.measureTemperature(data, result);
            if (result.getType() != ThermoMeasureResult.UNKNOWN) {
                emitter.onSuccess(result);
                return true;
            }
            return emitter.isDisposed();
        }

        @Override
        public void subscribe(SingleEmitter<ThermoMeasureResult> emitter) throws Exception {
            this.emitter = emitter;
            Thermometer thermometer = createThermometer();
            emitter.setCancellable(thermometer::stop);
            thermometer.getTemperatures(this);
        }
    }

    public Flowable<float[][]> getTemperatures() {
        return Flowable.create(new TemperaturesOnSubscribe(), BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.from(executor));
    }

    public Single<ThermoMeasureResult> getMeasureResult(ThermoAlgorithm algorithm) {
        return Single.create(new MeasureResultOnSubscribe(algorithm))
                .subscribeOn(Schedulers.from(executor));
    }

    public Single<ThermoMeasureResult> getMeasureResult() {
        return getMeasureResult(ThermoFactory.createDefaultAlgorithm());
    }

    private Single<Thermometer> getThermometer() {
        return Single.create(new SingleOnSubscribe<Thermometer>() {
            SingleEmitter<Thermometer> emitter;
            @Override
            public void subscribe(SingleEmitter<Thermometer> emitter) {
                this.emitter = emitter;
                Thermometer thermometer = createThermometer();
                emitter.setCancellable(thermometer::stop);
                emitter.onSuccess(thermometer);
            }
        }).subscribeOn(Schedulers.from(executor));
    }

    public Single<String> getVersion() {
        return getThermometer().map(Thermometer::getVersion);
    }

    public Single<Float> getAmbientTemperature() {
        return getThermometer().map(Thermometer::getAmbientTemperature);
    }
}

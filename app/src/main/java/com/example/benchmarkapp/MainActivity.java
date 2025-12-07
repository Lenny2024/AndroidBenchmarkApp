package com.example.benchmarkapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.sql.SQLOutput;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;


public class MainActivity extends AppCompatActivity {

    TextView deviceModel, androidVersion, cpuInfo, ramInfo, storageInfo, cpuResult, ramResult, storageResult;
    Button runBenchmark;

    private class GpuBenchmark {
        private long startTime;
        private int frames;
        private Paint paint;
        private Bitmap bitmap;
        private Canvas canvas;

        public GpuBenchmark() {
            bitmap = Bitmap.createBitmap(1080, 1920, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);

            paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL);
        }

        private void drawNextFrame() {
            long elapsed = System.currentTimeMillis() - startTime;

            if (elapsed >= 500) {
                showGpuResult(frames);
                return;
            }

            for (int i = 0; i < 200; i++) {
                float x = (float) Math.random() * 1080;
                float y = (float) Math.random() * 1920;
                float size = (float) (20 + Math.random() * 80);

                canvas.drawCircle(x, y, size, paint);
            }

            frames++;

            findViewById(R.id.main).postDelayed(this::drawNextFrame, 0);
        }
        public void start() {
            frames = 0;
            startTime = System.currentTimeMillis();
            drawNextFrame();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        deviceModel = findViewById(R.id.deviceModel);
        androidVersion = findViewById(R.id.androidVersion);
        cpuInfo = findViewById(R.id.cpuInfo);
        ramInfo = findViewById(R.id.ramInfo);
        storageInfo = findViewById(R.id.storageInfo);
        runBenchmark = findViewById(R.id.runBenchmark);
        cpuResult = findViewById(R.id.cpuResult);
        ramResult = findViewById(R.id.ramResult);
        storageResult = findViewById(R.id.storageResult);

        deviceModel.setText("Device model: " + Build.MANUFACTURER + " " + Build.MODEL);
        androidVersion.setText("Android Version: " + Build.VERSION.RELEASE);
        cpuInfo.setText("CPU: " + Build.HARDWARE + " (" + Runtime.getRuntime().availableProcessors() + " cores)");
        ramInfo.setText("RAM: " + getTotalRAM() + " MB");
        storageInfo.setText("Storage: " + getTotalStorage() + " GB");

        runBenchmark.setOnClickListener(v -> {
            long cpuTime = runCpuBenchmarkAverage(5);
            cpuResult.setText("CPU Benchmark Result: " + cpuTime + " ms");
            cpuResult.setVisibility(View.VISIBLE);

            long ramTime = runRamBenchmarkAverage(5);
            ramResult.setText("RAM Benchmark Result: " + ramTime + " ms");
            ramResult.setVisibility(View.VISIBLE);

            long storageTime = runStorageBenchmarkAverage(5);
            storageResult.setText("Storage Benchmark Result: " + storageTime + " ms");
            storageResult.setVisibility(View.VISIBLE);

            new GpuBenchmark().start();
        });
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private long getTotalRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem / (1024 * 1024); // MB
    }

    private long getTotalStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
        return bytesAvailable / (1024 * 1024 * 1024); // GB

    }

    private long runCpuBenchmark() {
        long startTime = System.currentTimeMillis();
        long result = 0;
        for (long i = 1; i <= 50000000; i++) {
            result += i * i;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        System.out.println("CPU Benchmark result: " + result);
        return duration;
    }

    private long runCpuBenchmarkAverage(int runs) {
        long totalTime = 0;
        // I am calling (and discarding the result of) the method before the actual calculation
        // because, for some reason,
        // the first run yields a significantly different result than any of the subsequent runs,
        // leading to an inaccurate result
        runCpuBenchmark();
        for (int i = 0; i < runs; i++) {
            totalTime += runCpuBenchmark();
        }
        return totalTime / runs;
    }

    private long runRamBenchmark() {
        long startTime = System.currentTimeMillis();

        int size = 10000000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("RAM Benchmark Result sum: " + array[size - 1]);
        return duration;
    }

    private long runRamBenchmarkAverage(int runs) {
        long totalTime = 0;
        runRamBenchmark(); // same approach as for the CPU benchmark
        for (int i = 0; i < runs; i++) {
            totalTime += runRamBenchmark();
        }

        return totalTime / runs;
    }

    private long runStorageBenchmark() {
        long startTime = System.currentTimeMillis();

        String fileName = "tempBenchmarkFile.txt";
        int numLines = 1000000;
        String text = "storagebenchmarktest";

        try {
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (int i = 0; i < numLines; i++) {
                writer.write(text);
                writer.newLine();
            }
            writer.close();

            FileInputStream fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while (reader.readLine() != null) {}
            reader.close();

            deleteFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long runStorageBenchmarkAverage(int runs) {
        long totalTime = 0;

        runStorageBenchmark(); // same approach

        for (int i = 0; i < runs; i++) {
            totalTime += runStorageBenchmark();
        }

        return totalTime / runs;
    }

    private void showGpuResult(int frames) {
        TextView tv = findViewById(R.id.gpuResult);
        tv.setText("GPU Benchmark: " + frames + " drawn frames");
        tv.setVisibility(View.VISIBLE);
    }
}
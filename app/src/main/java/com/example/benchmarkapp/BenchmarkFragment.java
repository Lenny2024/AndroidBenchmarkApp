package com.example.benchmarkapp;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class BenchmarkFragment extends Fragment {

    TextView deviceModel, androidVersion, cpuInfo, ramInfo, storageInfo;
    TextView cpuResult, ramResult, storageResult, gpuResult;
    Button runBenchmark;

    public BenchmarkFragment() {
        // Required empty public constructor
    }

    // --- GPU Benchmark Inner Class ---
    private class GpuBenchmark {
        private long startTime;
        private int frames;
        private Paint paint;
        private Bitmap bitmap;
        private Canvas canvas;
        private final View viewRef; // Reference to the view for posting updates

        public GpuBenchmark(View view) {
            this.viewRef = view;
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

            // Use the view reference to post the next frame
            if (viewRef != null) {
                viewRef.postDelayed(this::drawNextFrame, 0);
            }
        }

        public void start() {
            frames = 0;
            startTime = System.currentTimeMillis();
            drawNextFrame();
        }
    }
    // ---------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_benchmark, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bind UI elements
        deviceModel = view.findViewById(R.id.deviceModel);
        androidVersion = view.findViewById(R.id.androidVersion);
        cpuInfo = view.findViewById(R.id.cpuInfo);
        ramInfo = view.findViewById(R.id.ramInfo);
        storageInfo = view.findViewById(R.id.storageInfo);
        runBenchmark = view.findViewById(R.id.runBenchmark);
        cpuResult = view.findViewById(R.id.cpuResult);
        ramResult = view.findViewById(R.id.ramResult);
        storageResult = view.findViewById(R.id.storageResult);
        gpuResult = view.findViewById(R.id.gpuResult); // Bind the new GPU TextView

        // Set Info Text
        deviceModel.setText("Device model: " + Build.MANUFACTURER + " " + Build.MODEL);
        androidVersion.setText("Android Version: " + Build.VERSION.RELEASE);
        cpuInfo.setText("CPU: " + Build.HARDWARE + " (" + Runtime.getRuntime().availableProcessors() + " cores)");
        ramInfo.setText("RAM: " + getTotalRAM() + " MB");
        storageInfo.setText("Storage: " + getTotalStorage() + " GB");

        // Set Button Listener
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

            // Start GPU Benchmark using the current view for handlers
            new GpuBenchmark(view).start();
        });
    }

    private void showGpuResult(int frames) {
        // Ensure we are updating the UI on the main thread (safeguard)
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                gpuResult.setText("GPU Benchmark: " + frames + " drawn frames");
                gpuResult.setVisibility(View.VISIBLE);
            });
        }
    }

    // --- Helper Methods ---

    private long getTotalRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
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
        return endTime - startTime;
    }

    private long runCpuBenchmarkAverage(int runs) {
        long totalTime = 0;
        runCpuBenchmark(); // Warm-up
        for (int i = 0; i < runs; i++) {
            totalTime += runCpuBenchmark();
        }
        return totalTime / 5;
    }

    private long runRamBenchmark() {
        long startTime = System.currentTimeMillis();
        int size = 10000000;
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = i;
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long runRamBenchmarkAverage(int runs) {
        long totalTime = 0;
        runRamBenchmark(); // Warm-up
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
            FileOutputStream fos = requireContext().openFileOutput(fileName, Context.MODE_PRIVATE);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));

            for (int i = 0; i < numLines; i++) {
                writer.write(text);
                writer.newLine();
            }
            writer.close();

            FileInputStream fis = requireContext().openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            while (reader.readLine() != null) {}
            reader.close();

            requireContext().deleteFile(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private long runStorageBenchmarkAverage(int runs) {
        long totalTime = 0;
        runStorageBenchmark(); // Warm-up
        for (int i = 0; i < runs; i++) {
            totalTime += runStorageBenchmark();
        }
        return totalTime / runs;
    }
}
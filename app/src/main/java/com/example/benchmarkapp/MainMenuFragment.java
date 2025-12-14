package com.example.benchmarkapp;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class MainMenuFragment extends Fragment {

    public MainMenuFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnShowSpecs = view.findViewById(R.id.btn_show_specs);
        Button btnOpenBenchmarks = view.findViewById(R.id.btn_open_benchmarks);

        btnShowSpecs.setOnClickListener(v -> {
            showDeviceSpecsDialog();
        });

        btnOpenBenchmarks.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new BenchmarkFragment())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void showDeviceSpecsDialog() {
        String model = Build.MANUFACTURER + " " + Build.MODEL;
        String androidVer = Build.VERSION.RELEASE;
        String cpu = Build.HARDWARE + " (" + Runtime.getRuntime().availableProcessors() + " cores)";
        String ram = getTotalRAM() + " MB";
        String storage = getTotalStorage() + " GB";

        String message = "Model: " + model + "\n\n" +
                "OS: Android " + androidVer + "\n\n" +
                "CPU: " + cpu + "\n\n" +
                "RAM: " + ram + "\n\n" +
                "Storage: " + storage;

        new AlertDialog.Builder(requireContext())
                .setTitle("System Specifications")
                .setMessage(message)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }


    private long getTotalRAM() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) requireContext().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        return mi.totalMem / (1024 * 1024);
    }

    private long getTotalStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        long bytesAvailable = stat.getBlockSizeLong() * stat.getBlockCountLong();
        return bytesAvailable / (1024 * 1024 * 1024);
    }
}
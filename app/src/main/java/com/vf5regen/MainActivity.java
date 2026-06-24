package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvSpeed, tvRegen, tvBattery, tvBrake, tvRange;
    private TextView tvModuleStatus, tvScannerLog, tvDrivingLog;
    
    private final List<String> scannerLogs = new ArrayList<>();
    private final List<String> drivingLogs = new ArrayList<>();
    private final List<Integer> foundModules = new ArrayList<>();
    private static final int MAX_LOGS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Logger.init(this);

        tvSpeed = findViewById(R.id.tv_speed);
        tvRegen = findViewById(R.id.tv_regen);
        tvBattery = findViewById(R.id.tv_battery);
        tvRange = findViewById(R.id.tv_range);
        tvBrake = findViewById(R.id.tv_brake);
        
        tvModuleStatus = findViewById(R.id.tv_module_status);
        tvScannerLog = findViewById(R.id.tv_scanner_log);
        tvDrivingLog = findViewById(R.id.tv_driving_debug_log);

        // 1. Module 7 (Canbus) - SOC, Range, Regen
        CanbusManager.getInstance(this).connect(new CanbusManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> handleCoreCanbusData(code, value));
            }
            @Override
            public void onConnectionStatus(boolean connected) {}
        });

        // 2. Module 0 (Driving) - Speed, Brake, xi nhan...
        DrivingManager.getInstance(this).connect(new DrivingManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> {
                    handleCoreDrivingData(code, value);
                    
                    // Show Log for Module 0
                    if (code != 41) { // Skip noisy Pitch
                        String entry = "D[" + code + "]: " + value;
                        drivingLogs.add(0, entry);
                        if (drivingLogs.size() > MAX_LOGS) drivingLogs.remove(drivingLogs.size() - 1);
                        
                        StringBuilder sb = new StringBuilder();
                        for (String log : drivingLogs) sb.append(log).append("\n");
                        tvDrivingLog.setText(sb.toString());
                    }
                });
            }
            @Override
            public void onConnectionStatus(boolean connected) {}
        });

        // 3. Global Scanner (Module 1-20)
        Module5Manager.getInstance(this).connect(new Module5Manager.OnGlobalDataListener() {
            @Override
            public void onDataUpdate(int moduleId, int code, int value) {
                runOnUiThread(() -> {
                    String entry = "M[" + moduleId + "] C:" + code + " = " + value;
                    Logger.log(entry);
                    
                    scannerLogs.add(0, entry);
                    if (scannerLogs.size() > MAX_LOGS) scannerLogs.remove(scannerLogs.size() - 1);
                    
                    StringBuilder sb = new StringBuilder();
                    for (String log : scannerLogs) sb.append(log).append("\n");
                    tvScannerLog.setText(sb.toString());
                });
            }

            @Override
            public void onConnectionStatus(boolean connected) {
                runOnUiThread(() -> {
                    if (!connected) tvModuleStatus.setText("Status: Connection Lost...");
                });
            }

            @Override
            public void onNewModuleDiscovered(int moduleId) {
                runOnUiThread(() -> {
                    if (!foundModules.contains(moduleId)) {
                        foundModules.add(moduleId);
                        updateModuleHeader();
                    }
                });
            }
        });
    }

    private void updateModuleHeader() {
        StringBuilder sb = new StringBuilder("Modules Found: ");
        for (int id : foundModules) sb.append(id).append(", ");
        tvModuleStatus.setText(sb.toString());
    }

    private void handleCoreCanbusData(int code, int value) {
        switch (code) {
            case CanbusManager.U_REGEN_LEVEL:
                tvRegen.setText("Regen: " + (value == 0 ? "OFF" : (value == 1 ? "LOW" : "HIGH")));
                break;
            case CanbusManager.U_BATTERY_SOC:
                tvBattery.setText("SOC: " + value + "%");
                break;
            case CanbusManager.U_RANGE:
                tvRange.setText("Range: " + value + "km");
                break;
        }
    }

    private void handleCoreDrivingData(int code, int value) {
        if (code == DrivingManager.D_SPEED) {
            tvSpeed.setText(value + " km/h");
        }
        // Potential Brake IDs in Module 0
        if (code == 114 || code == 115 || code == 139) {
            tvBrake.setText(value > 0 ? "BRAKING (" + code + ")" : "RELEASED");
            tvBrake.setTextColor(value > 0 ? Color.RED : Color.GREEN);
        }
    }
}

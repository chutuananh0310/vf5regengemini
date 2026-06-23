package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvSpeed, tvRegen, tvBattery, tvBrake, tvRange;
    private TextView tvCanbusStatus, tvCanbusDebugLog;
    private TextView tvDrivingStatus, tvDrivingDebugLog;
    private TextView tvModule5Status, tvModule5DebugLog;
    
    private final List<String> canbusLogs = new ArrayList<>();
    private final List<String> drivingLogs = new ArrayList<>();
    private final List<String> module5Logs = new ArrayList<>();
    private static final int MAX_LOGS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed = findViewById(R.id.tv_speed);
        tvRegen = findViewById(R.id.tv_regen);
        tvBattery = findViewById(R.id.tv_battery);
        tvRange = findViewById(R.id.tv_range);
        tvBrake = findViewById(R.id.tv_brake);
        
        tvCanbusStatus = findViewById(R.id.tv_canbus_status);
        tvCanbusDebugLog = findViewById(R.id.tv_canbus_debug_log);
        
        tvDrivingStatus = findViewById(R.id.tv_driving_status);
        tvDrivingDebugLog = findViewById(R.id.tv_driving_debug_log);

        tvModule5Status = findViewById(R.id.tv_module5_status);
        tvModule5DebugLog = findViewById(R.id.tv_module5_debug_log);

        // Connect to Module 7 (Canbus)
        CanbusManager.getInstance(this).connect(new CanbusManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> handleCanbusData(code, value));
            }

            @Override
            public void onConnectionStatus(boolean connected) {
                runOnUiThread(() -> {
                    tvCanbusStatus.setText("Canbus: " + (connected ? "Connected" : "Disconnected"));
                    tvCanbusStatus.setTextColor(connected ? Color.GREEN : Color.RED);
                });
            }
        });

        // Connect to Module 0 (Driving/Main)
        DrivingManager.getInstance(this).connect(new DrivingManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> handleDrivingData(code, value));
            }

            @Override
            public void onConnectionStatus(boolean connected) {
                runOnUiThread(() -> {
                    tvDrivingStatus.setText("Driving: " + (connected ? "Connected" : "Disconnected"));
                    tvDrivingStatus.setTextColor(connected ? Color.GREEN : Color.RED);
                });
            }
        });

        // Connect to Module 5
        Module5Manager.getInstance(this).connect(new Module5Manager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> handleModule5Data(code, value));
            }

            @Override
            public void onConnectionStatus(boolean connected) {
                runOnUiThread(() -> {
                    tvModule5Status.setText("Module 5: " + (connected ? "Connected" : "Disconnected"));
                    tvModule5Status.setTextColor(connected ? Color.GREEN : Color.RED);
                });
            }
        });
    }

    private void handleCanbusData(int code, int value) {
        String logEntry = "C[" + code + "]: " + value;
        if (code == CanbusManager.U_TIME_TO_FULL) {
            int h = value / 60;
            int m = value % 60;
            logEntry += " (" + h + "h " + m + "m)";
        } else if (code == CanbusManager.U_CHARGING_STATUS) {
            logEntry += value == 1 ? " (CHARGING)" : " (DISCONNECTED)";
        }

        canbusLogs.add(0, logEntry);
        if (canbusLogs.size() > MAX_LOGS) canbusLogs.remove(canbusLogs.size() - 1);
        
        StringBuilder sb = new StringBuilder();
        for (String log : canbusLogs) sb.append(log).append("\n");
        tvCanbusDebugLog.setText(sb.toString());

        switch (code) {
            case CanbusManager.U_REGEN_LEVEL:
                String mode = (value == 0) ? "OFF" : (value == 1 ? "LOW" : "HIGH");
                tvRegen.setText("Regen: " + mode);
                break;
            case CanbusManager.U_BATTERY_SOC:
                tvBattery.setText("SOC: " + value + "%");
                break;
            case CanbusManager.U_RANGE:
                tvRange.setText("Range: " + value + "km");
                break;
            case CanbusManager.U_CHARGING_STATUS:
                if (value == 1) {
                    tvBattery.setTextColor(Color.YELLOW);
                } else {
                    tvBattery.setTextColor(Color.parseColor("#03A9F4"));
                }
                break;
        }
    }

    private void handleDrivingData(int code, int value) {
        String logEntry = "D[" + code + "]: " + value;
        drivingLogs.add(0, logEntry);
        if (drivingLogs.size() > MAX_LOGS) drivingLogs.remove(drivingLogs.size() - 1);
        
        StringBuilder sb = new StringBuilder();
        for (String log : drivingLogs) sb.append(log).append("\n");
        tvDrivingDebugLog.setText(sb.toString());

        switch (code) {
            case DrivingManager.D_SPEED:
                tvSpeed.setText(value + " km/h");
                break;
            case DrivingManager.D_BRAKE:
                tvBrake.setText(value > 0 ? "BRAKING" : "RELEASED");
                tvBrake.setTextColor(value > 0 ? Color.RED : Color.GREEN);
                break;
        }
    }

    private void handleModule5Data(int code, int value) {
        String logEntry = "M5[" + code + "]: " + value;
        module5Logs.add(0, logEntry);
        if (module5Logs.size() > MAX_LOGS) module5Logs.remove(module5Logs.size() - 1);
        
        StringBuilder sb = new StringBuilder();
        for (String log : module5Logs) sb.append(log).append("\n");
        tvModule5DebugLog.setText(sb.toString());
    }
}

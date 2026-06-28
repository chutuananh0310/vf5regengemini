package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvSpeed, tvBattery, tvBrake, tvExplorerStatus, tvExplorerLog, tvDrivingLog;
    
    private final List<String> explorerLogs = new ArrayList<>();
    private final List<String> drivingLogs = new ArrayList<>();
    private final List<Integer> foundModules = new ArrayList<>();
    private static final int MAX_LOGS = 1000; // Tăng dung lượng log để hiển thị nhiều ký tự hơn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Logger.init(this);

        tvSpeed = findViewById(R.id.tv_speed);
        tvBattery = findViewById(R.id.tv_battery);
        tvBrake = findViewById(R.id.tv_brake);
        
        tvExplorerStatus = findViewById(R.id.tv_explorer_status);
        tvExplorerLog = findViewById(R.id.tv_explorer_log);
        tvDrivingLog = findViewById(R.id.tv_driving_debug_log);

        // Module 7 ngầm (Pin)
        CanbusManager.getInstance(this).connect(new CanbusManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> {
                    if (code == CanbusManager.U_BATTERY_SOC) tvBattery.setText("SOC: " + value + "%");
                });
            }
            @Override
            public void onConnectionStatus(boolean connected) {}
        });

        // Module 0 (Driving) - Hiện log cột trái
        DrivingManager.getInstance(this).connect(new DrivingManager.OnDataListener() {
            @Override
            public void onDataUpdate(int code, int value) {
                runOnUiThread(() -> {
                    if (code == DrivingManager.D_SPEED) tvSpeed.setText(value + " km/h");
                    
                    // Cập nhật phanh nghi vấn
                    if (code == 114 || code == 115 || code == 139) {
                        tvBrake.setText(value > 0 ? "BRAKING (" + code + ")" : "RELEASED");
                        tvBrake.setTextColor(value > 0 ? Color.RED : Color.GREEN);
                    }

                    // Hiển thị Log Module 0 (Cột trái)
                    if (code != 41) { // Bỏ qua Pitch gây nhiễu
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

        // Trình quét tự động Module 21-500 (Cột phải)
        Module5Manager.getInstance(this).connect(new Module5Manager.OnExplorerListener() {
            @Override
            public void onDataUpdate(int moduleId, int code, int value) {
                runOnUiThread(() -> {
                    String entry = "M[" + moduleId + "] C:" + code + " = " + value;
                    Logger.log(entry); // Ghi file toàn bộ
                    
                    explorerLogs.add(0, entry);
                    if (explorerLogs.size() > MAX_LOGS) explorerLogs.remove(explorerLogs.size() - 1);
                    
                    StringBuilder sb = new StringBuilder();
                    for (String log : explorerLogs) sb.append(log).append("\n");
                    tvExplorerLog.setText(sb.toString());
                });
            }

            @Override
            public void onServiceStatus(boolean connected) {
                runOnUiThread(() -> {
                    if (connected) tvExplorerStatus.setText("Auto-Scanning Modules 21-500...");
                    else tvExplorerStatus.setText("Service Disconnected");
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
        StringBuilder sb = new StringBuilder("Auto-Found: ");
        for (int id : foundModules) sb.append(id).append(", ");
        tvExplorerStatus.setText(sb.toString());
    }
}

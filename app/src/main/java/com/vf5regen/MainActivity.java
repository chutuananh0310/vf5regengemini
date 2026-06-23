package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CanbusManager.OnDataListener, DrivingManager.OnDataListener  {
    private TextView tvSpeed, tvRegen, tvBattery, tvBrake, tvCanbusStatus, tvCanbusDebugLog, tvDrivingStatus, tvDrivingDebugLog, tvRange;
    private List<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 1000;

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

        CanbusManager.getInstance(this).connect(this);
    }

    @Override
    public void onDataUpdate(int code, int value) {
        runOnUiThread(() -> {
            // Không log hết 1000 mã để tránh lag, chỉ log những mã thay đổi
            String logEntry = "Code: " + code + " | Value: " + value;
            logs.add(0, logEntry);
            if (logs.size() > MAX_LOGS) logs.remove(logs.size() - 1);

            StringBuilder sb = new StringBuilder();
            for (String log : logs) sb.append(log).append("\n");
            tvCanbusDebugLog.setText(sb.toString());

            tvCanbusStatus.setText("LATEST: " + code + " -> " + value);
            tvCanbusStatus.setTextSize(22);

            switch (code) {
                case CanbusManager.U_SPEED:
                    tvSpeed.setText(value + " km/h");
                    break;
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
                case CanbusManager.U_BRAKE:
                    tvBrake.setText(value > 0 ? "BRAKING" : "RELEASED");
                    tvBrake.setTextColor(value > 0 ? Color.RED : Color.GREEN);
                    break;
                default:
                    // Monitor specific codes for Speed/Brake search
                    if (code == 7) {
                        tvSpeed.setText(value + " km/h?");
                    }
                    if (code == 118) {
                        tvCanbusStatus.setText("Status: Code 118 changed to " + value);
                    }
                    break;
            }
        });
    }

    @Override
    public void onConnectionStatus(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                tvCanbusStatus.setText("Status: Connected");
                tvCanbusStatus.setTextColor(Color.GREEN);
            } else {
                tvCanbusStatus.setText("Status: Disconnected - Retrying...");
                tvCanbusStatus.setTextColor(Color.RED);
            }
        });
    }

    private void updateDebugLog(int code, int value) {
        String logEntry = "Code: " + code + " | Value: " + value;
        logs.add(0, logEntry);
        if (logs.size() > MAX_LOGS) {
            logs.remove(logs.size() - 1);
        }

        StringBuilder sb = new StringBuilder();
        for (String log : logs) {
            sb.append(log).append("\n");
        }
        tvCanbusDebugLog.setText(sb.toString());
    }
}

package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CanbusManager.OnDataListener {
    private TextView tvSpeed, tvRegen, tvBattery, tvBrake, tvStatus, tvDebugLog;
    private List<String> logs = new ArrayList<>();
    private static final int MAX_LOGS = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed = findViewById(R.id.tv_speed);
        tvRegen = findViewById(R.id.tv_regen);
        tvBattery = findViewById(R.id.tv_battery);
        tvBrake = findViewById(R.id.tv_brake);
        tvStatus = findViewById(R.id.tv_status);
        tvDebugLog = findViewById(R.id.tv_debug_log);

        CanbusManager.getInstance(this).connect(this);
    }

    @Override
    public void onDataUpdate(int code, int value) {
        runOnUiThread(() -> {
            updateDebugLog(code, value);
            tvStatus.setText("Status: Connected (Last: Code " + code + ")");

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
                case CanbusManager.U_BRAKE:
                    tvBrake.setText(value > 0 ? "BRAKING" : "RELEASED");
                    tvBrake.setTextColor(value > 0 ? Color.RED : Color.GREEN);
                    break;
            }
        });
    }

    @Override
    public void onConnectionStatus(boolean connected) {
        runOnUiThread(() -> {
            if (connected) {
                tvStatus.setText("Status: Connected");
                tvStatus.setTextColor(Color.GREEN);
            } else {
                tvStatus.setText("Status: Disconnected - Retrying...");
                tvStatus.setTextColor(Color.RED);
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
        tvDebugLog.setText(sb.toString());
    }
}

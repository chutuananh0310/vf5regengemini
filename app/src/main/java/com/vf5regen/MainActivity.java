package com.vf5regen;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements CanbusManager.OnDataListener {
    private TextView tvSpeed, tvRegen, tvBattery, tvBrake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvSpeed = findViewById(R.id.tv_speed);
        tvRegen = findViewById(R.id.tv_regen);
        tvBattery = findViewById(R.id.tv_battery);
        tvBrake = findViewById(R.id.tv_brake);

        CanbusManager.getInstance(this).connect(this);
    }

    @Override
    public void onDataUpdate(int code, int value) {
        runOnUiThread(() -> {
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
}

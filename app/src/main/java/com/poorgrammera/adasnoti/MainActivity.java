package com.poorgrammera.adasnoti;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "AdasNotiPoC";

    private TextView tvServiceStatus;
    private Button btnReflectionTest;
    private Button btnAdasMonitor;
    private Button btnSaveSettings;
    private TextView tvTitle;
    private android.view.View layoutMonitoringDashboards;
    private int devModeClickCount = 0;
    private long lastDevModeClickTime = 0;

    private CheckBox cbGearSound;
    private CheckBox cbAccSound;
    private CheckBox cbBsdSound;
    private CheckBox cbRadarSound;
    private CheckBox cbEnergyRecycleSound;
    private CheckBox cbEnergyRoadSound;
    private CheckBox cbOperationModeSound;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs = getSharedPreferences(ConfigData.PREF_NAME, Context.MODE_PRIVATE);

        tvServiceStatus = findViewById(R.id.tv_service_status);
        btnReflectionTest = findViewById(R.id.btn_reflection_test);
        btnAdasMonitor = findViewById(R.id.btn_adas_monitor);
        btnSaveSettings = findViewById(R.id.btn_save_settings);

        tvTitle = findViewById(R.id.tv_title);
        layoutMonitoringDashboards = findViewById(R.id.layout_monitoring_dashboards);

        boolean isDevMode = prefs.getBoolean(ConfigData.KEY_DEVELOPER_MODE, false);
        layoutMonitoringDashboards.setVisibility(isDevMode ? android.view.View.VISIBLE : android.view.View.GONE);

        tvTitle.setOnClickListener(v -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastDevModeClickTime > 2000) {
                devModeClickCount = 0;
            }
            lastDevModeClickTime = currentTime;
            devModeClickCount++;

            if (prefs.getBoolean(ConfigData.KEY_DEVELOPER_MODE, false)) {
                android.widget.Toast.makeText(MainActivity.this, "Chế độ nhà phát triển đã được bật.", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            if (devModeClickCount >= 5) {
                prefs.edit().putBoolean(ConfigData.KEY_DEVELOPER_MODE, true).apply();
                layoutMonitoringDashboards.setVisibility(android.view.View.VISIBLE);
                android.widget.Toast.makeText(MainActivity.this, "Đã bật chế độ nhà phát triển!", android.widget.Toast.LENGTH_SHORT).show();
                devModeClickCount = 0;
            } else {
                int remaining = 5 - devModeClickCount;
                android.widget.Toast.makeText(MainActivity.this, "Còn " + remaining + " lần chạm nữa để bật chế độ nhà phát triển.", android.widget.Toast.LENGTH_SHORT).show();
            }
        });

        cbGearSound = findViewById(R.id.cb_gear_sound);
        cbAccSound = findViewById(R.id.cb_acc_sound);
        cbBsdSound = findViewById(R.id.cb_bsd_sound);
        cbRadarSound = findViewById(R.id.cb_radar_sound);
        cbEnergyRecycleSound = findViewById(R.id.cb_energy_recycle_sound);
        cbEnergyRoadSound = findViewById(R.id.cb_energy_road_sound);
        cbOperationModeSound = findViewById(R.id.cb_operation_mode_sound);

        btnReflectionTest.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReflectionTestActivity.class);
            startActivity(intent);
        });

        btnAdasMonitor.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdasDashboardActivity.class);
            startActivity(intent);
        });

        btnSaveSettings.setOnClickListener(v -> saveSettingsAndStartService());

        loadSettings();
        startServices();
    }

    private void loadSettings() {
        cbGearSound.setChecked(prefs.getBoolean(ConfigData.KEY_GEAR_SOUND, true));
        cbAccSound.setChecked(prefs.getBoolean(ConfigData.KEY_ACC_SOUND, true));
        cbBsdSound.setChecked(prefs.getBoolean(ConfigData.KEY_BSD_SOUND, true));
        cbRadarSound.setChecked(prefs.getBoolean(ConfigData.KEY_RADAR_SOUND, true));
        cbEnergyRecycleSound.setChecked(prefs.getBoolean(ConfigData.KEY_ENERGY_RECYCLE_SOUND, true));
        cbEnergyRoadSound.setChecked(prefs.getBoolean(ConfigData.KEY_ENERGY_ROAD_SOUND, true));
        cbOperationModeSound.setChecked(prefs.getBoolean(ConfigData.KEY_OPERATION_MODE_SOUND, true));
    }

    private void saveSettingsAndStartService() {
        prefs.edit()
                .putBoolean(ConfigData.KEY_GEAR_SOUND, cbGearSound.isChecked())
                .putBoolean(ConfigData.KEY_ACC_SOUND, cbAccSound.isChecked())
                .putBoolean(ConfigData.KEY_BSD_SOUND, cbBsdSound.isChecked())
                .putBoolean(ConfigData.KEY_RADAR_SOUND, cbRadarSound.isChecked())
                .putBoolean(ConfigData.KEY_ENERGY_RECYCLE_SOUND, cbEnergyRecycleSound.isChecked())
                .putBoolean(ConfigData.KEY_ENERGY_ROAD_SOUND, cbEnergyRoadSound.isChecked())
                .putBoolean(ConfigData.KEY_OPERATION_MODE_SOUND, cbOperationModeSound.isChecked())
                .apply();

        startServices();
    }

    @Override
    protected void onResume() {
        super.onResume();
        statusHandler.post(statusRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        statusHandler.removeCallbacks(statusRunnable);
    }

    private void startServices() {
        Intent bydIntent = new Intent(this, BydAutoService.class);
        startForegroundService(bydIntent);
    }

    private final android.os.Handler statusHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable statusRunnable = new Runnable() {
        @Override
        public void run() {
            updateServiceStatusUI();
            statusHandler.postDelayed(this, 2000);
        }
    };

    private void updateServiceStatusUI() {
        StringBuilder sb = new StringBuilder();
        sb.append("Trạng thái dịch vụ:\n");
        
        sb.append("• BYD Auto (Reflection): ");
        if (BydAutoService.isRunning) {
            sb.append("ĐANG CHẠY \uD83D\uDFE2\n");
        } else {
            sb.append("ĐÃ DỪNG \uD83D\uDD34\n");
        }
        
        tvServiceStatus.setText(sb.toString());
    }
}
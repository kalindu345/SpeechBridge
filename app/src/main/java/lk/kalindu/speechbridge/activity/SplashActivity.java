package lk.kalindu.speechbridge.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import lk.kalindu.speechbridge.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SHOW_PROGRESS_DELAY = 1000;
    private static final int SPLASH_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }
        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash);


        ProgressBar splashProgressBar = findViewById(R.id.splashProgressBar);


        // horizontal progress bar
        new Handler(Looper.getMainLooper()).postDelayed(() -> splashProgressBar.setVisibility(View.VISIBLE),
                SHOW_PROGRESS_DELAY);

        // Start MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            splashProgressBar.setVisibility(View.INVISIBLE);
            startActivity(new Intent(SplashActivity.this, RoleSelectionActivity.class));
            finish();
        }, SPLASH_DURATION);
    }
}


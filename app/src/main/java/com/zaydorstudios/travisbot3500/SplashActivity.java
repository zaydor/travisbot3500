package com.zaydorstudios.travisbot3500;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.zaydorstudios.travisbot3500.databinding.ActivitySplashBinding;

import java.io.File;

public class SplashActivity extends AppCompatActivity {
    public ActivitySplashBinding binding;
    private TextView webWatchText;
    private ImageView largeEyeImage;
    private TextView tagLineText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

        webWatchText = binding.WebWatchText;
        largeEyeImage = binding.LargeEyeImage;
        tagLineText = binding.TagLineText;

        webWatchText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_top_in));
        largeEyeImage.startAnimation(AnimationUtils.loadAnimation(this, android.R.anim.fade_in));
        tagLineText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_bottom_in));

        boolean isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;

        if (isDarkThemeOn) {
            webWatchText.setTextColor(getResources().getColor(R.color.pastel_green, null));
            tagLineText.setTextColor(getResources().getColor(R.color.pastel_green, null));
            largeEyeImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
        } else {
            webWatchText.setTextColor(getResources().getColor(R.color.pastel_blue, null));
            tagLineText.setTextColor(getResources().getColor(R.color.pastel_blue, null));
            largeEyeImage.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            File file = new File(getApplicationContext().getFilesDir(),"init");
            if(file.exists()) {
                // tutorial has been used before
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else{
                // first time using the app
                Intent intent = new Intent(this, TutorialActivity.class);
                startActivity(intent);
            }

            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        }, 2000);
    }
}

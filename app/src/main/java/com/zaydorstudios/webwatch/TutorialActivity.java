package com.zaydorstudios.webwatch;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.zaydorstudios.webwatch.databinding.ActivityTutorialBinding;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class TutorialActivity extends AppCompatActivity {
    public ActivityTutorialBinding binding;
    private TextView TutTitleText;
    private ImageButton LeftButton;
    private ImageButton RightButton;
    private ImageButton CloseButton;
    private ImageView CurrTutImage;
    private int tutIndex = 0;
    private final int maxTutSize = 5;
    private int[] tutorialPages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTutorialBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        boolean isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;

        TutTitleText = binding.TutTitleText;
        LeftButton = binding.LeftButton;
        RightButton = binding.RightButton;
        CloseButton = binding.CloseTutorialButton;
        CurrTutImage = binding.TutorialImage;

        LeftButton.setVisibility(View.INVISIBLE);
        tutorialPages = new int[6];

        if (isDarkThemeOn) {
            TutTitleText.setTextColor(getResources().getColor(R.color.pastel_green, null));
            LeftButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            RightButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            CloseButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            tutorialPages[0] = R.drawable.ic_tutorial_page_1_darkmode;
            tutorialPages[1] = R.drawable.ic_tutorial_page_2_darkmode_1_;
            tutorialPages[2] = R.drawable.ic_tutorial_page_3_darkmode_1_;
            tutorialPages[3] = R.drawable.ic_tutorial_page_4_darkmode_1_;
            tutorialPages[4] = R.drawable.ic_tutorial_page_5_darkmode_1_;
            tutorialPages[5] = R.drawable.ic_tutorial_page_6_darkmode;
        } else {
            TutTitleText.setTextColor(getResources().getColor(R.color.pastel_blue, null));
            LeftButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            RightButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            CloseButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            tutorialPages[0] = R.drawable.ic_tutorial_page_1_lightmode_1_;
            tutorialPages[1] = R.drawable.ic_tutorial_page_2_lightmode;
            tutorialPages[2] = R.drawable.ic_tutorial_page_3_lightmode;
            tutorialPages[3] = R.drawable.ic_tutorial_page_4_lightmode;
            tutorialPages[4] = R.drawable.ic_tutorial_page_5_lightmode;
            tutorialPages[5] = R.drawable.ic_tutorial_page_6_lightmode;
        }

        CurrTutImage.setImageResource(tutorialPages[tutIndex]);

        CloseButton.setOnClickListener(v -> {
            File file = new File(getApplicationContext().getFilesDir(),"init");
            if (!file.exists()) {
                try {
                    FileOutputStream fos = getApplicationContext().openFileOutput("init", Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
        });

        RightButton.setOnClickListener(v -> {
            tutIndex++;
            if (tutIndex == maxTutSize) {
                RightButton.setVisibility(View.INVISIBLE);
            }
            if (tutIndex > 0) {
                LeftButton.setVisibility(View.VISIBLE);
            }
            CurrTutImage.setImageResource(tutorialPages[tutIndex]);
        });

        LeftButton.setOnClickListener(v -> {
            tutIndex--;
            if (tutIndex == 0) {
                LeftButton.setVisibility(View.INVISIBLE);
            }
            if (tutIndex < maxTutSize) {
                RightButton.setVisibility(View.VISIBLE);
            }

            CurrTutImage.setImageResource(tutorialPages[tutIndex]);
        });
    }

    @Override
    public void onBackPressed () {

    }
}

package com.zaydorstudios.webwatch;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zaydorstudios.webwatch.databinding.ActivityLoopBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

public class LoopActivity extends AppCompatActivity {

    public int notificationId = 0;

    public ActivityLoopBinding binding;
    public static Elements OGSiteElement;
    public Elements newSiteElement;
    public static int timeInterval;
    public static int sessionLength;
    private Stack<Document> docStack = new Stack<>();
    private Stack<Elements> newSiteElementStack = new Stack<>();
    private boolean[] didSiteChange;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(3);
    private ScheduledFuture<?> beeperHandle;
    private ScheduledFuture<?> beeperHandle2;
    private ScheduledFuture<?> beeperHandle3;

    private ProgressBar progressBar;
    public boolean isDarkThemeOn;
    private RecyclerView recyclerView;
    private ImageView eyeImage;
    private TextView endText;
    private TextView titleText2;
    private TextView timeUntilText;

    TextView timerText;
    CountDownTimer countDownTimer;
    TextView sessionEndText;

    RecyclerViewAdapter adapter;

    // TODO: add a timer on this layout page when the next check will be
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoopBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        createNotificationChannel();

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

        isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;

        didSiteChange = new boolean[MainActivity.URLStack.size()];

        AnimationDrawable animationDrawable = (AnimationDrawable) binding.constraintLayout.getBackground();
        animationDrawable.setEnterFadeDuration(10);
        animationDrawable.setExitFadeDuration(5000);
        animationDrawable.start();

        AlertDialog dialog = buildAlertDialog();
        initRecyclerView();

        sessionEndText = binding.travisBotEndText;
        timerText = binding.timerText;
        progressBar = binding.progressBar;
        recyclerView = binding.RecyclerView;
        eyeImage = binding.EyeImage;
        endText = binding.travisBotEndText;
        titleText2 = binding.TitleText2;
        timeUntilText = binding.timeUntilText;

        if (isDarkThemeOn) {
 //           bottomRectangle.setImageResource(R.drawable.ic_rectangle_updated_darkmode);
        } else {
 //           bottomRectangle.setImageResource(R.drawable.ic_rectangle_updated);
        }

        updateProgressBar(99,100);

        timeInterval = MainActivity.timeInterval;
        sessionLength = MainActivity.sessionLength;

        long duration = timeInterval * 60000L;
        long totalSeconds = timeInterval * 60L;
        final long[] maxDuration = {(long) sessionLength * 60 * 60000};

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemain = (millisUntilFinished / 1000) % 60 ;
                long minutesRemain = ((millisUntilFinished / (1000*60)) % 60);
                float secRemain = (float) (millisUntilFinished / 1000.0);
                updateProgressBar(secRemain, totalSeconds);

                if (maxDuration[0] > 0) {
                    maxDuration[0] -= 1000;
                }

                long maxSecondsRemain = (maxDuration[0] / 1000) % 60 ;
                long maxMinutesRemain = ((maxDuration[0] / (1000*60)) % 60);
                long maxHoursRemain   = ((maxDuration[0] / (1000*60*60)) % 24);

                String min = String.format(Locale.ENGLISH, "%02d", minutesRemain);
                String sec = String.format(Locale.ENGLISH, "%02d", secondsRemain);

                String maxHour = String.format(Locale.ENGLISH, "%02d", maxHoursRemain);
                String maxMin = String.format(Locale.ENGLISH, "%02d", maxMinutesRemain);
                String maxSec = String.format(Locale.ENGLISH, "%02d", maxSecondsRemain);


                String timeRemain = min + ":" + sec;

                String fullText = "This Web Watch session will end in: " + maxHour + ":" + maxMin + ":" + maxSec;

                sessionEndText.setText(fullText);
                timerText.setText(timeRemain);
            }


            @Override
            public void onFinish() {
                if (maxDuration[0] < 1000) {
                    // send notification and go back to main
                    sendNotification("Web Watch Session is Over", "You need to restart your Web Watch session!", false, 0);
                    goBackToMain();
                }

                // when finish, restart timer
                this.start();
            }

        };

        String title = "Watching...";
        binding.TitleText2.setText(title);

        binding.BackButton.setOnClickListener(v -> dialog.show());
        System.out.println("time interval = " + timeInterval);
        webwatch(timeInterval);

    }

    public void webwatch(int timeInterval) {
        final Runnable beeper = () -> {
            for (int index = 0; index < MainActivity.URLStack.size(); index++) {
                newURLCheck(index);
            }
                countDownTimer.start();
        };
        final Runnable beeper2 = () -> {
            for (int index = 0; index < MainActivity.URLStack.size(); index++) {
                newSiteElementCheck(index);
            }
        };
        final Runnable beeper3 = this::compareNewSiteElement;
        beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 0, timeInterval * 60L, SECONDS);
        beeperHandle2 =
                scheduler.scheduleAtFixedRate(beeper2, 1, timeInterval * 60L, SECONDS);
        beeperHandle3 =
                scheduler.scheduleAtFixedRate(beeper3, 2, timeInterval * 60L, SECONDS);

        scheduler.schedule(() -> { beeperHandle.cancel(true); beeperHandle2.cancel(true);  beeperHandle3.cancel(true);}, 60 * 60 * 24, SECONDS);
    }

    private AlertDialog buildAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoopActivity.this);

        builder.setPositiveButton("Confirm", (dialog, id) -> goBackToMain());

        builder.setNegativeButton("Nevermind", (dialog, id) -> dialog.dismiss());

        builder.setMessage("This will end the current Web Watch session.")
                .setTitle("Are you sure?");

        return builder.create();
    }

    public void compareNewSiteElement(){
        boolean allChanged = true;

        for (int index = 0; index < newSiteElementStack.size(); index++) {
            try {
                if (!didSiteChange[index]) {
                    newSiteElement = newSiteElementStack.get(index);
                    OGSiteElement = MainActivity.siteElementArrayList.get(index);
                    if (newSiteElement.toString().equals(OGSiteElement.toString())) {
                        System.out.println("Same site elements");
                        allChanged = false;
                    } else {
                        System.out.println("Different site elements");
                        sendNotification("WEB WATCH ALERT!", "Something changed at " + MainActivity.URLStack.get(index), true, index);
                        updateSiteChangeArr(index);
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
                sendNotification("ERROR", "Oops, something went wrong :(", false, 0);
            }
        }

        newSiteElementStack.clear();
        docStack.clear();

        if (allChanged) {
            // stop timer
            stopLoop();

        }
    }


    public void sendNotification(String title, String text, boolean changeDetected, int index){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MemoBroadcast.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "WEB WATCH CHANNEL")
                .setSmallIcon(R.drawable.ic_webwatch_notif_icon)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (changeDetected) {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(MainActivity.URLStack.get(index)));
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
            builder.addAction(R.drawable.ic_timer, "Go to Website", contentIntent);
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notificationId, builder.build());
        notificationId++;

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "WEB WATCH CHANNEL";
            String description = "WEB WATCH CHANNEL FOR NOTIFS";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("WEB WATCH CHANNEL", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void newURLCheck(int index) {
        try{
            docStack.add(index, Jsoup.connect(MainActivity.URLStack.get(index)).timeout(10000).get());
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void newSiteElementCheck(int index) {
        try{
            newSiteElementStack.add(index, docStack.get(index).select("#" + MainActivity.IDStack.get(index)));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void stopLoop() {
        beeperHandle.cancel(true);
        beeperHandle2.cancel(true);
        beeperHandle3.cancel(true);
        countDownTimer.cancel();

        timerText.setText("00:00");
        endText.setText("Web Watch detected changes in every site!");
        titleText2.setText("Finished!");
        updateProgressBar(100,100);
    }

    private void goBackToMain() {
        stopLoop();

        MainActivity.returningToMain = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private void updateProgressBar(float secondsRemaining, float totalSeconds) {
        float progress = 100 * (secondsRemaining / totalSeconds);
        progressBar.setProgress((int) progress, true);
    }

    private void initRecyclerView(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = findViewById(R.id.RecyclerView);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecyclerViewAdapter(this, MainActivity.URLStack, MainActivity.IDStack, didSiteChange, isDarkThemeOn);
        recyclerView.setAdapter(adapter);

    }

    private void updateSiteChangeArr(int index) {
        didSiteChange[index] = true;
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed () {
        // toast to say "please use back button"
        Toast.makeText(getApplicationContext(),"Please use the 'Back' button",Toast.LENGTH_SHORT).show();
    }
}

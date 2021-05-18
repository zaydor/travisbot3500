package com.zaydorstudios.travisbot3500;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zaydorstudios.travisbot3500.databinding.ActivityLoopBinding;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.*;

public class LoopActivity extends AppCompatActivity {
    public int notificationId = 0;

    public ActivityLoopBinding binding;
    public static Document doc;
    public static Elements OGSiteElement;
    public Elements newSiteElement;
    public static int timeInterval;
    public static String URL;
    public static String ID;

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(3);
    private ScheduledFuture<?> beeperHandle;
    private ScheduledFuture<?> beeperHandle2;
    private ScheduledFuture<?> beeperHandle3;

    TextView timerText;
    CountDownTimer countDownTimer;
    TextView sessionEndText;


    // TODO: add a timer on this layout page when the next check will be
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoopBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        createNotificationChannel();

        AlertDialog dialog = buildAlertDialog();

        sessionEndText = binding.travisBotEndText;

        timerText = binding.timerText;

        doc = MainActivity.doc;
        timeInterval = MainActivity.timeInterval;
        URL = MainActivity.updateURL(MainActivity.URL);
        ID = MainActivity.ID;

        System.out.println("Loop built");
        System.out.println(timeInterval);

        long duration = timeInterval * 60000;
        final long[] maxDuration = {24 * 60 * 60000};

        countDownTimer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemain = (millisUntilFinished / 1000) % 60 ;
                long minutesRemain = ((millisUntilFinished / (1000*60)) % 60);

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

                String fullText = "This TravisBot3500 session will end in: " + maxHour + ":" + maxMin + ":" + maxSec;

                sessionEndText.setText(fullText);
                timerText.setText(timeRemain);
            }


            @Override
            public void onFinish() {
                if (maxDuration[0] < 1000) {
                    // send notification and go back to main
                    sendNotification("La Flame Says...", "You need to restart your TravisBot3500 session!", false);
                    goBackToMain();
                }

                // when finish, restart timer
                this.start();
            }

        };


        String response = "TravisBot3500 will check '" + URL + "' for changes on the element with ID '" + ID + "' every " + timeInterval + " minute";
        if (timeInterval > 1) {
            response += "s!";
        } else {
            response += "!";
        }
        binding.ActivityText.setText(response);

        binding.BackButton.setOnClickListener(v -> dialog.show());

        travisbot3500(timeInterval);

    }

    public void travisbot3500(int timeInterval) {
        final Runnable beeper = () -> {
                newURLCheck();
                countDownTimer.start();
        };
        final Runnable beeper2 = this::newSiteElementCheck;
        final Runnable beeper3 = this::compareNewSiteElement;
        beeperHandle =
                scheduler.scheduleAtFixedRate(beeper, 0, timeInterval * 60, SECONDS);
        beeperHandle2 =
                scheduler.scheduleAtFixedRate(beeper2, 1, timeInterval * 60, SECONDS);
        beeperHandle3 =
                scheduler.scheduleAtFixedRate(beeper3, 2, timeInterval * 60, SECONDS);

        scheduler.schedule(() -> { beeperHandle.cancel(true); beeperHandle2.cancel(true);  beeperHandle3.cancel(true);}, 60 * 60 * 24, SECONDS);
    }

    private AlertDialog buildAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(LoopActivity.this);

        builder.setPositiveButton("Confirm", (dialog, id) -> goBackToMain());

        builder.setNegativeButton("Nevermind", (dialog, id) -> dialog.dismiss());

        builder.setMessage("This will end the current TravisBot3500 session.")
                .setTitle("Are you sure?");

        return builder.create();
    }

    public void compareNewSiteElement(){

        System.out.println("New query: " + newSiteElement);
        System.out.println("Old query: " + OGSiteElement);
        try {
            if (newSiteElement.toString().equals(OGSiteElement.toString())) {
                System.out.println("Same site elements");
            } else {
                System.out.println("Different site elements");
                sendNotification("IT'S LIT!", "Something changed at " + URL, true);
            }
        } catch (Exception e) {
            System.out.println(e);
            sendNotification("ERROR", "Oops, something went wrong :(", false);
        }
    }


    public void sendNotification(String title, String text, boolean changeDetected){
        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MemoBroadcast.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "TRAVISBOT3500 CHANNEL")
                .setSmallIcon(R.mipmap.ic_travisbot)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                // Set the intent that will fire when the user taps the notification
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (changeDetected) {
            Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(URL));
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
            CharSequence name = "TRAVISBOT3500 CHANNEL";
            String description = "TRAVISBOT3500 CHANNEL FOR NOTIFS";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("TRAVISBOT3500 CHANNEL", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void newURLCheck() {
        try{
            doc = Jsoup.connect(URL).timeout(10000).get();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void newSiteElementCheck() {
        try{
            newSiteElement = doc.select("#" + ID);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void goBackToMain() {
        beeperHandle.cancel(true);
        beeperHandle2.cancel(true);
        beeperHandle3.cancel(true);
        countDownTimer.cancel();
        MainActivity.returningToMain = true;

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}

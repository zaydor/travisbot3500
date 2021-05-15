package com.example.travisbot3500;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.travisbot3500.databinding.ActivityMainBinding;

import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    public static Document doc;
    public static Elements siteElement;
    public getURL geturl;

    public static String ID;

    public static boolean isAsyncFinished = false;

    public static int timeInterval;
    public static String URL;

    public TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        responseText = binding.ResponseText;

        binding.SubmitButton.setOnClickListener(v -> {
            // TODO: Add checks to ensure values are not null
            URL = Objects.requireNonNull(binding.URLInput.getText()).toString();
            ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
            timeInterval = Integer.parseInt(Objects.requireNonNull(binding.TimeIntervalInput.getText().toString()));

            geturl = new getURL(URL, ID);
            geturl.execute();

            System.out.println("Submit button pressed!");
            setAlert();
        });
    }



    public static void setDoc(Document Doc){
        doc = Doc;
    }

    public static void setSiteElement(Elements SiteElement) {
        siteElement = SiteElement;
    }

    public void setAlert(){
        // String response;
        // response = "TravisBot3500 will check '" + URL + "' for changes on the element with ID '" + ID + "' every " + timeInterval + " minutes!";
        // binding.ResponseText.setText(response);
        Intent intent = new Intent(this, LoopActivity.class);
        startActivity(intent);
    }
}

class getURL extends AsyncTask<Void, Void, Document> {

    private final String URL;
    private Document doc;
    private final String ID;

    public getURL(String url, String id) {
        URL = url;
        ID = id;
    }

    @Override
    protected Document doInBackground(Void... params) {
        try{
            doc = Jsoup.connect(URL).timeout(10000).get();
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        MainActivity.setDoc(doc);
        return doc;
    }

    @Override
    protected void onPostExecute(Document localDoc) {
        getSiteElement getsiteelement = new getSiteElement(ID, doc);
        getsiteelement.execute();
    }
}

class getSiteElement extends AsyncTask<Void, Void, Void> {

    private final String ID;
    private final Document DOC;
    private Elements siteElement;

    public getSiteElement(String id, Document Doc) {
        ID = id;
        DOC = Doc;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            siteElement = DOC.select("#" + ID);

            if (siteElement.toString().isEmpty()) {
                System.out.println("TravisBot3500 was unable to find that ID!");
            } else {
                MainActivity.setSiteElement(siteElement);
            }
        } catch (Exception e) {
            System.out.println("Not a valid ID");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void e) {
        System.out.println(siteElement);
        MainActivity.isAsyncFinished = true;
        LoopActivity.OGSiteElement = siteElement;
    }
}
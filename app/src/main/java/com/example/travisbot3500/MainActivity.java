package com.example.travisbot3500;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.travisbot3500.databinding.ActivityMainBinding;

import java.util.Objects;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class MainActivity extends AppCompatActivity {

    public ActivityMainBinding binding;
    public static Document doc;
    public static Elements siteElement;

    public static String ID;
    public static boolean didIDChange;

    public static int timeInterval;
    public static String URL;

    public TextView responseText;
    public Button submitButton;
    public Button checkButton;
    public EditText URLText;
    public EditText IDText;
    public EditText TimeText;

    public boolean isValidURL;
    public boolean isValidID;

    public boolean isInitialURL;
    public boolean isInitialID;
    public boolean isInitialTime;

    Thread URLThread = new Thread(() -> {
        try  {
            System.out.println("URL: " + URL);
            URLCheck(URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    Thread siteElementThread = new Thread(() -> {
        try  {
            siteElementCheck(ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
    });

    MutableLiveData<Boolean> canSubmit = new MutableLiveData<>();
    MutableLiveData<Boolean> validURL = new MutableLiveData<>();
    MutableLiveData<Boolean> validID = new MutableLiveData<>();
    MutableLiveData<Boolean> validTime = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        URLText = binding.URLInput;
        IDText = binding.IDInput;
        TimeText = binding.TimeIntervalInput;

        isInitialURL = true;
        isInitialID = true;
        isInitialTime = true;

        canSubmit.setValue(false); // Initialize
        validURL.setValue(false);
        validID.setValue(false);
        validTime.setValue(false);

        ActionBar actionBar;
        actionBar = getSupportActionBar();

        // Define ColorDrawable object and parse color
        // using parseColor method
        // with color hash code as its parameter

        ColorDrawable colorDrawable;
        int nightModeFlags =
                getApplicationContext().getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_NO:
                // Night mode is not active, we're using the light theme
                colorDrawable = new ColorDrawable(Color.parseColor("#08d9d6"));
                break;
            case Configuration.UI_MODE_NIGHT_YES:
                // Night mode is active, we're using dark theme
                colorDrawable = new ColorDrawable(Color.parseColor("#e84a5f"));
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + nightModeFlags);
        }

        // Set BackgroundDrawable
        assert actionBar != null;
        actionBar.setBackgroundDrawable(colorDrawable);

        // TODO: have observers to let user know they are waiting for response from JSOUP
        canSubmit.observe(this, changedValue -> {
            //Do something with the changed value
            System.out.println("canSubmit has been changed to: " + canSubmit.getValue());
            if (canSubmit.getValue()) {
                submitButton.setEnabled(true);
                checkButton.setEnabled(false);
                checkButton.setVisibility(View.INVISIBLE);
            } else {
                submitButton.setEnabled(false);
            }
        });

        validURL.observe(this, changedValue -> {
            //Do something with the changed value
            System.out.println("validURL has been changed to: " + validURL.getValue());
            if (validURL.getValue()) {
                URL = Objects.requireNonNull(binding.URLInput.getText()).toString();
            } else {
                if (!isInitialURL) {
                    URLText.setError("Please enter a valid URL");
                }
            }
        });

        validID.observe(this, changedValue -> {
            //Do something with the changed value
            System.out.println("validID has been changed to: " + validID.getValue());

            if (validID.getValue() && validURL.getValue() && validTime.getValue()) {
                ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
                canSubmit.setValue(true);
            } else {
                canSubmit.setValue(false);
                if (!validID.getValue() && !isInitialID) {
                    IDText.setError("Pleaser enter a valid ID");
                }
            }
        });

        validTime.observe(this, changedValue -> {
            //Do something with the changed value
            System.out.println("validTime has been changed to: " + validTime.getValue());
            if (validID.getValue() && validURL.getValue() && validTime.getValue()) {
                timeInterval = Integer.parseInt(Objects.requireNonNull(binding.TimeIntervalInput.getText().toString()));
                canSubmit.setValue(true);
            } else {
                canSubmit.setValue(false);
                if (!validTime.getValue() && !isInitialTime) {
                    TimeText.setError("Pleaser enter a value greater than 0");
                }
            }
        });


        responseText = binding.ResponseText;
        checkButton = binding.CheckButton;
        submitButton = binding.SubmitButton;
        submitButton.setEnabled(false);
        checkButton.setVisibility(View.INVISIBLE);

        binding.URLInput.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {
                if (binding.URLInput.getText().toString().isEmpty()) {
                    // TODO: throw an error in the text box instead
                    Toast.makeText(getApplicationContext(),"Please enter a URL",Toast.LENGTH_SHORT).show();
                } else {
                    URL = Objects.requireNonNull(binding.URLInput.getText()).toString();
                    if (URLThread.getState() == Thread.State.NEW) {
                        URLThread.start();
                    } else {
                        URLThread.run();
                    }
                }
            }
        });

        binding.TimeIntervalInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                isInitialTime = false;
                if (s.length() > 0) {
                    int valueOfS = Integer.parseInt(Objects.requireNonNull(s.toString()));
                    if (valueOfS > 0) {
                        validTime.setValue(true);
                    } else {
                        validTime.setValue(false);
                    }
                } else {
                    validTime.setValue(false);
                }

            }
        });

        binding.URLInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                binding.IDInput.setText("");
                if (canSubmit.getValue()) {
                    canSubmit.setValue(false);
                    validURL.setValue(false);
                }
            }
        });

        binding.IDInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                didIDChange = true;
                if (canSubmit.getValue()) {
                    canSubmit.setValue(false);
                    validID.setValue(false);
                    checkButton.setVisibility(View.VISIBLE);
                    checkButton.setEnabled(true);
                }

            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.IDInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (binding.IDInput.getText().toString().isEmpty()) {
                    // TODO: throw an error in the text box instead
                    Toast.makeText(getApplicationContext(),"Please enter an ID",Toast.LENGTH_SHORT).show();
                } else {
                    ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
                    if (siteElementThread.getState() == Thread.State.NEW) {
                        siteElementThread.start();
                    } else {
                        siteElementThread.run();
                    }
                }
            }
        });

        binding.SubmitButton.setOnClickListener(v -> {
            LoopActivity.OGSiteElement = siteElement;
            System.out.println("Submit button pressed!");
            setAlert();
        });

        checkButton.setOnClickListener(v -> {
            ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
            if (siteElementThread.getState() == Thread.State.NEW) {
                siteElementThread.start();
            } else {
                siteElementThread.run();
            }
        });
    }

    public void setAlert(){
        Intent intent = new Intent(this, LoopActivity.class);
        startActivity(intent);
    }

    private void URLCheck(String url) {
        isInitialURL = false;
        try{
            doc = Jsoup.connect(url).timeout(10000).get();
            isValidURL = true;
            System.out.println("url check");
        } catch (Exception e) {
            System.out.println(e.toString());
            isValidURL = false;
            canSubmit.postValue(false);
        }

        if (isValidURL) {
            validURL.postValue(true);
        } else {
            validURL.postValue(false);
        }
    }

    private void siteElementCheck(String id) {
        isInitialID = false;
        try{
            siteElement = doc.select("#" + id);
            if (siteElement.isEmpty()) {
                isValidID = false;
                canSubmit.postValue(false);
            } else {
                isValidID = true;
                didIDChange = false;
                System.out.println("site element check: " + siteElement);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
            isValidID = false;
            canSubmit.postValue(false);
        }

        if (isValidID) {
            validID.postValue(true);
        } else {
            validID.postValue(false);
        }
    }
}
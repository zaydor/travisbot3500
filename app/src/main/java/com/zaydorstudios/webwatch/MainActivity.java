package com.zaydorstudios.webwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.MutableLiveData;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.zaydorstudios.webwatch.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class MainActivity extends AppCompatActivity {

    // TODO: Add comments
    // TODO: Split code into functions
    // TODO: Clean up form validation

    public ActivityMainBinding binding;
    public static Document doc;
    public static Elements siteElement;

    public static String ID;
    public static boolean didIDChange;

    public static int timeInterval;
    public static String URL;

    public static Stack<String> IDStack = new Stack<>();
    public static Stack<String> URLStack = new Stack<>();
    public static Stack<Elements> siteElementArrayList = new Stack<>();

    public TextView QueryListText;
    public Button submitButton;
    public Button checkButton;
    public EditText URLText;
    public EditText IDText;
    public EditText TimeText;
    public ImageButton HistoryButton;
    public ImageButton AddAnotherURLAndIDButton;
    public ImageButton CancelQueryButton;
    public ImageView Rectangle;
    public ImageView Blob;
    public TextView TitleText;
    public ImageButton MenuButton;

    public boolean isCancellingQuery = false;

    public boolean isValidURL;
    public boolean isValidID;

    public boolean isInitialURL;
    public boolean isInitialID;
    public boolean isInitialTime;
    public static boolean returningToMain = false;
    public boolean insertingFromHistoryMenu = false;

    public static boolean historyFileCreated = false;

    Thread URLThread = new Thread(() -> {
        try  {
            System.out.println("URL: " + URL);
            String updatedURL = updateURL(URL);
            URLCheck(updatedURL);
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

    MutableLiveData<Boolean> urlThreadFinished = new MutableLiveData<>();
    MutableLiveData<Boolean> siteElementThreadFinished = new MutableLiveData<>();
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

        View decorView = getWindow().getDecorView();
// Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
// Remember that you should never show the action bar if the
// status bar is hidden, so hide that too if necessary.

        boolean isDarkThemeOn = (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)  == Configuration.UI_MODE_NIGHT_YES;


        URLText = binding.URLInput;
        IDText = binding.IDInput;
        TimeText = binding.TimeIntervalInput;
        HistoryButton = binding.HistoryButton;
        AddAnotherURLAndIDButton = binding.AddAnotherURLAndIDButton;
        CancelQueryButton = binding.CancelQueryButton;
        QueryListText = binding.QueryList;
        Rectangle = binding.Rectangle;
        TitleText = binding.TitleText;
        Blob = binding.Blob;
        MenuButton = binding.MenuButton;

        AlertDialog dialog = buildAlertDialog();
        AlertDialog tutorialDialog = buildTutorialDialog();

        AddAnotherURLAndIDButton.setVisibility(View.INVISIBLE);
        CancelQueryButton.setVisibility(View.INVISIBLE);
        QueryListText.setMovementMethod(new ScrollingMovementMethod());
        QueryListText.setVisibility(View.INVISIBLE);

        if (isDarkThemeOn) {
            System.out.println("night mode is on");
            Rectangle.setImageResource(R.drawable.ic_rectangle_updated_darkmode);
            TitleText.setTextColor(getResources().getColor(R.color.pastel_green, null));
            HistoryButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            MenuButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            AddAnotherURLAndIDButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
            CancelQueryButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_green, null)));
        } else {
            System.out.println("night mode is off");
            Rectangle.setImageResource(R.drawable.ic_rectangle_updated);
            Blob.setImageResource(R.drawable.ic_blob_updated_lightmode);
            TitleText.setTextColor(getResources().getColor(R.color.pastel_blue, null));
            HistoryButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            MenuButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            AddAnotherURLAndIDButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
            CancelQueryButton.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.pastel_blue, null)));
        }

        try {
            getHistoryFileContents();
            historyFileCreated = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (historyFileCreated) {
            HistoryButton.setVisibility(View.VISIBLE);
        } else {
            HistoryButton.setVisibility(View.INVISIBLE);
        }

        if (!returningToMain) {
            isInitialURL = true;
            isInitialID = true;
            isInitialTime = true;

            canSubmit.setValue(false); // Initialize
            validURL.setValue(false);
            validID.setValue(false);
            validTime.setValue(false);
        } else {
            canSubmit.setValue(true); // Initialize
            validURL.setValue(true);
            validID.setValue(true);
            validTime.setValue(true);
            isInitialURL = false;
            isInitialID = false;
            isInitialTime = false;
            URLText.setText(URLStack.pop());
            IDText.setText(IDStack.pop());
            siteElementArrayList.pop();
            if (URLStack.isEmpty()){
                CancelQueryButton.setVisibility(View.INVISIBLE);
            } else {
                QueryListText.setVisibility(View.VISIBLE);
                setQueryListText();
                CancelQueryButton.setVisibility(View.VISIBLE);
            }
            AddAnotherURLAndIDButton.setVisibility(View.VISIBLE);
            String timeInt = LoopActivity.timeInterval + "";
            TimeText.setText(timeInt);
        }

        // TODO: have observers to let user know they are waiting for response from JSOUP
        urlThreadFinished.observe(this, changedValue -> {
            // if URL thread is finished and there is still more docs to get
        });

        siteElementThreadFinished.observe(this, changedValue -> {
            // if siteElement thread is finished and there is still more siteElements to get
        });

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
            if (insertingFromHistoryMenu) {
                insertingFromHistoryMenu = false;
                ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
                if (siteElementThread.getState() == Thread.State.NEW) {
                    siteElementThread.start();
                } else {
                    siteElementThread.run();
                }
            }
            if (validURL.getValue()) {
                URL = Objects.requireNonNull(binding.URLInput.getText()).toString();
                /*
                Need to add valid URL, ID, and SiteElements to respective array list
                Then we can increment InputIndex -- might not even need InputIndex.

                 */

            } else {
                AddAnotherURLAndIDButton.setVisibility(View.INVISIBLE);
                if (!isInitialURL && !isCancellingQuery) {
                    URLText.setError("Please enter a valid URL");
                }
            }
        });

        validID.observe(this, changedValue -> {
            //Do something with the changed value
            System.out.println("validID has been changed to: " + validID.getValue());

            if (validID.getValue()) {
                ID = Objects.requireNonNull(binding.IDInput.getText()).toString();
                AddAnotherURLAndIDButton.setVisibility(View.VISIBLE);
            }

            if (validID.getValue() && validTime.getValue()) {
                canSubmit.setValue(true);
            } else {
                canSubmit.setValue(false);
                if (!validID.getValue() && !isInitialID && !isCancellingQuery) {
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
                    TimeText.setError("Pleaser enter a value greater than 0 and less than or equal to 60");
                }
            }
        });

        checkButton = binding.CheckIDButton;
        submitButton = binding.SubmitButton;
        checkButton.setVisibility(View.INVISIBLE);

        binding.URLInput.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {
                if (binding.URLInput.getText().toString().isEmpty()) {
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                isInitialTime = false;
                if (s.length() > 0) {
                    int valueOfS = Integer.parseInt(Objects.requireNonNull(s.toString()));
                    if (valueOfS > 0 && valueOfS <= 60) {
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
                if (!isCancellingQuery) {
                    binding.IDInput.setText("");
                }

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
                if(!isCancellingQuery) {
                    if (canSubmit.getValue()) {
                        canSubmit.setValue(false);
                        validID.setValue(false);
                        checkButton.setVisibility(View.VISIBLE);
                        checkButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        binding.IDInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (binding.IDInput.getText().toString().isEmpty()) {
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
            URLStack.push(updateURL(URL));
            IDStack.push(ID);
            try {
                for (int index = 0; index < URLStack.size(); index++) {
                    setDataInHistoryFile(updateURL(URLStack.get(index)), IDStack.get(index));
                }

            } catch (JSONException | FileNotFoundException e) {
                e.printStackTrace();
            }
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

        MenuButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(MainActivity.this, v);
            menu.getMenu().add(0, 0, 0, "Tutorial");
            menu.getMenu().add(0, 1, 1, "App Info");
            menu.show();

            menu.setOnMenuItemClickListener(item -> {
                int i = item.getItemId();
                if (i == 0) {
                    // open tutorial activity
                    tutorialDialog.show();
                } else if (i == 1) {
                    // open app info dialog
                    dialog.show();
                } else {
                    // show premium features page
                }
                return true;
            });
        });

        HistoryButton.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(MainActivity.this, v);
            try {
                JSONArray contents = new JSONArray(getHistoryFileContents());
                for (int i = 0; i < contents.length(); i++) {
                    JSONObject obj = (JSONObject) contents.get(i);
                    menu.getMenu().add(0, i, i, "URL: " + obj.opt("URL") + "  |   ID: " + obj.opt("ID"));
                }
                menu.getMenu().add(0, contents.length(), contents.length(), "CLEAR HISTORY");
                menu.show();

                menu.setOnMenuItemClickListener(item -> {
                    int i = item.getItemId();
                    System.out.println("item id: " + i);

                    if (i == contents.length()) {
                        deleteHistoryFile();
                        return true;
                    }
                    try {
                        JSONObject chosenObj = (JSONObject) contents.get(i);
                        isCancellingQuery = true;
                        isInitialURL = true;
                        isInitialID = true;

                        URLText.setText(Objects.requireNonNull(chosenObj.opt("URL")).toString());
                        IDText.setText(Objects.requireNonNull(chosenObj.opt("ID")).toString());

                        isCancellingQuery = false;

                        URL = Objects.requireNonNull(binding.URLInput.getText()).toString();
                        insertingFromHistoryMenu = true;
                        if (URLThread.getState() == Thread.State.NEW) {
                            URLThread.start();
                        } else {
                            URLThread.run();
                        }

                        return true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return false;
                });
                System.out.println(contents);
            } catch (FileNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        });

        AddAnotherURLAndIDButton.setOnClickListener(v -> {
            isInitialID = true;
            isInitialURL = true;
            CancelQueryButton.setVisibility(View.VISIBLE);
            IDStack.push(ID);
            URLStack.push(updateURL(URL));
            siteElementArrayList.push(siteElement);
            IDText.setText("");
            URLText.setText("");
            canSubmit.setValue(false);
            validID.setValue(false);
            validURL.setValue(false);
            QueryListText.setVisibility(View.VISIBLE);
            setQueryListText();

        });

        CancelQueryButton.setOnClickListener(v -> {
            isInitialID = false;
            isInitialURL = false;
            isCancellingQuery = true;
            ID = IDStack.pop();
            IDText.setText(ID);
            URL = URLStack.pop();
            siteElement = siteElementArrayList.pop();
            URLText.setText(URL);
            isCancellingQuery = false;
            canSubmit.setValue(true);
            validID.setValue(true);
            validURL.setValue(true);

            if (IDStack.isEmpty()) {
                CancelQueryButton.setVisibility(View.INVISIBLE);
                QueryListText.setVisibility(View.INVISIBLE);
            } else {
                setQueryListText();
            }
        });
    }

    private void goToTutorial() {
        Intent intent = new Intent(this, TutorialActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.slide_right_in, R.anim.slide_right_out);
    }

    private AlertDialog buildTutorialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setPositiveButton("Yes", (dialog, id) -> goToTutorial());
        builder.setNegativeButton("No", (dialog, id) -> dialog.dismiss());

        builder.setMessage("Would you like to view the tutorial again?").setTitle("Opening the Tutorial...");

        return builder.create();
    }

    private AlertDialog buildAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setPositiveButton("Back", (dialog, id) -> dialog.dismiss());

        PackageManager pm = getApplicationContext().getPackageManager();
        String pkgName = getApplicationContext().getPackageName();
        PackageInfo pkgInfo = null;
        try {
            pkgInfo = pm.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String ver = pkgInfo.versionName;

        builder.setMessage("Web Watch was created by Isaiah Dorado.\nComments, questions, concerns? Email me: isaiahdorado@gmail.com")
                .setTitle("Web Watch Version: " + ver);

        return builder.create();
    }

    private void setQueryListText(){
        StringBuilder QueryList = new StringBuilder("Query List\n");

        for (int index = 0; index < URLStack.size(); index++) {
            QueryList.append("\n");
            QueryList.append("URL: ").append(URLStack.get(index)).append("\n");
            QueryList.append("ID: ").append(IDStack.get(index)).append("\n");
        }

        QueryListText.setText(QueryList.toString());

    }


    public void setAlert(){
        siteElementArrayList.push(siteElement);
        Intent intent = new Intent(this, LoopActivity.class);
        startActivity(intent);

        overridePendingTransition(R.anim.slide_left_in, R.anim.slide_left_out);
    }

    public static String updateURL(String url) {
        if (url.startsWith("https://")) {
            return url;
        } else {
            return "https://" + url;
        }
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
        try{
            isInitialID = false;
            siteElement = doc.select("#" + id);
            if (siteElement.isEmpty()) {
                System.out.println("siteElement is empty");
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

    private void setDataInHistoryFile(String url, String id) throws JSONException, FileNotFoundException {
        JSONObject addressMap = new JSONObject();
        addressMap.put("URL", url);
        addressMap.put("ID", id);
        JSONArray contents;
        if (historyFileCreated) {
            contents = new JSONArray(getHistoryFileContents());
        } else {
            contents = new JSONArray();
        }
        HistoryButton.setVisibility(View.VISIBLE);

        for (int i = 0; i < contents.length(); i++) {
            System.out.println("contents: " + contents.get(i));
            if (contents.get(i).toString().equals(addressMap.toString())) {
                System.out.println("already in history");
                historyFileCreated = true;
                return;
            }
        }

        contents.put(addressMap);

        String filename = "webwatch History";
        String fileContents = contents.toString();

        try (FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)) {
            fos.write(fileContents.getBytes());

        } catch (IOException e) {
            e.printStackTrace();
        }

        historyFileCreated = true;
    }

    private String getHistoryFileContents() throws FileNotFoundException {
        String fileContents;
        FileInputStream fis = getApplicationContext().openFileInput("webwatch History");
        InputStreamReader inputStreamReader =
                new InputStreamReader(fis, StandardCharsets.UTF_8);
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
            String line = reader.readLine();
            while (line != null) {
                stringBuilder.append(line).append('\n');
                line = reader.readLine();
            }
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
            System.out.println("Error when opening raw file for reading");
        } finally {
            fileContents = stringBuilder.toString();
        }
        return fileContents;

    }

    private void deleteHistoryFile() {
        URI uri = getApplication().getFilesDir().toURI();
        File fdelete = new File(uri.getPath().concat("webwatch History"));
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + uri.getPath().concat("webwatch History"));
                HistoryButton.setVisibility(View.INVISIBLE);
                historyFileCreated = false;
            } else {
                System.out.println("file not Deleted :" + uri.getPath().concat("webwatch History"));
            }
        }
    }
}
package com.poe.dit82.poegps10;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.MalformedJsonException;
import com.toptoche.searchablespinnerlibrary.SearchableListDialog;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.poe.dit82.poegps10.MainActivity.readJSONFile;


public class ActivityCoord extends AppCompatActivity implements GpsStatus.Listener, GpsStatus.NmeaListener, LocationListener {

    private static final int REQUEST_CODE_PERMISSION_READ_CONTACTS = 102;
    private List<String>originalData = null;
    private List<String>filteredData = null;


    EditText oprNameText;
    EditText provodNameText;
    String oprName;
    String provodName;
    TextView lat;
    TextView lng;
    TableLayout tableLayout;
    double latCoord;
    double lngCoord;
    TextView enabledGPS;
    boolean onCreateOtp = false;
    boolean fromSavedTp = false;
    boolean isNewLine = true;
    private ArrayAdapter arrayAdapter;
    List<Tp> tpList = new ArrayList<>();
    List<String> tpNames = new ArrayList<>();
    String lineName, invent;
    TextView lineNameT;
    EditText firstOprT;
    int id;
    int position;
    int pid,parentId;
    int multi;
    String volt;
    boolean backPressed = false;
    boolean saved;
    LinearLayout gps;

    private SharedPreferences mMyPrefs;
    private Editor mMyEdit;

    private LocationManager manager;
    private boolean savedButtonPressed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coord);
        Bundle bundle = getIntent().getExtras();
        saved = false;

        mMyPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mMyEdit = mMyPrefs.edit();

        gps = findViewById(R.id.GPS_Layout);
        if (bundle != null) {
            isNewLine = bundle.getBoolean("isNewLine");
            onCreateOtp = bundle.getBoolean("onCreateOtp");
            oprName = bundle.getString("oprName");
            latCoord = bundle.getDouble("lat");
            lngCoord = bundle.getDouble("lng");
            provodName = bundle.getString("provod");
            lineName = bundle.getString("lineName");
            invent = bundle.getString("invent");
            pid = bundle.getInt("pid");
            parentId = bundle.getInt("pid");
            position = bundle.getInt("position");
            backPressed = bundle.getBoolean("backPressed");
           // Log.i("pid", pid + "");
        }


        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        File file = new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json");
       // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_DATA, invent +".json");
        if (file.exists()) {
         //   Log.i("pravilno", "vse ok");
            //читаем все объекта массива в джава
            String textJSON = readJSONFile(file.getAbsolutePath());
           // Log.e("textJson", textJSON);
            Line[] line = gson.fromJson(textJSON, Line[].class);
            ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));
            if (!backPressed) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getName().equals(lineName)) {
                        lineNameT = findViewById(R.id.lineName);
                        lineNameT.setText(lineName);
                        List<Mast> mast = list.get(i).getMast();
                        if (list.get(i).getMast().size() == 0 && !savedButtonPressed) {
                            savedButtonPressed = true;
                            tpList = readFromTxt();
                            createStartDialog(tpList);
                        } else if (onCreateOtp) {
                            isNewLine = false;
                            position = i;
                            createStartDialog(tpList);
                            addNewIntentRow(oprName, latCoord, lngCoord, provodName);
                        } else {
                            for (Mast coord : mast) {
                                addNewIntentRow(coord.getOpr(), coord.getLat(), coord.getLng(), coord.getWire());
                            }
                        }
                    }
                }
            } else {
               // Log.i("pravilno", "ne ok");
                for (int i = 0; i < list.size(); i++) {
                    if(list.get(i).getId() == pid) {
                        position = i;
                        lineNameT = findViewById(R.id.lineName);
                        lineNameT.setText(list.get(i).getName());
                        List<Mast> mast = list.get(i).getMast();
                        pid = list.get(i).getPid();
                        lineName = list.get(i).getName();
                        for (Mast coord : mast) {
                            addNewIntentRow(coord.getOpr(), coord.getLat(), coord.getLng(), coord.getWire());
                        }
                    }
                }
            }
        }

        lat = (TextView) findViewById(R.id.GPS_Latitude);
        lng = (TextView) findViewById(R.id.GPS_Longitude);

        enabledGPS = findViewById(R.id.GPS_Status);

        manager =
                (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Проверка наличия разрешений
            // Если нет разрешения на использование соответсвующих разркешений выполняем какие-то действия
            return;
        }
        manager.addGpsStatusListener(this);
        manager.addNmeaListener(this);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 2, 0, this);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_READ_CONTACTS:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                }
                return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Проверка наличия разрешений
            // Если нет разрешения на использование соответсвующих разркешений выполняем какие-то действия
            return;
        }
        manager.addGpsStatusListener(this);
        manager.addNmeaListener(this);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000 * 2, 0, this);
        checkEnabled();

    }


    @Override
    protected void onPause() {
        super.onPause();
        manager.removeUpdates(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!saved && isNewLine) {
            saveCoords();
        }
    }
   /* @Override
    protected void onDestroy() {
        super.onDestroy();
        saveCoords();
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_coord_menu, menu);
        // setContentView(R.layout.table_row);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.add_opr:
                addOpr();
                break;
            case R.id.add_savedopr:
                addSavedOpr();
                break;
            case R.id.add_savedopr_from_small:
                addSavedSmallOpr();
                break;
            case R.id.add_savedtp_from_small:
                addSavedTp();
                break;
            case R.id.create_gap:
                createGap();
                break;
            case android.R.id.home:
                saveCoords();
                Intent intent = new Intent(ActivityCoord.this, MainActivity.class);
                startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveCoords();
        pid = checkPid();
        if (pid>0) {
            Intent intent = new Intent(ActivityCoord.this, ActivityCoord.class);
            intent.putExtra("backPressed", true);
            intent.putExtra("pid", pid);
            intent.putExtra("isNewLine", true);
            intent.putExtra("invent", invent);
            intent.putExtra("lineName", lineName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(ActivityCoord.this, MainActivity.class);
            intent.putExtra("backPressed", false);
            startActivity(intent);
        }


      //  Intent intent = new Intent(ActivityCoord.this, MainActivity.class);
       // startActivity(intent);
        super.onBackPressed();  // optional depending on your needs
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    public void onLocationChanged(Location location) {
        latCoord = location.getLatitude();
        lngCoord = location.getLongitude();
        showLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        checkEnabled();
    }

    @Override
    public void onProviderDisabled(String provider) {
        checkEnabled();
    }


    private void showLocation(Location location) {
        TextView textAcc = (TextView) findViewById(R.id.GPS_Accuracy);

        if (location == null) {

            return;
        }
        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {

            lat.setText(formatLocationLat(location));
            lng.setText(formatLocationLng(location));
            if(location.hasAccuracy()) {
                Default.GPS_accuracy = location.getAccuracy();
                textAcc.setText(String.format("%4.1f", Default.GPS_accuracy));
            } else{
                textAcc.setText("000.0");
            }


        }
    }

    private String formatLocationLat(Location location) {
        if (location == null)
            return "";
        return String.format(
                "%1$.5f",
                location.getLatitude());
    }


    private String formatLocationLng(Location location) {
        if (location == null)
            return "";
        return String.format(
                "%1$.5f",
                location.getLongitude());
    }

    private void checkEnabled() {
        TextView textAcc = (TextView) findViewById(R.id.GPS_Accuracy);

        if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enabledGPS.setText("ON");
        } else {
            enabledGPS.setText("OFF");
            lat.setText(String.valueOf(0.0));
            lng.setText(String.valueOf(0.0));
            textAcc.setText("000.0");
        }
    }

    public List<Tp> readFromTxt() {
        List<Tp> list = new ArrayList<>();

            try {
                InputStream inputStream;

                inputStream = getResources().openRawResource(R.raw.tp);

                BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
                String lines;
                while ((lines = in.readLine()) != null) {
                    String[] words = lines.split(";");
                    Tp tp = new Tp();
                    tp.setTplnr(words[0]);
                    tp.setName(words[3]);
                    tp.setLat(Double.valueOf(words[1]));
                    tp.setLng(Double.valueOf(words[2]));
                    list.add(tp);

                }
                in.close();
                inputStream.close();


            } catch (IOException ex) {
                ex.printStackTrace();
            }

        return list;
    }

    public List<Tp> readFromTxt(String volt) {
        List<Tp> list = new ArrayList<>();

        try {
            InputStream inputStream;

                inputStream = getResources().openRawResource(R.raw.tp0_4);

            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String lines;
            while ((lines = in.readLine()) != null) {
                String[] words = lines.split(";");
                Tp tp = new Tp();
                tp.setTplnr(words[0]);
                tp.setName(words[3]);
                tp.setLat(Double.valueOf(words[1]));
                tp.setLng(Double.valueOf(words[2]));
                list.add(tp);

            }
            in.close();
            inputStream.close();


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return list;
    }

    public void createStartDialog(final List<Tp> tpList) {
        if (!onCreateOtp) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.create_from_tp, null);
            builder.setView(dialogView);
            tpNames.clear();
            for (int i = 0; i < tpList.size(); i++) {
                tpNames.add(tpList.get(i).getName());

            }

            final SearchableSpinner spinner2 = (SearchableSpinner) dialogView.findViewById(R.id.spinner2);
            // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
            final ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tpNames);
            // Определяем разметку для использования при выборе элемента
            adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Применяем адаптер к элементу spinner
            spinner2.setTitle("Виберіть ТП:");
            spinner2.setPositiveButton("Відмінити");
            spinner2.setAdapter(adapter2);
            adapter2.notifyDataSetChanged();

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();
                    spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            String selectedItemText = parent.getItemAtPosition(position).toString();
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

            Button create = dialogView.findViewById(R.id.choose);
            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = spinner2.getSelectedItemPosition();
                    addTpRow(tpList.get(position).getName(), tpList.get(position).getLat(), tpList.get(position).getLng());
                    alertDialog.cancel();
                }
            });

            Button cancel = dialogView.findViewById(R.id.cancel);
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alertDialog.cancel();
                }
            });
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView1 = inflater.inflate(R.layout.create_from_opr, null);
            builder.setCancelable(false);
            builder.setView(dialogView1);
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {

                    Intent intent = new Intent(ActivityCoord.this,
                            ActivityCoord.class);
                    intent.putExtra("onCreateOtp", false);
                    //  intent.putExtra("oprName", opr.getText().toString());
                     // intent.putExtra("lat", Double.parseDouble(lat.getText().toString()));
                    //   intent.putExtra("lng", Double.parseDouble(lng.getText().toString()));
                    //  intent.putExtra("provod", provod.getText().toString());
                    intent.putExtra("invent", invent);
                    intent.putExtra("lineName", lineName);
                    intent.putExtra("pid", id);
                    intent.putExtra("position", position);
                    //finish();
                    startActivity(intent);
                }
            });
            firstOprT = dialogView1.findViewById(R.id.lastOpr);

            final Spinner spinner = (Spinner) dialogView1.findViewById(R.id.wireSpinner);
            // Создаем адаптер ArrayAdapter с помощью массива строк и стандартной разметки элемета spinner
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, Default.wireArray);
            // Определяем разметку для использования при выборе элемента
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Применяем адаптер к элементу spinner
            spinner.setAdapter(adapter);
            int selectedPosition = mMyPrefs.getInt("selected_position", 0);
            spinner.setSelection(selectedPosition);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                    mMyEdit.putInt("selected_position", spinner.getSelectedItemPosition());
                    mMyEdit.commit();
                }
                @Override
                public void onNothingSelected(AdapterView<?> adapterView) { }
            });

            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            final Button create = dialogView1.findViewById(R.id.choose);
            create.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String firstOpr = firstOprT.getText().toString();
                    if (!firstOpr.equals("")) {
                        int position = spinner.getSelectedItemPosition();
                        lineName = "Відп. від оп. " + oprName + " до оп. " + firstOpr;
                        lineNameT.setText(lineName);
                        addRow(firstOpr, Default.wireArray[position]);
                        alertDialog.dismiss();
                        isNewLine = true;
                    } else {

                    }
                }
            });

        }

    }

    public void addOpr() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_opr, null);
        builder.setView(dialogView);

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        File file = new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json");
        Log.e("words", file.getName());
      //  File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_DATA, invent +".json");
            //читаем все объекта массива в джава
            String textJSON = readJSONFile(file.getAbsolutePath());
        Log.e("words", textJSON);
            Line[] line = gson.fromJson(textJSON, Line[].class);
            final ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));


        oprNameText = (EditText) dialogView.findViewById(R.id.oprName);
      //  provodNameText = dialogView.findViewById(R.id.provodName);

        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.wireSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, Default.wireArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);
        int selectedPosition = mMyPrefs.getInt("selected_position", 0);
        spinner.setSelection(selectedPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mMyEdit.putInt("selected_position", spinner.getSelectedItemPosition());
                mMyEdit.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        Button save = (Button) dialogView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean exists = false;
                oprName = oprNameText.getText().toString();
                for (int i=0; i<list.size(); i++) {
                    List<Mast> mast = list.get(i).getMast();
                    for (Mast coord : mast ) {
                        if (coord.getOpr().equals(oprName)) {
                            Toast.makeText(getApplicationContext(), "Опора з такою назвою вже існує", Toast.LENGTH_SHORT).show();
                            exists = true;
                            alertDialog.cancel();
                            break;
                        }
                    }
                }
                    List<Mast> rows = readTableLayout();
                    for (int i = 0; i < rows.size(); i++) {
                        if (rows.get(i).getOpr().equals(oprName)) {
                            Toast.makeText(getApplicationContext(), "Опора з такою назвою вже існує", Toast.LENGTH_SHORT).show();
                            exists = true;
                            alertDialog.cancel();
                            break;
                        }
                    }

                 if (!exists){
                    int position = spinner.getSelectedItemPosition();
                    provodName = Default.wireArray[position];
                    addRow(oprName, provodName);
                    Toast.makeText(getApplicationContext(), "Создана новая опора", Toast.LENGTH_SHORT).show();
                    alertDialog.cancel();
                }
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    public void addSavedOpr() {

        ArrayList<String> lines = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_saved_opr, null);
        builder.setView(dialogView);


        File file =  new File(getExternalFilesDir(Default.PATH_MAIN),"common.json");
       // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_MAIN, "common.json");
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
            String textJSON = readJSONFile(file.getAbsolutePath());
           // Log.i("startJSON", textJSON);

            JsonReader reader = new JsonReader(new StringReader(textJSON));
            reader.setLenient(true);
            Line[] line = gson.fromJson(reader, Line[].class);


        final ArrayList<Line>list = new ArrayList<>(Arrays.asList(line));
        for (int i =0; i<list.size();i++) {
            if (!list.get(i).getInvent().equals(invent) && !list.get(i).getName().equals(lineName) && list.get(i).getMast().size()>0)
                lines.add(list.get(i).getName());
        }

        final Spinner linespinner = (Spinner) dialogView.findViewById(R.id.lineSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, lines);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        linespinner.setAdapter(spinnerArrayAdapter);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button save = (Button) dialogView.findViewById(R.id.choose);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lineName1 = linespinner.getSelectedItem().toString();
              chooseOpr(list, lineName1);
                alertDialog.cancel();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void addSavedSmallOpr() {

        ArrayList<String> namesArray = new ArrayList<>();
        final ArrayList<Line> linesArray = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_saved_opr, null);
        builder.setView(dialogView);

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.line0_4);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String lines;
            while ((lines = in.readLine()) != null) {
                String[] words = lines.split(";");
                Line line = new Line();
                String checkLineName = words[0].replace("/","\\");
                line.setName(checkLineName);
                namesArray.add(checkLineName);
                String checkCoords = words[1].replace("/","\\");
                JsonReader reader = new JsonReader(new StringReader(checkCoords));
                GsonBuilder gsonBuilder = new GsonBuilder();
                Gson gson = gsonBuilder.create();
                Mast[] points = gson.fromJson(reader, Mast[].class);
                ArrayList<Mast> list = new ArrayList<>(Arrays.asList(points));
                line.setMast(list);
                linesArray.add(line);
            }
            in.close();
            inputStream.close();

        } catch (JsonSyntaxException | IOException ex) {
            ex.printStackTrace();
        }

        final Spinner linespinner = (Spinner) dialogView.findViewById(R.id.lineSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, namesArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        linespinner.setAdapter(spinnerArrayAdapter);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button save = (Button) dialogView.findViewById(R.id.choose);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String lineName1 = linespinner.getSelectedItem().toString();
                chooseOpr(linesArray, lineName1);
                alertDialog.cancel();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void addSavedTp() {
        onCreateOtp = false;
        tpList = readFromTxt("low");
        createStartDialog(tpList);
    }

    public void chooseOpr(ArrayList<Line> list, String lineName) {
        final ArrayList<String> oprs = new ArrayList<>();
        final ArrayList<Double> lats = new ArrayList<>();
        final ArrayList<Double> lngs = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.choose_opr, null);
        builder.setView(dialogView);

        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).getName().equals(lineName)) {
              //  Log.i("fsf", lineName);
                List<Mast> mast = list.get(j).getMast();
                for (Mast coord : mast ) {
                    oprs.add(coord.getOpr());
                    lats.add(coord.getLat());
                    lngs.add(coord.getLng());
                }
            }
        }

        final Spinner coordspinner = (Spinner) dialogView.findViewById(R.id.coordSpinner);

        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, oprs);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        coordspinner.setAdapter(spinnerArrayAdapter2);

        final Spinner wirespinner = (Spinner) dialogView.findViewById(R.id.wireSpinner);
        final ArrayAdapter<String> spinnerArrayAdapter3 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, Default.wireArray);
        spinnerArrayAdapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        wirespinner.setAdapter(spinnerArrayAdapter3);
        int selectedPosition = mMyPrefs.getInt("selected_position", 0) ;
        wirespinner.setSelection(selectedPosition);
        wirespinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mMyEdit.putInt("selected_position", wirespinner.getSelectedItemPosition());
                mMyEdit.commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button save = (Button) dialogView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              addNewIntentRow(oprs.get(coordspinner.getSelectedItemPosition()), lats.get(coordspinner.getSelectedItemPosition()),lngs.get(coordspinner.getSelectedItemPosition()),Default.wireArray[wirespinner.getSelectedItemPosition()]);
                alertDialog.cancel();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    public void addTpRow(String name, double lat, double lng) {
        if (!name.equals("")) {
            final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
            LayoutInflater inflater = LayoutInflater.from(this);

            final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
            //TextView rowName = (TextView) tr.getChildAt(1);
            //  View view = inflater.inflate(R.layout.table_row, null)
            TextView rowName = tr.findViewById(R.id.opr);
            rowName.setText(name);//.substring(0,name.indexOf(" ")));
            LinearLayout lr = tr.findViewById(R.id.textViews);
            final TextView latT = lr.findViewById(R.id.lat);
            latT.setText(String.valueOf(lat));
            //lat.setId(0);
            final TextView lngT = lr.findViewById(R.id.lng);
            lngT.setText(String.valueOf(lng));
            TextView provodName = tr.findViewById(R.id.provod);
            provodName.setText("");
            final Button take = tr.findViewById(R.id.add_opr);
            take.setEnabled(false);

            tableLayout.addView(tr);

            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            lr.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showDialog(tableLayout, tr);
                    // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            //i++;
        } else {
            Toast.makeText(getApplicationContext(), "Вы не ввели название опоры", Toast.LENGTH_SHORT).show();
        }

    }

    public void addRow(String name, String provod) {
        if (!name.equals("")) {
            final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
            LayoutInflater inflater = LayoutInflater.from(this);

            final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
            //TextView rowName = (TextView) tr.getChildAt(1);
            //  View view = inflater.inflate(R.layout.table_row, null)
            TextView rowName = tr.findViewById(R.id.opr);
            rowName.setText(name);
            LinearLayout lr = tr.findViewById(R.id.textViews);
            final TextView latT = lr.findViewById(R.id.lat);
            latT.setText("0.0");
            //lat.setId(0);
            final TextView lngT = lr.findViewById(R.id.lng);
            lngT.setText("0.0");
            TextView provodName = tr.findViewById(R.id.provod);
            provodName.setText(provod);
            final Button take = tr.findViewById(R.id.add_opr);
            take.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (latCoord!=0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        latT.setText(String.valueOf(latCoord));
                        lngT.setText(String.valueOf(lngCoord));
                        take.setEnabled(false);
                    } else if(latCoord==0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(getApplicationContext(), "Заждіть доки не знайдуться нові спутники!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Будь-ласка, ввімкніть GPS!", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            tableLayout.addView(tr);

            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            lr.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showDialog(tableLayout, tr);
                    // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            //i++;
        } else {
            Toast.makeText(getApplicationContext(), "Вы не ввели название опоры", Toast.LENGTH_SHORT).show();
        }

    }

    public void addRow(String name, String provod, int index) {
        if (!name.equals("")) {
            final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
            LayoutInflater inflater = LayoutInflater.from(this);

            final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
            //TextView rowName = (TextView) tr.getChildAt(1);
            //  View view = inflater.inflate(R.layout.table_row, null)
            TextView rowName = tr.findViewById(R.id.opr);
            rowName.setText(name);
            LinearLayout lr = tr.findViewById(R.id.textViews);
            final TextView latT = lr.findViewById(R.id.lat);
            latT.setText("0.0");
            //lat.setId(0);
            final TextView lngT = lr.findViewById(R.id.lng);
            lngT.setText("0.0");
            TextView provodName = tr.findViewById(R.id.provod);
            provodName.setText(provod);
            final Button take = tr.findViewById(R.id.add_opr);
            //  take.setId(0);
            take.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (latCoord!=0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        latT.setText(String.valueOf(latCoord));
                        lngT.setText(String.valueOf(lngCoord));
                        take.setEnabled(false);
                    } else if(latCoord==0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                        Toast.makeText(getApplicationContext(), "Заждіть доки не знайдуться нові спутники!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Будь-ласка, ввімкніть GPS!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            tableLayout.addView(tr, index);

            lr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
            lr.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    showDialog(tableLayout, tr);
                    // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            //i++;
        } else {
            Toast.makeText(getApplicationContext(), "Вы не ввели название опоры", Toast.LENGTH_SHORT).show();
        }

    }

    private void createGap() {
        final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        LayoutInflater inflater = LayoutInflater.from(this);

        final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
        //TextView rowName = (TextView) tr.getChildAt(1);
        //  View view = inflater.inflate(R.layout.table_row, null)
        TextView rowName = tr.findViewById(R.id.opr);
        rowName.setText("Розрив");
        LinearLayout lr = tr.findViewById(R.id.textViews);
        final TextView latT = lr.findViewById(R.id.lat);
        latT.setText("1.0");
        //lat.setId(0);
        final TextView lngT = lr.findViewById(R.id.lng);
        lngT.setText("1.0");
        TextView provodName = tr.findViewById(R.id.provod);
        provodName.setText("");
        final Button take = tr.findViewById(R.id.add_opr);
        take.setEnabled(false);
        tableLayout.addView(tr);

        lr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showDialog(tableLayout, tr);
                // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void createGap(TableLayout tl, TableRow tableRow) {

        final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        LayoutInflater inflater = LayoutInflater.from(this);

        final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);
        TextView rowName = tr.findViewById(R.id.opr);
        rowName.setText("Разрыв");
        LinearLayout lr = tr.findViewById(R.id.textViews);
        final TextView latT = lr.findViewById(R.id.lat);
        latT.setText("1.0");
        //lat.setId(0);
        final TextView lngT = lr.findViewById(R.id.lng);
        lngT.setText("1.0");
        TextView provodName = tr.findViewById(R.id.provod);
        provodName.setText("");
        final Button take = tr.findViewById(R.id.add_opr);
        take.setEnabled(false);
        tableLayout.addView(tr, tl.indexOfChild(tableRow) + 1);
        lr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showDialog(tableLayout, tr);
                // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void showDialog(final TableLayout tableLayout, final TableRow row) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Выберите действие");

        LayoutInflater inflater2 = this.getLayoutInflater();
        final View dialogView = inflater2.inflate(R.layout.opr_long_tap, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();
        final Button take = row.findViewById(R.id.add_opr);
        final TextView opr = row.findViewById(R.id.opr);
        final TextView lat = row.findViewById(R.id.lat);
        final TextView lng = row.findViewById(R.id.lng);
        final TextView provod = row.findViewById(R.id.provod);

        Button createNext = dialogView.findViewById(R.id.addNext);
        createNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNextOpr(tableLayout, row);
                alertDialog.cancel();
            }
        });

        Button clearCoord = dialogView.findViewById(R.id.clear_coord);
        clearCoord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            if(tableLayout.indexOfChild(row)!=0 || !lat.getText().toString().equals("1.0")) {
                lat.setText("0.0");
                lng.setText("0.0");
                take.setEnabled(true);
                take.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        lat.setText(String.valueOf(latCoord));
                        lng.setText(String.valueOf(lngCoord));
                        take.setEnabled(false);
                    }
                });
            } else
                Toast.makeText(getApplicationContext(),"Дія неможлива", Toast.LENGTH_SHORT);
            alertDialog.cancel();
            }
        });

        Button deleteOpr = dialogView.findViewById(R.id.deleteOpr);
        deleteOpr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tableLayout.removeView(row);
                saveCoords();
                alertDialog.cancel();
            }
        });

        final Button createOtp = dialogView.findViewById(R.id.createOtp);
        createOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lat.getText().toString().equals("1.0")) {
                    createOtp(row);
                } else
                    Toast.makeText(getApplicationContext(),"Дія неможлива", Toast.LENGTH_SHORT);
                alertDialog.cancel();
            }
        });

        final Button createGap = dialogView.findViewById(R.id.create_gap);
        createGap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!lat.getText().toString().equals("1.0")) {
                    createGap(tableLayout, row);
                } else
                    Toast.makeText(getApplicationContext(),"Дія неможлива", Toast.LENGTH_SHORT);
                alertDialog.cancel();
            }
        });

        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });

    }

    private void createNextOpr(final TableLayout tableLayout, final TableRow row) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_opr, null);
        builder.setView(dialogView);

        oprNameText = (EditText) dialogView.findViewById(R.id.oprName);
      //  provodNameText = dialogView.findViewById(R.id.provodName);
        final Spinner spinner = (Spinner) dialogView.findViewById(R.id.wireSpinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, Default.wireArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view

        spinner.setAdapter(spinnerArrayAdapter);
        int selectedPosition = mMyPrefs.getInt("selected_position", 0) ;
        spinner.setSelection(selectedPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                mMyEdit.putInt("selected_position", spinner.getSelectedItemPosition());
                mMyEdit.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        Button save = (Button) dialogView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                oprName = oprNameText.getText().toString();
                provodName = Default.wireArray[spinner.getSelectedItemPosition()];
              //  provodName = provodNameText.getText().toString();
                //придумать как прихерачить айдшник опоры с которой хочешь создать следующую

                int rez = tableLayout.indexOfChild(row) + 1;
              //  Log.i("RowID", rez + "");
                addRow(oprName, provodName, rez);
                Toast.makeText(getApplicationContext(), "Создана новая опора", Toast.LENGTH_SHORT).show();
                alertDialog.cancel();
            }
        });
        Button cancel = (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void createOtp(TableRow row) {
        onCreateOtp = true;
        final TextView opr = row.findViewById(R.id.opr);
        final TextView provod = row.findViewById(R.id.provod);
        final TextView lat = row.findViewById(R.id.lat);
        final TextView lng = row.findViewById(R.id.lng);
        if(Double.parseDouble(lat.getText().toString())>0.0) {
            saveCoords();
            Intent intent = new Intent(ActivityCoord.this,
                    ActivityCoord.class);
            intent.putExtra("onCreateOtp", onCreateOtp);
            intent.putExtra("oprName", opr.getText().toString());
            intent.putExtra("lat", Double.parseDouble(lat.getText().toString()));
            intent.putExtra("lng", Double.parseDouble(lng.getText().toString()));
            intent.putExtra("provod", provod.getText().toString());
            intent.putExtra("invent", invent);
            intent.putExtra("lineName", lineName);
            intent.putExtra("position", position);
            intent.putExtra("pid", id);

            //finish();
            startActivity(intent);
        } else Toast.makeText(getApplicationContext(), "Спочатку зніміть координати опори!", Toast.LENGTH_SHORT).show();

    }

    private void addNewIntentRow(String oprName, double lat, double lng, String provod) {
        final TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        LayoutInflater inflater = LayoutInflater.from(this);

        final TableRow tr = (TableRow) inflater.inflate(R.layout.table_row, null);

        TextView rowName = tr.findViewById(R.id.opr);
        rowName.setText(oprName);
        LinearLayout lr = tr.findViewById(R.id.textViews);
        final TextView latT = lr.findViewById(R.id.lat);
        latT.setText(String.valueOf(lat));
        final TextView lngT = lr.findViewById(R.id.lng);
        lngT.setText(String.valueOf(lng));
        TextView provodName = tr.findViewById(R.id.provod);
        provodName.setText(provod);
        final Button take = tr.findViewById(R.id.add_opr);
        if (tableLayout.indexOfChild(tr)==0) {
            take.setEnabled(false);
            take.setVisibility(View.INVISIBLE);
        } else {
            if(lat>0.0) {
                take.setEnabled(false);
            } else {
                take.setEnabled(true);
            }
        }

        take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (latCoord!=0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    latT.setText(String.valueOf(latCoord));
                    lngT.setText(String.valueOf(lngCoord));
                    take.setEnabled(false);
                } else if(latCoord==0.0 && manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Toast.makeText(getApplicationContext(), "Заждіть доки не знайдуться нові спутники!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Будь-ласка, ввімкніть GPS!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        tableLayout.addView(tr);
        lr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        lr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                showDialog(tableLayout, tr);
                // Toast.makeText(getApplicationContext(), "Лонг", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    public void saveCoords() {
        boolean isCreated = false;
        multi = 0;

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        File dir = new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json");
        //File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_DATA, invent +".json");
        String textJSON = readJSONFile(dir.getAbsolutePath());


        Line[] line = gson.fromJson(textJSON, Line[].class);
        ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));
        Line newLine = null;

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).getName().equals(lineName)) {
                newLine = new Line();
                newLine.setTplnr(list.get(i).getTplnr());
                newLine.setPid(list.get(i).getPid());
                pid = list.get(i).getPid();
                newLine.setName(list.get(i).getName());
                newLine.setInvent(list.get(i).getInvent());
                newLine.setId(list.get(i).getId());
                List<Mast> rows = readTableLayout();
                newLine.setMulti(multi);
                newLine.setMast(rows);
                list.remove(i);
                list.add(i, newLine);
                id = list.get(i).getId();

               // Log.i("first", gson.toJson(list.get(i)));
                isCreated = true;
            }
        }

        if (!isCreated) {
            //что это?
            for (int i = 0; i < list.size(); i++) {
                if (id < list.get(i).getId()) {
                    id = list.get(i).getId();
                }
            }

            newLine = new Line();
            id = id + 1;
            newLine.setId(id);
            newLine.setPid(pid);
            newLine.setTplnr(list.get(0).getTplnr());
            newLine.setName(lineName);
            newLine.setInvent(invent);
          //  newLines.add(lineName);
            List<Mast> rows = readTableLayout();
            newLine.setMulti(multi);
            newLine.setMast(rows);
            int listPosition = position +1;
            list.add(listPosition, newLine);
        }
    //    Log.i("PID2", pid + "");
        //list.add(newLine);
         //  Log.i("chevishlo", gson.toJson(list));
        try {
            Writer writer = new FileWriter(dir, false);
            gson.toJson(list, writer);
            // writer.write(collection);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //  readJSONFile(dir.getAbsolutePath());
        reloadCommonJson();
        saved = true;
    }

    public List<Mast> readTableLayout() {

        List<Mast> rows = new ArrayList<>();

        TableLayout l1 = (TableLayout) findViewById(R.id.tableLayout);

        for (int i = 0; i < l1.getChildCount(); i++) {
            View child = l1.getChildAt(i);

            if (child instanceof TableRow) {
                TableRow row = (TableRow) child;

                for (int x = 0; x < row.getChildCount(); x++) {
                    Mast m = new Mast();
                    LinearLayout lr = (LinearLayout) row.getChildAt(0);
                    TextView oprT = (TextView) lr.getChildAt(0); // get child index on particular row
                    String opr = oprT.getText().toString();

                    LinearLayout layout = (LinearLayout) lr.getChildAt(1);
                    TextView latT = (TextView) layout.getChildAt(0);
                    TextView lngT = (TextView) layout.getChildAt(1);
                    double lat = Double.parseDouble(latT.getText().toString());
                    double lng = Double.parseDouble(lngT.getText().toString());

                    if (opr.equals("Розрив") || opr.equals("Разрыв")) {
                        multi = 1;
                    }

                    TextView provodT = (TextView) lr.getChildAt(2);
                    String provod = provodT.getText().toString();

                    m.setOpr(opr);
                    m.setLat(lat);
                    m.setLng(lng);
                    m.setWire(provod);
                    rows.add(m);
                }
            }
        }
        return rows;
    }

    private void reloadCommonJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Collection collection = new ArrayList();

        File commonJSON = new File(getExternalFilesDir(Default.PATH_MAIN), "common.json");
       // File commonJSON = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_MAIN, "common.json");

        File[] dirs = new File(getExternalFilesDir(Default.PATH_DATA).getAbsolutePath() ).listFiles();
    //    File[] dirs = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+Default.PATH_MAIN).listFiles();

        if (null != dirs && dirs.length > 0) {
            for (int i = 0; i < dirs.length; i++) {
                // Log.i("NEW FILE", "NEWFILE");
                if (dirs[i].getAbsolutePath().replace("/","\\").contains("json")) {
                    //Log.i("how much2", dirs[i].getAbsolutePath());
                    String textJSON = readJSONFile(dirs[i].getAbsolutePath());
                    //Log.i("textJSON", textJSON);
                    Line[] line = gson.fromJson(textJSON, Line[].class);
                    for (int j = 0; j < line.length; j++) {
                        collection.add(line[j]);
                    }
                }
            }
        }
        try {
         //   Log.i("chevishlo", gson.toJson(collection));
            Writer writer = new FileWriter(commonJSON, false);
            gson.toJson(collection, writer);
            // writer.write(collection);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
       // Log.i("completeFile", gson.toJson(collection));
    }

    private int checkPid() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        String textJSON = readJSONFile(new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json").getAbsolutePath());
      //  String textJSON = readJSONFile(new File(getExternalFilesDir(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_DATA), invent +".json").getAbsolutePath());

        Line[] line = gson.fromJson(textJSON, Line[].class);
        ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(lineName)) {
                pid = list.get(i).getPid();
            }
        }
        return pid;
    }

    @Override
    public void onGpsStatusChanged(int status) {
        updateGpsStatus(status);
    }

    @Override
    public void onNmeaReceived(long l, String s) {

    }

    private void updateGpsStatus(int status) {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        switch (status) {
            case GpsStatus.GPS_EVENT_STARTED:
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                break;
        }

        GpsStatus gpsStatus = locationManager.getGpsStatus(null);
        double snrs[] = new double[gpsStatus.getMaxSatellites()];

        Iterator<GpsSatellite> sats = gpsStatus.getSatellites().iterator();
        int i = 0;
        while(sats.hasNext()) {
            GpsSatellite sat = sats.next();
            snrs[i] = sat.getSnr();
            i++;
        }
        // Чотири рівня сигналів
        int red, ora, yel, gre;
        red = ora = yel = gre = 0;
        for(double snr:snrs) {
            if(snr < 10.0) red++;
            if(10.0 <= snr && snr < 20.0) ora++;
            if(20.0 <= snr && snr < 30.0) yel++;
            if(30.0 <= snr) gre++;
        }
        Default.GPS_sat_red = red;
        Default.GPS_sat_ora = ora;
        Default.GPS_sat_yel = yel;
        Default.GPS_sat_gre = gre;

        // Червоні не виводимо, їх занадто багато > 100
        ((TextView) findViewById(R.id.GPS_SatOra)).setText(Integer.toString(Default.GPS_sat_ora));
        ((TextView) findViewById(R.id.GPS_SatYel)).setText(Integer.toString(Default.GPS_sat_yel));
        ((TextView) findViewById(R.id.GPS_SatGre)).setText(Integer.toString(Default.GPS_sat_gre));
    }
    
    
}


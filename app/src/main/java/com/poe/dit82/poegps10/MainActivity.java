package com.poe.dit82.poegps10;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayAdapter arrayAdapter;
    ListView lineList;
    EditText invent,lineName,tplnr;
  //  List<Line> list = new ArrayList<>();
    ArrayList<String> text = new ArrayList<>();
    String saveLineName,saveInvent, saveTplnr;
    boolean downloadPressed = false;
    int id;
    static Parcelable state;

    String path =  Default.PATH_DATA+"/";
    String pathMain = Default.PATH_MAIN+"/";
    private XmlSerializer xmlSerializer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        File file =  new File(getExternalFilesDir(Default.PATH_MAIN),"common.json");
       // File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS +Default.PATH_MAIN), "common.json");

        //Log.i("FILES", file.getAbsolutePath());

        if (file.exists()) { //+ !downloadPressed
         // file.delete();
            //читаем все объекта массива в джава
            String textJSON = readJSONFile(file.getAbsolutePath());
           // Log.i("startJSON", textJSON);
            JsonReader reader = new JsonReader(new StringReader(textJSON));
            reader.setLenient(true);
            Line[] line = gson.fromJson(reader, Line[].class);
            ArrayList<Line>list = new ArrayList<>(Arrays.asList(line));

            for (int i = 0; i<list.size();i++) {
                    //считываем инвентарник  и название
                    String info = list.get(i).getInvent() +" " + list.get(i).getName();
                    text.add(info);
            }

            //записываем в text
           // Log.i("true", "exists");
            downloadPressed = true;

        } /*else if (file.exists() && downloadPressed) {
            for (int i = 0; i<list.size(); i++) {
                String info = list.get(i).getInvnr() +" " + list.get(i).getName();
                text.add(info);
            }
        }*/


        lineList = findViewById(R.id.listView1);
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                text);
        lineList.setAdapter(arrayAdapter);
        if(state != null) {
            lineList.onRestoreInstanceState(state);
        }
        registerForContextMenu(lineList);



        lineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

            //    Toast.makeText(getApplicationContext(), "Клик", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ActivityCoord.class);
                String[] splitStr = text.get(position).split("\\s+",2);
                String invent = splitStr[0];
                String lineName = splitStr[1];
                intent.putExtra("invent", invent);
                intent.putExtra("isNewLine", true);
                intent.putExtra("lineName", lineName);
                intent.putExtra("pid", id);
                intent.putExtra("onCreateOtp", false);
                startActivity(intent);
            }
        });

     /*   lineList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
               // Toast.makeText(getApplicationContext(), "Лонг-Клик" + i, Toast.LENGTH_SHORT).show();
                return true;
            }
        });*/
    }

    @Override
    protected void onPause() {
        state = lineList.onSaveInstanceState();
        super.onPause();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String newText) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
              //  Log.i("changeText", "pomenyalo");
                final ArrayList<String> tempList = new ArrayList<>();

                for (String temp : text) {
                    if(temp.toLowerCase().contains(newText.toLowerCase())) {
                        tempList.add(temp);
                    }
                }
                ArrayAdapter<String>  arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1,
                        tempList);
                lineList.setAdapter(arrayAdapter);

                lineList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        //    Toast.makeText(getApplicationContext(), "Клик", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ActivityCoord.class);
                        String[] splitStr = adapterView.getAdapter().getItem(position).toString().split("\\s+",2);
                        String invent = splitStr[0];
                        String lineName = splitStr[1];
                        intent.putExtra("invent", invent);
                        intent.putExtra("lineName", lineName);
                        intent.putExtra("pid", id);
                        intent.putExtra("onCreateOtp", false);
                        startActivity(intent);
                    }
                });
                return true;
            }
        });
        return true;
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.create:
                showLongClickDialog(0);
                //Toast.makeText(getApplicationContext(), "Создать новый инвентарник", Toast.LENGTH_SHORT).show();
                break;
            case R.id.search:
                //Toast.makeText(getApplicationContext(), "Поиск инвентарника", Toast.LENGTH_SHORT).show();
                break;
            case R.id.download:
                if (downloadPressed == false) {
                   // Toast.makeText(getApplicationContext(), "Подождите, пока загрузится список...", Toast.LENGTH_SHORT);
                    readFromTxt();
                    downloadPressed = true;
                } else {
                    Toast.makeText(getApplicationContext(), "Дані вже завантажені", Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu_main, menu);
        menu.setHeaderTitle("Выберіть дію:");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

      //  lineList= findViewById(R.id.listView1);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                renameLine(info.position);
                return true;
            case R.id.deleteOtp:
                deleteOtp(info.position);
                return true;
            case R.id.createKml:
                try {
                    saveKml(info.position);
                } catch (IOException e ) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }
                return true;
            /*case R.id.delete:
                text.remove(info.position);
                arrayAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "Линия удалена", Toast.LENGTH_SHORT).show();
                return true;*/
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void renameLine(final int position) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        final Gson gson = gsonBuilder.create();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Введіть нову назву лінії:");
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.rename_line, null);
        builder.setView(dialogView);

        final EditText rename = dialogView.findViewById(R.id.newLineName);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button save= (Button) dialogView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newName = rename.getText().toString();
                String[] splitStr = text.get(position).split("\\s+",2);
                String invent = splitStr[0];
                String lineName = splitStr[1];
                File dir = new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json");
               // File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +Default.PATH_DATA, invent +".json");
                String inventJ = readJSONFile(dir.getAbsolutePath());
                Line[] line = gson.fromJson(inventJ, Line[].class);
                ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));

                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getName().equals(lineName)) {
                        list.get(i).setName(newName);
                        text.remove(position);
                        text.add(position, list.get(i).getInvent() +" "+ list.get(i).getName());
                        arrayAdapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Зміни збережені", Toast.LENGTH_SHORT).show();
                    }
                }
                try {
                    Writer writer = new FileWriter(dir, false);
                    gson.toJson(list, writer);
                    // writer.write(collection);
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                reloadCommonJson();

                alertDialog.cancel();
            }
        });

        Button cancel= (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });
    }

    private void deleteOtp(int position) {
        String[] splitStr = text.get(position).split("\\s+",2);
        String invent = splitStr[0];
        String lineName = splitStr[1];

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        File dir = new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json");
     //   File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+Default.PATH_DATA) , invent +".json");
        String textJSON = readJSONFile(dir.getAbsolutePath());
        Line[] line = gson.fromJson(textJSON, Line[].class);
        ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));

        boolean OtpotOpt = false;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(lineName)) {
                int id = list.get(i).getId();
                int pid = list.get(i).getPid();
                for (int j = 0; j<list.size();j++) {
                    if (id == list.get(j).getPid()) {
                        Toast.makeText(getApplicationContext(), "Спочатку видаліть вихідні від цієї лінії відпайки", Toast.LENGTH_SHORT).show();
                        OtpotOpt = true;
                        break;
                    }
                }
                if (OtpotOpt) {
                    break;
                }
                else if (pid != 0) {
                    text.remove(position);
                    arrayAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), list.get(i).getName() + " успішно видалена", Toast.LENGTH_SHORT).show();
                    list.remove(i);
                }
                else if (pid == 0) {
                    Toast.makeText(getApplicationContext(), "Неможливо видалити магістральну лінію", Toast.LENGTH_LONG).show();
                }
            }
        }
        try {
            Writer writer = new FileWriter(dir, false);
            gson.toJson(list, writer);
            // writer.write(collection);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadCommonJson();

    }

    private void reloadCommonJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Collection collection = new ArrayList();

        File commonJSON = new File(getExternalFilesDir(Default.PATH_MAIN), "common.json");
        //File commonJSON = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+Default.PATH_MAIN) , "common.json");

        File[] dirs = new File(getExternalFilesDir(Default.PATH_DATA).getAbsolutePath() ).listFiles();
       // File[] dirs = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+Default.PATH_MAIN).listFiles();
        if (null != dirs && dirs.length > 0) {

            for (int i = 0; i < dirs.length; i++) {
                //   Log.i("NEW FILE", "NEWFILE");
                //  Log.i("how much2" , dirs[i].getAbsolutePath());
                if (!dirs[i].getAbsolutePath().contains("kml")) {
                    String textJSON = readJSONFile(dirs[i].getAbsolutePath());
                    //   Log.i("textJSON", textJSON);
                    Line[] line = gson.fromJson(textJSON, Line[].class);
                    for (int j = 0; j < line.length; j++) {
                        collection.add(line[j]);
                    }
                }
            }
        }
        try {
            Writer writer = new FileWriter(commonJSON, false);
            gson.toJson(collection, writer);
            // writer.write(collection);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
      //  Log.i("completeFile", gson.toJson(collection));
    }

    private String showLongClickDialog(final int position) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Створити нову лінію");

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.create_invent, null);
        builder.setView(dialogView);

        invent = (EditText) dialogView.findViewById(R.id.invent);
        tplnr = (EditText) dialogView.findViewById(R.id.tplnr);
        lineName = (EditText) dialogView.findViewById(R.id.name);

        final AlertDialog alertDialog = builder.create();

        alertDialog.show();

        Button save= (Button) dialogView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveLineName = lineName.getText().toString();
                saveInvent = invent.getText().toString();
                saveTplnr = tplnr.getText().toString();
                if (position!=0)
                text.add(position, saveInvent + " " + saveLineName);
                else text.add(saveInvent + " " + saveLineName);
                arrayAdapter.notifyDataSetChanged();
                createJSONFile(saveInvent, saveTplnr, saveLineName);
                // inventNew.add(saveInvent + " " + saveInventName);
                Toast.makeText(getApplicationContext(), "Сохранило", Toast.LENGTH_SHORT).show();
                alertDialog.cancel();
            }
        });
        Button cancel= (Button) dialogView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });

        return saveInvent +" " +saveLineName;

    }

    public void readFromTxt() {

        try {
            InputStream inputStream = getResources().openRawResource(R.raw.lines);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String lines;
            while ((lines = in.readLine()) != null) {
                String[] words = lines.split(";");
                Line line = new Line();
                line.setInvent(words[0]);
                line.setTplnr(words[1]);
                line.setName(words[2]);
                text.add(words[0]+ " " + words[2]);
                createJSONFile( line.getInvent(), line.getTplnr(), line.getName());
            }
            createCommonJSON();
            arrayAdapter.notifyDataSetChanged();
            in.close();
            inputStream.close();


        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    //создается json-файл , при чтении из тхт файла
    public void createJSONFile(String invent, String tplnr, String name) {

        try {

            File dir = new File(getExternalFilesDir(path).getAbsolutePath());
          //  File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+path).getAbsolutePath());
                if (!dir.exists()) {
                  //  Log.i("создает папки", "created");
                    dir.mkdirs();
                }
            //создаю каждому инвентарнику свой файл

            //new File(invent+".json").createNewFile();
            //String names = name.replace("/", "\\");
           File invnr = new File(getExternalFilesDir(path), invent+".json");
          //  File invnr = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS +Default.PATH_MAIN), invent+ ".json");
            //записываю стартовый json в файл
            Gson gson = new Gson();
            Collection collection = new ArrayList();
                Line line = new Line();
                    line.setId(1);
                    id = line.getId();
                    line.setInvent(invent);
                    line.setTplnr(tplnr);
                    line.setName(name);
                    line.setMulti(0);
                    collection.add(line);
            Writer writer = new FileWriter(invnr);
            gson.toJson(collection, writer);
            writer.close();
         //   Log.i("createJson", gson.toJson(collection));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //создаем общий json, в котором все изменения
    public void createCommonJSON () {

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Collection collection = new ArrayList();
        File commonJSON =  new File(getExternalFilesDir(Default.PATH_MAIN),"common.json");
      //  File commonJSON = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS +Default.PATH_MAIN), "common.json");
        File[] dirs = new File(getExternalFilesDir(path).getAbsolutePath()).listFiles();
       // File[] dirs = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS+path).getAbsolutePath()).listFiles();
        if (null != dirs && dirs.length > 0) {
            for (int i = 0; i < dirs.length; i++) {

             //   Log.i("how much2" , dirs[i].getAbsolutePath());
                String textJSON = readJSONFile(dirs[i].getAbsolutePath());
             //   Log.i("textJSON2", textJSON);
               Line[] line = gson.fromJson(textJSON, Line[].class);
                collection.add(line[0]);
            }
        }
        try {
            Writer writer = new FileWriter(commonJSON, true);
            gson.toJson(collection, writer);
           // writer.write(collection);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
     //   Log.i("completeFile", gson.toJson(collection));
    }

    public static String readJSONFile(String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(fileName)));
            while ((line = in.readLine()) != null) {
                stringBuilder.append(line);
            }
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

       // Log.i("reading", stringBuilder.toString());

        return stringBuilder.toString();
    }

    public static ArrayList<Line> parseJSON(String json) {

        JsonReader reader = new JsonReader(new StringReader(json));

        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        Line[] line = gson.fromJson(reader, Line[].class);
        ArrayList<Line> list = new ArrayList<>(Arrays.asList(line));
        return list;
    }

    private void saveKml (int position) throws IOException, XmlPullParserException {

        String[] splitStr = text.get(position).split("\\s+",2);
        String invent = splitStr[0];
        String name = splitStr[1].replace("/","\\");

        String textJSON = readJSONFile(new File(getExternalFilesDir(Default.PATH_DATA), invent + ".json").getAbsolutePath());

        ArrayList<Line> list = parseJSON(textJSON);

        File dir = new File(getExternalFilesDir(Default.PATH_DATA), name + ".kml");
        FileOutputStream fileOutputStream = new FileOutputStream(dir);

        xmlSerializer = XmlPullParserFactory.newInstance().newSerializer();
        xmlSerializer.setOutput(fileOutputStream, "UTF-8");
        xmlSerializer.startDocument(null, null);
        xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        xmlSerializer.startTag(null, "kml");

        int coordsCount = 0;
        int depth = 0;
        int previoudsID = 1;

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPid() == 0) {
                xmlSerializer.startTag(null, "Document");
                xmlSerializer.startTag(null, "name");
                xmlSerializer.text(name);
                xmlSerializer.endTag(null, "name");
                xmlSerializer.startTag(null, "Style");
                xmlSerializer.startTag(null, "LineStyle");
                xmlSerializer.startTag(null, "color");
                xmlSerializer.text("A6FFFF00");
                xmlSerializer.endTag(null, "color");
                xmlSerializer.endTag(null, "LineStyle");
                xmlSerializer.endTag(null, "Style");
            }
            else {
                if(list.get(i).getPid() != previoudsID  ) {
                    xmlSerializer.endTag(null, "Folder");
                    Log.e("endFolder", "}");
                    for (int d = 1; d < 20; d++) {
                        if (list.get(i).getPid() == d) {
                            while (depth > d && depth > 1) {
                                xmlSerializer.endTag(null, "Folder");
                                depth--;
                            }

                        }
                    }
                } /*if (list.get(i).getPid() == 1 ){
                    while (depth > 1){
                        xmlSerializer.endTag(null, "Folder");
                        depth--;
                    }
                } */
                else {
                    depth++;
                }
            }

            //Log.e("ids", list.get(i).getId() +" "+ list.get(i).getPid() +" "+ depth);
            previoudsID = list.get(i).getId();



            xmlSerializer.startTag(null, "Folder");
            Log.e("startFolder", "{");
            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(list.get(i).getName());
            xmlSerializer.endTag(null, "name");
            xmlSerializer.startTag(null, "open");
            xmlSerializer.text("1");
            xmlSerializer.endTag(null, "open");
//            xmlSerializer.startTag(null, "Style");
//            xmlSerializer.startTag(null, "LineStyle");
//            xmlSerializer.startTag(null, "color");
//            xmlSerializer.text("A6FFFF00");
//            xmlSerializer.endTag(null, "color");
//            xmlSerializer.endTag(null, "LineStyle");
//            xmlSerializer.endTag(null, "Style");

            List<Mast> coords = list.get(i).getMast();

            if (list.get(i).getMulti() == 0) {
                StringBuffer mainCoordinates = new StringBuffer();

                for (int j = 0; j < coords.size(); j++) {
                    if (coords.get(j).getLng() != 0.0 && coords.get(j).getLat() != 0.0) {
                        coordsCount++;

                        xmlSerializer.startTag(null, "Placemark");
                        xmlSerializer.startTag(null, "name");
                        xmlSerializer.text(coords.get(j).getOpr());
                        xmlSerializer.endTag(null, "name");
                        xmlSerializer.startTag(null, "description");
                        xmlSerializer.text(list.get(i).getTplnr() + " WIRE::" + coords.get(j).getWire() + "::");
                        xmlSerializer.endTag(null, "description");
                        xmlSerializer.startTag(null, "Style");
                        xmlSerializer.startTag(null, "IconStyle");
                        xmlSerializer.startTag(null, "scale");
                        xmlSerializer.text("0.5");
                        xmlSerializer.endTag(null, "scale");
                        xmlSerializer.startTag(null, "heading");
                        xmlSerializer.text("0.0");
                        xmlSerializer.endTag(null, "heading");
                        xmlSerializer.startTag(null, "Icon");
                        xmlSerializer.startTag(null, "href");
                        if (coords.get(j).getOpr().toLowerCase().contains("тп") || coords.get(j).getOpr().toLowerCase().contains("рп") || coords.get(j).getOpr().toLowerCase().contains("пс")) {
                            xmlSerializer.text("files\\bigcity.png");
                        } else
                            xmlSerializer.text("files\\1.png");
                        xmlSerializer.endTag(null, "href");
                        xmlSerializer.endTag(null, "Icon");
                        xmlSerializer.endTag(null, "IconStyle");
                        xmlSerializer.startTag(null, "LabelStyle");
                        xmlSerializer.startTag(null, "scale");
                        xmlSerializer.text("0.79");
                        xmlSerializer.endTag(null, "scale");
                        xmlSerializer.endTag(null, "LabelStyle");
                        xmlSerializer.endTag(null, "Style");
                        xmlSerializer.startTag(null, "Point");
                        xmlSerializer.startTag(null, "extrude");
                        xmlSerializer.text("1");
                        xmlSerializer.endTag(null, "extrude");
                        xmlSerializer.startTag(null, "coordinates");
                        xmlSerializer.text(coords.get(j).getLng() + "," + coords.get(j).getLat());
                        mainCoordinates.append(coords.get(j).getLng() + "," + coords.get(j).getLat() + " ");
                        // Mast lngLat = new Mast(coords.get(j).getLng()+","+coords.get(j).getLat());
                        //mainCoordinates.add(lngLat);
                        xmlSerializer.endTag(null, "coordinates");
                        xmlSerializer.endTag(null, "Point");
                        xmlSerializer.endTag(null, "Placemark");
                    }
                }
                xmlSerializer.startTag(null, "Placemark");
                xmlSerializer.startTag(null, "name");
                xmlSerializer.text(list.get(i).getTplnr());
                xmlSerializer.endTag(null, "name");
                xmlSerializer.startTag(null, "description");
                xmlSerializer.text(list.get(i).getName());
                xmlSerializer.endTag(null, "description");
                xmlSerializer.startTag(null, "Style");
                xmlSerializer.startTag(null, "LineStyle");
                xmlSerializer.startTag(null, "width");
                xmlSerializer.text("2.0");
                xmlSerializer.endTag(null, "width");
                xmlSerializer.endTag(null, "LineStyle");
                xmlSerializer.endTag(null, "Style");
                xmlSerializer.startTag(null, "LineString");
                xmlSerializer.startTag(null, "extrude");
                xmlSerializer.text("1");
                xmlSerializer.endTag(null, "extrude");
                xmlSerializer.startTag(null, "coordinates");
                if (coordsCount > 1) {
                    xmlSerializer.text(mainCoordinates.toString());
                } else {
                    xmlSerializer.text(mainCoordinates.toString() + mainCoordinates.toString());
                }
                xmlSerializer.endTag(null, "coordinates");
                xmlSerializer.endTag(null, "LineString");
                xmlSerializer.endTag(null, "Placemark");
            } else {
                StringBuilder[] coordinates = new StringBuilder[10];
                int index = 0;
                coordinates[index] = new StringBuilder();
                for (int j = 0; j < coords.size(); j++) {
                    if (coords.get(j).getLng() != 0.0 && coords.get(j).getLat() != 0.0) {
                        if (coords.get(j).getLng() != 1.0 && coords.get(j).getLat() != 1.0) {
                            //coordsCount++;
                            xmlSerializer.startTag(null, "Placemark");
                            xmlSerializer.startTag(null, "name");
                            xmlSerializer.text(coords.get(j).getOpr());
                            xmlSerializer.endTag(null, "name");
                            xmlSerializer.startTag(null, "description");
                            xmlSerializer.text(list.get(i).getTplnr() + "::" + coords.get(j).getWire() + "::");
                            xmlSerializer.endTag(null, "description");
                            xmlSerializer.startTag(null, "Style");
                            xmlSerializer.startTag(null, "IconStyle");
                            xmlSerializer.startTag(null, "scale");
                            xmlSerializer.text("0.5");
                            xmlSerializer.endTag(null, "scale");
                            xmlSerializer.startTag(null, "heading");
                            xmlSerializer.text("0.0");
                            xmlSerializer.endTag(null, "heading");
                            xmlSerializer.startTag(null, "Icon");
                            xmlSerializer.startTag(null, "href");
                            xmlSerializer.text("files\\1.png");
                            xmlSerializer.endTag(null, "href");
                            xmlSerializer.endTag(null, "Icon");
                            xmlSerializer.endTag(null, "IconStyle");
                            xmlSerializer.startTag(null, "LabelStyle");
                            xmlSerializer.startTag(null, "scale");
                            xmlSerializer.text("0.79");
                            xmlSerializer.endTag(null, "scale");
                            xmlSerializer.endTag(null, "LabelStyle");
                            xmlSerializer.endTag(null, "Style");
                            xmlSerializer.startTag(null, "Point");
                            xmlSerializer.startTag(null, "extrude");
                            xmlSerializer.text("1");
                            xmlSerializer.endTag(null, "extrude");
                            xmlSerializer.startTag(null, "coordinates");
                            xmlSerializer.text(coords.get(j).getLng() + "," + coords.get(j).getLat());
                            coordinates[index].append(coords.get(j).getLng() + "," + coords.get(j).getLat() + " ");
                            xmlSerializer.endTag(null, "coordinates");
                            xmlSerializer.endTag(null, "Point");
                            xmlSerializer.endTag(null, "Placemark");
                        } else {
                            index++;
                            coordinates[index] = new StringBuilder();
                        }
                    }
                }
                for (int count = 0; count <= index; count++){
                xmlSerializer.startTag(null, "Placemark");
                xmlSerializer.startTag(null, "name");
                xmlSerializer.text(list.get(i).getTplnr()+"#"+(count+1));
                xmlSerializer.endTag(null, "name");
                xmlSerializer.startTag(null, "description");
                xmlSerializer.text(list.get(i).getName());
                xmlSerializer.endTag(null, "description");
                xmlSerializer.startTag(null, "Style");
                xmlSerializer.startTag(null, "LineStyle");
                xmlSerializer.startTag(null, "width");
                xmlSerializer.text("2.0");
                xmlSerializer.endTag(null, "width");
                xmlSerializer.endTag(null, "LineStyle");
                xmlSerializer.endTag(null, "Style");
                //xmlSerializer.startTag(null, "MultiGeometry");

                    xmlSerializer.startTag(null, "LineString");
                    xmlSerializer.startTag(null, "extrude");
                    xmlSerializer.text("1");
                    xmlSerializer.endTag(null, "extrude");
                    xmlSerializer.startTag(null, "coordinates");
                   // if (coordsCount > 1) {
                        xmlSerializer.text(coordinates[count].toString());
                   // } else {
                   //     xmlSerializer.text(coordinates[count].toString() + coordinates[count].toString());
                   // }
                    xmlSerializer.endTag(null, "coordinates");
                    xmlSerializer.endTag(null, "LineString");

                //xmlSerializer.endTag(null, "MultiGeometry");
                xmlSerializer.endTag(null, "Placemark");
                }
            }
           // if (list.get(i).getPid() != 0) {
                  //  xmlSerializer.endTag(null, "Folder");
          //  }

            if (i == list.size()-1) {
                Log.e("lastLine", "zashlo" + depth);
                for(int counter = 0; counter < depth; counter++) {//Log.e("endFolder", "}");
                    xmlSerializer.endTag(null, "Folder");
                }
                xmlSerializer.endTag(null, "Folder");
                xmlSerializer.endTag(null, "Document");
                xmlSerializer.endTag(null, "kml");
            }
        }
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        //writer.close();
        fileOutputStream.close();
    }



}

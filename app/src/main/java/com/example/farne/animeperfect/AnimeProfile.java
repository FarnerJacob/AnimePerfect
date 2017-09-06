package com.example.farne.animeperfect;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;


import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import static com.example.farne.animeperfect.Home.EXTRA_MESSAGE;

public class AnimeProfile extends AppCompatActivity implements AdapterView.OnItemSelectedListener{


    //ToDo: Make the episode button find the link onClick. I think it'll speed up the page load time.


    public String serverChoice = "STREAMANGO";
    public int episodeStart = 1;
    public int episodeEnd = 1;
    public int[] episodesWatched;
    public String episodeLink = null;
    public String title = null;
    public DbHelper db = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_profile);

        Intent intent = getIntent();
        final String link = intent.getStringExtra(EXTRA_MESSAGE);
        Thread thread = new Thread(new Runnable() {

            public void run() {

                Document doc = null;
                try {
                    doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                    Elements links = doc.getElementsByClass("main_body");
                    String image = links.first().child(1).child(0).child(0).attr("src");
                    loadThumb(image);
                    title = links.first().child(1).child(0).child(1).text();
                    printHtml(title,"title");
                    String summary = links.first().child(1).child(0).child(4).ownText();
                    printHtml(summary,"summary");
                    Elements episodeList = doc.getElementsByAttribute("ep_start");
                    episodeStart = Integer.parseInt(episodeList.first().attr("ep_start"))+1;
                    episodeEnd = Integer.parseInt(episodeList.last().attr("ep_end"));
                    episodeLink = title.replaceAll("[(+.^:,)]","");
                    episodeLink = episodeLink.replaceAll(" ","-");
                    db = new DbHelper(getBaseContext());
                    episodesWatched = db.getEpisodesWatched(title, episodeStart, episodeEnd);
                    printEpisodes(episodeStart, episodeEnd, episodeLink, episodesWatched);
                } catch (IOException e) {
                    printError(e);
                }
            }
        });
        thread.start();
    }

    public void printError(final Exception e){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView box = (TextView) findViewById(R.id.titleBox);
                box.setText(e.toString());
            }
        });
    }

    public void printHtml(final String string,final String boxName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int identifier = getResources().getIdentifier(boxName+"Box","id",getPackageName());
                TextView box = (TextView) findViewById(identifier);
                box.setMovementMethod(new ScrollingMovementMethod());
                box.setText(string.toString());
            }
        });
    }

    public void loadThumb(final String image){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                final Context context = imageView.getContext();
                Picasso.with(context).load(image).fit().into(imageView);
            }
        });
    }

    public void printEpisodes(final int episodeStart, final int episodeEnd,final String episodeLink, final int[] array){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                LinearLayout list = (LinearLayout) findViewById(R.id.episodeList);
                Context context = list.getContext();
                for (int i = episodeStart; i <= episodeEnd; i++) {

                    final Button butt = new Button(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    butt.setText("Episode " + i);
                    if(array[i-1]==1){
                        butt.setBackgroundColor(Color.parseColor("#a9a9a9"));
                    }
                    else{
                        butt.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    }
                    list.addView(butt);
                    final int index = i;
                    butt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            butt.setBackgroundColor(Color.parseColor("#a9a9a9"));
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Spinner dropDownBar = (Spinner) findViewById(R.id.spinner);
                                    Context dropContext = dropDownBar.getContext();
                                    dropDownBar.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) dropContext);
                                    serverChoice = dropDownBar.getSelectedItem().toString();
                                    String link = "https://gogoanime.io/" + episodeLink + "-episode-" + (index);
                                    Boolean available = true;
                                    try {
                                        Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                                        Elements serverLinks = null;
                                        Element resultLinks = null;
                                        Elements rawLinks = null;
                                        switch (serverChoice) {
                                            case "VIDSTREAMING":
                                                serverLinks = doc.getElementsByClass("anime");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "STREAMANGO":
                                                serverLinks = doc.getElementsByClass("streamango");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "ESTREAM":
                                                serverLinks = doc.getElementsByClass("estram");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "OLOAD":
                                                serverLinks = doc.getElementsByClass("open");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "OPENLOAD":
                                                serverLinks = doc.getElementsByClass("open");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.last().attr("data-video");
                                                }
                                                break;
                                            case "THEVIDEO":
                                                serverLinks = doc.getElementsByClass("thevideo");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "MP4UPLOAD":
                                                serverLinks = doc.getElementsByClass("mp4");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "YOURUPLOAD":
                                                serverLinks = doc.getElementsByClass("your");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                            case "BESTREAM":
                                                serverLinks = doc.getElementsByClass("bestream");
                                                if (serverLinks == null || serverLinks.isEmpty()) {
                                                    link = "null";
                                                } else {
                                                    resultLinks = serverLinks.first();
                                                    rawLinks = resultLinks.children();
                                                    link = rawLinks.first().attr("data-video");
                                                }
                                                break;
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    if(link.equals("null")){
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        Context buttContext = butt.getContext();
                                        db = new DbHelper(buttContext);
                                        db.insertRecentlyWatched(db.getNextID(), title, index);
                                        int result = 1;
                                        Intent returnIntent = new Intent();
                                        returnIntent.putExtra("changed",result);
                                        setResult(Activity.RESULT_OK,returnIntent);
                                        finish();

                                        Intent intent = new Intent(buttContext, Video.class);
                                        intent.putExtra(EXTRA_MESSAGE, link);
                                        startActivity(intent);
                                    }
                                }
                            });
                            thread.start();
                        }

                    });
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position,long id) {
        //Toast.makeText(getApplicationContext(), serverChoice, Toast.LENGTH_LONG);
        LinearLayout episodeBox = (LinearLayout) findViewById(R.id.episodeList);
        episodeBox.removeAllViews();
        printEpisodes(episodeStart, episodeEnd, episodeLink, episodesWatched);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
/*
    public void printEpisodesFirst(final int episodeStart, final int episodeEnd,final String episodeLink, final String boxName){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int identifier = getResources().getIdentifier(boxName,"id",getPackageName());
                LinearLayout list = (LinearLayout) findViewById(identifier);
                Context context = list.getContext();
                for(int i = episodeStart; i <= episodeEnd; i++){
                    final Button butt = new Button(context);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    butt.setText("Episode " + i);
                    list.addView(butt);
                    final String link2 = "https://gogoanime.io/"+episodeLink+"-episode-"+i;
                    Spinner dropDownBar = (Spinner)findViewById(R.id.spinner);
                    final Context dropContext = dropDownBar.getContext();
                    dropDownBar.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) dropContext);
                    serverChoice = dropDownBar.getSelectedItem().toString();

                    Thread thread = new Thread(new Runnable(){
                        @Override
                        public void run() {
                            String link = null;
                            Boolean available = true;
                            try {
                                Document doc = Jsoup.connect(link2).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                                Elements serverLinks = null;
                                Element resultLinks = null;
                                Elements rawLinks = null;
                                switch(serverChoice){
                                    case "VIDSTREAMING":
                                        serverLinks = doc.getElementsByClass("anime");


                                        if(serverLinks.isEmpty()){
                                            //runOnUiThread(new Runnable() {
                                             //   @Override
                                              //  public void run() {
                                               //     butt.setText("Server Unavailable");
                                                //}
                                            //});
                                            available = false;
                                        } else {
                                            resultLinks = serverLinks.first();
                                            rawLinks = resultLinks.children();
                                            link = rawLinks.first().attr("data-video");
                                        }

                                        break;
                                    case "STREAMANGO":
                                        serverLinks = doc.getElementsByClass("streamango");
                                        if(serverLinks.isEmpty()){


                                        } else {
                                            resultLinks = serverLinks.first();
                                            rawLinks = resultLinks.children();
                                            link = rawLinks.first().attr("data-video");
                                        }
                                        break;
                                    case "ESTREAM":
                                        serverLinks = doc.getElementsByClass("estream");
                                        if(serverLinks.isEmpty()){

                                        } else link = serverLinks.first().child(0).attr("data-video");
                                        break;
                                    case "OLOAD":
                                        serverLinks = doc.getElementsByClass("open");
                                        if(serverLinks.isEmpty()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                                    LinearLayout episodeBox = (LinearLayout) findViewById(R.id.episodeList);
                                                    episodeBox.removeAllViews();
                                                }
                                            });
                                        } else link = serverLinks.first().child(0).attr("data-video");
                                        break;
                                    case "OPENLOAD":
                                        serverLinks = doc.getElementsByClass("open");
                                        if(serverLinks.isEmpty()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                                    LinearLayout episodeBox = (LinearLayout) findViewById(R.id.episodeList);
                                                    episodeBox.removeAllViews();
                                                }
                                            });
                                        } else link = serverLinks.first().child(0).attr("data-video");
                                        break;
                                    case "THEVIDEO":
                                        serverLinks = doc.getElementsByClass("thevideo");
                                        if(serverLinks.isEmpty()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                                    LinearLayout episodeBox = (LinearLayout) findViewById(R.id.episodeList);
                                                    episodeBox.removeAllViews();
                                                }
                                            });
                                        } else link = serverLinks.first().child(0).attr("data-video");
                                        break;
                                    case "MP4UPLOAD":
                                        serverLinks = doc.getElementsByClass("mp4");
                                        if(serverLinks.isEmpty()){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                                    LinearLayout episodeBox = (LinearLayout) findViewById(R.id.episodeList);
                                                    episodeBox.removeAllViews();
                                                }
                                            });
                                        } else link = serverLinks.first().child(0).attr("data-video");
                                        break;
                                }
                                final String finalLink = link;
                                final Boolean availability = true;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(availability) {
                                            butt.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Context buttContext = butt.getContext();
                                                    Intent intent = new Intent(buttContext, Video.class);
                                                    intent.putExtra(EXTRA_MESSAGE, finalLink);
                                                    startActivity(intent);
                                                }
                                            });
                                        }
                                    }
                                });

                            } catch (IOException e) {
                                e.printStackTrace();
                                LinearLayout ll = (LinearLayout) findViewById(R.id.episodeList);
                                ll.removeAllViews();
                            }
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    public void printEpisodesSecond(final int episodeStart, final int episodeEnd,final String episodeLink, final String boxName){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<String> linkArray = new ArrayList<>();
                Spinner dropDownBar = (Spinner) findViewById(R.id.spinner);
                Context dropContext = dropDownBar.getContext();
                dropDownBar.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) dropContext);
                serverChoice = dropDownBar.getSelectedItem().toString();
                for (int i = 0; i < episodeEnd; i++) {
                    String link = "https://gogoanime.io/" + episodeLink + "-episode-" + (i + 1);
                    Boolean available = true;
                    try {
                        Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                        Elements serverLinks = null;
                        Element resultLinks = null;
                        Elements rawLinks = null;
                        switch (serverChoice) {
                            case "VIDSTREAMING":
                                serverLinks = doc.getElementsByClass("anime");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "STREAMANGO":
                                serverLinks = doc.getElementsByClass("streamango");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "ESTREAM":
                                serverLinks = doc.getElementsByClass("estram");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "OLOAD":
                                serverLinks = doc.getElementsByClass("open");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "OPENLOAD":
                                serverLinks = doc.getElementsByClass("open");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.last().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "THEVIDEO":
                                serverLinks = doc.getElementsByClass("thevideo");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "MP4UPLOAD":
                                serverLinks = doc.getElementsByClass("mp4");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "YOURUPLOAD":
                                serverLinks = doc.getElementsByClass("your");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                            case "BESTREAM":
                                serverLinks = doc.getElementsByClass("bestream");
                                if (serverLinks==null||serverLinks.isEmpty()) {
                                    linkArray.add("null");
                                } else {
                                    resultLinks = serverLinks.first();
                                    rawLinks = resultLinks.children();
                                    link = rawLinks.first().attr("data-video");
                                    linkArray.add(link);
                                }
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                final ArrayList<String> linkList = linkArray;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        int identifier = getResources().getIdentifier(boxName,"id",getPackageName());
                        LinearLayout list = (LinearLayout) findViewById(identifier);
                        Context context = list.getContext();
                        for(int i = episodeStart; i <= episodeEnd; i++) {
                            final Button butt = new Button(context);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            butt.setText("Episode " + i);
                            list.addView(butt);
                            final int index = i-1;
                            if(linkList.get(index).equals("null")){
                                butt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Toast.makeText(getApplicationContext(), "Server Unavailable", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                butt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        Context buttContext = butt.getContext();

                                        db = new DbHelper(buttContext);
                                        db.insertRecentlyWatched(db.getNextID(),title,index+1);

                                        Intent intent = new Intent(buttContext, Video.class);
                                        intent.putExtra(EXTRA_MESSAGE, linkList.get(index));
                                        startActivity(intent);
                                    }
                                });
                            }

                        }
                    }
                });
            }
        });
        thread.start();
    }
*/

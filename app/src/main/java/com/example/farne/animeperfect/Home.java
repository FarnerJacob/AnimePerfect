package com.example.farne.animeperfect;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class Home extends AppCompatActivity {

    //ToDo: play each video externally. P.S. GG good luck this is a little more advanced. The current system is ok.
    //ToDo: create a search bar

    //WebView webView;
    public static final String EXTRA_MESSAGE = "animePerfectMessage";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getRecentlyWatched();
        Thread thread = new Thread(new Runnable() {

            public void run() {
                Document doc = null;

                try {

                    doc = Jsoup.connect("https://www.gogoanime.io/popular.html").userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();

                    Elements links = doc.getElementsByClass("last_episodes");
                    String image = null;
                    String link = null;
                    for (int i=0;i<=19;i++) {
                        image = links.first().child(0).child(i).child(0).child(0).child(0).attr("src");
                        link = links.first().child(0).child(i).child(0).child(0).attr("href");
                        link = "https://gogoanime.io"+link;
                        image = image.replaceAll("./List Anime Popular at Gogoanime_files", "https://images.gogoanime.tv/images/upload");
                        String num = Integer.toString(i);
                        loadThumb(link, image, i);
                    }
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
                TextView box = (TextView) findViewById(R.id.textBox);
                box.setText(e.toString());
            }
        });
    }

    public void printHtml(final String string){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView box = (TextView) findViewById(R.id.textBox);
                box.setMovementMethod(new ScrollingMovementMethod());
                box.setText(string.toString());

            }
        });
    }

    public void loadThumb(final String link, final String image, final int count){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int identifier = getResources().getIdentifier("butt"+count,"id",getPackageName());
                ImageButton butt = (ImageButton) findViewById(identifier);
                final Context context = butt.getContext();
                butt.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AnimeProfile.class);
                        intent.putExtra(EXTRA_MESSAGE,link);
                        startActivity(intent);
                    }
                });

                Picasso.with(context).load(image).fit().into(butt);

            }
        });
    }

    public void getRecentlyWatched(){
        DbHelper db = new DbHelper(this);
        ArrayList<String> recentlyWatched = db.getRecentlyWatched();
        LinearLayout recentlyWatchedList = (LinearLayout) findViewById(R.id.recentlyWatchedList);
        ImageButton historyButton = (ImageButton) findViewById(R.id.historyButton);
        if(recentlyWatched.size()>0) {
            for(int i = 0; i < recentlyWatched.size(); i++) {
                final Context context = recentlyWatchedList.getContext();
                String preLink = recentlyWatched.get(i);
                preLink = preLink.replaceAll("[(+.^:,)]","");
                preLink = preLink.replaceAll(" ","-");
                final String link = "https://gogoanime.io/category/"+preLink;
                final int index = i;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                            Elements links = doc.getElementsByClass("main_body");
                            String image = links.first().child(1).child(0).child(0).attr("src");
                            loadThumb(image, index, link);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
            for(int i = 4; i >= recentlyWatched.size(); i--){
                int identifier = getResources().getIdentifier("recent"+i,"id",getPackageName());
                ImageButton butt = (ImageButton) findViewById(identifier);
                recentlyWatchedList.removeView(butt);
            }
            int position = recentlyWatched.size();
            //recentlyWatchedList.addView(historyButton);
        }
        else{
            ImageButton noRecent = (ImageButton) findViewById(R.id.recent0);
            noRecent.setImageResource(R.drawable.norecenthistory);
            for(int i = 1; i <5; i++){
                int identifier = getResources().getIdentifier("recent"+i,"id",getPackageName());
                ImageButton butt = (ImageButton) findViewById(identifier);
                recentlyWatchedList.removeView(butt);
            }
            recentlyWatchedList.removeView(historyButton);
        }
    }

    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.search_a_z:
                Intent intent = new Intent(this, AnimeList.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadThumb(final String image, final int index, final String link){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout list = (LinearLayout) findViewById(R.id.recentlyWatchedList);
                int identifier = getResources().getIdentifier("recent"+index,"id",getPackageName());
                ImageButton imageButton = (ImageButton) findViewById(identifier);
                final Context context = imageButton.getContext();
                Picasso.with(context).load(image).fit().into(imageButton);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, AnimeProfile.class);
                        intent.putExtra(EXTRA_MESSAGE,link);
                        //ToDO: find a way to get the home page to refresh the recently watched list. Somehow the default back button does this
                        startActivityForResult(intent,1);
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                getRecentlyWatched();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
        else getRecentlyWatched();
    }
}

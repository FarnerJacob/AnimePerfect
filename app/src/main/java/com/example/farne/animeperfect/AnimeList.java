package com.example.farne.animeperfect;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.farne.animeperfect.Home.EXTRA_MESSAGE;

public class AnimeList extends AppCompatActivity {

    public ArrayList<Button> buttons = new ArrayList<>();
    public ArrayList<Button> pages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime_list);
        Button symbolButton = (Button) findViewById(R.id.symbolButton);
        symbolButton.setTextColor(Color.parseColor("#ffff8800"));
        getPages("https://gogoanime.io/anime-list-0");
        getResults("https://gogoanime.io/anime-list-0");
        setButtonLinks();
    }

    public void printList(final ArrayList<String> animeList, final ArrayList<String> animeLink){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.animeList);
                linearLayout.removeAllViews();
                Context context = linearLayout.getContext();
                for(int i = 0; i < animeList.size(); i++) {
                    final Button butt = new Button(context);
                    butt.setText(animeList.get(i));
                    linearLayout.addView(butt);
                    final String link = "https://gogoanime.io" + animeLink.get(i);
                    butt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Context buttContext = butt.getContext();
                            Intent intent = new Intent(buttContext, AnimeProfile.class);
                            intent.putExtra(EXTRA_MESSAGE,link);
                            startActivity(intent);
                        }
                    });
                }
            }
        });
    }

    public void colorChange(Button touchedButt, String type){
        LinearLayout linearLayout = null;
        switch(type){
            case "letter":
                linearLayout = (LinearLayout) findViewById(R.id.animeList);
                for (Button button : buttons) {
                    button.setTextColor(Color.parseColor("#ffffffff"));
                }
                touchedButt.setTextColor(Color.parseColor("#ffff8800"));
            case "page":
                linearLayout = (LinearLayout) findViewById(R.id.pageList);
                for (Button page : pages) {
                    page.setTextColor(Color.parseColor("#ffffffff"));
                }
                touchedButt.setTextColor(Color.parseColor("#ffff8800"));
        }
    }

    public void setButtonLinks(){

        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.animeList);
                Button symbolButton = (Button) findViewById(R.id.symbolButton);
                Button aButton = (Button) findViewById(R.id.aButton);
                Button bButton = (Button) findViewById(R.id.bButton);
                Button cButton = (Button) findViewById(R.id.cButton);
                Button dButton = (Button) findViewById(R.id.dButton);
                Button eButton = (Button) findViewById(R.id.eButton);
                Button fButton = (Button) findViewById(R.id.fButton);
                Button gButton = (Button) findViewById(R.id.gButton);
                Button hButton = (Button) findViewById(R.id.hButton);
                Button iButton = (Button) findViewById(R.id.iButton);
                Button jButton = (Button) findViewById(R.id.jButton);
                Button kButton = (Button) findViewById(R.id.kButton);
                Button lButton = (Button) findViewById(R.id.lButton);
                Button mButton = (Button) findViewById(R.id.mButton);
                Button nButton = (Button) findViewById(R.id.nButton);
                Button oButton = (Button) findViewById(R.id.oButton);
                Button pButton = (Button) findViewById(R.id.pButton);
                Button qButton = (Button) findViewById(R.id.qButton);
                Button rButton = (Button) findViewById(R.id.rButton);
                Button sButton = (Button) findViewById(R.id.sButton);
                Button tButton = (Button) findViewById(R.id.tButton);
                Button uButton = (Button) findViewById(R.id.uButton);
                Button vButton = (Button) findViewById(R.id.vButton);
                Button wButton = (Button) findViewById(R.id.wButton);
                Button xButton = (Button) findViewById(R.id.xButton);
                Button yButton = (Button) findViewById(R.id.yButton);
                Button zButton = (Button) findViewById(R.id.zButton);
                buttons.add(symbolButton);
                buttons.add(aButton);
                buttons.add(bButton);
                buttons.add(cButton);
                buttons.add(dButton);
                buttons.add(eButton);
                buttons.add(fButton);
                buttons.add(gButton);
                buttons.add(hButton);
                buttons.add(iButton);
                buttons.add(jButton);
                buttons.add(kButton);
                buttons.add(lButton);
                buttons.add(mButton);
                buttons.add(nButton);
                buttons.add(oButton);
                buttons.add(pButton);
                buttons.add(qButton);
                buttons.add(rButton);
                buttons.add(sButton);
                buttons.add(tButton);
                buttons.add(uButton);
                buttons.add(vButton);
                buttons.add(wButton);
                buttons.add(xButton);
                buttons.add(yButton);
                buttons.add(zButton);
                for(final Button button : buttons) {
                    String preLink = button.getText().toString();
                    if(preLink.equals("#")){
                        preLink = "0";
                    }
                    final String link = "https://gogoanime.io/anime-list-" + preLink;
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            linearLayout.removeAllViews();
                            getPages(link);
                            getResults(link);
                            colorChange(button, "letter");
                        }
                    });
                }
            }
        });
        thread.start();
    }

    public void getResults(final String link){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                    Elements elementList = doc.getElementsByClass("listing");
                    Element firstResult = elementList.first();
                    Elements rawAnimeList = firstResult.children();
                    ArrayList<String> animeList = new ArrayList<>();
                    ArrayList<String> animeLink = new ArrayList<>();
                    for(Element child: rawAnimeList){
                        animeList.add(child.child(0).ownText());
                        animeLink.add(child.child(0).attr("href"));
                    }
                    printList(animeList, animeLink);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public void getPages(final String link){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Document doc = null;
                try {
                    doc = Jsoup.connect(link).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:49.0) Gecko/20100101 Firefox/49.0").ignoreHttpErrors(true).followRedirects(true).timeout(100000).ignoreContentType(true).get();
                    Elements otherPages = doc.getElementsByClass("pagination-list");
                    Element pageResults = otherPages.first();
                    Elements rawPages = null;
                    int numPages = 0;
                    if(pageResults!=null){
                        rawPages = pageResults.children();
                        for(Element element : rawPages){
                            numPages++;
                        }
                    } else numPages = 1;
                    final int pageCount = numPages;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout pageList = (LinearLayout) findViewById(R.id.pageList);
                            pageList.removeAllViews();
                            Context context = pageList.getContext();
                            for(int i = 1; i <= pageCount; i++){
                                ContextThemeWrapper newContext = new ContextThemeWrapper(context, R.style.Widget_AppCompat_Button_Borderless);
                                final Button butt = new Button(newContext);
                                pages.add(butt);
                                butt.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.MULTIPLY);
                                butt.setText("PAGE "+i);
                                if(i==1)
                                    butt.setTextColor(Color.parseColor("#ffff8800"));
                                else
                                    butt.setTextColor(Color.parseColor("#ffffffff"));
                                butt.setTextSize(9);
                                pageList.addView(butt);
                                final int pageNumber = i;
                                butt.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        colorChange(butt, "page");
                                        getResults(link+"?page="+pageNumber);
                                    }
                                });
                            }
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();

    }
}

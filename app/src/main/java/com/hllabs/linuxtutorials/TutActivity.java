package com.hllabs.linuxtutorials;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;

public class TutActivity extends AppCompatActivity {

    int index;

    String file;

    MarkdownView markdownView;

    private String[] titles = new String[]{
            "Moving around in the Filesystem",
            "Working with Files",
            "Shell features",
            "File Permissions",
            "User Management",
            "systemd",
            "Writing systemd unit files"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tut);

        if( getIntent() != null && getIntent().getIntExtra("i",-1)!= -1 ){
            index = getIntent().getIntExtra("i",1);
        }

        try {
            getSupportActionBar().setElevation(0f);
            getSupportActionBar().setTitle(titles[index - 1]);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        markdownView = findViewById(R.id.article_view);

        file = index + "/" + index + ".md";


        InternalStyleSheet css = new Github();
        css.removeRule(".scrollup");
        css.addRule("img", "max-width: 100%");
        css.addRule("body","background-color: #101010","color: #fff","padding: 10px");
        css.addRule("pre", "background-color: #000000","color: #009688","border-radius: 0px");
        css.addRule("code", "background-color: #000000","color: #009688","border-radius: 0px");
        css.removeRule("table tr:nth-child(2n)");
        markdownView.addStyleSheet(css);
        markdownView.loadMarkdownFromAsset(file);


    }
}

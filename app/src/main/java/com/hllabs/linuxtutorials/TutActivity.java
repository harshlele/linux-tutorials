package com.hllabs.linuxtutorials;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;

public class TutActivity extends AppCompatActivity {

    int index;

    String file;

    MarkdownView markdownView;

    float x,y;

    private String[] titles = new String[]{
            "Moving around in the Filesystem",
            "Working with Files",
            "Shell features",
            "File Permissions",
            "User Management",
            "systemd",
            "Writing systemd unit files"
    };

    @SuppressLint("ClickableViewAccessibility")
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

        markdownView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        markdownView.getSettings().setJavaScriptEnabled(true);

        markdownView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                float density = getResources().getDisplayMetrics().density;
                float touchX = motionEvent.getX() / density;
                float touchY = motionEvent.getY() / density;
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    x = touchX;
                    y = touchY;
                }

                if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    float dx = Math.abs(touchX-x);
                    float dy = Math.abs(touchY-y);
                    if(dx<10.0/density&&dy<10.0/density){
                        clickImage(touchX,touchY);

                    }
                }
                return false;
            }


        });

        markdownView.addJavascriptInterface(new JsInterface(this), "imageClick");

    }


    private void clickImage(float touchX, float touchY) {
        String js = "javascript:(function(){" +
                "var  obj=document.elementFromPoint("+touchX+","+touchY+");"
                +"if(obj.src!=null && obj instanceof HTMLImageElement){"+ " window.imageClick.click(obj.src);}" +
                "})()";


        markdownView.loadUrl(js);
    }



    class JsInterface{
        Context context;
        public JsInterface(Context context){
            this.context = context;
        }

        @JavascriptInterface
        public void click(String url){
            Log.d ("LOG!",url);
        }
    }
}

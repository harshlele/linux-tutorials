package com.hllabs.linuxtutorials;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.tiagohm.markdownview.MarkdownView;
import br.tiagohm.markdownview.css.InternalStyleSheet;
import br.tiagohm.markdownview.css.styles.Github;

/*
The tutorial activity. Tutorials are markdown files that have been loaded from the assets folder
 */
public class TutActivity extends AppCompatActivity {
    //index of the tutorial to load(begins from 1,NOT 0)
    int index;
    //file name
    String file;
    //markdown view
    MarkdownView markdownView;
    //touch co-ordinates(used for handling image clicks)
    float x,y;

    //title array
    private String[] titles = Titles.titles;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tut);

        //get index from intent
        if( getIntent() != null && getIntent().getIntExtra("i",-1)!= -1 ){
            index = getIntent().getIntExtra("i",1);
        }

        //make the action bar flat, and set the title
        try {
            getSupportActionBar().setElevation(0f);
            getSupportActionBar().setTitle(titles[index - 1]);
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }

        markdownView = findViewById(R.id.article_view);

        //create the path of the file(contained in the assets folder)
        file = index + "/" + index + ".md";

        //add a new stylesheet
        InternalStyleSheet css = new Github();
        //remove the scrollup FAB
        css.removeRule(".scrollup");

        /*CSS RULES */
        // adjust image width to fit in the screen
        css.addRule("img", "max-width: 100%");
        //make the article background black and text color white, decrease the padding
        css.addRule("body","background-color: #101010","color: #fff","padding: 10px");
        //change the colors for code,pre tags and even rows of tables
        css.addRule("pre", "background-color: #000000","color: #009688","border-radius: 0px");
        css.addRule("code", "background-color: #000000","color: #009688","border-radius: 0px");
        css.removeRule("table tr:nth-child(2n)");

        //add the CSS, and load the file
        markdownView.addStyleSheet(css);
        markdownView.loadMarkdownFromAsset(file);

        markdownView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        //enable JS for getting touch events
        markdownView.getSettings().setJavaScriptEnabled(true);

        //add a touch listener to get the co-ordinates of the touch event
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
                    //if it's a click(and not a drag gesture),
                    // call clickImage(which loads javascript, which calls the JS interface)
                    if(dx<10.0/density&&dy<10.0/density){
                        clickImage(touchX,touchY);
                    }
                }
                return false;
            }


        });

        //add a JS interface(so JS code can call Java code)
        markdownView.addJavascriptInterface(new JsInterface(this), "imageClick");

    }

    //load JS that gets the touched element using touch co-ordinates,
    // checks if the clicked event was an image.
    // If it was, it calls the JS interface to get the URL of the image that was touched.
    private void clickImage(float touchX, float touchY) {
        String js = "javascript:(function(){" +
                "var  obj=document.elementFromPoint("+touchX+","+touchY+");"
                +"if(obj.src!=null && obj instanceof HTMLImageElement){"+ " window.imageClick.click(obj.src);}" +
                "})()";


        markdownView.loadUrl(js);
    }


    // class containing JS interface for getting the touched image URL.
    class JsInterface{
        Context context;
        public JsInterface(Context context){
            this.context = context;
        }

        //post a new Event containing the clicked url
        @JavascriptInterface
        public void click(String url){
            EventBus.getDefault().post(new ImageClickEvent(url));
        }
    }

    //event that is generated when an image element is clicked
    public static class ImageClickEvent {
        public String url;

        public ImageClickEvent(String url) {
            this.url = url;
        }
    }

    //create a dialog with the image
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImageClickEvent event) {

        AlertDialog.Builder ImageDialog = new AlertDialog.Builder(TutActivity.this);
        ImageDialog.setTitle(event.url);
        PhotoView showImage = new PhotoView(TutActivity.this);
        //use picasso to load the image
        Picasso.get()
                .load(event.url)
                .placeholder(R.drawable.ic_image_128dp)
                .into(showImage);

        ImageDialog.setView(showImage);
        ImageDialog.setNegativeButton("Close", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface arg0, int arg1)
            {}
        });
        ImageDialog.show();

    }



    //register for eventbus
    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    //unregister eventbus
    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}

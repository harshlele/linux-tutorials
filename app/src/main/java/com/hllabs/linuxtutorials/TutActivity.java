package com.hllabs.linuxtutorials;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.MalformedURLException;
import java.net.URL;

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
    //ad
    private InterstitialAd mInterstitialAd;

    //shows find dialog is visible
    private boolean isFindDialogShown = false;

    //Layout of the dialog shown when finding a word,along with all the buttons inside the dialog
    private LinearLayout findLayout;
    private ImageButton nextBtn,prevBtn,closeBtn;

    //title array
    private String[] titles = Titles.titles;

    private AdRequest adRequest;
    //GDPR consent form
    private ConsentForm form;

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
            Log.d("LOG!", "onCreate: " + e.toString());
        }

        //initialise views
        markdownView = findViewById(R.id.article_view);
        findLayout = findViewById(R.id.find_layout);
        nextBtn = findViewById(R.id.btn_next);
        prevBtn = findViewById(R.id.btn_prev);
        closeBtn = findViewById(R.id.btn_close);

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
        //change the colors for code,pre tags and all the even rows of tables
        css.addRule("pre", "background-color: #000000","color: #009688","border-radius: 0px");
        css.addRule("code", "background-color: #000000","color: #009688","border-radius: 0px");
        css.addRule("pre[language]::before","color: #009688");
        css.addRule("code.bash","color: #009688");
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

        //set a listener for when a word search is finished so the search dialog can be shown
        markdownView.setFindListener(new WebView.FindListener() {
            @Override
            public void onFindResultReceived(int i, int i1, boolean b) {
                if (b && !isFindDialogShown){
                    showFindDialog(true);
                }
            }
        });


        //go to the next search result in the page
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markdownView.findNext(true);
            }
        });

        //go to the previous search result in the page
        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markdownView.findNext(false);
            }
        });

        //clear all highlighted results, and hide the search dialog
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                markdownView.clearMatches();
                findLayout.setVisibility(View.GONE);
                isFindDialogShown = false;
            }
        });

        final ConsentInformation consentInformation = ConsentInformation.getInstance(getApplicationContext());
        String[] publisherIds = {"pub-7444749934962149"};
        //update consent info
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d("LOG!", "onConsentInfoUpdated: ");
                //if consent info is known, run ads accordingly.
                //Else, show the consent dialog
                switch (consentStatus){
                    case PERSONALIZED:
                        initAd(true);
                        Log.d("LOG!", "onConsentInfoUpdated: personalised");
                        break;
                    case NON_PERSONALIZED:
                        Log.d("LOG!", "onConsentInfoUpdated: non-personalised");
                        initAd(false);
                        break;

                    default:
                        Log.d("LOG!", "onConsentInfoUpdated: default");
                        if(consentInformation.isRequestLocationInEeaOrUnknown()) showConsentDialog();
                        else initAd(true);
                        break;
                }
            }

            //if consent info can't be obtained and this is a EU user, show the consent form
            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                Log.d("LOG!", "onFailedToUpdateConsentInfo ");
                if (consentInformation.isRequestLocationInEeaOrUnknown())  showConsentDialog();
                else initAd(true);
            }
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7444749934962149/8280385634");

    }


    private void showConsentDialog(){

        Log.d("LOG!", "showConsentDialog: ");

        URL privacyUrl = null;
        try {
            privacyUrl = new URL("https://hllabs.github.io/linuxtuts/privacy_policy");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        form = new ConsentForm.Builder(TutActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    //show the form only after it has loaded
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                    }

                    @Override
                    public void onConsentFormOpened() {}

                    @Override
                    public void onConsentFormClosed(
                            ConsentStatus consentStatus, Boolean userPrefersAdFree) {

                            switch (consentStatus){
                                case PERSONALIZED:
                                    initAd(true);
                                    break;
                                default:
                                    initAd(false);
                                    break;
                            }

                        }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d("LOG!" , "onConsentFormError: " + errorDescription);
                        initAd(false);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();

        form.load();

    }

    private void showForm(){
        if(form != null) form.show();
    }

    //show the search layout
    private void showFindDialog(boolean show){
        if(show){
            findLayout.setVisibility(View.VISIBLE);
            isFindDialogShown = true;
        }
    }

    //initialise and load ads
    private void initAd(boolean personalised){
        Log.d("LOG!", "initAd: ");
        Bundle extras = new Bundle();
        extras.putString("npa", "1");

        if(!personalised){
            adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, extras).build();
        }
        else adRequest = new AdRequest.Builder().build();

        mInterstitialAd.loadAd(adRequest);

    }

    @Override
    public void onBackPressed() {

        if(mInterstitialAd.isLoaded()){

            mInterstitialAd.show();
        }
        super.onBackPressed();
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

    //set appbar menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tut_menu, menu);
        return true;
    }

    //show search dialog when search icon is pressed
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_search:
                showSearchDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //show a dialog with a textfield where the user can enter search query.
    private void showSearchDialog(){
        final AlertDialog.Builder searchDialog = new AlertDialog.Builder(TutActivity.this);
        searchDialog.setTitle("Find in Page");

        final EditText searchText = new EditText(getApplicationContext());
        searchText.setTextColor(Color.BLACK);
        searchText.setPadding(16,16,16,16);
        searchText.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        searchDialog.setView(searchText);

        searchDialog.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String query = searchText.getText().toString();
                searchTextInPage(query);
                dialogInterface.dismiss();
            }
        });
        searchDialog.show();

    }

    //search the page for text
    private void searchTextInPage(String text){
        if(text != null && !text.equals("")) {
            markdownView.findAllAsync(text);
        }
    }

    //create a dialog with the image
    //event fired when an image is clicked
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(ImageClickEvent event) {

        AlertDialog.Builder ImageDialog = new AlertDialog.Builder(TutActivity.this);
        ImageDialog.setTitle("Double tap to zoom");
        PhotoView showImage = new PhotoView(TutActivity.this);
        showImage.setPadding(0,16,0,0);
        showImage.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

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

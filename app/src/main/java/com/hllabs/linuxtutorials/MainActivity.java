package com.hllabs.linuxtutorials;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;

public class MainActivity extends AppCompatActivity {

    //list of tutorials
    private RecyclerView tutorialList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //make the action bar flat
        try {
            getSupportActionBar().setElevation(0f);

        }catch (NullPointerException e){
            e.printStackTrace();
        }


        tutorialList = findViewById(R.id.post_list);
        tutorialList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        tutorialList.setAdapter(new TutorialListAdapter());
        tutorialList.setItemAnimator(new DefaultItemAnimator());


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_share){
            String url = "https://play.google.com/store/apps/details?id=com.hllabs.linuxtutorials";

            try {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "Linux Tutorials");
                String sAux = "\nCheck out this app: \n\n";
                sAux = sAux + url + " \n\n";
                i.putExtra(Intent.EXTRA_TEXT, sAux);
                startActivity(Intent.createChooser(i, "Share Via..."));
            } catch(Exception e) {
                Log.d("LOG!" , e.toString());
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //tutorial list adapter
    class TutorialListAdapter extends RecyclerView.Adapter<TutorialListAdapter.ViewHolder>{

        //array of titles
        private String[] titles = Titles.titles;

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.tut_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                holder.titleText.setText(titles[position]);
                TextDrawable drawable = TextDrawable.builder()
                    .buildRound(String.valueOf(position+1),getResources().getColor(R.color.colorAccent));
                holder.noImage.setImageDrawable(drawable);
                final int i = position + 1;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getApplicationContext(),TutActivity.class);
                        intent.putExtra("i",i);
                        startActivity(intent);
                    }
                });
        }

        @Override
        public int getItemCount() {
            return titles.length;
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private TextView titleText;
            private ImageView noImage;

            public ViewHolder(View itemView) {
                super(itemView);
                titleText = itemView.findViewById(R.id.tut_name);
                noImage = itemView.findViewById(R.id.circle_image);

            }
        }
    }
}

package com.hllabs.linuxtutorials;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
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


    //tutorial list adapter
    class TutorialListAdapter extends RecyclerView.Adapter<TutorialListAdapter.ViewHolder>{

        //array of titles
        private String[] titles = new String[]{
                "Moving around in the Filesystem",
                "Working with Files",
                "Shell features",
                "File Permissions",
                "User Management",
                "systemd",
                "Writing systemd unit files"
        };

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

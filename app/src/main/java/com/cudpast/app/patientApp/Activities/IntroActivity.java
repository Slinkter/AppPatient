package com.cudpast.app.patientApp.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.cudpast.app.patientApp.Activities.SupportIntro.IntroViewPagerAdapter;
import com.cudpast.app.patientApp.Activities.SupportIntro.ScreenItem;
import com.cudpast.app.patientApp.R;

import java.util.ArrayList;
import java.util.List;

public class IntroActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private IntroViewPagerAdapter adapter;
    private List<ScreenItem> mListSlide;
    private TabLayout tabIndicator;

    private Animation animation;


    private Button btnNext;
    private int position;
    private Button btnStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_intro);
        //full-screen
        viewPager = findViewById(R.id.viewPager);
        tabIndicator = findViewById(R.id.tab_indicator);
        btnNext = findViewById(R.id.btn_next);
        btnStarted = findViewById(R.id.btn_get_started);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.button_animation);
        // Falso = todavia no vio
        // Verdadero = ya vio
        if (showSlideFirst()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        loadData();
        //
        adapter = new IntroViewPagerAdapter(this, mListSlide);
        viewPager.setAdapter(adapter);
        tabIndicator.setupWithViewPager(viewPager);
        btnNext
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        position = viewPager.getCurrentItem();
                        if (mListSlide.size() - 1 == position) {
                            loadLastScreen();
                        }
                        if (mListSlide.size() - 1 > position) {
                            position++;
                            viewPager.setCurrentItem(position);
                        }
                    }
                });

        tabIndicator.addOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (mListSlide.size() - 1 == tab.getPosition()) {

                    loadLastScreen();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        btnStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent goMain = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(goMain);
                savePrefData();
                finish();
            }
        });
    }


    private boolean showSlideFirst() {
        SharedPreferences sp;
        Boolean isIntroActivityOpenedBefore;
        sp = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        isIntroActivityOpenedBefore = sp.getBoolean("isIntroOpened", false);
        return isIntroActivityOpenedBefore;
    }

    private void loadData() {
        mListSlide = new ArrayList<>();
        mListSlide.add(new ScreenItem("Bienvenido", "Paciente APP es una app  para\natención medica  ", R.drawable.ic_ambulance));
        mListSlide.add(new ScreenItem("Ubicación", "Es importante tener la última versión de\nGoogle Maps", R.drawable.ic_map_slide_2));
        mListSlide.add(new ScreenItem("Disponibilidad", "Horarios flexibles para visitarte en\ntu zona", R.drawable.ic_doctorapp));
    }

    private void loadLastScreen() {
        btnNext.setVisibility(View.INVISIBLE);
        tabIndicator.setVisibility(View.INVISIBLE);
        btnStarted.setVisibility(View.VISIBLE);
        btnStarted.setAnimation(animation);
    }

    private void savePrefData() {
        SharedPreferences sp;
        SharedPreferences.Editor editor;
        sp = getApplicationContext().getSharedPreferences("myPrefs", MODE_PRIVATE);
        editor = sp.edit();
        editor.putBoolean("isIntroOpened", true);
        editor.commit();
    }
}

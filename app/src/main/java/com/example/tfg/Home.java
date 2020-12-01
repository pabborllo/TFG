package com.example.tfg;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import android.os.Bundle;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    BottomNavigationView bottomView;
    NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ImageView homeBg = findViewById(R.id.home_bg);
        Glide.with(Home.this).load(R.drawable.home).into(homeBg);

        bottomView = findViewById(R.id.bottomBar);
        navController = Navigation.findNavController(this, R.id.fragment);

        NavigationUI.setupWithNavController(bottomView, navController);
    }

}

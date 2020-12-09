package com.example.threadexample;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

import com.b09302083.myfuture.MyFuture;

import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyFuture<Activity> myFuture = new MyFuture<>();
        myFuture.done(this);
        try {
            if (myFuture.get() != this) {
                throw new RuntimeException();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }
}
package com.example.josephgallahan.chitchat;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends SingleFragmentActivity
{

    public static Intent newIntent(Context packageContext)
    {
        Intent i = new Intent(packageContext, MainActivity.class);
        return i;
    }

    @Override
    protected Fragment createFragment()
    {
        setContentView(R.layout.activity_main);
        return new PostFragment();
    }

}
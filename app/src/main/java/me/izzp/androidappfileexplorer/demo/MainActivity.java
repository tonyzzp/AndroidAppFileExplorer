package me.izzp.androidappfileexplorer.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.InputStream;
import java.util.Arrays;

import me.izzp.androidappfileexplorer.FileExplorerActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onFileExplorerClick(View view) {
        Intent intent = FileExplorerActivity.Companion.create(this, getFilesDir().getParent());
        startActivity(intent);
    }

    public void onCreateTestFilesClick(View view) {
        MakeTestFilesKt.makeFiles(this, getFilesDir());
    }
}
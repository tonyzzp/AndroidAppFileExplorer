package me.izzp.androidappfileexplorer.demo;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.izzp.androidappfileexplorer.AppFileExplorer;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppFileExplorer.showNotification(this);
        List<String> list = new ArrayList<>();
        list.add(Environment.getExternalStorageDirectory().getAbsolutePath());
        list.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath());
        AppFileExplorer.addDirs(list);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppFileExplorer.dismissNotification(this);
    }

    public void onFileExplorerClick(View view) {
        AppFileExplorer.open(this);
    }

    public void onCreateTestFilesClick(View view) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在创建测试文件");
        progressDialog.show();
        final Activity act = this;
        new Thread() {
            @Override
            public void run() {
                super.run();
                MakeTestFilesKt.makeFiles(act, new File(getFilesDir(), "testfiles"));
                getSharedPreferences("testSharedPrefences", MODE_PRIVATE)
                        .edit()
                        .putString("name", "zzp")
                        .putInt("age", 18)
                        .putBoolean("handsome", true)
                        .apply();
                createTestDatabase();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        Toast.makeText(act, "创建成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.start();
    }

    private void createTestDatabase() {
        final Activity act = this;
        class DbOpenHelper extends SQLiteOpenHelper {

            private DbOpenHelper() {
                super(act, "testdb", null, 8);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL("create table user(id integer,name text,age integer);");
                db.execSQL("create table article(id integer primary key autoincrement,title text,content text,auth text);");
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                db.execSQL("drop table if exists user;");
                db.execSQL("drop table if exists article;");
                onCreate(db);
            }
        }

        DbOpenHelper helper = new DbOpenHelper();
        SQLiteDatabase db = helper.getWritableDatabase();
        db.beginTransaction();
        for (int i = 0; i < 13; i++) {
            ContentValues cv = new ContentValues();
            cv.put("id", i);
            cv.put("name", "name" + i);
            cv.put("age", i * 2);
            db.insert("user", null, cv);
        }
        String content = "";
        try {
            content = Util.readText(getAssets().open("poem.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 120; i++) {
            ContentValues cv = new ContentValues();
            cv.put("title", "title" + i);
            cv.put("content", content);
            cv.put("auth", "李白");
            db.insert("article", null, cv);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        helper.close();
    }
}
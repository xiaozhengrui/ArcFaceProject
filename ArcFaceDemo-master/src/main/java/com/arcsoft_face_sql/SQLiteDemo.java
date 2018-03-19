package com.arcsoft_face_sql;

/**
 * Created by techwinxiao on 18-3-7.
 */

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.arcsoft_face_ui.R;

/*
 * 什么是SQLiteDatabase？
 * 一个SQLiteDatabase的实例代表了一个SQLite的数据库，通过SQLiteDatabase实例的一些方法，我们可以执行SQL语句，
 * 对数据库进行增、删、查、改的操作。需要注意的是，数据库对于一个应用来说是私有的，并且在一个应用当中，数据库的名字也是惟一的。
 */

/*
 * 什么是SQLiteOpenHelper ？
 * 这个类主要生成一个数据库，并对数据库的版本进行管理。
 * 当在程序当中调用这个类的方法getWritableDatabase()或者getReadableDatabase()方法的时候，如果当时没有数据，那么Android系统就会自动生成一个数据库。
 * SQLiteOpenHelper 是一个抽象类，我们通常需要继承它，并且实现里边的3个函数，
 *     onCreate（SQLiteDatabase）：在数据库第一次生成的时候会调用这个方法，一般我们在这个方法里边生成数据库表。
 *     onUpgrade（SQLiteDatabase, int, int）：当数据库需要升级的时候，Android系统会主动的调用这个方法。一般我们在这个方法里边删除数据表，并建立新的数据表，当然是否还需要做其他的操作，完全取决于应用的需求。
 *     onOpen（SQLiteDatabase）：这是当打开数据库时的回调函数，一般也不会用到。
 */

public class SQLiteDemo extends Activity {

    OnClickListener listener1 = null;
    OnClickListener listener2 = null;
    OnClickListener listener3 = null;
    OnClickListener listener4 = null;
    OnClickListener listener5 = null;

    Button button1;
    Button button2;
    Button button3;
    Button button4;
    Button button5;

    DatabaseHelper mOpenHelper;

    private static final String DATABASE_NAME = "dbForArcFace.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "ArcFace";
    private static final String TITLE = "title";
    private static final String BODY = "body";

    //建立一个内部类,主要生成一个数据库
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        //在数据库第一次生成的时候会调用这个方法，一般我们在这个方法里边生成数据库表。
        @Override
        public void onCreate(SQLiteDatabase db) {

            String sql = "CREATE TABLE " + TABLE_NAME + " (" + TITLE
                    + " text not null, " + BODY + " text not null " + ");";
            Log.i("haiyang:createDB=", sql);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("SQLiteDemo","SQLiteDemo onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sqlite_activity);
        prepareListener();
        initLayout();
        mOpenHelper = new DatabaseHelper(this);

    }

    private void initLayout() {
        button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(listener1);

        button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(listener2);

        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(listener3);

        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(listener4);

        button5 = (Button) findViewById(R.id.button5);
        button5.setOnClickListener(listener5);

    }

    private void prepareListener() {
        listener1 = new OnClickListener() {
            public void onClick(View v) {
                CreateTable();
            }
        };
        listener2 = new OnClickListener() {
            public void onClick(View v) {
                dropTable();
            }
        };
        listener3 = new OnClickListener() {
            public void onClick(View v) {
                insertItem();
            }
        };
        listener4 = new OnClickListener() {
            public void onClick(View v) {
                deleteItem();
            }
        };
        listener5 = new OnClickListener() {
            public void onClick(View v) {
                showItems();
            }
        };
    }

    /*
     * 重新建立数据表
     */
    private void CreateTable() {
        //mOpenHelper.getWritableDatabase()语句负责得到一个可写的SQLite数据库，如果这个数据库还没有建立，
        //那么mOpenHelper辅助类负责建立这个数据库。如果数据库已经建立，那么直接返回一个可写的数据库。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String sql = "CREATE TABLE " + TABLE_NAME + " (" + TITLE
                + " text not null, " + BODY + " text not null " + ");";
        Log.i("CreateTable=", sql);

        try {
            db.execSQL("DROP TABLE IF EXISTS diary");
            db.execSQL(sql);
            setTitle("数据表成功重建");
        } catch (SQLException e) {
            setTitle("数据表重建错误");
        }
    }

    /*
     * 删除数据表
     */
    private void dropTable() {
        //mOpenHelper.getWritableDatabase()语句负责得到一个可写的SQLite数据库，如果这个数据库还没有建立，
        //那么mOpenHelper辅助类负责建立这个数据库。如果数据库已经建立，那么直接返回一个可写的数据库。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String sql = "drop table " + TABLE_NAME;
        try {
            db.execSQL(sql);
            setTitle("数据表成功删除：" + sql);
        } catch (SQLException e) {
            setTitle("数据表删除错误");
        }
    }

    /*
     * 插入两条数据
     */
    private void insertItem() {
        //mOpenHelper.getWritableDatabase()语句负责得到一个可写的SQLite数据库，如果这个数据库还没有建立，
        //那么mOpenHelper辅助类负责建立这个数据库。如果数据库已经建立，那么直接返回一个可写的数据库。
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        String sql1 = "insert into " + TABLE_NAME + " (" + TITLE + ", " + BODY
                + ") values('haiyang', 'android的发展真是迅速啊');";
        String sql2 = "insert into " + TABLE_NAME + " (" + TITLE + ", " + BODY
                + ") values('icesky', 'android的发展真是迅速啊');";
        try {
            // Log.i（）会将参数内容打印到日志当中，并且打印级别是Info级别
            // Android支持5种打印级别，分别是Verbose、Debug、Info、Warning、Error，当然我们在程序当中一般用到的是Info级别
            Log.i("haiyang:sql1=", sql1);
            Log.i("haiyang:sql2=", sql2);
            db.execSQL(sql1);
            db.execSQL(sql2);
            setTitle("插入两条数据成功");
        } catch (SQLException e) {
            setTitle("插入两条数据失败");
        }
    }

    /*
     * 删除其中的一条数据
     */
    private void deleteItem() {
        try {
            //mOpenHelper.getWritableDatabase()语句负责得到一个可写的SQLite数据库，如果这个数据库还没有建立，
            //那么mOpenHelper辅助类负责建立这个数据库。如果数据库已经建立，那么直接返回一个可写的数据库。
            SQLiteDatabase db = mOpenHelper.getWritableDatabase();
            //第一个参数是数据库表名，在这里是TABLE_NAME，也就是diary。
            //第二个参数，相当于SQL语句当中的where部分，也就是描述了删除的条件。
            //如果在第二个参数当中有“？”符号，那么第三个参数中的字符串会依次替换在第二个参数当中出现的“？”符号。
            db.delete(TABLE_NAME, " title = 'haiyang'", null);
            setTitle("删除title为haiyang的一条记录");
        } catch (SQLException e) {

        }

    }

    /*
     * 在屏幕的title区域显示当前数据表当中的数据的条数。
     */
    /*
     * Cursor cur = db.query(TABLE_NAME, col, null, null, null, null, null)语句将查询到的数据放到一个Cursor 当中。
        这个Cursor里边封装了这个数据表TABLE_NAME当中的所有条列。
        query()方法相当的有用，在这里我们简单地讲一下。
            第一个参数是数据库里边表的名字，比如在我们这个例子，表的名字就是TABLE_NAME，也就是"diary"。
            第二个字段是我们想要返回数据包含的列的信息。在这个例子当中我们想要得到的列有title、body。我们把这两个列的名字放到字符串数组里边来。
            第三个参数为selection，相当于SQL语句的where部分，如果想返回所有的数据，那么就直接置为null。
            第四个参数为selectionArgs。在selection部分，你有可能用到“?”，那么在selectionArgs定义的字符串会代替selection中的“?”。
            第五个参数为groupBy。定义查询出来的数据是否分组，如果为null则说明不用分组。
            第六个参数为having ，相当于SQL语句当中的having部分。
            第七个参数为orderBy，来描述我们期望的返回值是否需要排序，如果设置为null则说明不需要排序。
     */

    private void showItems() {

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        String col[] = { TITLE, BODY };
        //查询数据
        Cursor cur = db.query(TABLE_NAME, col, null, null, null, null, null);
        Integer num = cur.getCount();
        setTitle(Integer.toString(num) + " 条记录");
    }
}
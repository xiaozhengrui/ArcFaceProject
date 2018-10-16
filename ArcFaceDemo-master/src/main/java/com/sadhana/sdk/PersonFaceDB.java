package com.sadhana.sdk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.arc_sdk.FaceDB;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PersonFaceDB {
    String mDBPath;
    String localDBPath;
    boolean loadDBfinish = false;
    public List<FaceRegist> mRegister;
    Context mContext;
    public class FaceRegist {
        public String mName;
        public List<Person> mFaceList;

        public FaceRegist(String name) {
            mName = name;
            mFaceList = new ArrayList<>();
        }
    }
    public PersonFaceDB(String path,Context mContext){
        this.mContext = mContext;
        mDBPath = path;
        mRegister = new ArrayList<>();
        localDBPath = mContext.getFilesDir().getAbsolutePath() + "/demo.db";
    }

    protected boolean moveDB2Local(){
        try {
            FileOutputStream outputStream = new FileOutputStream(localDBPath);
            FileInputStream inputStream = new FileInputStream(mDBPath + "/demo.db");
            byte[] buf = new byte[40960];
            int n = 0;
            do {
                n = inputStream.read(buf);
                if (n > 0)
                    outputStream.write(buf, 0, n);
            } while (n > 0);
            inputStream.close();
            outputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loadFaces() {
        if (!mRegister.isEmpty()) {
            return false;
        }

        if(moveDB2Local()){
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(localDBPath,null);
            Cursor cursor = db.query("t_person", null, null, null, null, null, "id");
            int index = 0;
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(0);
                    String name = cursor.getString(1);
                    byte[] image = cursor.getBlob(2);
                    Bitmap bmp = BitmapFactory.decodeByteArray(image, 0, image.length);
                    byte[] feature = cursor.getBlob(3);
                    Person person = new Person(Long.toString(id), feature);
                    mRegister.add(new FaceRegist(new String(name)));
                    mRegister.get(index).mFaceList.add(person);
                    index++;
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
            loadDBfinish = true;
            return true;
        }
        return false;
    }

    public boolean addFace(String name,Person person,Bitmap bmp) {
        boolean add = true;
        boolean insertFlag = true;
        if(!loadDBfinish){
            if(!moveDB2Local()){
                insertFlag = false;
            }
        }

        for (FaceRegist frface : mRegister) {
            if (frface.mName.equals(name)) {
                frface.mFaceList.add(person);
                add = false;
                break;
            }
        }
        if (add) { // not registered.
            FaceRegist frface = new FaceRegist(name);
            frface.mFaceList.add(person);
            mRegister.add(frface);
        }

        if(insertFlag){
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(localDBPath,null);

            ContentValues values = new ContentValues();
            values.put("id",person.getId());
            values.put("name",name);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(bmp.getByteCount());
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            values.put("image",outputStream.toByteArray());

            values.put("feature",person.getFeature());
            db.insert("t_person",null,values);
            db.close();
            return true;
        }
        return false;
    }


    public boolean delete(String name) {
        boolean deleteFlag = true;
        if(!loadDBfinish){
            if(!moveDB2Local()){
                deleteFlag = false;
            }
        }

        if(deleteFlag){
            for (FaceRegist frface : mRegister) {
                if (frface.mName.equals(name)) {
                    mRegister.remove(frface);
                    break;
                }
            }
            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(localDBPath,null);
            Cursor cursor = db.query("t_person", null, null, null, null, null, "id");
            if (cursor.moveToFirst()) {
                do {
                    String pName = cursor.getString(1);
                    if(pName.equals(name)){
                        db.delete("t_person","where name=?",new String[]{pName});
                        break;
                    }
                } while (cursor.moveToNext());
                cursor.close();
            }
            db.close();
        }
        return false;
    }
}

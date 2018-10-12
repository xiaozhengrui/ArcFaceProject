package com.json_parse;

import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class json_service {
    private final String TAG = "json_advert";
    //获取广告排期json文件信息
    public ArrayList<json_advert> getJsonForAdvert(String Path){
        String jsonPath = null;
        try{
            File dir = Environment.getExternalStoragePublicDirectory(Path);

            jsonPath = dir.getAbsolutePath();
            Log.d(TAG, "onCreate: dir -> " + jsonPath);
            if (dir.exists()) {
                for (File f : dir.listFiles()) {
                    Log.d(TAG, " ----- " + f);
                    if (f.getAbsolutePath().endsWith(".json")) {
                        jsonPath = f.getAbsolutePath();
                        Log.d(TAG, "onCreate: json -> " + jsonPath);
                        break;
                    }
                }
            } else {
                Log.e(TAG, "onCreate: JsonPath not exist");
            }
            FileInputStream is = new FileInputStream(jsonPath);

            InputStreamReader isr = new InputStreamReader(is,"UTF-8");
            BufferedReader br = new BufferedReader(isr);
            String line;
            StringBuilder builder = new StringBuilder();
            while((line = br.readLine()) != null){
                builder.append(line);
            }
            br.close();
            isr.close();
            JsonObject jsonObject = new JsonParser().parse(builder.toString()).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("ads");
            Gson gson = new Gson();
            ArrayList<json_advert> advertBeanList = new ArrayList<>();
            //循环遍历
            for (JsonElement user : jsonArray) {
                //通过反射 得到UserBean.class
                json_advert userBean = gson.fromJson(user, new TypeToken<json_advert>() {}.getType());
                advertBeanList.add(userBean);
            }
            return advertBeanList;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

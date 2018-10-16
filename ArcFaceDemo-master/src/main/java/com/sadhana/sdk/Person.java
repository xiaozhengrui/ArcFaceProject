package com.sadhana.sdk;

import java.util.ArrayList;

public class Person {
    /**
     *  人员标识
     */
    private String mId;

    /**
     *  人脸特征
     */
    private byte[] mFeature;


    public Person(String id, byte[] feature) {
        mId = String.format("%s", id);
        mFeature = new byte[feature.length];
        System.arraycopy(feature, 0, mFeature, 0, feature.length);
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public byte[] getFeature() {
        return mFeature;
    }

    public void setFeature(byte[] feature) {
        this.mFeature = feature;
    }
}
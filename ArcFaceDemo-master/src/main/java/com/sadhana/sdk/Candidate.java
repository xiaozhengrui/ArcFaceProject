package com.sadhana.sdk;

public class Candidate {

    /**
     *  人员标识
     */
    private String mId;

    /**
     *  候选人相似度
     */
    private float mSimilarity;

    public Candidate(String id, float similarity) {
        mId = id;
        mSimilarity = similarity;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public float getSimilarity() {
        return mSimilarity;
    }

    public void setSimilarity(float similarity) {
        mSimilarity = similarity;
    }
}

package com.json_parse;

import java.net.URL;

//广告排期对应javaBean类
public class json_advert {
    private String ad_id;
    private Long start;
    private Long end;
    private String type;
    private String url;
    private String md5;

    public void setAd_id(String ad_id) {
        this.ad_id = ad_id;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public String getAd_id() {
        return ad_id;
    }

    public Long getStart() {
        return start;
    }

    public Long getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getMd5() {
        return md5;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}

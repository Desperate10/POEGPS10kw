package com.poe.dit82.poegps10;

public class Tp {

    private String tplnr;
    private String name;
    private double lat;
    private double lng;

    Tp() {}

    public void setTplnr(String tplnr) {
        this.tplnr = tplnr;
    }
    public String getTplnr() {
        return tplnr;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLat() {
        return lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
    public double getLng() {
        return lng;
    }
}

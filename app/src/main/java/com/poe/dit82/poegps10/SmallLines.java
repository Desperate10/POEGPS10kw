package com.poe.dit82.poegps10;

import java.util.ArrayList;
import java.util.List;

public class SmallLines {
    private int id;
    private int pid;
    private int invent;
    private String tplnr;
    private String name;
    List<Mast> mast = new ArrayList<>();

    SmallLines() {}

    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
    public int getPid() {
        return pid;
    }

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

    public void setMast(List<Mast> mast) {
        this.mast = mast;
    }
    public List<Mast> getMast() {
        return mast;
    }
}

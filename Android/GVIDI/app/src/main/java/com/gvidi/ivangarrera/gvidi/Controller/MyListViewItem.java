package com.example.ivangarrera.example.Controller;

public class MyListViewItem {
    private String expedition_name;
    private String guide_name;

    public MyListViewItem(String expedition_name, String guide_name) {
        super();
        this.expedition_name = expedition_name;
        this.guide_name = guide_name;
    }

    public String getExpeditionName() {
        return expedition_name;
    }

    public String getGuideName() {
        return guide_name;
    }
}

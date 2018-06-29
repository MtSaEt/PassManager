package com.example.anon.passmanager.model;

/**
 * Created by Anon on 2016-12-16.
 */

public class ItemType {
    private String name;
    private int icon;
    private boolean hasUsername; // assuming he has
    
    
    public ItemType(String name, Integer icon) {
        this.name = name;
        this.icon = icon;
        this.hasUsername = true;
    }

    public ItemType(String name, Integer icon, boolean hasUsername) {
        this.name = name;
        this.icon = icon;
        this.hasUsername = hasUsername;
    }
    
    public String getName() {
        return name;
    }
    
    public int getDrawable() {
        return icon;
    }

    public boolean hasUsername() {
        return hasUsername;
    }
}

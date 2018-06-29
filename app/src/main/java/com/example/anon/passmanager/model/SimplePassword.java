package com.example.anon.passmanager.model;

import java.io.Serializable;

/**
 * Created by Anon on 2016-12-15.
 */
public class SimplePassword implements Serializable {
    private String id, alias, name, password;
    private int type;
    private long date, lastModified;
    public int position = -1;
    public boolean isSelected = false;

    public SimplePassword(String id, String alias, String name, String password, int type, long date, long lastModified) {
        this.id = id;
        this.alias = alias;
        this.name = name;
        this.password = password;
        this.type = type;
        this.date = date;
        this.lastModified = lastModified;
    }
    
    public SimplePassword(SimplePassword simplePassword) {
        this (
                simplePassword.getId(),
                simplePassword.getAlias(),
                simplePassword.getName(), 
                simplePassword.getPassword(),
                simplePassword.getType(),
                simplePassword.getDate(),
                simplePassword.getLastModified()
        );
    }
    
    public SimplePassword() {}
    
    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }
}

package com.zane.smapiinstaller.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AppConfig {
    @Id(autoincrement = true) private Long id;
    @Unique
    private String name;
    private String value;
    @Generated(hash = 1859776450)
    public AppConfig(Long id, String name, String value) {
        this.id = id;
        this.name = name;
        this.value = value;
    }
    @Generated(hash = 136961441)
    public AppConfig() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return this.value;
    }
    public void setValue(String value) {
        this.value = value;
    }
}

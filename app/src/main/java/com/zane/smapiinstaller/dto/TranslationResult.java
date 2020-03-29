package com.zane.smapiinstaller.dto;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import org.greenrobot.greendao.annotation.Generated;

/**
 * @author Zane
 */
@Entity(indexes = {@Index(value = "origin, locale, translator asc", unique = true)})
public class TranslationResult {
    @Id(autoincrement = true) private Long id;
    private String origin;
    private String locale;
    private String translator;
    private String translation;
    private Long createTime;
    @Generated(hash = 1588069114)
    public TranslationResult(Long id, String origin, String locale,
            String translator, String translation, Long createTime) {
        this.id = id;
        this.origin = origin;
        this.locale = locale;
        this.translator = translator;
        this.translation = translation;
        this.createTime = createTime;
    }
    @Generated(hash = 2000565040)
    public TranslationResult() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getOrigin() {
        return this.origin;
    }
    public void setOrigin(String origin) {
        this.origin = origin;
    }
    public String getLocale() {
        return this.locale;
    }
    public void setLocale(String locale) {
        this.locale = locale;
    }
    public String getTranslation() {
        return this.translation;
    }
    public void setTranslation(String translation) {
        this.translation = translation;
    }
    public Long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    public String getTranslator() {
        return this.translator;
    }
    public void setTranslator(String translator) {
        this.translator = translator;
    }
}

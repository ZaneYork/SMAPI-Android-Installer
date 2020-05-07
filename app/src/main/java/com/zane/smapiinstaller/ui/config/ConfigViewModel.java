package com.zane.smapiinstaller.ui.config;

import android.view.View;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.hjq.language.LanguagesManager;
import com.zane.smapiinstaller.MainApplication;
import com.zane.smapiinstaller.constant.AppConfigKey;
import com.zane.smapiinstaller.entity.AppConfig;
import com.zane.smapiinstaller.entity.AppConfigDao;
import com.zane.smapiinstaller.entity.DaoSession;
import com.zane.smapiinstaller.entity.ModManifestEntry;
import com.zane.smapiinstaller.entity.TranslationResult;
import com.zane.smapiinstaller.entity.TranslationResultDao;
import com.zane.smapiinstaller.logic.CommonLogic;
import com.zane.smapiinstaller.logic.ListenableObject;
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.ConfigUtils;
import com.zane.smapiinstaller.utils.TranslateUtil;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import java9.util.Objects;
import java9.util.function.Predicate;
import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

class ConfigViewModel extends ViewModel implements ListenableObject<List<ModManifestEntry>> {

    @NonNull
    private final List<ModManifestEntry> modList;
    private List<ModManifestEntry> filteredModList;

    private String sortBy = "Name asc";

    public String getSortBy() {
        return sortBy;
    }

    private final View root;

    private final List<Predicate<List<ModManifestEntry>>> onChangedListener = new ArrayList<>();

    ConfigViewModel(View root) {
        this.root = root;
        this.modList = ModAssetsManager.findAllInstalledMods();
        translateLogic(root);
        MainApplication app = CommonLogic.getApplicationFromView(root);
        if (null != app) {
            AppConfig appConfig = ConfigUtils.getConfig(app, AppConfigKey.MOD_LIST_SORT_BY, sortBy);
            sortBy = appConfig.getValue();
        }
        sortLogic(sortBy);
    }

    public void switchSortBy(String sortBy) {
        MainApplication app = CommonLogic.getApplicationFromView(root);
        if (null == app) {
            return;
        }
        this.sortBy = sortBy;
        AppConfig appConfig = new AppConfig(null, AppConfigKey.MOD_LIST_SORT_BY, sortBy);
        ConfigUtils.saveConfig(app, appConfig);
        sortLogic(appConfig.getValue());
    }

    private void sortLogic(String sortBy) {
        switch (sortBy) {
            case "Name asc":
                Collections.sort(modList, (a, b) -> a.getName().compareTo(b.getName()));
                if (filteredModList != null && filteredModList != modList) {
                    Collections.sort(filteredModList, (a, b) -> a.getName().compareTo(b.getName()));
                }
                break;
            case "Name desc":
                Collections.sort(modList, (a, b) -> b.getName().compareTo(a.getName()));
                if (filteredModList != null && filteredModList != modList) {
                    Collections.sort(filteredModList, (a, b) -> b.getName().compareTo(a.getName()));
                }
                break;
            case "Date asc":
                Collections.sort(modList, (a, b) -> a.getLastModified().compareTo(b.getLastModified()));
                if (filteredModList != null && filteredModList != modList) {
                    Collections.sort(filteredModList, (a, b) -> a.getLastModified().compareTo(b.getLastModified()));
                }
                break;
            case "Date desc":
                Collections.sort(modList, (a, b) -> b.getLastModified().compareTo(a.getLastModified()));
                if (filteredModList != null && filteredModList != modList) {
                    Collections.sort(filteredModList, (a, b) -> b.getLastModified().compareTo(a.getLastModified()));
                }
                break;
            default:
                return;
        }
        if (filteredModList != null) {
            emitDataChangeEvent(filteredModList);
        } else {
            emitDataChangeEvent(modList);
        }
    }

    private void translateLogic(View root) {
        MainApplication app = CommonLogic.getApplicationFromView(root);
        if (null != app) {
            DaoSession daoSession = app.getDaoSession();
            AppConfig activeTranslator = ConfigUtils.getConfig(app, AppConfigKey.ACTIVE_TRANSLATOR, TranslateUtil.NONE);
            if (StringUtils.equals(activeTranslator.getValue(), TranslateUtil.NONE)) {
                String translator = activeTranslator.getValue();
                List<String> descriptions = StreamSupport.stream(this.modList).map(ModManifestEntry::getDescription).filter(Objects::nonNull).collect(Collectors.toList());
                String language = LanguagesManager.getAppLanguage(app).getLanguage();
                Query<TranslationResult> query = daoSession.getTranslationResultDao().queryBuilder().where(
                        TranslationResultDao.Properties.Origin.in(descriptions),
                        TranslationResultDao.Properties.Locale.eq(language),
                        TranslationResultDao.Properties.Translator.eq(translator)
                ).build();
                List<TranslationResult> translationResults = query.list();
                ImmutableMap<String, TranslationResult> translateMap = Maps.uniqueIndex(translationResults, TranslationResult::getOrigin);
                List<String> untranslatedText = StreamSupport.stream(modList).map(mod -> {
                    if (translateMap.containsKey(mod.getDescription())) {
                        mod.setTranslatedDescription(translateMap.get(mod.getDescription()).getTranslation());
                        return null;
                    } else {
                        return mod.getDescription();
                    }
                }).filter(Objects::nonNull).distinct().collect(Collectors.toList());
                if (!untranslatedText.isEmpty()) {
                    TranslateUtil.translateText(untranslatedText, translator, language, (result) -> {
                        CommonLogic.doOnNonNull(result, (results) -> {
                            daoSession.getTranslationResultDao().insertOrReplaceInTx(results);
                            ImmutableMap<String, TranslationResult> map = Maps.uniqueIndex(results, TranslationResult::getOrigin);
                            for (ModManifestEntry mod : modList) {
                                if (map.containsKey(mod.getDescription())) {
                                    mod.setTranslatedDescription(map.get(mod.getDescription()).getTranslation());
                                }
                            }
                            emitDataChangeEvent(modList);
                        });
                        return true;
                    });
                }
            }
        }
    }

    @NonNull
    public List<ModManifestEntry> getModList() {
        return modList;
    }

    public void removeAll(Predicate<ModManifestEntry> predicate) {
        for (int i = modList.size() - 1; i >= 0; i--) {
            if (predicate.test(modList.get(i))) {
                modList.remove(i);
            }
        }
    }

    public void filter(CharSequence text) {
        if (StringUtils.isBlank(text)) {
            filteredModList = modList;
        } else {
            filteredModList = StreamSupport.stream(modList).filter(mod -> {
                if (StringUtils.containsIgnoreCase(mod.getName(), text)) {
                    return true;
                }
                if (StringUtils.isNoneBlank(mod.getTranslatedDescription())) {
                    return StringUtils.containsIgnoreCase(mod.getTranslatedDescription(), text);
                } else {
                    return StringUtils.containsIgnoreCase(mod.getDescription(), text);
                }
            }).collect(Collectors.toList());
        }
        emitDataChangeEvent(filteredModList);
    }

    @Override
    public List<Predicate<List<ModManifestEntry>>> getOnChangedListenerList() {
        return onChangedListener;
    }
}
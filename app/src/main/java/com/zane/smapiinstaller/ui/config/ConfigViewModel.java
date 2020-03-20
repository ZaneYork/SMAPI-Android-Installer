package com.zane.smapiinstaller.ui.config;

import android.view.View;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
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
import com.zane.smapiinstaller.logic.ModAssetsManager;
import com.zane.smapiinstaller.utils.TranslateUtil;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

class ConfigViewModel extends ViewModel {

    @NonNull
    private List<ModManifestEntry> modList;
    private List<ModManifestEntry> filteredModList;

    private List<Predicate<List<ModManifestEntry>>> onChangedListener = new ArrayList<>();

    ConfigViewModel(View root) {
        this.modList = ModAssetsManager.findAllInstalledMods();
        translateLogic(root);
        Collections.sort(this.modList, (a, b) -> {
            if (a.getContentPackFor() != null && b.getContentPackFor() == null) {
                return 1;
            } else if (b.getContentPackFor() != null) {
                return -1;
            }
            return a.getName().compareTo(b.getName());
        });
    }

    private void translateLogic(View root) {
        MainApplication app = CommonLogic.getApplicationFromView(root);
        if (null != app) {
            DaoSession daoSession = app.getDaoSession();
            AppConfig activeTranslator = daoSession.getAppConfigDao().queryBuilder().where(AppConfigDao.Properties.Name.eq(AppConfigKey.ACTIVE_TRANSLATOR)).build().unique();
            if(activeTranslator != null) {
                String translator = activeTranslator.getValue();
                ArrayList<String> descriptions = Lists.newArrayList(Iterables.filter(Iterables.transform(this.modList, ModManifestEntry::getDescription), item -> item != null));
                String language = LanguagesManager.getAppLanguage(app).getLanguage();
                Query<TranslationResult> query = daoSession.getTranslationResultDao().queryBuilder().where(
                        TranslationResultDao.Properties.Origin.in(descriptions),
                        TranslationResultDao.Properties.Locale.eq(language),
                        TranslationResultDao.Properties.Translator.eq(translator)
                ).build();
                List<TranslationResult> translationResults = query.list();
                ImmutableMap<String, TranslationResult> translateMap = Maps.uniqueIndex(translationResults, TranslationResult::getOrigin);
                List<String> untranslatedText = Lists.newArrayList(Sets.newHashSet(Iterables.filter(Iterables.transform(modList, mod -> {
                    assert mod != null;
                    if (translateMap.containsKey(mod.getDescription())) {
                        mod.setTranslatedDescription(translateMap.get(mod.getDescription()).getTranslation());
                        return null;
                    } else {
                        return mod.getDescription();
                    }
                }), item -> item != null)));
                if (untranslatedText.size() > 0) {
                    TranslateUtil.translateText(untranslatedText, translator, language, (results) -> {
                        daoSession.getTranslationResultDao().insertOrReplaceInTx(results);
                        ImmutableMap<String, TranslationResult> map = Maps.uniqueIndex(results, TranslationResult::getOrigin);
                        for (ModManifestEntry mod : modList) {
                            if (map.containsKey(mod.getDescription())) {
                                mod.setTranslatedDescription(map.get(mod.getDescription()).getTranslation());
                            }
                        }
                        for (Predicate<List<ModManifestEntry>> listener : onChangedListener) {
                            listener.apply(modList);
                        }
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
            if (predicate.apply(modList.get(i))) {
                modList.remove(i);
            }
        }
    }

    /**
     * 注册列表变化监听器
     *
     * @param onChanged 回调
     */
    public void registerListChangeListener(Predicate<List<ModManifestEntry>> onChanged) {
        this.onChangedListener.add(onChanged);
    }

    public void filter(CharSequence text) {
        if(StringUtils.isBlank(text)) {
            filteredModList = modList;
        }
        else {
            filteredModList = Lists.newArrayList(Iterables.filter(modList, mod -> {
                if(StringUtils.containsIgnoreCase(mod.getName(), text)) {
                    return true;
                }
                if(StringUtils.isNoneBlank(mod.getTranslatedDescription())){
                    return StringUtils.containsIgnoreCase(mod.getTranslatedDescription(), text);
                }
                else {
                    return StringUtils.containsIgnoreCase(mod.getDescription(), text);
                }
            }));
        }
        for (Predicate<List<ModManifestEntry>> listener : onChangedListener) {
            listener.apply(filteredModList);
        }
    }
}
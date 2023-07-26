package com.zane.smapiinstaller.ui.config

import android.view.View
import androidx.lifecycle.ViewModel
import com.google.common.collect.Maps
import com.hjq.language.MultiLanguages
import com.zane.smapiinstaller.constant.AppConfigKeyConstants
import com.zane.smapiinstaller.entity.AppConfig
import com.zane.smapiinstaller.entity.ModManifestEntry
import com.zane.smapiinstaller.entity.TranslationResultDao
import com.zane.smapiinstaller.logic.CommonLogic.getApplicationFromView
import com.zane.smapiinstaller.logic.ListenableObject
import com.zane.smapiinstaller.logic.ModAssetsManager
import com.zane.smapiinstaller.utils.ConfigUtils
import com.zane.smapiinstaller.utils.TranslateUtil
import org.apache.commons.lang3.StringUtils

class ConfigViewModel(private val root: View) : ViewModel(),
    ListenableObject<List<ModManifestEntry>> {
    val modList: MutableList<ModManifestEntry> = ModAssetsManager.findAllInstalledMods()
    private var filteredModList: MutableList<ModManifestEntry>? = null
    var sortBy = "Name asc"
        private set
    override val onChangedListenerList: MutableList<(List<ModManifestEntry>) -> Boolean> =
        ArrayList()

    init {
        translateLogic(root)
        val app = getApplicationFromView(root)
        if (null != app) {
            val appConfig =
                ConfigUtils.getConfig(app, AppConfigKeyConstants.MOD_LIST_SORT_BY, sortBy)
            sortBy = appConfig.value
        }
        sortLogic(sortBy)
    }

    fun switchSortBy(sortBy: String) {
        val app = getApplicationFromView(root) ?: return
        this.sortBy = sortBy
        val appConfig = AppConfig(null, AppConfigKeyConstants.MOD_LIST_SORT_BY, sortBy)
        ConfigUtils.saveConfig(app, appConfig)
        sortLogic(appConfig.value)
    }

    private fun sortLogic(sortBy: String) {
        when (sortBy) {
            "Name asc" -> {
                modList.sortWith { a, b ->
                    a.name.compareTo(
                        b.name
                    )
                }
                if (filteredModList !== modList) {
                    filteredModList?.let {
                        it.sortWith { a, b ->
                            a.name.compareTo(
                                b.name
                            )
                        }
                    }
                }
            }

            "Name desc" -> {
                modList.sortWith { a, b ->
                    b.name.compareTo(
                        a.name
                    )
                }
                if (filteredModList !== modList) {
                    filteredModList?.let {
                        it.sortWith { a, b ->
                            b.name.compareTo(
                                a.name
                            )
                        }
                    }
                }
            }

            "Date asc" -> {
                modList.sortWith { a, b ->
                    a.lastModified.compareTo(
                        b.lastModified
                    )
                }
                if (filteredModList !== modList) {
                    filteredModList?.let {
                        it.sortWith { a, b ->
                            a.lastModified.compareTo(
                                b.lastModified
                            )
                        }
                    }
                }
            }

            "Date desc" -> {
                modList.sortWith { a, b ->
                    b.lastModified.compareTo(
                        a.lastModified
                    )
                }
                if (filteredModList !== modList) {
                    filteredModList?.let {
                        it.sortWith { a, b ->
                            b.lastModified.compareTo(
                                a.lastModified
                            )
                        }
                    }
                }
            }

            else -> return
        }
        filteredModList?.let {
            emitDataChangeEvent(it)
        } ?: emitDataChangeEvent(modList)
    }

    private fun translateLogic(root: View) {
        val app = getApplicationFromView(root)
        if (null != app) {
            val daoSession = app.daoSession
            val activeTranslator = ConfigUtils.getConfig(
                app, AppConfigKeyConstants.ACTIVE_TRANSLATOR, TranslateUtil.NONE
            )
            if (!StringUtils.equals(activeTranslator.value, TranslateUtil.NONE)) {
                val translator = activeTranslator.value
                val descriptions = modList.mapNotNull { obj -> obj.description }.toMutableList()
                val language = MultiLanguages.getAppLanguage().language
                val query = daoSession.translationResultDao.queryBuilder().where(
                    TranslationResultDao.Properties.Origin.`in`(descriptions),
                    TranslationResultDao.Properties.Locale.eq(language),
                    TranslationResultDao.Properties.Translator.eq(translator)
                ).build()
                val translationResults = query.list()
                val translateMap = Maps.uniqueIndex(translationResults) { obj -> obj!!.origin }
                val untranslatedText = modList.map { mod ->
                    if (translateMap.containsKey(mod.description)) {
                        mod.translatedDescription = translateMap[mod.description]!!.translation
                        return@map null
                    } else {
                        return@map mod.description
                    }
                }.filterNotNull().distinct().toMutableList()
                if (untranslatedText.isNotEmpty()) {
                    TranslateUtil.translateText(
                        untranslatedText, translator, language
                    ) { result ->
                        result?.let { results ->
                            daoSession.translationResultDao.insertOrReplaceInTx(results)
                            val map = Maps.uniqueIndex(results) { obj -> obj!!.origin }
                            for (mod in modList) {
                                if (map.containsKey(mod.description)) {
                                    mod.translatedDescription = map[mod.description]!!.translation
                                }
                            }
                            emitDataChangeEvent(modList)
                        }
                        true
                    }
                }
            }
        }
    }

    fun removeAll(predicate: (ModManifestEntry) -> Boolean) {
        for (i in modList.indices.reversed()) {
            if (predicate.invoke(modList[i])) {
                modList.removeAt(i)
            }
        }
    }

    fun filter(text: CharSequence?) {
        val list = if (StringUtils.isBlank(text)) {
            modList
        } else {
            modList.filter { mod ->
                if (StringUtils.containsIgnoreCase(mod.name, text)) {
                    true
                } else if (StringUtils.isNoneBlank(mod.translatedDescription)) {
                    StringUtils.containsIgnoreCase(mod.translatedDescription, text)
                } else {
                    StringUtils.containsIgnoreCase(mod.description, text)
                }
            }.toMutableList()
        }
        filteredModList = list
        emitDataChangeEvent(list)
    }
}
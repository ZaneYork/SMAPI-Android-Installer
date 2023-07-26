package com.zane.smapiinstaller.utils

import com.google.common.base.Joiner
import com.google.common.base.Predicate
import com.google.common.base.Splitter
import com.google.common.collect.Lists
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.zane.smapiinstaller.constant.Constants
import com.zane.smapiinstaller.dto.GoogleTranslationDto
import com.zane.smapiinstaller.dto.YouDaoTranslationDto
import com.zane.smapiinstaller.entity.TranslationResult
import org.apache.commons.lang3.StringUtils
import java.util.Locale

/**
 * @author Zane
 */
object TranslateUtil {
    const val NONE = "OFF"
    const val GOOGLE = "Google"
    const val YOU_DAO = "YouDao"
    fun translateText(
        textList: List<String>,
        translator: String?,
        locale: String?,
        resultCallback: Predicate<List<TranslationResult?>?>
    ) {
        if (textList.isEmpty()) {
            return
        }
        val pendingTextList = textList
            .filter { item -> StringUtils.isNoneBlank(item) && !item.contains("\n") }
            .toList()
        if (pendingTextList.isEmpty()) {
            return
        }
        val queryText = Joiner.on("%0A").join(pendingTextList)
        if (queryText.length > Constants.URL_LENGTH_LIMIT) {
            if (pendingTextList.size == 1) {
                return
            }
            val subListA = pendingTextList.subList(0, pendingTextList.size / 2)
            translateText(subListA, translator, locale, resultCallback)
            val subListB = pendingTextList.subList(pendingTextList.size / 2, pendingTextList.size)
            translateText(subListB, translator, locale, resultCallback)
        }
        if (StringUtils.equalsIgnoreCase(translator, YOU_DAO)) {
            if (!StringUtils.equalsAnyIgnoreCase(locale, Locale.CHINA.language)) {
                return
            }
            OkGo.get<String>(String.format(Constants.TRANSLATE_SERVICE_URL_YOUDAO, queryText))
                .execute(object : StringCallback() {
                    override fun onSuccess(response: Response<String>) {
                        val translationDto =
                            JsonUtil.fromJson(response.body(), YouDaoTranslationDto::class.java)
                        if (translationDto != null && translationDto.errorCode == 0) {
                            val lists = translationDto.translateResult
                            val translations: MutableList<TranslationResult?> =
                                ArrayList(lists.size)
                            for (list in lists) {
                                val result = TranslationResult()
                                result.origin = ""
                                result.locale = locale
                                result.translation = ""
                                result.translator = translator
                                result.createTime = System.currentTimeMillis()
                                for (entry in list) {
                                    if (entry == null) {
                                        continue
                                    }
                                    result.origin = result.origin + entry.src
                                    result.translation = result.translation + entry.tgt
                                }
                                translations.add(result)
                            }
                            resultCallback.apply(Lists.newArrayList(translations))
                        }
                    }
                })
        } else if (StringUtils.equalsIgnoreCase(translator, GOOGLE)) {
            OkGo.get<String>(
                String.format(
                    Constants.TRANSLATE_SERVICE_URL_GOOGLE,
                    locale,
                    queryText
                )
            ).execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    val translationDto =
                        JsonUtil.fromJson(response.body(), GoogleTranslationDto::class.java)
                    if (translationDto != null) {
                        val sourceText = Splitter.on("%0A").splitToList(queryText)
                        val translations: MutableList<TranslationResult?> =
                            ArrayList(sourceText.size)
                        for (source in sourceText) {
                            val result = TranslationResult()
                            result.origin = source
                            result.locale = locale
                            result.translation = source
                            result.translator = translator
                            result.createTime = System.currentTimeMillis()
                            for (entry in translationDto.sentences) {
                                val orig = StringUtils.strip(entry.orig, "\n")
                                val trans = StringUtils.strip(entry.trans, "\n")
                                result.translation =
                                    StringUtils.replace(result.translation, orig, trans)
                            }
                            translations.add(result)
                        }
                        resultCallback.apply(Lists.newArrayList(translations))
                    }
                }
            })
        }
    }
}
package com.zane.smapiinstaller.utils;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zane.smapiinstaller.constant.Constants;
import com.zane.smapiinstaller.dto.GoogleTranslationDto;
import com.zane.smapiinstaller.entity.TranslationResult;
import com.zane.smapiinstaller.dto.YouDaoTranslationDto;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java9.util.stream.Collectors;
import java9.util.stream.StreamSupport;

/**
 * @author Zane
 */
public class TranslateUtil {

    public static final String GOOGLE = "Google";
    public static final String YOU_DAO = "YouDao";

    public static void translateText(List<String> textList, String translator, String locale, Predicate<List<TranslationResult>> resultCallback) {
        if(textList == null || textList.size() == 0) {
            return;
        }
        textList = StreamSupport.stream(textList).filter(item -> StringUtils.isNoneBlank(item) && !item.contains("\n")).collect(Collectors.toList());
        if(textList.isEmpty()) {
            return;
        }
        String queryText = Joiner.on("%0A").join(textList);
        if(queryText.length() > Constants.URL_LENGTH_LIMIT) {
            if(textList.size() == 1) {
                return;
            }
            List<String> subListA = textList.subList(0, textList.size() / 2);
            translateText(subListA, translator, locale, resultCallback);
            List<String> subListB = textList.subList(textList.size() / 2, textList.size());
            translateText(subListB, translator, locale, resultCallback);
        }
        if(StringUtils.equalsIgnoreCase(translator, YOU_DAO)) {
            if (!StringUtils.equalsAnyIgnoreCase(locale, Locale.CHINA.getLanguage())) {
                return;
            }
            OkGo.<String>get(String.format(Constants.TRANSLATE_SERVICE_URL_YOUDAO, queryText)).execute(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    YouDaoTranslationDto translationDto = JSONUtil.fromJson(response.body(), YouDaoTranslationDto.class);
                    if (translationDto != null && translationDto.getErrorCode() == 0) {
                        List<List<YouDaoTranslationDto.Entry>> lists = translationDto.getTranslateResult();
                        List<TranslationResult> translations = new ArrayList<>(lists.size());
                        for (List<YouDaoTranslationDto.Entry> list : lists) {
                            TranslationResult result = new TranslationResult();
                            result.setOrigin("");
                            result.setLocale(locale);
                            result.setTranslation("");
                            result.setTranslator(translator);
                            result.setCreateTime(System.currentTimeMillis());
                            for (YouDaoTranslationDto.Entry entry : list) {
                                if (entry == null) {
                                    continue;
                                }
                                result.setOrigin(result.getOrigin() + entry.getSrc());
                                result.setTranslation(result.getTranslation() + entry.getTgt());
                            }
                            translations.add(result);
                        }
                        resultCallback.apply(Lists.newArrayList(translations));
                    }
                }
            });
        }
        else if(StringUtils.equalsIgnoreCase(translator, GOOGLE)) {
            OkGo.<String>get(String.format(Constants.TRANSLATE_SERVICE_URL_GOOGLE, locale, queryText)).execute(new StringCallback() {
                @Override
                public void onSuccess(Response<String> response) {
                    GoogleTranslationDto translationDto = JSONUtil.fromJson(response.body(), GoogleTranslationDto.class);
                    if(translationDto != null) {
                        List<String> sourceText = Splitter.on("%0A").splitToList(queryText);
                        List<TranslationResult> translations = new ArrayList<>(sourceText.size());
                        for (String source : sourceText) {
                            TranslationResult result = new TranslationResult();
                            result.setOrigin(source);
                            result.setLocale(locale);
                            result.setTranslation(source);
                            result.setTranslator(translator);
                            result.setCreateTime(System.currentTimeMillis());
                            for (GoogleTranslationDto.Entry entry: translationDto.getSentences()) {
                                String orig = StringUtils.strip(entry.getOrig(), "\n");
                                String trans = StringUtils.strip(entry.getTrans(), "\n");
                                result.setTranslation(StringUtils.replace(result.getTranslation(), orig, trans));
                            }
                            translations.add(result);
                        }
                        resultCallback.apply(Lists.newArrayList(translations));
                    }
                }
            });
        }
    }
}

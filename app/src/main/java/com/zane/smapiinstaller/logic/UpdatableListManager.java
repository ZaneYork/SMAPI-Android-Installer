package com.zane.smapiinstaller.logic;

import android.view.View;

import com.hjq.language.LanguagesManager;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.zane.smapiinstaller.entity.UpdatableList;
import com.zane.smapiinstaller.utils.FileUtils;
import com.zane.smapiinstaller.utils.JsonUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import java9.util.function.Predicate;

/**
 * 在线列表更新管理器
 * @author Zane
 * @param <T> 列表类型
 */
public class UpdatableListManager<T extends UpdatableList> implements ListenableObject<T> {
    private static final ConcurrentHashMap<Class<?>, Boolean> updateChecked = new ConcurrentHashMap<>();

    private static UpdatableList updatableList = null;

    private final List<Predicate<T>> onChangedListener = new ArrayList<>();

    /**
     * @param root      context容器
     * @param filename  本地文件名
     * @param tClass    目标类型
     * @param updateUrl 更新地址
     */
    public UpdatableListManager(View root, String filename, Class<T> tClass, String updateUrl) {
        updatableList = FileUtils.getLocaledAssetJson(root.getContext(), filename, tClass);
        Boolean updated = updateChecked.get(tClass);
        if(updated == null || !updated) {
            updateChecked.put(tClass, true);
            String languageSuffix = '.' + LanguagesManager.getAppLanguage(root.getContext()).getLanguage();
            updateList(root, tClass, updateUrl, filename, languageSuffix);
        }
    }

    private void updateList(View root, Class<T> tClass, String updateUrl, String filename, String languageSuffix) {
        String finalUpdateUrl = updateUrl + languageSuffix;
        String finalFilename = filename + languageSuffix;
        OkGo.<String>get(finalUpdateUrl).execute(new StringCallback(){
            @Override
            public void onError(Response<String> response) {
                if(StringUtils.isNoneBlank(languageSuffix)) {
                    updateList(root, tClass, updateUrl, filename, "");
                }
                super.onError(response);
            }

            @Override
            public void onSuccess(Response<String> response) {
                UpdatableList content = JsonUtil.fromJson(response.body(), tClass);
                if(content != null && updatableList.getVersion() < content.getVersion()) {
                    FileUtils.writeAssetJson(root.getContext(), finalFilename, content);
                    updatableList = content;
                    emitDataChangeEvent(getList());
                }
            }
        });
    }

    /**
     * @return 列表
     */
    public T getList() {
        return (T) updatableList;
    }

    @Override
    public List<Predicate<T>> getOnChangedListenerList() {
        return onChangedListener;
    }
}

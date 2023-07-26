package com.zane.smapiinstaller.logic

import android.view.View
import com.hjq.language.MultiLanguages
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.zane.smapiinstaller.entity.UpdatableList
import com.zane.smapiinstaller.utils.FileUtils
import com.zane.smapiinstaller.utils.JsonUtil
import org.apache.commons.lang3.StringUtils
import java.util.concurrent.ConcurrentHashMap

/**
 * 在线列表更新管理器
 * @author Zane
 * @param <T> 列表类型
</T> */
class UpdatableListManager<T : UpdatableList?>(
    root: View,
    filename: String,
    tClass: Class<T>,
    updateUrl: String
) : ListenableObject<T> {
    override val onChangedListenerList: MutableList<(T) -> Boolean> = ArrayList()

    /**
     * @param root      context容器
     * @param filename  本地文件名
     * @param tClass    目标类型
     * @param updateUrl 更新地址
     */
    init {
        updatableList = FileUtils.getLocaledAssetJson(root.context, filename, tClass)!!
        val updated = updateChecked[tClass]
        if (updated == null || !updated) {
            updateChecked[tClass] = true
            val languageSuffix = '.'.toString() + MultiLanguages.getAppLanguage().language
            updateList(root, tClass, updateUrl, filename, languageSuffix)
        }
    }

    private fun updateList(
        root: View,
        tClass: Class<T>,
        updateUrl: String,
        filename: String,
        languageSuffix: String
    ) {
        val finalUpdateUrl = updateUrl + languageSuffix
        val finalFilename = filename + languageSuffix
        OkGo.get<String>(finalUpdateUrl).execute(object : StringCallback() {
            override fun onError(response: Response<String>) {
                if (StringUtils.isNoneBlank(languageSuffix)) {
                    updateList(root, tClass, updateUrl, filename, "")
                }
                super.onError(response)
            }

            override fun onSuccess(response: Response<String>) {
                val content: UpdatableList? = JsonUtil.fromJson(response.body(), tClass)
                if (content != null && updatableList.version < content.version) {
                    FileUtils.writeAssetJson(root.context, finalFilename, content)
                    updatableList = content
                    list?.let { emitDataChangeEvent(it) }
                }
            }
        })
    }

    /**
     * @return 列表
     */
    val list: T
        get() = updatableList as T

    companion object {
        private val updateChecked = ConcurrentHashMap<Class<*>, Boolean>()
        private lateinit var updatableList: UpdatableList
    }
}
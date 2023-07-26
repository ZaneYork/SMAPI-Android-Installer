package com.zane.smapiinstaller.logic

/**
 * @author Zane
 */
interface ListenableObject<T> {
    /**
     * 返回一个当前已注册的监听器列表
     * @return 监听器列表
     */
    val onChangedListenerList: MutableList<(T) -> Boolean>

    /**
     * 注册数据变化监听器
     *
     * @param onChanged 回调
     */
    fun registerOnChangeListener(onChanged: (T) -> Boolean) {
        onChangedListenerList.add(onChanged)
    }

    /**
     * 发起数据变化事件
     * @param data 数据
     */
    fun emitDataChangeEvent(data: T) {
        for (listener in onChangedListenerList) {
            if (listener.invoke(data)) {
                return
            }
        }
    }
}
package com.zane.smapiinstaller.logic;

import java.util.List;

import java9.util.function.Predicate;

/**
 * @author Zane
 */
public interface ListenableObject<T> {
    /**
     * 返回一个当前已注册的监听器列表
     * @return 监听器列表
     */
    List<Predicate<T>> getOnChangedListenerList();
    /**
     * 注册数据变化监听器
     *
     * @param onChanged 回调
     */
    default void registerOnChangeListener(Predicate<T> onChanged) {
        getOnChangedListenerList().add(onChanged);
    }

    /**
     * 发起数据变化事件
     * @param data 数据
     */
    default void emitDataChangeEvent(T data) {
        for (Predicate<T> listener : getOnChangedListenerList()) {
            if(listener.test(data)) {
                return;
            }
        }
    }
}

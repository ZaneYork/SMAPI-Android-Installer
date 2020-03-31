package com.zane.smapiinstaller.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.request.base.Request;

import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Zane
 */
public abstract class JsonCallback<T> extends AbsCallback<T> {

    private TypeReference<T> type;
    private Class<T> clazz;

    public JsonCallback(TypeReference<T> type) {
        this.type = type;
    }

    public JsonCallback(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void onStart(Request<T, ? extends Request> request) {
        super.onStart(request);
    }

    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
     */
    @Override
    public T convertResponse(Response response) throws Throwable {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }
        T data = null;
        if (type != null) {
            data = JSONUtil.fromJson(body.string(), type);
        }
        if (clazz != null) {
            data = JSONUtil.fromJson(body.string(), clazz);
        }
        return data;
    }
}
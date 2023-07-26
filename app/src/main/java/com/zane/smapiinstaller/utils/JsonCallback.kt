package com.zane.smapiinstaller.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.lzy.okgo.callback.AbsCallback
import com.lzy.okgo.request.base.Request
import okhttp3.Response

/**
 * @author Zane
 */
abstract class JsonCallback<T> : AbsCallback<T> {
    private var type: TypeReference<T>? = null
    private var clazz: Class<T>? = null

    constructor(clazz: Class<T>?) {
        this.clazz = clazz
    }

    constructor(type: TypeReference<T>?) {
        this.type = type
    }

    override fun onStart(request: Request<T, out Request<*, *>?>?) {
        super.onStart(request)
    }

    /**
     * 该方法是子线程处理，不能做ui相关的工作
     * 主要作用是解析网络返回的 response 对象,生产onSuccess回调中需要的数据对象
     * 这里的解析工作不同的业务逻辑基本都不一样,所以需要自己实现,以下给出的时模板代码,实际使用根据需要修改
     */
    @Throws(Throwable::class)
    override fun convertResponse(response: Response): T? {
        val body = response.body() ?: return null
        var data: T? = null
        if (type != null) {
            data = JsonUtil.fromJson(body.string(), type)
        }
        if (clazz != null) {
            data = JsonUtil.fromJson(body.string(), clazz)
        }
        return data
    }
}
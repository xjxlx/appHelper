package com.android.helper.httpclient;

import android.text.TextUtils;

import com.android.helper.utils.LogUtil;
import com.android.helper.utils.NetworkUtil;

import java.io.IOException;

import io.reactivex.subscribers.DisposableSubscriber;
import okhttp3.ResponseBody;
import retrofit2.Response;

/**
 * Rxjava2 中的观察者对象
 *
 * @param <T>
 */
public abstract class BaseHttpSubscriber<T> extends DisposableSubscriber<T> {

    @Override
    protected void onStart() {
        super.onStart();
        // 可以在这里做一些初始化的工作，例如dialog的显示
        boolean network = NetworkUtil.getInstance().isNetworkConnected();
        if (!network) {
            onError(new Exception("网络连接异常，请检查网络连接是否正常"));

            // 如果网络是断开的，那么久停止后续的网络请求操作
            boolean disposed = isDisposed();
            LogUtil.e("网络是否请求中：" + disposed);
            if (isDisposed()) {
                dispose();
                LogUtil.e("网络请求的时候断网了，停止后续的网络请求！");
            }
        }
    }

    @Override
    public void onNext(T t) {
        //  这种数据类型是最全面的，里面包含了请求头、请求体所有的数据，如果用的到请求头信息的，就可以用Response去包裹一下对象，就能获取到所有的数据信息了
        if (t instanceof Response) {
            Response<?> response = (Response<?>) t;
            if (response.isSuccessful()) {
                // 如果是接口成功了，则直接返回成功的回调方法
                onSuccess(t);
            } else {
                // 如果失败了，则返回失败的回调方法
                String message = ((Response<?>) t).message();
                if (TextUtils.isEmpty(message)) {
                    ResponseBody errorBody = ((Response<?>) t).errorBody();
                    if (errorBody != null) {
                        try {
                            message = errorBody.string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                onError(new Exception(message));
            }
        } else {
            // 如果不是Response类型的数据，则直接返回成功的对象
            onSuccess(t);
        }
    }

    @Override
    public void onError(Throwable t) {
        onFailure(new BaseException(t));
    }

    @Override
    public void onComplete() {
        // 可以在这里做一些结束的工作，例如dialog的隐藏
    }

    /**
     * 成功的回调方法
     *
     * @param t 成功的对象
     */
    public abstract void onSuccess(T t);

    /**
     * 失败的回调方法
     *
     * @param e 失败的原因
     */
    public abstract void onFailure(BaseException e);
}

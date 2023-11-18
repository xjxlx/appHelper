package com.android.helper.base.title;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.android.common.base.BaseActivity;
import com.android.helper.R;
import com.android.helper.app.AppException;
import com.android.helper.base.recycleview.PlaceholderResource;
import com.android.helper.utils.NetworkUtil;
import com.android.helper.utils.TextViewUtil;
import com.android.helper.utils.ViewUtil;

/**
 * @author 流星
 * @CreateDate: 2021/11/29-1:41
 * @Description: 带标题头的Activity
 * <ol>
 *   使用说明：
 *       1：使用配置：
 *             为了方便使用书写代码，不用每次都自己去添加一个title，所有封装了这个页面，使用这个页面的时候，会自动的把title给加入到布局中去，
 *          但是，需要提前去初始化一个{@link PageLayoutBuilder }的配置信息，封装所有的title资源id，以便于去寻找对象，建议是在Application中去配置，
 *          供全局使用。
 * <p>
 *       2：view分层：
 *             大面上分为了上下两层，上面一层是title的布局，下面一层是activity真正使用的布局
 *       3：title布局
 *             ①：title的布局分为三个布局，左侧的是返回的布局，其中包括返回的按钮，和返回的文字说明，点击返回的时候，点击的是整个返回的父布局
 *             ②：中间的是一个title的具体布局的内容，可以手动去设置
 *             ③：右侧的是一个 RelativeLayout 布局，里面包含了一个textView,一般是用来设置设置文字，如果有其他的自定义需求的话，可以隐藏文字布局，
 *                然后给RelativeLayout 添加一个需要的布局，并去具体的设置以及使用。
 *       4: 左侧的点击事件，会回调方法{@link #onTitleLeftClick(View)}
 *       6：具体的Api设置方法，都在{@link PageLayoutBuilder }的方法中有具体的说明，可以去按需求使用
 */
public abstract class AppBaseTitleActivity extends BaseActivity {

    /**
     * 当前view的标题栏对象
     */
    private PageLayoutManager mPageLayoutManager = PageLayoutManager.getGlobalTitleBarBuilder()
            .build(this);
    /**
     * 顶部titleBar的布局
     */
    private ViewGroup mTitleBarLayout;
    /**
     * title下面contentView的对象
     */
    protected ViewGroup mContentLayout;

    /**
     * title右侧的资源布局View
     */
    private ViewGroup mRightLayout;
    /**
     * 右侧单独的标题view
     */
    private TextView mRightText;
    /**
     * 占位图的父布局
     */
    private ViewGroup mPlaceHolderLayout;
    protected View mTitleRootLayout;
    private boolean isLoading = true;// 是否加载占位图
    private PlaceholderResource mPlaceholder = PlaceholderResource.getGlobalPlaceholder();// 占位图
    private View mEmptyView;

    @SuppressLint("InflateParams")
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mPageLayoutManager != null) {
            // 获取title的根布局
            mTitleRootLayout = mPageLayoutManager.getTitleRootLayout();

            // 添加真实的activity
            int titleLayout = getTitleLayout();

            if (mTitleRootLayout != null) {
                // 获取顶部titleBar的布局
                mTitleBarLayout = mPageLayoutManager.getTitleBarLayout();

                // 返回的父类布局
                View leftBackLayout = mPageLayoutManager.getLeftBackLayout();
                // 左侧返回键的点击事件
                leftBackLayout.setOnClickListener(v -> {
                    boolean back = onTitleLeftClick(v);
                    if (back) {
                        finish();
                    }
                });

                // 设置中间的标题
                String titleContent = setTitleContent();
                if (!TextUtils.isEmpty(titleContent)) {
                    TextView title = mPageLayoutManager.getTitleView(); // 标题的内容
                    TextViewUtil.setText(title, titleContent);
                }

                // 加载占位
                boolean autoLoading = autoLoading();
                if (autoLoading) {
                    // 占位图可见，页面不可见
                    if (mPlaceHolderLayout != null) {
                        mPlaceHolderLayout.setVisibility(View.VISIBLE);
                    }
                    if (mContentLayout != null) {
                        mContentLayout.setVisibility(View.GONE);
                    }

                    // 检查网络
                    boolean networkConnected = NetworkUtil.getInstance().isNetworkConnected();
                    if (!networkConnected) {
                        // 设置无网的站位图
                        setPlaceHolderNetWork();
                    }
                }

                // 右侧标题的父布局
                mRightLayout = mPageLayoutManager.getRightLayout();
                // 右侧标题的textView
                mRightText = mPageLayoutManager.getRightTextView();

                // 获取title下面内容的根布局
                mContentLayout = mPageLayoutManager.getContentLayout();

                // 占位图的父布局
                mPlaceHolderLayout = mPageLayoutManager.getPlaceHolderLayout();

                if (titleLayout != 0) {
                    // 把真实的布局添加到 mFlActivityContent 中去
                    if (mContentLayout != null) {
                        LayoutInflater.from(this).inflate(titleLayout, mContentLayout, true);

                        // 设置布局
                        setContentView(mTitleRootLayout);

                        initListener();
                        initData(savedInstanceState);
                    }
                }
            }
        }
    }

    /**
     * @return 获取布局资源
     */
    protected abstract int getTitleLayout();

    /**
     * @return 设置标题内容
     */
    protected abstract String setTitleContent();

    /**
     * @return 获取顶部titleBar的布局
     */
    public View getTitleBarView() {
        return mTitleBarLayout;
    }

    /**
     * @return 获取titleBar下面真实使用到的activity的布局
     */
    public View getContentLayoutView() {
        return mContentLayout;
    }

    /**
     * 设置右侧的标题
     *
     * @param rightTitle 标题
     * @param color      颜色 ,必须是指定的Color 资源，不能是int资源，例如：R.color.xxx,应该是：ContextCompat.getColor(xxx)
     * @param size       大小，文字的大小，默认是sp的单位
     */
    protected void setRightTitle(String rightTitle, @ColorInt int color, int size) {
        if (!TextUtils.isEmpty(rightTitle)) {
            if (mRightText != null) {
                // 设置父布局可见
                ViewUtil.setViewVisible(mRightLayout, true);
                // 设置右侧标题可见
                ViewUtil.setViewVisible(mRightText, true);
                // 设置内容
                TextViewUtil.setText(mRightText, rightTitle);
                if (color != 0) {
                    mRightText.setTextColor(color);
                }
                if (size > 0) {
                    mRightText.setTextSize(size);
                }
            }
        }
    }

    /**
     * 设置右侧的标题
     *
     * @param rightTitle 右侧的title
     */
    protected void setRightTitle(String rightTitle) {
        setRightTitle(rightTitle, 0, 0);
    }

    /**
     * 设置右侧的标题
     *
     * @param rightTitle 右侧的title
     */
    protected void setRightTitle(String rightTitle, View.OnClickListener listener) {
        setRightTitle(rightTitle, 0, 0);

        // 点击事件
        if (listener != null) {
            mRightText.setOnClickListener(listener);
        }
    }

    /**
     * @param view 返回的的监听
     * @return 返回true, 可以直接结束页面，false:只相应事件，不结束页面，默认可以结束页面
     */
    protected boolean onTitleLeftClick(View view) {
        return true;
    }

    /**
     * @param titleBar 单个页面指定的titleBar的信息
     */
    public void setTitleBar(PageLayoutManager titleBar) {
        this.mPageLayoutManager = titleBar;
    }

    /**
     * 设置占位图，需要三种状态
     * 1：等待状态
     * 2：数据为空，或者接口异常
     * 3：网络异常
     *
     * @param placeHolder 设置占位图对象，如果不设置，就会使用默认的公用展位图
     */
    protected void setPlaceHolder(PlaceholderResource placeHolder) {
        if (placeHolder != null) {
            mPlaceholder = placeHolder;
        }
    }

    /**
     * @return 是否自动加载占位图
     */
    public boolean autoLoading() {
        return isLoading;
    }

    /**
     * 是指是否自动加载占位图
     *
     * @param isLoading true:自动加载，false:不加载
     */
    public void setAutoLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    /**
     * 加载中的动画
     */
    protected void showLoading() {

    }

    /**
     * 关闭动画
     */
    protected void closeLoading() {

    }

    /**
     * 设置数据成功的占位
     */
    public void setPlaceHolderSuccess() {
        closeLoading();// 关闭动画

        if (mPlaceHolderLayout != null) {
            mPlaceHolderLayout.setVisibility(View.GONE);
        }
        // 真实布局可见
        if (mContentLayout != null) {
            mContentLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 加载错误的站位图
     *
     * @param throwable 具体的错误
     */
    public void setPlaceHolderError(Throwable throwable) {
        closeLoading();// 关闭动画

        // 在此处去处理错误的区分
        String exception = AppException.exception(throwable);
        if (!TextUtils.isEmpty(exception)) {
            if (mPlaceholder != null) {
                mPlaceholder.setErrorContent(exception);
                // 设置错误的站位图
                if (mPlaceHolderLayout != null) {
                    // 空布局可见
                    mPlaceHolderLayout.setVisibility(View.VISIBLE);
                    // 真实布局不可见
                    if (mContentLayout != null) {
                        mContentLayout.setVisibility(View.GONE);
                    }

                    if (mPlaceholder != null) {
                        // 如果没有view，就去添加view
                        mEmptyView = mPlaceholder.getRootView(mPlaceHolderLayout);
                        if (mEmptyView != null) {
                            mPlaceHolderLayout.addView(mEmptyView);
                        }

                        // 错误数据
                        int errorImage = mPlaceholder.getErrorResource();
                        String errorContent = mPlaceholder.getErrorContent();
                        int errorTitleColor = mPlaceholder.getErrorTitleColor();
                        float errorTitleSize = mPlaceholder.getErrorTitleSize();

                        if (mEmptyView != null) {
                            // 底部可见
                            TextView ivBaseErrorPlaceholder = mEmptyView.findViewById(R.id.iv_base_error_placeholder);
                            if (ivBaseErrorPlaceholder != null) {
                                ivBaseErrorPlaceholder.setVisibility(View.VISIBLE);
                            }

                            // 错误图片对象
                            ImageView ivBasePlaceholderImage = mEmptyView.findViewById(R.id.iv_base_placeholder_image);
                            if (ivBasePlaceholderImage != null) {
                                if (errorImage > 0) {
                                    ivBasePlaceholderImage.setImageResource(errorImage);
                                }
                            }

                            // 错误的文字
                            TextView tvBasePlaceholderMsg = mEmptyView.findViewById(R.id.tv_base_placeholder_msg);
                            if (tvBasePlaceholderMsg != null) {
                                // 文字
                                TextViewUtil.setText(tvBasePlaceholderMsg, errorContent);
                                // 大小
                                if (errorTitleSize != 0) {
                                    tvBasePlaceholderMsg.setTextSize(errorTitleSize);
                                }
                                // 颜色
                                if (errorTitleColor != 0) {
                                    tvBasePlaceholderMsg.setTextColor(errorTitleColor);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 空数据的操作
     */
    public void setPlaceHolderEmpty() {
        closeLoading();

        if (mPlaceHolderLayout != null) {
            // 空布局可见
            mPlaceHolderLayout.setVisibility(View.VISIBLE);
            // 真实布局不可见
            if (mContentLayout != null) {
                mContentLayout.setVisibility(View.GONE);
            }

            if (mPlaceholder != null) {
                // 如果没有添加到view中，就去主动添加一次
                mEmptyView = mPlaceholder.getRootView(mPlaceHolderLayout);
                if (mEmptyView != null) {
                    mPlaceHolderLayout.addView(mEmptyView);
                }

                // 空数据
                int pageEmptyResource = mPlaceholder.getPageEmptyResource();
                String pageEmptyContent = mPlaceholder.getPageEmptyContent();
                float pageEmptyContentSize = mPlaceholder.getPageEmptyTitleSize();
                int pageEmptyContentColor = mPlaceholder.getPageEmptyTitleColor();

                if (mEmptyView != null) {
                    // 隐藏底部重新请求的按钮
                    TextView refreshView = mPlaceholder.getRefreshView();
                    if (refreshView != null) {
                        refreshView.setVisibility(View.GONE);
                    }

                    // 空布局的图片对象
                    // 设置空布局图片
                    if (pageEmptyResource > 0) {
                        ImageView placeHolderView = mPlaceholder.getPlaceHolderView();
                        if (placeHolderView != null) {
                            placeHolderView.setImageResource(pageEmptyResource);
                        }
                    }

                    // 空布局的文字
                    TextView messageView = mPlaceholder.getMessageView();
                    if (messageView != null) {
                        // 文字
                        TextViewUtil.setText(messageView, pageEmptyContent);
                        // 大小
                        if (pageEmptyContentSize != 0) {
                            messageView.setTextSize(pageEmptyContentSize);
                        }
                        // 颜色
                        if (pageEmptyContentColor != 0) {
                            messageView.setTextColor(pageEmptyContentColor);
                        }
                    }
                }
            }
        }
    }

    /**
     * 断网的操作
     */
    public void setPlaceHolderNetWork() {
        if (mPlaceHolderLayout != null) {
            // 空布局可见
            mPlaceHolderLayout.setVisibility(View.VISIBLE);
            // 真实布局不可见
            if (mContentLayout != null) {
                mContentLayout.setVisibility(View.GONE);
            }

            if (mPlaceholder != null) {
                // 如果没有view，就去添加view
                mEmptyView = mPlaceholder.getRootView(mPlaceHolderLayout);
                if (mEmptyView != null) {
                    mPlaceHolderLayout.addView(mEmptyView);
                }

                // 断网数据
                int noNetWorkImage = mPlaceholder.getNoNetWorkImage();
                String errorContent = mPlaceholder.getNoNetWorkButtonContent();
                int errorTitleColor = mPlaceholder.getNoNetWorkTitleColor();
                float errorTitleSize = mPlaceholder.getNoNetWorkTitleSize();

                if (mEmptyView != null) {
                    // 底部可见
                    TextView refreshView = mPlaceholder.getRefreshView();
                    if (refreshView != null) {
                        refreshView.setVisibility(View.VISIBLE);
                    }

                    // 错误图片对象
                    ImageView placeHolderView = mPlaceholder.getPlaceHolderView();
                    if (placeHolderView != null) {
                        if (noNetWorkImage > 0) {
                            placeHolderView.setImageResource(noNetWorkImage);
                        }
                    }

                    // 错误的文字
                    TextView messageView = mPlaceholder.getMessageView();
                    if (messageView != null) {
                        // 文字
                        TextViewUtil.setText(messageView, errorContent);
                        // 大小
                        if (errorTitleSize != 0) {
                            messageView.setTextSize(errorTitleSize);
                        }
                        // 颜色
                        if (errorTitleColor != 0) {
                            messageView.setTextColor(errorTitleColor);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        onTitleLeftClick(null);
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mTitleRootLayout != null) {
            mTitleRootLayout = null;
        }

        if (mRightText != null) {
            mRightText = null;
        }
        if (mPageLayoutManager != null) {
            mPageLayoutManager = null;
        }
    }

    /***********************************************************************************************************************************************/

    /**
     * 页面的返回
     *
     * @param backId    返回的id
     * @param listeners 返回的点击事件
     */
    public void setTitleBack(int backId, View.OnClickListener listeners) {
        if (backId != 0) {
            if (listeners != null) {
                findViewById(backId).setOnClickListener(listeners);
            } else {
                setTitleBack(backId);
            }
        }
    }

    /**
     * 指定id的单纯页面返回
     *
     * @param backId 返回的id
     */
    public void setTitleBack(int backId) {
        if (backId != 0) {
            findViewById(backId).setOnClickListener(v -> finish());
        }
    }

    /**
     * 设置标题
     *
     * @param titleId      标题控件的id
     * @param titleContent 标题的内容
     */
    public void setTitleContent(int titleId, String titleContent) {
        if (titleId != 0) {
            View view = findViewById(titleId);
            if (view instanceof TextView) {
                TextView titleView = (TextView) view;
                TextViewUtil.setText(titleView, titleContent);
            }
        }
    }

}

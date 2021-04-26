package com.android.helper.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.widget.RemoteViews;

import androidx.annotation.DrawableRes;
import androidx.core.app.NotificationCompat;

import com.android.helper.R;
import com.android.helper.interfaces.listener.ViewCallBackListener;

public class NotificationUtil {

    /**
     * 点击通知时候跳转的请求码
     */
    public static final int CODE_JUMP_REQUEST = 1000;

    /**
     * handler的单独发送
     */
    public static final int CODE_WHAT_SEND_START_FOREGROUND = CODE_JUMP_REQUEST + 1;
    /**
     * handler的轮询发送
     */
    public static final int CODE_WHAT_SEND_START_FOREGROUND_LOOP = CODE_WHAT_SEND_START_FOREGROUND + 1;

    private final Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static NotificationUtil util;
    private Intent mIntentActivity;
    private Intent mIntentService;

    // 消息对象
    private Notification mNotification;
    private String mTickerText;     //  消息设置首次出现的名字
    private Uri mContentSound;             // 消息声音的uri
    private String mContentTitle;          // 消息标题头
    private String mContentText;           // 消息内容
    private int mContentSmallIcon;         // 消息的图标
    private int mNotificationNumber;       // 消息的数量
    private int mNotificationLevel;        // 消息的等级
    private boolean isLockScreenVisibility = true; // 是否打开锁屏通知,默认是打开的
    private PendingIntent pendingIntent;

    /**
     * 自定义震动
     * <p>
     * vibrate属性是一个长整型的数组，用于设置手机静止和振动的时长，以毫秒为单位。
     * 参数中下标为0的值表示手机静止的时长，下标为1的值表示手机振动的时长， 下标为2的值又表示手机静止的时长，以此类推。
     */
    private final long[] vibrates = {0, 1000, 1000, 1000};

    private String mChannelDescription;                 // 渠道的描述
    private String mChannelName;                        // 渠道的名字
    private NotificationManager manager;
    private int mRemoteViewsLayout;                     // 状态栏布局
    private Service mService;                           // 服务类
    private long mIntervalTime;                         // 轮询的间隔
    private ViewCallBackListener<RemoteViews> mViewCallBackListener;

    private NotificationUtil(Context context) {
        this.mContext = context;
        if (mContext != null) {
            // 创建管理器
            manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public static NotificationUtil getInstance(Context context) {
        if (util == null) {
            util = new NotificationUtil(context);
        }
        return util;
    }

    public NotificationUtil setActivity(Class<? extends Activity> activityCls) {
        mIntentActivity = new Intent(mContext, activityCls);
        mIntentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return util;
    }

    public NotificationUtil setService(Class<? extends Service> serviceCls) {
        mIntentService = new Intent(mContext, serviceCls);
        mIntentService.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return util;
    }

    /**
     * 通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
     */
    public NotificationUtil setTickerText(String tickerText) {
        //通知首次出现在通知栏，带上升动画效果的，可设置文字，图标
        this.mTickerText = tickerText;
        return util;
    }

    /**
     * 设置标题头
     *
     * @param title 标题头
     */
    public NotificationUtil setContentTitle(String title) {
        this.mContentTitle = title;
        return util;
    }

    public NotificationUtil setContentText(String text) {
        this.mContentText = text;
        return util;
    }

    public NotificationUtil setSmallIcon(@DrawableRes int icon) {
        this.mContentSmallIcon = icon;
        return util;
    }

    /**
     * @param description 渠道的描述
     * @return 可见的渠道的描述
     */
    public NotificationUtil setChannelDescription(String description) {
        this.mChannelDescription = description;
        return util;
    }

    /**
     * @param channelName 渠道的名字
     * @return 设置渠道的名字
     */
    public NotificationUtil setChannelName(String channelName) {
        this.mChannelName = channelName;
        return util;
    }

    /**
     * 设置声音，使用这个方法的话，必须在资源目录下设置一个raw的目录，资源设置的话，也需要R.raw.xx,例如： R.raw.tougong
     *
     * @param sound 声音的路径，使用
     */
    public NotificationUtil setSound(@DrawableRes int sound) {
        if (mContext != null) {
            // 自定义声音
            Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + mContext.getPackageName() + "/" + sound);
            if (uri != null) {
                this.mContentSound = uri;
            }
        }
        return util;
    }

    public NotificationUtil setNumber(int number) {
        this.mNotificationNumber = number;
        return util;
    }

    /**
     * @return 是否锁屏可见，true：可见，false：不可见，默认可见
     */
    public NotificationUtil setLockScreenVisibility(boolean lockScreenVisibility) {
        this.isLockScreenVisibility = lockScreenVisibility;
        return util;
    }

    /**
     * @param level 消息通知的等级，7.0以下使用 {@link Notification#PRIORITY_HIGH } ，7.0以上使用 {@link NotificationManager#IMPORTANCE_HIGH }去设置
     * @RequiresApi(api = Build.VERSION_CODES.N)  7.0 以上使用
     */
    public NotificationUtil setNotificationLevel(int level) {
        this.mNotificationLevel = level;
        return util;
    }

    /**
     * @param layoutId 布局的资源
     * @return 设置消息的通知栏布局
     */
    public <T> NotificationUtil setRemoteView(int layoutId, ViewCallBackListener<RemoteViews> callBackListener) {
        this.mRemoteViewsLayout = layoutId;
        this.mViewCallBackListener = callBackListener;
        return util;
    }

    /**
     * 创建消息的对象
     */
    public NotificationUtil createNotification() {

        if (mContext != null) {

            // 跳转的activity意图
            if (mIntentActivity != null) {
                pendingIntent = PendingIntent.getActivity(mContext, CODE_JUMP_REQUEST, mIntentActivity, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            if (mIntentService != null) {
                pendingIntent = PendingIntent.getService(mContext, CODE_JUMP_REQUEST, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
            }

            // 使用用户的包名作为渠道的id，保证唯一性
            String channelId = mContext.getPackageName();

            // 当SDK大于16且小于26的时候
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);

                // 首次出现的提示
                if (!TextUtils.isEmpty(mTickerText)) {
                    builder.setTicker(mTickerText);
                }

                // 消息的标题
                if (!TextUtils.isEmpty(mContentTitle)) {
                    builder.setContentTitle(mContentTitle);
                }

                // 消息的内容
                if (!TextUtils.isEmpty(mContentText)) {
                    builder.setContentText(mContentText);
                }

                // 消息的图标
                if (mContentSmallIcon != 0) {
                    builder.setSmallIcon(mContentSmallIcon);
                }

                // 消息的声音
                if (mContentSound != null) {
                    builder.setSound(mContentSound);
                }

                // 设置消息的数量
                if (mNotificationNumber > 0) {
                    builder.setNumber(mNotificationNumber);
                }

                //点击之后的页面
                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                }

                // 悬浮通知
                if (pendingIntent != null) {
                    builder.setFullScreenIntent(pendingIntent, true);
                }

                // 设置通知的等级
                if (mNotificationLevel == 0) {
                    mNotificationLevel = Notification.PRIORITY_HIGH;
                }

                // 锁屏可见
                if (isLockScreenVisibility) {
                    builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                }

                mNotification =
                        builder
                                .setVibrate(vibrates) // 震动
                                .setLights(Color.GREEN, 1000, 1000) // 灯光
                                .setPriority(mNotificationLevel)// 设置优先级
                                .build();

            } else {    // 当SDK大于26的时候

                CharSequence channelName = "";

                // 渠道的名字，如果为空，则用app的名字
                if (TextUtils.isEmpty(mChannelName)) {
                    // 使用app的名字作为渠道的名字，用户可以看到的通知渠道的名字.
                    channelName = mContext.getResources().getString(R.string.app_name);
                }

                // 设置通知的等级
                if (mNotificationLevel == 0) {
                    mNotificationLevel = NotificationManager.IMPORTANCE_HIGH;
                }
                // 渠道对象
                NotificationChannel mChannel = new NotificationChannel(channelId, channelName, mNotificationLevel);

                if (TextUtils.isEmpty(mChannelDescription)) {
                    // 默认的渠道描述
                    mChannelDescription = channelName + "的渠道描述";
                }

                // 消息通知的描述 , 用户可以看到的通知渠道的描述
                mChannel.setDescription(mChannelDescription);

                // 设置通知出现时的闪灯（如果 android 设备支持的话）
                mChannel.enableLights(true);
                mChannel.setLightColor(Color.RED);

                // 设置通知出现时的震动（如果 android 设备支持的话）
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(vibrates);

                mChannel.setShowBadge(true);//显示logo

                // 锁屏可见
                if (isLockScreenVisibility) {
                    mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC); //设置锁屏可见 VISIBILITY_PUBLIC=可见
                }

                // 通知Manager去创建渠道
                manager.createNotificationChannel(mChannel);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);

                // 首次出现的提示
                if (!TextUtils.isEmpty(mTickerText)) {
                    builder.setTicker(mTickerText);
                }

                // 消息的标题
                if (!TextUtils.isEmpty(mContentTitle)) {
                    builder.setContentTitle(mContentTitle);
                }

                // 消息的内容
                if (!TextUtils.isEmpty(mContentText)) {
                    builder.setContentText(mContentText);
                }

                // 消息的图标
                if (mContentSmallIcon != 0) {
                    builder.setSmallIcon(mContentSmallIcon);
                }

                // 消息的声音
                if (mContentSound != null) {
                    builder.setSound(mContentSound);
                }

                // 设置消息的数量
                if (mNotificationNumber > 0) {
                    builder.setNumber(mNotificationNumber);
                }

                //点击之后的页面
                if (pendingIntent != null) {
                    builder.setContentIntent(pendingIntent);
                }

                // 悬浮通知
                if (pendingIntent != null) {
                    builder.setFullScreenIntent(pendingIntent, true);
                }

                mNotification =
                        builder
                                .setVibrate(vibrates) // 震动
                                .setPriority(mNotificationLevel)// 设置优先级
                                .setLights(Color.GREEN, 100, 100) // 灯光
                                .setChannelId(channelId)// 设置渠道
                                .build();
            }

            // 通知栏的状态布局
            if (mRemoteViewsLayout != 0) {
                RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), mRemoteViewsLayout);
                if (mIntentActivity != null) {
                    pendingIntent = PendingIntent.getActivity(mContext, CODE_JUMP_REQUEST, mIntentActivity, PendingIntent.FLAG_UPDATE_CURRENT);

                    //点击图片进入页面DemoActivity2
                    remoteViews.setOnClickPendingIntent(remoteViews.getLayoutId(), pendingIntent);
                }
                mNotification.contentView = remoteViews;

                // 把布局回调回去
                mViewCallBackListener.callBack(null, remoteViews);
            }
        }
        return util;
    }

    /**
     * @param id 消息的id
     * @return 发送一个消息
     */
    public NotificationUtil sendNotification(int id) {
        // 发送消息通知
        if ((manager != null) && (mNotification != null)) {
            manager.notify(id, mNotification);
        }
        return util;
    }

    public Notification getNotification() {
        return mNotification;
    }

    /**
     * @param service 指定的服务类型
     * @return 开启前台服务
     */
    public NotificationUtil startForeground(int id, Service service) {
        if (service != null) {
            if (id > 0) {
                this.mService = service;

                Message message = mHandler.obtainMessage();
                message.what = CODE_WHAT_SEND_START_FOREGROUND;
                message.arg1 = id;
                mHandler.sendMessage(message);
            } else {
                LogUtil.e("发送通知的id不能为0！");
            }
        }
        return util;
    }

    /**
     * 开始轮询的发送服务的通知，避免间隔的时间长了，服务被误判，停止联网的操作
     *
     * @param intervalTime 每次间隔的时间
     * @param service      指定的服务
     */
    @SuppressLint("CheckResult")
    public void startLoopForeground(int id, long intervalTime, Service service) {
        if (service != null) {
            if (id > 0) {
                this.mService = service;
                this.mIntervalTime = intervalTime;

                Message message = mHandler.obtainMessage();
                message.what = CODE_WHAT_SEND_START_FOREGROUND_LOOP;
                message.arg1 = id;
                mHandler.sendMessage(message);
            } else {
                LogUtil.e("发送通知的id不能为0！");
            }
        }
    }

    /**
     * 停止轮询服务的发送
     */
    public void stopLoopForeground() {
        mHandler.removeCallbacksAndMessages(null);
        LogUtil.e("停止了轮训消息的发送！");
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            // 先停止之前的消息发送，避免数据的快速轮询
            stopLoopForeground();

            if (mService != null) {
                int id = msg.arg1;

                switch (msg.what) {
                    case CODE_WHAT_SEND_START_FOREGROUND:
                        LogUtil.e("开始了服务消息的单独发送！");
                        mService.startForeground(id, mNotification);

                        break;

                    case CODE_WHAT_SEND_START_FOREGROUND_LOOP:
                        LogUtil.e("开始了服务消息的轮询发送！");
                        mService.startForeground(id, mNotification);

                        Message message = mHandler.obtainMessage();
                        message.what = CODE_WHAT_SEND_START_FOREGROUND_LOOP;
                        message.arg1 = id;
                        mHandler.sendMessageDelayed(message, mIntervalTime);
                        break;
                }
            }

        }
    };

}

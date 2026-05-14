package com.example.anifocus.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.anifocus.R;
import com.example.anifocus.ui.main.MainActivity;

public class NotificationHelper {
    private static final String CHANNEL_ID = "anifocus_channel";
    private static final String CHANNEL_NAME = "AniFocus 学习提醒";
    private static final int CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT;

    private final Context context;
    private final NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    CHANNEL_IMPORTANCE
            );
            channel.setDescription("AniFocus 学习打卡提醒通知");
            channel.enableLights(true);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public void sendStudyReminder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("AniFocus 学习提醒")
                .setContentText("今天还没有完成学习任务哦，快去学习吧！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("今天还没有完成学习任务哦～\n记得要好好学习，天天向上呀！"));

        NotificationManagerCompat.from(context).notify(1001, builder.build());
    }

    public void sendDailyCheckInReminder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("AniFocus 每日打卡")
                .setContentText("今天的打卡还没有完成，快去打卡吧！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("今天的打卡还没有完成～\n连续打卡天数可不能断哦！"));

        NotificationManagerCompat.from(context).notify(1002, builder.build());
    }

    public void sendDeadlineReminder(String taskTitle) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("任务截止提醒")
                .setContentText("「" + taskTitle + "」即将截止，请尽快完成！")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("任务「" + taskTitle + "」即将截止！\n不要忘记完成它哦～"));

        NotificationManagerCompat.from(context).notify(1003, builder.build());
    }

    public void sendCheckInSuccess(int streakDays) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("打卡成功！")
                .setContentText("已连续打卡 " + streakDays + " 天，继续保持！")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createPendingIntent())
                .setAutoCancel(true);

        NotificationManagerCompat.from(context).notify(1004, builder.build());
    }
}

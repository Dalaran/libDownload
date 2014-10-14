package com.sjwyx.app.libao.down;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.sjwyx.app.libao.R;
import com.sjwyx.app.libao.activity.GameDownManagerActivity;

public class DownloadNotificationListener implements DownloadListener {

	private Context mContext;

	private Notification mNotification;

	private int mId;

	private NotificationManager mNotificationManager;

	private int mProgress = 0;

	public DownloadNotificationListener(Context context, DownloadTask task) {
		mContext = context;
		mId = task.getUrl().hashCode();
		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = initNotifiction(task.getTitle());
	}

	@Override
	public void onDownloadStop() {
		mNotification.contentView.setTextViewText(R.id.notify_state, "暂停");

		mNotificationManager.notify(mId, mNotification);
		mNotificationManager.cancel(mId);
	}

	@Override
	public void onDownloadStart() {
		mNotificationManager.notify(mId, mNotification);
	}

	@Override
	public void onDownloadProgress(long finishedSize, long totalSize, long speed) {
		int percent = (int) (finishedSize * 100 / totalSize);
		if (percent - mProgress > 1) { // 降低状态栏进度刷新频率，性能问题
			mProgress = percent;
			mNotification.contentView.setTextViewText(R.id.notify_state, "下载:"
					+ mProgress + "%   速度:" + speed + "KB/S");
			mNotification.contentView.setProgressBar(R.id.notify_processbar,
					100, percent, false);
			mNotificationManager.notify(mId, mNotification);
		}
	}

	@Override
	public void onDownloadPause() {
		mNotification.contentView.setTextViewText(R.id.notify_state, "暂停");
		mNotification.contentView.setProgressBar(R.id.notify_processbar, 100,
				0, true);
		mNotificationManager.notify(mId, mNotification);
	}

	@Override
	public void onDownloadFinish(String filepath) {
		mNotification.icon = android.R.drawable.stat_sys_download_done;
		mNotification.contentView.setTextViewText(R.id.notify_state, "下载完成");
		mNotification.contentView.setProgressBar(R.id.notify_processbar, 100,
				100, false);
		mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.defaults |= Notification.DEFAULT_LIGHTS;

		Intent intent = new Intent(mContext, GameDownManagerActivity.class);
		mNotification.contentIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mNotificationManager.notify(mId, mNotification);
	}

	@Override
	public void onDownloadFail(String errorMsg) {
		mNotification.contentView.setTextViewText(R.id.notify_state, errorMsg);
		mNotification.contentView.setProgressBar(R.id.notify_processbar, 100,
				0, true);
		mNotificationManager.notify(mId, mNotification);
		mNotificationManager.cancel(mId);
	}

	public Notification initNotifiction(String title) {
		Notification notification = new Notification(
				android.R.drawable.stat_sys_download, "准备下载 " + title,
				System.currentTimeMillis());
		notification.contentView = new RemoteViews(mContext.getPackageName(),
				R.layout.notify_download);
		notification.contentView.setProgressBar(R.id.notify_processbar, 100, 0,
				false);
		notification.contentView.setTextViewText(R.id.notify_state, "准备下载");
		notification.contentView.setTextViewText(R.id.notify_text, title);
		Intent intent = new Intent(mContext, GameDownManagerActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		notification.contentIntent = PendingIntent.getActivity(mContext, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		return notification;

	}
}

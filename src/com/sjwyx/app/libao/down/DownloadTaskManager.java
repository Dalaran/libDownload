package com.sjwyx.app.libao.down;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import android.content.Context;
import android.webkit.URLUtil;

import com.sjwyx.app.libao.utils.AppDataManager;

public class DownloadTaskManager {
	// private static final String TAG = "DownloadTaskManager";

	/**
	 * default save path: /sdcard/download
	 */
	private final String DEFAULT_FILE_PATH;

	/**
	 * single instance
	 */
	private static DownloadTaskManager sMe;

	private final static Object syncObj = new Object();
	// 同时任务下载量
	public final static int MaxTaskCount = 3;

	/**
	 * Download Database Helper
	 */
	private DownloadDBHelper mDownloadDBHelper;

	/**
	 * one download task own a download worker
	 */
	private HashMap<DownloadTask, DownloadOperator> mDownloadMap;

	private HashMap<DownloadTask, CopyOnWriteArraySet<DownloadListener>> mDownloadListenerMap;

	public HashMap<DownloadTask, DownloadOperator> getmDownloadMap() {
		return mDownloadMap;
	}

	/**
	 * private constructor
	 * 
	 * @param context
	 */
	private DownloadTaskManager(Context context) {
		mDownloadMap = new HashMap<DownloadTask, DownloadOperator>();
		mDownloadListenerMap = new HashMap<DownloadTask, CopyOnWriteArraySet<DownloadListener>>();
		// 数据库操作对象实例化
		mDownloadDBHelper = new DownloadDBHelper(context,
				DownloadDBHelper.DB_NAME);
		DEFAULT_FILE_PATH = AppDataManager.getInst(context).APP_DOWN_GAME_PATH;
	}

	/**
	 * Get a single instance of DownloadTaskManager
	 * 
	 * @param context
	 *            Context
	 * @return DownloadTaskManager instance
	 */
	public static DownloadTaskManager getInstance(Context context) {
		if (sMe == null) {
			synchronized (syncObj) {
				if (sMe == null) {
					sMe = new DownloadTaskManager(context);
				}
			}
		}
		return sMe;
	}

	/**
	 * Start new download Task, if a same download Task already existed,it will
	 * exit and leave a "task existed" log.
	 * 
	 * @param downloadTask
	 *            DownloadTask
	 */
	public void startDownload(DownloadTask downloadTask) {
		if (downloadTask.getFilePath() == null
				|| downloadTask.getFilePath().length() == 0) {
			downloadTask.setFilePath(DEFAULT_FILE_PATH);
		}

		if (downloadTask.getFileName() == null
				|| downloadTask.getFileName().trim().length() == 0) {
			throw new IllegalArgumentException("file name is invalid");
		}
		if (mDownloadListenerMap.get(downloadTask) == null) {
			CopyOnWriteArraySet<DownloadListener> set = new CopyOnWriteArraySet<DownloadListener>();
			mDownloadListenerMap.put(downloadTask, set);
		}
		downloadTask.setDownloadState(DownloadState.INITIALIZE);
		insertDownloadTask(downloadTask);

		DownloadOperator dlOperator = new DownloadOperator(this, downloadTask);
		mDownloadMap.put(downloadTask, dlOperator);
		dlOperator.startDownload();
	}

	/**
	 * Pause a downloading task
	 * 
	 * @param downloadTask
	 *            DownloadTask
	 */
	public void pauseDownload(DownloadTask downloadTask) {
		downloadTask.setDownloadState(DownloadState.PAUSE);
		if (mDownloadMap.containsKey(downloadTask)) {
			mDownloadMap.get(downloadTask).pauseDownload();
		}
	}

	/**
	 * Continue or restart a downloadTask.
	 * 
	 * @param downloadTask
	 *            DownloadTask
	 */
	public void continueDownload(DownloadTask downloadTask) {
		if (downloadTask.getFilePath() == null
				|| downloadTask.getFilePath().trim().length() == 0) {
			downloadTask.setFilePath(DEFAULT_FILE_PATH);
		}

		if (downloadTask.getFileName() == null
				|| downloadTask.getFileName().trim().length() == 0) {
			throw new IllegalArgumentException("file name is invalid");
		}

		if (null == downloadTask.getUrl()
				|| !URLUtil.isHttpUrl(downloadTask.getUrl())) {
			throw new IllegalArgumentException("invalid http url");
		}

		if (null == mDownloadListenerMap.get(downloadTask)) {
			CopyOnWriteArraySet<DownloadListener> set = new CopyOnWriteArraySet<DownloadListener>();
			mDownloadListenerMap.put(downloadTask, set);
		}
		downloadTask.setDownloadState(DownloadState.DOWNLOADING);
		DownloadOperator dlOperator = new DownloadOperator(this, downloadTask);
		mDownloadMap.put(downloadTask, dlOperator);
		dlOperator.startDownload();
	}

	/**
	 * Stop a task,this method not used now。Please use pauseDownload instead.
	 * 
	 * @param downloadTask
	 *            DownloadTask
	 */
	// @Deprecated
	// public void stopDownload(DownloadTask downloadTask) {
	// mDownloadMap.get(downloadTask).stopDownload();
	// mDownloadMap.remove(downloadTask);
	// }
	// 更新 原有 stopDownload
	public void stopDownload(DownloadTask downloadTask, Context context) {
		// 先停止下载
		if (downloadTask.getDownloadState().equals(DownloadState.DOWNLOADING)) {
			downloadTask.setDownloadState(DownloadState.Stop);
			final DownloadOperator tmpOperator = mDownloadMap.get(downloadTask);
			if (tmpOperator != null) {
				tmpOperator.stopDownload();
			}
		} else {
			this.stopDownloadAfter(downloadTask);
			File file = new File(downloadTask.getFilePath() + File.separator
					+ downloadTask.getFileName());
			if (file.exists()) {
				file.delete();
			}
			mDownloadDBHelper.delete(downloadTask);
		}
	}

	/**
	 * get all Download task from database
	 * 
	 * @return DownloadTask list
	 */
	public List<DownloadTask> getAllDownloadTask() {
		return mDownloadDBHelper.queryAll();
	}

	/**
	 * get all Downloading task from database
	 * 
	 * @return DownloadTask list
	 */
	public List<DownloadTask> getDownloadingTask() {
		return mDownloadDBHelper.queryUnDownloaded();
	}

	/**
	 * get all download finished task from database
	 * 
	 * @return DownloadTask list
	 */
	public List<DownloadTask> getFinishedDownloadTask() {
		return mDownloadDBHelper.queryDownloaded();
	}

	/**
	 * insert a download task to database
	 * 
	 * @param downloadTask
	 */
	void insertDownloadTask(DownloadTask downloadTask) {
		mDownloadDBHelper.insert(downloadTask);
	}

	/**
	 * update a download task to database
	 * 
	 * @param downloadTask
	 */
	void updateDownloadTask(DownloadTask downloadTask) {
		mDownloadDBHelper.update(downloadTask);
	}

	/**
	 * delete a download task from download queue, remove it's listeners, and
	 * delete it from database.
	 * 
	 * @param downloadTask
	 */
	public void deleteDownloadTask(DownloadTask downloadTask) {
		for (DownloadListener listener : mDownloadListenerMap.get(downloadTask)) {
			listener.onDownloadStop();
		}
		mDownloadMap.remove(downloadTask);
		mDownloadListenerMap.remove(downloadTask);
		mDownloadDBHelper.delete(downloadTask);
	}

	/**
	 * 暂停 下载任务
	 * 
	 * @param task
	 */
	public void pauseDownloadAfter(DownloadTask task) {
		if (mDownloadListenerMap.containsKey(task)) {
			for (DownloadListener item : mDownloadListenerMap.get(task)) {
				item.onDownloadPause();
			}
		}
		mDownloadDBHelper.update(task);
	}

	/**
	 * 停止 下载任务
	 * 
	 * @param task
	 */
	public void stopDownloadAfter(DownloadTask task) {
		File file = new File(task.FILE_FULL_PATH_TEMP);
		if (file.exists()) {
			file.delete();
		}
		if (mDownloadListenerMap.containsKey(task)) {
			for (DownloadListener item : mDownloadListenerMap.get(task)) {
				item.onDownloadStop();
			}
		}
		mDownloadMap.remove(task);
		mDownloadListenerMap.remove(task);
		mDownloadDBHelper.delete(task);
	}

	/**
	 * 下载完成
	 * 
	 * @param downloadTask
	 */
	public void finishDownloadAfter(DownloadTask downloadTask) {
		if (mDownloadListenerMap.containsKey(downloadTask)) {
			for (DownloadListener listener : mDownloadListenerMap
					.get(downloadTask)) {
				listener.onDownloadFinish(downloadTask.FILE_FULL_PATH_TEMP);
			}
		}
		mDownloadMap.remove(downloadTask);
		mDownloadListenerMap.remove(downloadTask);
		mDownloadDBHelper.update(downloadTask);
	}

	/**
	 * 下载 失败
	 * 
	 * @param downloadTask
	 */
	public void failedDownloadAfter(DownloadTask downloadTask, String errorMsg) {
		for (DownloadListener item : mDownloadListenerMap.get(downloadTask)) {
			item.onDownloadFail(errorMsg);
		}
		mDownloadDBHelper.update(downloadTask);
	}

	/**
	 * Get all Listeners of a download task
	 * 
	 * @param downloadTask
	 * @return
	 */
	public CopyOnWriteArraySet<DownloadListener> getListeners(
			DownloadTask downloadTask) {
		if (null != mDownloadListenerMap.get(downloadTask)) {
			return mDownloadListenerMap.get(downloadTask);
		} else {
			return new CopyOnWriteArraySet<DownloadListener>();
		}
	}

	/**
	 * Register a DownloadListener to a downloadTask. You can register many
	 * DownloadListener to a downloadTask in any time. Such as register a
	 * listener to update you own progress bar, do something after file download
	 * finished.
	 * 
	 * @param downloadTask
	 * @param listener
	 */
	public void registerListener(DownloadTask downloadTask,
			DownloadListener listener) {
		if (null != mDownloadListenerMap.get(downloadTask)) {
			mDownloadListenerMap.get(downloadTask).add(listener);
		} else {
			CopyOnWriteArraySet<DownloadListener> set = new CopyOnWriteArraySet<DownloadListener>();
			set.add(listener);
			mDownloadListenerMap.put(downloadTask, set);
		}
	}

	/**
	 * Remove Listeners from a downloadTask, you do not need manually call this
	 * method.
	 * 
	 * @param downloadTask
	 */
	public void removeListener(DownloadTask downloadTask) {
		mDownloadListenerMap.remove(downloadTask);
	}

	/**
	 * delete a file
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * If url exist in database and the download state is FINISHED, and the file
	 * existed, return true.
	 * 
	 * @param url
	 * @return
	 */
	/*
	 * public boolean isUrlDownloaded(String url) { boolean re = false;
	 * 
	 * DownloadTask task = mDownloadDBHelper.query(url); if (null != task) { if
	 * (task.getDownloadState() == DownloadState.FINISHED) { File file = new
	 * File(task.getFilePath() + "/" + task.getFileName()); if (file.exists()) {
	 * re = true; } } } return re; }
	 */

	/**
	 * 判断 是否 达到最大下载任务
	 * 
	 * @return
	 */
	public boolean isReachMaxTask() {
		return mDownloadMap.size() >= MaxTaskCount;
	}

	/**
	 * 下载任务 是否已经存在
	 * 
	 * @param url
	 * @return
	 */
	public boolean isTaskExists(String url) {
		boolean result = false;
		Iterator<DownloadTask> iterator = mDownloadMap.keySet().iterator();
		while (iterator.hasNext()) {
			if (iterator.next().getUrl().equals(url)) {
				result = true;
				break;
			}
		}
		return result;
	}
}

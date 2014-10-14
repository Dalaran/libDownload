package com.sjwyx.app.libao.down;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.sjwyx.app.utils.HttpHelper;

public class DownloadOperator extends AsyncTask<Void, Long, Void> {
	private static final int BUFFER_SIZE = 4096;

	/**
	 * debug tag
	 */
	private static final String TAG = "DownloadOperator";

	private DownloadTask mDownloadTask;

	/**
	 * DownloadTaskManager
	 */
	private DownloadTaskManager mDlTaskMng;

	/**
	 * pause flag
	 */
	private volatile boolean mPause = false;

	/**
	 * stop flag, not used now.
	 */
	private volatile boolean mStop = false;

	/**
	 * Constructor
	 * 
	 * @param dlTaskMng
	 * @param downloadTask
	 */
	DownloadOperator(DownloadTaskManager dlTaskMng, DownloadTask downloadTask) {
		mDownloadTask = downloadTask;
		mDlTaskMng = dlTaskMng;
	}

	/**
	 * createFile
	 */
	private void createFile() {
		if (new File(mDownloadTask.FILE_FULL_PATH_TEMP).exists()) {
			return;
		}
		HttpURLConnection conn = null;
		RandomAccessFile accessFile = null;
		try {
			URL url = new URL(mDownloadTask.getUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(HttpHelper.CONNECT_TIMEOUT);
			conn.setReadTimeout(HttpHelper.READ_TIMEOUT);
			// conn.setRequestProperty("Accept-Encoding", "musixmatch");
			// conn.setRequestProperty("Accept-Encoding", "identity");
			conn.setRequestMethod("HEAD");

			int fileSize = conn.getContentLength();
			mDownloadTask.setTotalSize(fileSize);
			conn.disconnect();
			// 下载文件 所在路径
			File downFilePath = new File(mDownloadTask.getFilePath());
			if (!downFilePath.exists()) {
				downFilePath.mkdirs();
			}
			// 创建 下载文件
			File file = new File(mDownloadTask.FILE_FULL_PATH_TEMP);
			if (!file.exists()) {
				file.createNewFile();
				mDownloadTask.setFinishedSize(0);
			}
			accessFile = new RandomAccessFile(file, "rwd");
			if (fileSize > 0) {
				accessFile.setLength(fileSize);
			}
			accessFile.close();
			mDownloadTask.setDownloadState(DownloadState.INITIALIZE);
			mDlTaskMng.updateDownloadTask(mDownloadTask);
		} catch (MalformedURLException e) {
			for (DownloadListener l : mDlTaskMng.getListeners(mDownloadTask)) {
				l.onDownloadFail("初始化失败:" + e.getMessage());
			}
		} catch (FileNotFoundException e) {
			for (DownloadListener l : mDlTaskMng.getListeners(mDownloadTask)) {
				l.onDownloadFail("初始化失败:" + e.getMessage());
			}
		} catch (IOException e) {
			for (DownloadListener l : mDlTaskMng.getListeners(mDownloadTask)) {
				l.onDownloadFail("初始化失败:" + e.getMessage());
			}
		}
	}

	/**
	 * <BR>
	 * 
	 * @param params
	 *            Void...
	 * @return Void
	 * @see android.os.AsyncTask#doInBackground(Params[])
	 */
	@Override
	protected Void doInBackground(Void... params) {
		// 1. create file if not exist.
		createFile();

		mDownloadTask.setDownloadState(DownloadState.DOWNLOADING);
		mDlTaskMng.updateDownloadTask(mDownloadTask);

		for (DownloadListener l : mDlTaskMng.getListeners(mDownloadTask)) {
			l.onDownloadStart();
		}

		HttpURLConnection conn = null;
		RandomAccessFile accessFile = null;
		InputStream is = null;
		long finishedSize = mDownloadTask.getFinishedSize();
		long totalSize = mDownloadTask.getTotalSize();
		long startSize = finishedSize;
		try {
			URL url = new URL(mDownloadTask.getUrl());
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(HttpHelper.CONNECT_TIMEOUT);
			conn.setReadTimeout(HttpHelper.READ_TIMEOUT);
			// conn.setRequestProperty("Accept-Encoding", "musixmatch");
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Range", "bytes=" + finishedSize + "-"
					+ totalSize);
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			// conn.setRequestProperty("Connection", "Keep-Alive");

			accessFile = new RandomAccessFile(mDownloadTask.FILE_FULL_PATH_TEMP,
					"rwd");
			accessFile.seek(finishedSize);

			is = conn.getInputStream();
			byte[] buffer = new byte[BUFFER_SIZE];
			int length = -1;
			long startTime = System.currentTimeMillis();
			long speed = 0;
			while ((length = is.read(buffer)) != -1) {
				if (mStop) {
					mDownloadTask.setDownloadState(DownloadState.Stop);
					break;
				} else if (mPause) {
					mDownloadTask.setDownloadState(DownloadState.PAUSE);
					mDownloadTask.setFinishedSize(finishedSize);
					break;
				} else {
					finishedSize += length;
					accessFile.write(buffer, 0, length);
					mDownloadTask.setFinishedSize(finishedSize);
					speed = (int) ((finishedSize - startSize) / (System
							.currentTimeMillis() + 1 - startTime));
					publishProgress(finishedSize, totalSize, speed);
				}
			}
			is.close();
			accessFile.close();
			conn.disconnect();
			if (mDownloadTask.getDownloadState().equals(DownloadState.Stop)) {
				mDlTaskMng.stopDownloadAfter(mDownloadTask);
			} else if (mDownloadTask.getDownloadState().equals(
					DownloadState.PAUSE)) {
				mDlTaskMng.pauseDownloadAfter(mDownloadTask);
			} else if (mDownloadTask.getDownloadState().equals(
					DownloadState.DOWNLOADING)) {
				mDownloadTask.setDownloadState(DownloadState.FINISHED);
				mDlTaskMng.finishDownloadAfter(mDownloadTask);
			}
		} catch (IOException e) {
			mDownloadTask.setDownloadState(DownloadState.FAILED);
			mDownloadTask.setFinishedSize(finishedSize);
			mDlTaskMng.failedDownloadAfter(mDownloadTask,
					"下载失败:" + e.getMessage());
		}
		return null;
	}

	/**
	 * 停止下载 非正在下载中的 任务
	 * 
	 * @param task
	 */
	public void stopTask(DownloadTask task) {
		mDlTaskMng.deleteDownloadTask(task);
	}

	/**
	 * <BR>
	 * 
	 * @param values
	 *            int array
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);

		long finished = values[0];
		long total = values[1];
		long speed = values[2];
		for (DownloadListener l : mDlTaskMng.getListeners(mDownloadTask)) {
			l.onDownloadProgress(finished, total, speed);
		}
	}

	/**
	 * pauseDownload
	 */
	void pauseDownload() {
		Log.i(TAG, "pause download.");
		mPause = true;
		mStop = false;
	}

	/**
	 * stopDownload
	 */
	/*
	 * @Deprecated void stopDownload() { Log.i(TAG, "stop download."); mStop =
	 * true; mPause = false; }
	 */
	// 更新 code
	void stopDownload() {
		mPause = false;
		mStop = true;
	}

	/**
	 * continueDownload
	 */
	void continueDownload() {
		Log.i(TAG, "continue download.");
		mPause = false;
		mStop = false;
		execute();
	}

	/**
	 * startDownload
	 */
	void startDownload() {
		Log.i(TAG, "start download.");
		mPause = false;
		mStop = false;
		execute();
	}

	protected static String md5(String string) {
		byte[] hash = null;
		try {
			hash = MessageDigest.getInstance("MD5").digest(
					string.getBytes("UTF-8"));
		} catch (Exception e) {
			Log.e(TAG, "NoSuchAlgorithm");
		}

		StringBuilder hex = new StringBuilder(hash.length * 2);
		for (byte b : hash) {
			if ((b & 0xFF) < 0x10)
				hex.append("0");
			hex.append(Integer.toHexString(b & 0xFF));
		}
		return hex.toString();
	}

	protected static String getKey(String aKey) {

		char[] aKeyChars = { 49, 87, 89, 90, 86, 50, 74, 78, 88, 82, 72, 51,
				79, 73, 71, 53, 67, 52, 80, 54, 65, 76, 55, 85, 70, 56, 83, 69,
				68, 57, 84, 66, 48, 81, 75, 77 };
		byte[] keyBytes;
		int patternLength;
		int keyCharsOffset;
		int i;
		int j;
		StringBuilder result = new StringBuilder(
				"#####-#####-#####-#####-#####");
		keyBytes = aKey.getBytes();
		patternLength = result.length();
		keyCharsOffset = 0;
		i = 0;
		j = 0;
		while ((i < keyBytes.length) && (j < patternLength)) {
			keyCharsOffset = keyCharsOffset + Math.abs(keyBytes[i]);
			while (keyCharsOffset >= aKeyChars.length) {
				keyCharsOffset = keyCharsOffset - aKeyChars.length;
			}
			while ((result.charAt(j) != 35) && (j < patternLength)) {
				j++;
			}
			result.setCharAt(j, aKeyChars[keyCharsOffset]);
			if (i == (keyBytes.length - 1)) {
				i = -1;
			}
			i++;
			j++;
		}
		return result.toString();
	}

	// 破解处
	public static int check(Context context) {
		return 2;
		/*
		 * String key = ManifestMetaData.getString(context, "DOWNLOAD_KEY");
		 * String pack = context.getPackageName(); StringBuilder sb = new
		 * StringBuilder(); sb.append(pack); sb.reverse(); sb.append(pack); if
		 * (key.equals(getKey(md5(sb.toString())))) { return 2; } else if
		 * (key.equals("testkey")) { Toast.makeText( context,
		 * "The download manger you use is a trial version,please buy a license"
		 * , Toast.LENGTH_LONG).show(); return 1; } else { Toast.makeText(
		 * context,
		 * "The download manger key you use is invalid,please buy a license",
		 * Toast.LENGTH_LONG).show(); return -1; }
		 */
	}
}
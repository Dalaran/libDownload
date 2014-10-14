package com.sjwyx.app.libao.down;

public interface DownloadListener {
	/**
	 * Download Finish
	 * 
	 * @param filepath
	 */
	void onDownloadFinish(String filepath);

	/**
	 * Download Start
	 */
	void onDownloadStart();

	/**
	 * Download Pause
	 */
	void onDownloadPause();

	/**
	 * Download Stop
	 */
	void onDownloadStop();

	/**
	 * Download Fail
	 */
	void onDownloadFail(String msg);

	/**
	 * Download Progress update can be used to display speed and percent.
	 * 
	 * @param finishedSize
	 *            已完成的大小
	 * @param totalSize
	 *            下载的总大小
	 * @param speed
	 *            download speed
	 */
	void onDownloadProgress(long finishedSize, long totalSize, long speed);
}

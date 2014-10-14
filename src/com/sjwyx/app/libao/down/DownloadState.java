package com.sjwyx.app.libao.down;

public enum DownloadState {
	/**
	 * init
	 */
	INITIALIZE,

	/**
	 * downloading
	 */
	DOWNLOADING,
	/**
	 * download failed, the reason may be network error, file io error etc.
	 */
	FAILED,
	/**
	 * download finished
	 */
	FINISHED,

	/**
	 * download paused
	 */
	PAUSE,
	/**
	 * 停止下载
	 */
	Stop
}

package com.sjwyx.app.libao.down;

import java.io.File;

public class DownloadTask {
	/**
	 * 正在下载
	 */
	public static final String MARK_TMP=".tmp";
	private int _id;
	/**
	 * download url
	 */
	private String url;

	/**
	 * fileName
	 */
	private String fileName;

	private String title;
	/**
	 * 文件 小图标
	 */
	private String thumbnail;

	/**
	 * 不包括文件名
	 */
	private String filePath;

	/**
	 * download finished Size
	 */
	private long finishedSize;

	/**
	 * total Size
	 */
	private long totalSize;

	/**
	 * finished percent
	 */
	private int percent;
	/**
	 * 
	 */
	private long speed;

	/**
	 * download state
	 */
	private volatile DownloadState downloadState;

	public final String FILE_FULL_PATH;
	public final String FILE_FULL_PATH_TEMP;

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * DownloadTask constructor for create a new download task.
	 * 
	 * @param url
	 *            must a http url.
	 * @param filePath
	 *            if filePath is null, we will use the default download path
	 *            "/sdcard/download"
	 * @param fileName
	 *            file name, must input
	 * @param title
	 *            task title for display.Can be null
	 * @param thumbnail
	 *            task thumbnail image,should be a uri string. Can be null
	 */
	public DownloadTask(String url, String filePath, String fileName,
			String title, String thumbnail) {
		this.url = url;
		this.fileName = fileName;
		this.title = title;
		this.thumbnail = thumbnail;
		this.filePath = filePath;
		FILE_FULL_PATH=filePath+File.separator+fileName;
		FILE_FULL_PATH_TEMP =FILE_FULL_PATH+MARK_TMP;
	}

	/**
	 * get url
	 * 
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * set url
	 * 
	 * @param url
	 *            the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * get fileName
	 * 
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * set fileName
	 * 
	 * @param fileName
	 *            the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * get filePath
	 * 
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * set filePath
	 * 
	 * @param filePath
	 *            the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	/**
	 * get finishedSize
	 * 
	 * @return the finishedSize
	 */
	public long getFinishedSize() {
		return finishedSize;
	}

	/**
	 * set finishedSize
	 * 
	 * @param l
	 *            the finishedSize to set
	 */
	public void setFinishedSize(long l) {
		this.finishedSize = l;
	}

	/**
	 * get totalSize
	 * 
	 * @return the totalSize
	 */
	public long getTotalSize() {
		return totalSize;
	}

	/**
	 * get percent
	 * 
	 * @return the percent
	 */
	public int getPercent() {
		return percent;
	}

	/**
	 * set download percent
	 * 
	 * @param l
	 */
	public void setPercent(int l) {
		this.percent = l;
	}

	public long getSpeed() {
		return speed;
	}

	public void setSpeed(long speed) {
		this.speed = speed;
	}

	/**
	 * get downloadState
	 * 
	 * @return the downloadState
	 */
	public DownloadState getDownloadState() {
		return downloadState;
	}

	/**
	 * set downloadState
	 * 
	 * @param downloadState
	 *            the downloadState to set
	 */
	public void setDownloadState(DownloadState downloadState) {
		this.downloadState = downloadState;
	}

	/*
	 * @Override public int hashCode() { final int prime = 31; int result = 1;
	 * result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
	 * result = prime * result + ((filePath == null) ? 0 : filePath.hashCode());
	 * result = prime * result + ((url == null) ? 0 : url.hashCode()); return
	 * result; }
	 * 
	 * @Override public boolean equals(Object obj) { if (this == obj) return
	 * true; if (obj == null) return false; if (getClass() != obj.getClass())
	 * return false; DownloadTask other = (DownloadTask) obj; if (fileName ==
	 * null) { if (other.fileName != null) return false; } else if
	 * (!fileName.equals(other.fileName)) return false; if (filePath == null) {
	 * if (other.filePath != null) return false; } else if
	 * (!filePath.equals(other.filePath)) return false; if (url == null) { if
	 * (other.url != null) return false; } else if (!url.equals(other.url))
	 * return false; return true; }
	 */

	public int get_id() {
		return _id;
	}

	public void set_id(int _id) {
		this._id = _id;
	}

	@Override
	public String toString() {
		return "DownloadTask [url=" + url + ", finishedSize=" + finishedSize
				+ ", totalSize=" + totalSize + ", dlPercent=" + percent
				+ ", downloadState=" + downloadState + ", fileName=" + fileName
				+ ", title=" + title + "]";
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DownloadTask other = (DownloadTask) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

}

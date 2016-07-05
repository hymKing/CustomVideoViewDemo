package com.flyup.download;


import com.flyup.common.utils.MD5Utils;

public class DownloadInfo {

	private String id;//app的id，和appInfo中的id对应
	private int downloadState = DownloadState.STATE_NONE;//下载的状态
	private String url;//下载地址
	private String path;//保存路径

	private long totalSize = 0;//文件长度size
	private long currentSize = 0;//当前的size

	/** 从AppInfo中构建出一个DownLoadInfo */
	public static DownloadInfo build(String url,String localPath) {
		DownloadInfo downloadInfo = new DownloadInfo();
		downloadInfo.id = MD5Utils.generate(url);
		downloadInfo.downloadState = DownloadManager.STATE_NONE;
		downloadInfo.url = url;
		downloadInfo.currentSize = 0;
		downloadInfo.path = localPath;
		return downloadInfo;
	}

	public synchronized long getTotalSize() {
		return totalSize;
	}

	public synchronized void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public synchronized long getCurrentSize() {
		return currentSize;
	}

	public synchronized void setCurrentSize(long currentSize) {
		this.currentSize = currentSize;
	}

	public String getPath() {
		return path;
	}

	public synchronized String getUrl() {
		return url;
	}

	public synchronized void setUrl(String url) {
		this.url = url;
	}

	public synchronized String getId() {
		return id;
	}

	public synchronized void setId(String id) {
		this.id = id;
	}


	public synchronized int getDownloadState() {
		return downloadState;
	}

	public void setDownloadState(int downloadState) {
		this.downloadState = downloadState;
	}

	@Override
	public String toString() {
		return "DownloadInfo{" +
				"id='" + id + '\'' +
				", downloadState=" + downloadState +
				", url='" + url + '\'' +
				", path='" + path + '\'' +
				", totalSize=" + totalSize +
				", currentSize=" + currentSize +
				'}';
	}
}

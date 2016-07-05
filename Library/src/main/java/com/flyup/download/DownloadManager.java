package com.flyup.download;


import com.flyup.common.utils.FileUtil;
import com.flyup.common.utils.LogUtil;
import com.flyup.common.utils.MD5Utils;
import com.flyup.common.utils.ThreadManager;
import com.flyup.common.utils.UIUtils;
import com.flyup.net.OkHttp3ClientImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Request;
import okhttp3.Response;


public class DownloadManager implements DownloadState {


    private static DownloadManager instance;

    private DownloadManager() {
    }

    /**
     * 用于记录下载信息，如果是正式项目，需要持久化保存
     */
    private Map<String, DownloadInfo> mDownloadMap = new ConcurrentHashMap<>();
    /**
     * 用于记录观察者，当信息发送了改变，需要通知他们
     */
    private List<DownloadObserver> mObservers = new ArrayList<DownloadObserver>();
    /**
     * 用于记录所有下载的任务，方便在取消下载时，通过id能找到该任务进行删除
     */
    public Map<String, DownloadTask> mTaskMap = new ConcurrentHashMap<>();

    public static synchronized DownloadManager getInstance() {
        if (instance == null) {
            instance = new DownloadManager();
        }
        return instance;
    }

    /**
     * 注册观察者
     */
    public void registerObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            if (!mObservers.contains(observer)) {
                mObservers.add(observer);
            }
        }
    }

    /**
     * 反注册观察者
     */
    public void unRegisterObserver(DownloadObserver observer) {
        synchronized (mObservers) {
            if (mObservers.contains(observer)) {
                mObservers.remove(observer);
            }
        }
    }

    /**
     * 当下载状态发送改变的时候回调 ,一般在子线程中
     */
    public void notifyDownloadStateChanged(DownloadInfo info) {
        synchronized (mObservers) {
            List<DownloadObserver> temps = new ArrayList<>();
            temps.addAll(mObservers);//避免在循环的时候操作mObservers出现并发异常
            for (DownloadObserver observer : temps) {
                observer.onDownloadStateChanged(info);
            }
        }
    }

    /**
     * 当下载进度发送改变的时候回调
     */
    public void notifyDownloadProgressed(DownloadInfo info) {
        synchronized (mObservers) {
            for (DownloadObserver observer : mObservers) {
                observer.onDownloadProgressed(info);
            }
        }
    }

    public static String generateFilePath(String url) {
        String filename = MD5Utils.generate(url);
        int index = url.lastIndexOf(".");
        if (index > 0) {
            String extend = url.substring(index);
            filename = filename.concat(extend);
        }
        final File target = new File(FileUtil.getCacheDir(UIUtils.getContext()), filename);

//        return FileUtil.getExternalCacheDir(UIUtils.getContext()) + MD5Utils.generate(url);
        return target.getAbsolutePath();
    }


    /**
     * 下载，需要传入一个appInfo对象
     */
    public synchronized void download(String url) {
        LogUtil.i("Downloadmanager", "start download " + url);
        //检查本地是否已经存在
        String path = generateFilePath(url);
        File file = new File(path);
        if (file.exists() && file.isFile() && file.length() > 0) {
            DownloadInfo info = DownloadInfo.build(url, generateFilePath(url));
            info.setDownloadState(STATE_DOWNLOADED);
            notifyDownloadStateChanged(info);
            return;
        }

        //先判断是否有这个文件的下载信息
        String id = MD5Utils.generate(url);
        DownloadInfo info = mDownloadMap.get(id);
        if (info == null) {//如果没有，则根据appInfo创建一个新的下载信息
            info = DownloadInfo.build(url, generateFilePath(url));
            mDownloadMap.put(id, info);
        }

        //判断状态是否为STATE_NONE、STATE_PAUSED、STATE_ERROR。只有这3种状态才能进行下载，其他状态不予处理
        if (info.getDownloadState() == STATE_NONE || info.getDownloadState() == STATE_PAUSED || info.getDownloadState() == STATE_ERROR) {
            //下载之前，把状态设置为STATE_WAITING，因为此时并没有产开始下载，只是把任务放入了线程池中，当任务真正开始执行时，才会改为STATE_DOWNLOADING
            info.setDownloadState(STATE_WAITING);
            notifyDownloadStateChanged(info);//每次状态发生改变，都需要回调该方法通知所有观察者
            DownloadTask task = new DownloadTask(info, this);//创建一个下载任务，放入线程池
            mTaskMap.put(info.getId(), task);
            ThreadManager.getLongPool().execute(task);
        }
    }

    public String parseId(String url) {
        return MD5Utils.generate(url);
    }

    /**
     * 暂停下载
     */
    public synchronized void pause(String url) {
        String id = parseId(url);
        stopDownload(id);
        DownloadInfo info = mDownloadMap.get(id);//找出下载信息
        if (info != null) {//修改下载状态
            info.setDownloadState(STATE_PAUSED);
            notifyDownloadStateChanged(info);
        }
    }

    /**
     * 取消下载，逻辑和暂停类似，只是需要删除已下载的文件
     */
    public synchronized void cancel(String url) {
        String id = parseId(url);
        stopDownload(id);
        DownloadInfo info = mDownloadMap.get(id);//找出下载信息
        if (info != null) {//修改下载状态并删除文件
            info.setDownloadState(STATE_NONE);
            notifyDownloadStateChanged(info);
            info.setCurrentSize(0);
            File file = new File(info.getPath());
            file.delete();
        }
    }


    /**
     * 如果该下载任务还处于线程池中，且没有执行，先从线程池中移除
     */
    private void stopDownload(String id) {
        DownloadTask task = mTaskMap.remove(id);//先从集合中找出下载任务
        if (task != null) {
            ThreadManager.getLongPool().cancel(task);//然后从线程池中移除
        }
    }

    /**
     * 获取下载信息
     */
    public synchronized DownloadInfo getDownloadInfo(String id) {
        return mDownloadMap.get(id);
    }


    public interface DownloadObserver {

        void onDownloadStateChanged(DownloadInfo info);

        void onDownloadProgressed(DownloadInfo info);
    }


    private static final String TAG = "DownloadManager";

    //下载图片到本地,返回图片地址,同步
    public static File downloadSync(String url) {
        try {

            File file = new File(DownloadManager.generateFilePath(url));//获取下载文件

            if (file.exists() && file.isFile()) {
                file.delete();
            }

            if (!file.exists()) {
                file.createNewFile();
            }

            Request req = new Request.Builder()
                    .url(url)
                    .tag(url)
                    .build();
            Response resp = OkHttp3ClientImpl.execute(req);

            if (resp.isSuccessful()) {
                InputStream stream = resp.body().byteStream();

                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(file);
                    int count = -1;
                    byte[] buffer = new byte[1024 * 1024];
                    while ((count = stream.read(buffer)) != -1) {
                        fos.write(buffer, 0, count);
                        fos.flush();
                    }
                    LogUtil.i(TAG, "下载完成 " + file.getAbsolutePath());
                    return file;
                } catch (Exception e) {
                    LogUtil.e(e);//出异常后需要修改状态并删除文件
                } finally {
                    resp.body().close();
                    IOUtils.close(fos);
                    if (resp.body() != null) {
                        resp.body().close();
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}

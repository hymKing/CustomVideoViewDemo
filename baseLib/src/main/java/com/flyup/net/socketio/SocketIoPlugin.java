package com.flyup.net.socketio;

/**
 * Created by solo on 16/4/5.
 * QQ:1049447621
 * Version 1.0
 */
public interface SocketIoPlugin {
    /**
     * 获取推送服务器的地址
     */
    String getUrl();


    /**
     *返回数据
     */
    void postData(String json);
}

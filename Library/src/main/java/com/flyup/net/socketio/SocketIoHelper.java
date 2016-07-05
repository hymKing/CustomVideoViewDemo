package com.flyup.net.socketio;

import android.util.Log;

import com.flyup.common.utils.LogUtil;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import io.socket.engineio.client.EngineIOException;
import io.socket.engineio.client.transports.WebSocket;

/**
 * Created by solo on 16/4/5.
 * QQ:1049447621
 * Version 1.0
 */
public class SocketIoHelper {
    private static final String TAG = SocketIoHelper.class.getSimpleName();

    public static volatile SocketIoHelper sIoHelper  = null;

    public static SocketIoHelper getInstance(){
        SocketIoHelper  sIh = sIoHelper;
        if(sIh == null){
            synchronized (SocketIoHelper.class){
                sIh = sIoHelper;
                if(sIh == null){
                    sIh = new SocketIoHelper();
                    sIoHelper = sIh;
                }
            }
        }
        return sIh;
    }

    private SocketIoHelper(){};



    /**
     * 链接IM 服务器
     * @throws URISyntaxException
     */
    public void connectSocketServer(SocketIoPlugin socketIoPlugin) throws URISyntaxException {
        if(socketIoPlugin == null){
            throw new NullPointerException("the SocketIoPlugin is null");
        }
        //断开之前的链接,保证socket不能重复链接
        if(Global.socket != null){
            Global.socket.off();
            Global.socket.close();
            while (Global.socket.connected()){
                LogUtil.i(TAG, "wait for socket close !!");
            }
            LogUtil.i(TAG,"the socket was closed !");
            Global.socket = null;
        }

        //socket配置
        IO.Options opts = new IO.Options();
        opts.forceNew = true;
        opts.reconnection = false;
        opts.transports = new String[]{WebSocket.NAME};
        Global.socket = IO.socket(socketIoPlugin.getUrl(),opts);
        setSocketListenrer(socketIoPlugin);
        Global.socket.connect();
    }


    /**
     * 设置IM监听
     */
    private static void setSocketListenrer(final SocketIoPlugin socketIoPlugin) {
        Global.socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... arg0) {
                printIOException(Socket.EVENT_CONNECT_ERROR,arg0);
            }
        });

        Global.socket.on(Socket.EVENT_ERROR, new Emitter.Listener() {
            @Override
            public void call(final Object... arg0) {
                printIOException(Socket.EVENT_ERROR,arg0);
            }
        });

        Global.socket.on(Socket.EVENT_CONNECT_TIMEOUT,new Emitter.Listener(){
            @Override
            public void call(Object... arg0) {
                printIOException(Socket.EVENT_CONNECT_TIMEOUT,arg0);
            }
        });


        Global.socket.on(Socket.EVENT_DISCONNECT,new Emitter.Listener(){
            @Override
            public void call(Object... arg0) {
                printIOException(Socket.EVENT_DISCONNECT,arg0);
            }
        });

        Global.socket.on(Socket.EVENT_RECONNECT_ERROR,new Emitter.Listener(){
            @Override
            public void call(Object... arg0) {
                printIOException(Socket.EVENT_RECONNECT_ATTEMPT,arg0);
            }
        });


        Global.socket.on(Socket.EVENT_RECONNECT_FAILED, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                printIOException(Socket.EVENT_RECONNECT_FAILED, arg0);
            }
        });


        Global.socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... arg0) {
                printIOException(Socket.EVENT_CONNECT, arg0);
            }
        });

        Global.socket.on(Socket.EVENT_MESSAGE, new Emitter.Listener() {
            @Override
            public void call(final Object... arg0) {
                Log.i(TAG, "arg0>>>" + arg0[0].toString());
                //把消息插入到数据库
               socketIoPlugin.postData(arg0[0].toString());
            }
        });
    }

    /**
     *打出IM日志
     * @param key
     * @param arg0
     */
    private static void printIOException(String key ,Object... arg0) {
        if(arg0.length > 0){
            if(arg0[0] instanceof EngineIOException){
                LogUtil.e(TAG, key + "::" + arg0[0].toString());
                //checkCode(arg0);
            }
        }
    }



}

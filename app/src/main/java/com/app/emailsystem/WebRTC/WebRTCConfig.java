package com.app.emailsystem.WebRTC;

import android.content.Context;

import org.webrtc.PeerConnection;

import java.util.ArrayList;
import java.util.List;


public class WebRTCConfig {

    public WebRTCConfig(Context context){}

    public static List<PeerConnection.IceServer> getIceServersList() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        // STUN
        iceServers.add(PeerConnection.IceServer.builder("stun:47.96.106.242:3478")
                .createIceServer());

        // TURN UDP
        iceServers.add(PeerConnection.IceServer.builder("turn:47.96.106.242:3478?transport=udp")
                .setUsername("admin")
                .setPassword("admin")
                .createIceServer());

        // TURN TCP
        iceServers.add(PeerConnection.IceServer.builder("turn:47.96.106.242:3478?transport=tcp")
                .setUsername("admin")
                .setPassword("admin")
                .createIceServer());

        return iceServers;
    }
}



/*
public class WebRTCConfig {

    // 配置多个 STUN 和 TURN 服务器
    private final static String STUN_URL_1 = "stun:stun.l.google.com:19302";
    private final static String STUN_URL_2 = "stun:stun1.l.google.com:19302";
    private final static String TURN_URL = "turn:numb.viagenie.ca";
    private final static String TURN_USERNAME = "webrtc@live.com";
    private final static String TURN_PASSWORD = "muazkh";

    public WebRTCConfig(Context context) {
        // 初始化其他配置
    }

    // 获取 ICE 服务器列表
    public static List<PeerConnection.IceServer> getIceServersList() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();

        // 配置多个 STUN 服务器
        iceServers.add(PeerConnection.IceServer.builder(STUN_URL_1).createIceServer());
        iceServers.add(PeerConnection.IceServer.builder(STUN_URL_2).createIceServer());

        // 配置 TURN 服务器（需要用户名和密码）
        iceServers.add(PeerConnection.IceServer.builder(TURN_URL)
                .setUsername(TURN_USERNAME)
                .setPassword(TURN_PASSWORD)
                .createIceServer());

        return iceServers;
    }
}

 */

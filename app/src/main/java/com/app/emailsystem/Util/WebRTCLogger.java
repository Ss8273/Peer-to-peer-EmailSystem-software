package com.app.emailsystem.Util;


import android.util.Log;

public class WebRTCLogger {
    private static final String TAG = "WebRTC_LOG";

    public static void log(String tag, String message) {
        Log.d(TAG, "[" + tag + "] " + message);
    }

    public static void sdp(String direction, String type, String sdp) {
        Log.d(TAG, "[SDP " + direction.toUpperCase() + "] Type: " + type + ", Description: \n" + sdp);
    }

    public static void iceSend(String sdpMid, int sdpMLineIndex, String candidate) {
        Log.d(TAG, "[ICE SEND] Mid: " + sdpMid + ", Index: " + sdpMLineIndex + ", Candidate: \n" + candidate);
    }

    public static void iceRecv(String source, String candidate) {
        Log.d(TAG, "[ICE RECV] From: " + source + ", Candidate: \n" + candidate);
    }

    public static void dataChannelState(String state) {
        Log.d(TAG, "[DataChannel] State changed to: " + state);
    }

    public static void dataChannelMsg(String role, String content) {
        Log.d(TAG, "[DataChannel Msg] As " + role + ", received content: \n" + content);
    }

    public static void step(String stepName) {
        Log.d(TAG, "[STEP] " + stepName);
    }
}
package com.app.emailsystem.Util;

import android.util.Log;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;

public class IceLogger {
    private static final String TAG = "ICE_LOG";

    public static void logCandidate(IceCandidate ice) {
        String summary = ice.sdp.toLowerCase().contains("typ relay") ? "[RELAY]" :
                ice.sdp.toLowerCase().contains("typ srflx") ? "[SRFLX]" :
                        ice.sdp.toLowerCase().contains("typ host") ? "[HOST]" : "[UNKNOWN]";
        Log.d(TAG, summary + " Candidate → " + ice.sdp);
    }

    public static void logIceState(PeerConnection.IceConnectionState state) {
        Log.d(TAG, "📶 ICE 状态变化 → " + state);
    }
}
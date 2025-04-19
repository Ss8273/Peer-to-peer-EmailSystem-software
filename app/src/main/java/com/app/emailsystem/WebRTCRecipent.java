package com.app.emailsystem;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.app.emailsystem.WebRTC.Peer;
import com.app.emailsystem.WebRTC.PeerManager;
import com.app.emailsystem.network.SocketManager;
import com.app.emailsystem.Util.WebRTCLogger;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.SessionDescription;
import io.socket.client.Socket;

public class WebRTCRecipent extends Service {
    private static final String TAG = "WebRTCRecipent";
    private Socket mSocket;
    private PeerManager peerManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "✅ WebRTCRecipent Service Created");

        mSocket = SocketManager.getSocket();
        if (mSocket == null) {
            Log.e(TAG, "❌ Socket 未初始化");
            return;
        }

        peerManager = PeerManager.getInstance();

        Log.d(TAG, "✅ socket.isConnected: " + mSocket.connected());

        mSocket.on("SdpInfo", args -> {
            Log.d("sdpInfo-recipent","received");
            try {
                JSONObject json = (JSONObject) args[0];
                String source = json.getString("source");
                String destination = json.getString("destination");
                String type = json.getString("type");
                String description = json.getString("description");

                WebRTCLogger.step("📥 receive SdpInfo: " + type);

                SessionDescription.Type sdpType = SessionDescription.Type.fromCanonicalForm(type);
                SessionDescription sdp = new SessionDescription(sdpType, description);

                // ✅ 使用 Peer 自身作为 observer 创建连接（终极补丁核心）
                Peer peer = new Peer(mSocket, destination,source);
                peer.init(peerManager.getFactory(), null, peerManager.getIceServers());
                Log.d(TAG, "✅ PeerConnection 已创建，观察者是自己: " + peer);

                peerManager.getPeerMap().put(source + "-" + destination, peer);

                // ✅ 设置远端 SDP，触发 WebRTC 自动协商
                peer.getConnection().setRemoteDescription(peer, sdp);
                if (sdpType == SessionDescription.Type.OFFER) {
                    peer.getConnection().createAnswer(peer, new MediaConstraints());
                }

                // ✅ 等待 WebRTC 自动触发 onDataChannel()
            } catch (Exception e) {
                Log.e(TAG, "❌ SDP 处理异常", e);
            }
        });

        mSocket.on("IceInfo", args -> {
            Log.d("iceInfo-recipent","received");
            try {
                JSONObject json = (JSONObject) args[0];
                String source = json.getString("source");
                String destination = json.getString("destination");
                String sdpMid = json.getString("id");
                int sdpMLineIndex = json.getInt("label");
                String candidate = json.getString("candidate");

                WebRTCLogger.step("📥 receiver ICE from " + source);

                IceCandidate ice = new IceCandidate(sdpMid, sdpMLineIndex, candidate);
                String key = source + "-" + destination;
                Peer peer = peerManager.getPeerMap().get(key);
                if (peer != null && peer.getConnection() != null) {
                    peer.getConnection().addIceCandidate(ice);
                } else {
                    Log.w(TAG, "⚠️ no Peer or connection is not initialized: " + key);
                }
            } catch (Exception e) {
                Log.e(TAG, "❌ ICE 处理异常", e);
            }
        });

        mSocket.on("connect", args -> Log.d(TAG, "🟢 Socket connected"));
        mSocket.on("disconnect", args -> Log.w(TAG, "🔌 Socket disconnected"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "✅ WebRTCRecipent Service Started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "❎ WebRTCRecipent Destroyed");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
package com.app.emailsystem.WebRTC;

import android.util.Log;
import com.app.emailsystem.MyApplication;
import com.app.emailsystem.Util.WebRTCLogger;
import com.app.emailsystem.network.SocketManager;
import org.json.JSONObject;
import org.webrtc.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.Socket;

public class PeerManager {
    private static final String TAG = "PeerManager";
    private static PeerManager instance;
    private Map<String, Peer> peerMap = new HashMap<>();
    private List<PeerConnection.IceServer> iceServers;
    private PeerConnectionFactory factory;
    private Socket mSocket;
    private boolean isBusy = false;

    public static synchronized PeerManager getInstance() {
        if (instance == null) {
            instance = new PeerManager();
        }
        return instance;
    }

    private PeerManager() {
        mSocket = SocketManager.getSocket();
        iceServers = WebRTCConfig.getIceServersList();
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(MyApplication.getAppContext())
                        .setEnableInternalTracer(true)
                        .createInitializationOptions()
        );
        factory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    public DataChannel startPeerConnection(String source, String dest) {
        WebRTCLogger.step("1️⃣ create PeerConnection");
        WebRTCLogger.step("2️⃣ create DataChannel");
        WebRTCLogger.step("3️⃣ create Offer");

        if (isBusy) {
            Log.w(TAG, "Transmission in progress, please wait");
            return null;
        }

        isBusy = true;
        String key = source + "-" + dest;
        Peer peer = new Peer(mSocket, source, dest);
        peer.init(factory, null, iceServers);
        peerMap.put(key, peer);

        // 创建 DataChannel
        DataChannel.Init init = new DataChannel.Init();
        init.ordered = true;
        DataChannel dc = peer.getConnection().createDataChannel("data", init);
        peer.setDataChannel(dc);

        // SDP & ICE 监听
        mSocket.on("SdpInfo", args -> {
            Log.d("sdpInfo-peermanager","received");
            try {
                JSONObject json = (JSONObject) args[0];
                if (!json.optString("destination").equals(source)) return;
                SessionDescription sdp = new SessionDescription(
                        SessionDescription.Type.fromCanonicalForm(json.optString("type")),
                        json.optString("description")
                );
                peer.getConnection().setRemoteDescription(peer, sdp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        mSocket.on("IceInfo", args -> {
            Log.d("iceInfo-peermanager","received");
            try {
                JSONObject json = (JSONObject) args[0];
                if (!json.optString("destination").equals(source)) return;
                IceCandidate ice = new IceCandidate(
                        json.optString("id"),
                        json.optInt("label"),
                        json.optString("candidate")
                );
                peer.getConnection().addIceCandidate(ice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        peer.getConnection().createOffer(peer, new MediaConstraints());
        return dc;
    }

    public Peer getPeer(String source, String dest) {
        return peerMap.get(source + "-" + dest);
    }

    public void removePeer(String source, String dest) {
        String key = source + "-" + dest;
        Peer peer = peerMap.remove(key);
        if (peer != null && peer.getConnection() != null) {
            peer.getConnection().close();
        }
        isBusy = false;
    }
    public PeerConnectionFactory getFactory() {
        return factory;
    }

    public List<PeerConnection.IceServer> getIceServers() {
        return iceServers;
    }

    public Map<String, Peer> getPeerMap() {
        return peerMap;
    }

}

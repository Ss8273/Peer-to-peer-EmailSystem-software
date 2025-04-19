package com.app.emailsystem.WebRTC;

import android.content.Context;
import android.util.Log;
import com.app.emailsystem.MyApplication;
import com.app.emailsystem.Util.DatabaseHelper;
import com.app.emailsystem.Util.EmailFileHelper;
import com.app.emailsystem.Util.IceLogger;
import com.app.emailsystem.Util.WebRTCLogger;

import org.json.JSONObject;
import org.webrtc.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.socket.client.Socket;

public class Peer implements PeerConnection.Observer, SdpObserver {
    private static final String TAG = "Peer";
    private PeerConnection mConn;
    private Socket mSocket;
    private String mSourceName;
    private String mDestName;
    private DataChannel dataChannel;
    private boolean isReceiver = false;
    private String expectedFilePath = null;
    private long connectionStartTime;  // 记录连接开始时间
    private long firstDataReceivedTime;  // 记录首次数据收到时间


    public Peer(Socket socket, String sourceName, String destName) {
        mSocket = socket;
        mSourceName = sourceName;
        mDestName = destName;
    }


    public void init(PeerConnectionFactory factory, MediaStream stream, List<PeerConnection.IceServer> iceServers) {
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        //rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.RELAY; // 仅使用 TURN
        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        //rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE;
        //rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE;

        connectionStartTime = System.currentTimeMillis();
        Log.d("connectionStartTime",String.valueOf(connectionStartTime));
        mConn = factory.createPeerConnection(rtcConfig, this);

    }

    public PeerConnection getConnection() {
        return mConn;
    }

    public void setDataChannel(DataChannel channel) {
        this.dataChannel = channel;
        registerDataObserver();
    }

    private void registerDataObserver() {
        if (dataChannel == null) return;

        dataChannel.registerObserver(new DataChannel.Observer() {
            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                firstDataReceivedTime = System.currentTimeMillis();
                long delay = firstDataReceivedTime - connectionStartTime;
                Log.d("WebRTC", "⏱ 首次数据传输延迟: " + delay + "ms");
                ByteBuffer data = buffer.data;
                byte[] bytes = new byte[data.remaining()];
                data.get(bytes);
                String msg = new String(bytes, StandardCharsets.UTF_8);

                WebRTCLogger.dataChannelMsg("receiver", msg);

                try {
                    JSONObject json = new JSONObject(msg);
                    String type = json.optString("type");

                    if ("emailInfo".equals(type)) {
                        // 发件人角色
                        isReceiver = true;
                        String sender = json.getString("sender");
                        String receiver = json.getString("receiver");
                        String subject = json.getString("subject");
                        String time = json.getString("time");

                        expectedFilePath = EmailFileHelper.getFilePath(sender, receiver, time, subject);
                        Log.d(TAG, "send file content，filepath：" + expectedFilePath);

                        File file = new File(expectedFilePath);
                        if (!file.exists()) {
                            Log.e(TAG, "file doesn't exist！");
                            return;
                        }

                        FileInputStream fis = new FileInputStream(file);
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        byte[] buffers = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffers)) != -1) {
                            bos.write(buffers, 0, bytesRead);
                        }
                        fis.close();

                        String contentStr = bos.toString(StandardCharsets.UTF_8.name());

                        JSONObject contentJson = new JSONObject();
                        contentJson.put("type", "emailContent");
                        contentJson.put("content", contentStr);
                        contentJson.put("sender", sender);
                        contentJson.put("receiver", receiver);
                        contentJson.put("subject", subject);
                        contentJson.put("time", time);

                        ByteBuffer buf = ByteBuffer.wrap(contentJson.toString().getBytes(StandardCharsets.UTF_8));
                        dataChannel.send(new DataChannel.Buffer(buf, false));

                        Log.d(TAG, "✅ The body of the email has been sent");
                    } else if ("emailContent".equals(type)) {
                        // 收件人角色
                        if (!isReceiver) {
                            String content = json.getString("content");
                            String sender = json.optString("sender");
                            String receiver = json.optString("receiver");
                            String subject = json.optString("subject");
                            String time = json.optString("time");

                            expectedFilePath = EmailFileHelper.getFilePath(sender, receiver, time, subject);

                            if (expectedFilePath != null) {
                                Context context = MyApplication.getAppContext();
                                EmailFileHelper.saveContentToFile(context, expectedFilePath, content);

                                DatabaseHelper.getInstance(context)
                                        .insertEmail(sender, receiver, subject, time, expectedFilePath, 3);

                                Log.d(TAG, "📥 get email and insert to database: " + subject);
                                PeerManager.getInstance().setBusy(false);
                            } else {
                                Log.e(TAG, "❌ expectedFilePath is null");
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "❌ Exception in onMessage", e);
                }
            }

            @Override
            public void onStateChange() {
                // 你可以在这里添加日志或状态检查
                Log.d(TAG, "📡 DataChannel status change: " + dataChannel.state());
            }

            @Override
            public void onBufferedAmountChange(long l) {
                // 可选实现
            }
        });

    }

    @Override public void onDataChannel(DataChannel dc) {
        WebRTCLogger.step("6️⃣ onDataChannel was invoked！");
        Log.d(TAG, "🟢 Receive DataChannel invoke onDataChannel！");
        this.dataChannel = dc;
        registerDataObserver();
    }

    @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
    @Override public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
        Log.d(TAG, "📶 ICE status changes: " + state);

        IceLogger.logIceState(state); // ✅ 打印状态变化
    }
    @Override public void onIceConnectionReceivingChange(boolean b) {}
    @Override public void onIceGatheringChange(PeerConnection.IceGatheringState s) {}

    @Override public void onIceCandidate(IceCandidate iceCandidate) {
        Log.d("ICE", "Candidate: " + iceCandidate.sdp);
        IceLogger.logCandidate(iceCandidate); // ✅ 打印候选类型
        WebRTCLogger.iceSend(iceCandidate.sdpMid, iceCandidate.sdpMLineIndex, iceCandidate.sdp);
        try {
            JSONObject json = new JSONObject();
            json.put("id", iceCandidate.sdpMid);
            json.put("label", iceCandidate.sdpMLineIndex);
            json.put("candidate", iceCandidate.sdp);
            json.put("source", mSourceName);
            json.put("destination", mDestName);
            mSocket.emit("IceInfo", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
    @Override public void onAddStream(MediaStream mediaStream) {}
    @Override public void onRemoveStream(MediaStream mediaStream) {}
    @Override public void onRenegotiationNeeded() {}
    @Override public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "📡 create SDP successfully，type：" + sessionDescription.type);
        mConn.setLocalDescription(this, sessionDescription);
        try {
            JSONObject json = new JSONObject();
            json.put("type", sessionDescription.type.canonicalForm());
            json.put("description", sessionDescription.description);
            json.put("source", mSourceName);
            json.put("destination", mDestName);
            mSocket.emit("SdpInfo", json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void onSetSuccess() {
        Log.d(TAG, "✅ SDP set successfully");
    }
    @Override public void onCreateFailure(String s) {}
    @Override public void onSetFailure(String s) {}

    @Override
    public void onSelectedCandidatePairChanged(CandidatePairChangeEvent event) {
        IceCandidate local = event.local;
        IceCandidate remote = event.remote;

        Log.d("CANDIDATE", "Local: " + local.toString());
        Log.d("CANDIDATE", "Remote: " + remote.toString());
    }
    public DataChannel getDataChannel() {
        return dataChannel;
    }
}

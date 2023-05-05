package com.example.whatschat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera1Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.socket.client.Socket;

public class CallListAdapter extends RecyclerView.Adapter<CallListAdapter.ViewHolder> {
    private static final String TAG = "CallListAdapter";
    private List<CallModel> mCallList;
    private Context mContext;
    CallModel call;

    public CallListAdapter(Context context, List<CallModel> callList) {
        mCallList = callList;
        mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.call_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        call = mCallList.get(position);

        holder.mNameTextView.setText(call.getCallerName());
        holder.mTimeTextView.setText(call.getCallTime());
        holder.mCallerAvatar.setBackgroundResource(call.getCallerAvatar());
        holder.mCallType.setImageResource(R.drawable.ic_call_received);

        holder.mMakeCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), CallWindow.class);
                intent.putExtra("name", holder.mNameTextView.getText());
                view.getContext().startActivity(intent);
//                callUser(call.getSocketId());
            }
        });

        if (call.getCallType() == CallModel.MISSED_CALL && holder.mCallType != null) {
            holder.mCallType.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.red), android.graphics.PorterDuff.Mode.MULTIPLY);
        } else if (holder.mCallType != null) {
            holder.mCallType.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.button_rounded_green), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    @Override
    public int getItemCount() {
        return mCallList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mNameTextView;
        public TextView mTimeTextView;
        public ImageView mCallerAvatar;
        public ImageView mCallType;
        public ImageButton mMakeCall;

        public ViewHolder(View itemView) {
            super(itemView);

            mNameTextView = itemView.findViewById(R.id.nameTextView);
            mTimeTextView = itemView.findViewById(R.id.timeTextView);
            mCallerAvatar = itemView.findViewById(R.id.callerAvatar);
            mCallType = itemView.findViewById(R.id.callType);
            mMakeCall = itemView.findViewById(R.id.make_call);
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), CallWindow.class);
            intent.putExtra("name", mNameTextView.getText());
            v.getContext().startActivity(intent);
        }
    }

}

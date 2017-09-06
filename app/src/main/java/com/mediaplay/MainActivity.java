package com.mediaplay;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Administrator on 2017/9/6.
 */

public class MainActivity extends Activity  implements SeekBar.OnSeekBarChangeListener,MediaPlayer.OnCompletionListener{

    private String TAG="Mediaplay";
    private boolean isStopUpdatingProgress =false;
    private EditText mPath;
    private MediaPlayer mMediaplayer;
    private SeekBar mSeekBar;
    private TextView mCurrentTime;
    private TextView mTotalTime;
    private SurfaceView mSufaceView;

    private final int NORMAL=0;//空闲
    private final int PLAYING=1; //播放中
    private final int PAUSED=2; // 暂停中
    private final int STOPPED=3; //停止

    private int currentState=NORMAL;

    // mHolder 用于控制Surface;
    private SurfaceHolder mHolder;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.main_layout);

        mPath=(EditText)findViewById(R.id.patch);
        mSeekBar=(SeekBar)findViewById((R.id.current_time));
        mCurrentTime=(TextView)findViewById(R.id.tv_current_time);
        mTotalTime=(TextView)findViewById((R.id.tv_total_time));
        mSufaceView=(SurfaceView)findViewById(R.id.surfaceview);

        mHolder=mSufaceView.getHolder();

        //采用自内部的双缓冲区，
        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

    }


    public void start(View v){
        Log.d(TAG, "start:");
        if(mMediaplayer != null){
            if(currentState!= STOPPED){
                mMediaplayer.start();
                currentState=PLAYING;
                isStopUpdatingProgress=false;
            } else if(currentState == STOPPED){
                mMediaplayer.reset();
                mMediaplayer.release();
            }
        }
        play();
    }

    private void play(){
        Log.d(TAG,"play");
        mMediaplayer=new MediaPlayer();

        String Sdcard= Environment.getExternalStorageDirectory().getPath();
        String path= "sdcard/video/2V.mp4";
        try{
            // 设置音频流类型
            mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置播放器的位置
            mMediaplayer.setDisplay(mHolder);

            mMediaplayer.setDataSource("sdcard/Arirang.mp3");
            mMediaplayer.prepare();
            mMediaplayer.start();

            mMediaplayer.setOnCompletionListener(this);
            currentState=PLAYING;

            // 读取音乐的总长度，设置给seekbar的最大值
            int duration= mMediaplayer.getDuration();
            mSeekBar.setMax(duration);

            int m= duration/1000/60;
            int s= duration/1000%60;

            mTotalTime.setText(m+":"+s);
            mCurrentTime.setText("00:00");

            isStopUpdatingProgress=false;
            new Thread(new updateProgressRunnable()).start();

        }catch ( Exception e){
            e.printStackTrace();
        }

    }

    public void stop(View v){
        if(mMediaplayer != null) {
            mMediaplayer.stop();
            currentState=STOPPED;
        }
    }

    public void pause(View v){
        if(mMediaplayer != null && currentState== PLAYING){
            mMediaplayer.pause();
            currentState=PAUSED;
            isStopUpdatingProgress= true;
        }
    }

    public void restart(View v){
        if(mMediaplayer!= null ){
            mMediaplayer.reset();
            mMediaplayer.release();
            play();
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){

    }

    // 开始拖动进度条
    public  void onStartTrackingTouch(SeekBar seekBar){
        isStopUpdatingProgress=true;
    }

    //停止拖动进度条
    public void onStopTrackingTouch(SeekBar seekBar){
        int progress=seekBar.getProgress();
        mMediaplayer.seekTo(progress);
        isStopUpdatingProgress=false;

        new Thread(new updateProgressRunnable()).start();
    }

    class updateProgressRunnable implements Runnable{
        public void run(){
            while (isStopUpdatingProgress == false){
                int currentPosition= mMediaplayer.getCurrentPosition();
                //得到当前的时间， 并转换为分钟和秒
                mSeekBar.setProgress(currentPosition);
                final int m=currentPosition/1000/60;
                final int s=currentPosition/1000%60;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentTime.setText(m+":"+s);
                    }
                });
            }
        }
    }


    public void onCompletion(MediaPlayer mp){
        Toast.makeText(this,"重新播放",Toast.LENGTH_SHORT).show();
        mp.start();
    }


}

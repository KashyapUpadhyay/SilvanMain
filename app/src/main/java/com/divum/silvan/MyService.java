package com.divum.silvan;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import com.divum.callback.BgThreadCallback;
import com.divum.callback.VDBCallback;
import com.divum.callback.VDPCallback;
import com.divum.utils.App_Variable;
import com.divum.utils.BackgroundThread;
import com.divum.utils.CustomLog;

public class MyService extends Service implements BgThreadCallback, VDBCallback, VDPCallback {

    private static final String TAG = "MyService";

    private static Intent _intent;
    private boolean showOnetimeCamera = false;
    private boolean isOneTimeSensor = false;

    private String tag = "MyService";

    ResultReceiver receiver;
    private boolean isServiceFromNotify;
    //   Messenger messenger;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

    }


    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        _intent = intent;
        CustomLog.camera(TAG, "camera Service onRebind:" + _intent);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        _intent = intent;
        CustomLog.camera(TAG, "camera Service onStartCommand:" + _intent);
        if (_intent != null) {
            receiver = intent.getParcelableExtra("receiverTag");
            isServiceFromNotify = intent.getBooleanExtra("isNotify",false);
            //receiver.send(204,null);
           /* Messenger messenger = _intent.getParcelableExtra("messenger");

            try {
                messenger.send(Message.obtain(null, 3));
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }*/
           // CustomLog.camera(TAG, "Service hashcode:" + messenger.hashCode());

       /* int msgWhat =0;
        if(_intent!=null) {
             msgWhat = _intent.getIntExtra("msgWhat", -1);
        }*/


            //		Runnable r = new Runnable() {
            //			public void run() {
            BackgroundThread bgProcess = new BackgroundThread(MyService.this, true, intent);
            // if (msgWhat == 0)
            bgProcess.callSensorStatus(true,isServiceFromNotify);


            //			}
            //		};
            //		Thread t = new Thread(r);
            //		t.start();

      /*  if (null == intent || null == intent.getAction()) {
            String source = null == intent ? "intent" : "action";
            Log.e(TAG, source + " was null, flags=" + flags + " bits=" + Integer.toBinaryString(flags));
            return START_STICKY;
        }*/

       }
        return Service.START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        Log.i(TAG, "Service onBind");
        _intent = intent;
        CustomLog.camera(TAG, "camera Service onBind:" + _intent);

        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        //ResultReceiver receiver = rootIntent.getParcelableExtra("messenger");
        restartServiceTask.putExtra("receiverTag", receiver);
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void ShowHomeView(boolean _isNotification, String isFrom) {
        CustomLog.show("sensor Service ShowHomeView" + _isNotification);
        if (_isNotification) {
            CustomLog.show("sensor Service ShowHomeView notification");
            NotifyUserSensor();
        }


        if (_intent != null) {
            CustomLog.debug("sensor Service ShowHomeView not null");
          //  final Messenger messenger = (Messenger) _intent.getParcelableExtra("messenger");
           // Message message = null;
            if (isFrom.equals("sensor")) {
                receiver.send(201,null);
               // message = Message.obtain(null, 1);
            } else {
                receiver.send(200,null);
               // message = Message.obtain(null, 0);
            }
           /* try {
                messenger.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }*/
        }


		/*Intent intent=new Intent("home");		
        intent.putExtra("page", "sensor");
		sendBroadcast(intent);
		stopSelf();*/

    }

    @Override
    public void showCameraView(Intent intent) {

        //CustomLog.error("myservice","VDBSTATUS1 Service showCameraView");
        NotifyUserCamera();

        CustomLog.camera("camera Service showCameraView 1:" + _intent + ",," + intent);
        CustomLog.camera("camera Service showCameraView 2:" + getApplication());

        if (_intent != null) {
           // Messenger messenger = (Messenger) _intent.getParcelableExtra("messenger");
            CustomLog.camera("camera VDBSTATUS1 Service showCameraView not null");
            //final Message message = Message.obtain(null, 2);
           /* try {
                messenger.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }*/

            receiver.send(202,null);
        }

    }

    public void NotifyUserSensor() {
        //beep(10,"");
        System.out.println("pause stage:" + App_Variable.appMinimize);
        if (App_Variable.appMinimize == 0) {
            App_Variable.appMinimizeSensor = true;
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

            Intent intent = new Intent(this, SplashActivity.class);
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("section", 0);
            intent.putExtra("isNotification", true);
            intent.putExtra("screen", "sensor");


            //use the flag FLAG_UPDATE_CURRENT to override any notification already there
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification(R.drawable.ic_launcher, "Silvan", System.currentTimeMillis());
            //notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS ;// Notification.DEFAULT_SOUND

            notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.firbell);
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS;


            notification.setLatestEventInfo(this, "Silvan", "Your sensor is on", contentIntent);
            //10 is a random number I chose to act as the id for this notification

            notificationManager.notify(1, notification);
        }

    }

    /**
     * push notification for camera
     */
    private void NotifyUserCamera() {
        //beep(10,"");
        CustomLog.camera("camera from notification:" + App_Variable.appMinimize);
        if (App_Variable.appMinimize == 0) {
            App_Variable.appMinimizeCamera = true;

            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("isNotification", true);

            intent.putExtra("page", 1);
            intent.putExtra("screen", "vdb");
            intent.putExtra("vdb_trigger", 1);
            CustomLog.camera("camera from notification: CameraActivity");


            //		Intent intent = new Intent(this, HomeActivity.class);
            //		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //		intent.putExtra("position", -1);
            //		intent.putExtra("room", "");
            //		intent.putExtra("screen", "camera");

            //use the flag FLAG_UPDATE_CURRENT to override any notification already there
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            Notification notification = new Notification(R.drawable.ic_launcher, "Silvan", System.currentTimeMillis());
            //notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS ;//| Notification.DEFAULT_SOUND;

            notification.sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.doorbell);
            notification.defaults &= notification.DEFAULT_SOUND;
            notification.flags = Notification.FLAG_ONLY_ALERT_ONCE | Notification.FLAG_AUTO_CANCEL | Notification.DEFAULT_LIGHTS;

            notification.setLatestEventInfo(this, "Silvan", "Someone is on Your Door Step", contentIntent);
            //10 is a random number I chose to act as the id for this notification
            notificationManager.notify(2, notification);
        }

    }

	/*@Override
    public void ShowNormalView() {
		if (_intent != null) {
			final Messenger messenger = (Messenger) _intent.getParcelableExtra("messenger");
			final Message message = Message.obtain(null, 0);

			try {
				messenger.send(message);
			} catch (RemoteException exception) {
				exception.printStackTrace();
			}
		}

	}*/

    private void beep(int volume, String path) {


		/*AudioManager manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		manager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

		Uri notification = RingtoneManager
				.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		// Uri notification = Uri.parse("file:///android_asset/doorbell.mp3");

		MediaPlayer player = MediaPlayer.create(getApplicationContext(), notification);
		player.start();*/

        Uri nuri = Uri.parse("file:///android_asset/doorbell.mp3");

		/*try {
			RingtoneManager.setActualDefaultRingtoneUri(this,RingtoneManager.TYPE_NOTIFICATION, nuri);
		}
		catch (Throwable t) {
			// error handling goes here -- also, use something other than Throwable
		}
		AudioManager audio = (AudioManager)
				getSystemService(Context.AUDIO_SERVICE);
		audio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
		for (int volumeType: (new int[] { AudioManager.STREAM_SYSTEM,
				AudioManager.STREAM_RING,

				AudioManager.STREAM_NOTIFICATION,

				AudioManager.STREAM_ALARM })) {
			int maxVolume = audio.getStreamMaxVolume(volumeType);
			audio.setStreamVolume(volumeType, maxVolume,
					AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_VIBRATE);
			audio.setStreamMute(volumeType, false);
			audio.setVibrateSetting(volumeType,
					AudioManager.VIBRATE_SETTING_ON);
		}*/

    }


    @Override
    public void VDPResponse(String response, Intent intent) {
        CustomLog.camera("VDBSTATUS1 camera:->" + response + "," + _intent);
        /**
         * Only for vdb,If the 5th line has alarmin.status=1, then need to go the VDB page
         */

        if (response != null) {
            response = response.trim();
            if (response.contains("alarmin.status=1")) {
                CustomLog.camera("VDBSTATUS1 camera:->" + showOnetimeCamera);
                CustomLog.camera("VDBSTATUS1 camera:->" + "alarmin.status=1");

                if (!showOnetimeCamera) {
                    CustomLog.camera("Camera", "VDBSTATUS1->:" + "camra inside");
                    showOnetimeCamera = true;
                    showCameraView(intent);
                }
            }

            if (response.contains("alarmin.status=0")) {
                //CustomLog.camera("VDBSTATUS1 camera:->"+"alarmin.status=0");
                showOnetimeCamera = false;
            }
        }

    }

    @Override
    public void VDPException(String trim, String vdbType) {
        CustomLog.camera("VDPException camera->" + trim);

        if (vdbType.equalsIgnoreCase("local")) {
            vdbType = "Static";
        }

    }


    @Override
    public void VDBResponse(String mVDBStatus, Intent intent) {
        CustomLog.camera("sensor response ->:" + mVDBStatus);
        //	System.out.println("sensor response::"+mVDBStatus);
        /**
         * If VDBSTATUS=1, navigate VDB page
         * If VDBSTATUS=7, navigate Sensor page
         * If VDBSTATUS=0, ignore
         *
         */

        if (mVDBStatus == null) {
            mVDBStatus = "0";
        }


        if (!isOneTimeSensor) {
            isOneTimeSensor = true;
            if (mVDBStatus.trim().equals("1")) {
                CustomLog.show(tag, "VDBSTATUS0->:" + "camra inside");
                showCameraView(intent);
            } else if (mVDBStatus.trim().equals("7")) {
                CustomLog.camera(tag, "VDBSTATUS0->:" + "inside");
                ShowHomeView(true, "sensor");
            }

        }

        if (mVDBStatus.trim().equals("0")) {
            isOneTimeSensor = false;
        }

    }


    @Override
    public void VDBException(String exception) {
        CustomLog.camera("sensor exception->:" + exception);

    }


}
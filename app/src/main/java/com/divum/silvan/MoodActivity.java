package com.divum.silvan;

import java.net.URLEncoder;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.divum.callback.HomeListCallback;
import com.divum.constants.GetAPI;
import com.divum.entity.HomeEntity;
import com.divum.entity.MoodEntity;
import com.divum.parser.BaseParser;
import com.divum.parser.MoodConfigParser;
import com.divum.silvan.callback.MoodDataCallback;
import com.divum.utils.AnalyticsTracker;
import com.divum.utils.App_Variable;
import com.divum.utils.CommonPingURL;
import com.divum.utils.CustomLog;
import com.divum.utils.GetMooodData;
import com.divum.utils.HomeList;
import com.divum.utils.NameSorter;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class MoodActivity extends SlidingFragmentActivity implements OnClickListener, MoodDataCallback, HomeListCallback {

    private TextView tab_config, tab_schedule, tab_status;
    private ImageView img_tab_status_bottom, img_tab_schedule_bottom, img_tab_config_bottom;

    private GetMooodData gmd;
    private GetMooodConfigData gcd;
    private ProgressDialog progress;

    private ImageView img_slider;
    private LinearLayout layout_mood;
    private String[] moodNames = {"Master On", "Master Off", "Morning", "Evening", "Dinner"};
    private int[] moodImage = {R.drawable.mood_master,
            R.drawable.mood_master,
            R.drawable.mood_morning,
            R.drawable.mood_evening,
            R.drawable.mood_dinner};
    private LinearLayout layout_mood_schedule;

    private TextView txtMoodAreaValue, txtMoodRoomValue;
    private LinkedHashMap<String, String> listhashtype;
    private RelativeLayout layout_mood_config;
    private LinearLayout view_config_dimmer, view_config_light, view_config_curtain;
    private LinearLayout view_config_fan, view_config_ac, view_config_tv;

    private String _whichSection = "";
    private String _whichMood = "Master On";
    private TextView txtApplyDateMood;
    private TextView txtApplyTimeMood;
    private View txtApplyDailyMood;
    private TextView txtApplyOnceMood;
    private Dialog dd;
    private Calendar cal;
    private int day;
    private int month;
    private int year;

    private int mHour;
    private int mMinute;
    private int mHourSelected;
    private int mMinSelected;

    private int mDaySelected;
    private int mMonthSelected;
    private int mYearSelected;
    private final String screenName = "Mood Settings Screen";
    private String selectedDate = "";

    private GetMoodScheduleApply gs;

    private String tag = "";
    private ScrollView layout_mood_status;
    private LinearLayout layout_linear_status;
    private GetMoodStatus gms;
    private GetMoodStausDelete gsd;

    private ArrayList<MoodEntity> listDimmers = new ArrayList<MoodEntity>();
    private ArrayList<MoodEntity> listCurtains = new ArrayList<MoodEntity>();
    private ArrayList<MoodEntity> listLights = new ArrayList<MoodEntity>();
    private ArrayList<String> listFan = new ArrayList<String>();
    private ArrayList<MoodEntity> listAC = new ArrayList<MoodEntity>();
    private ArrayList<String> listTV = new ArrayList<String>();

    private ArrayList<String> listDimmersID = new ArrayList<String>();
    private ArrayList<String> listLightsID = new ArrayList<String>();

    LinkedHashMap<String, String> hashKey;
    boolean moodvalue = true;

    private ArrayList<LinkedHashMap<String, LinkedHashMap<String, String>>> listMoodConfig = null;
    private static int CURRENT_TAB = 0;
    private static int CONFIG_TAB = 0;
    private static int SCHDULE_TAB = 1;
    private static int STATUS_TAB = 2;

    private TextView[] dimmerLevel;
    private TextView[] fanLevel;
    private int MOOD_SIZE = 5;
    private boolean isData = false, isExtraData = false;
    private ArrayList<String> list_rooms = new ArrayList<String>();
    private TextView ApplyMoodConfig;
    private ImageView imgMoodOn, imgMoodOff, imgMoodMorning, imgMoodEvening, imgMoodDinner;
    private ArrayList<Hashtable<String, HomeEntity>> listData;
    private String listele;

    SlidingMenu sm;
    /*public MoodActivity() {
        super(R.string.app_name);
		// TODO Auto-generated constructor stub
	}*/
    private int roomPosition = 1;
    private int moodPosition = 1;
    private ProgressDialog progressDlg;
    private String selectedSection = "";
    private LinearLayout moodMorning, moodEvening, moodDinner;
    private int[] dimmerRange;
    private HashMap<String, String[]> lightStatusList;
    private HashMap<String, ArrayList<MoodEntity>> lightListMap;
    private HashMap<String, int[]> dimmerStatusList;
    private HashMap<String, ArrayList<MoodEntity>> dimmerListMap;
    private HashMap<String, String[]> curtainStatusList;
    private HashMap<String, ArrayList<MoodEntity>> curtainListMap;
    private HashMap<String, String[]> acStatusList;
    private HashMap<String, ArrayList<MoodEntity>> acListMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        try {
            super.onCreate(savedInstanceState);
            App_Variable.appMinimize = 1;

            CURRENT_TAB = CONFIG_TAB;

            setBehindContentView(R.layout.slide_menu);
            setContentView(R.layout.mood_view);
            AnalyticsTracker.trackScreen(this, screenName);

            //	hashMoodData=new HashMap<String, MoodEntity>();

            //SharedPreferences storagePref=getSharedPreferences("IP", MODE_PRIVATE);
            GetAPI.BASE_URL = App_Variable.getBaseAPI(MoodActivity.this);


            Set entrySet = App_Variable.hashRoomType.entrySet();
            Iterator iterator = entrySet.iterator();
            while (iterator.hasNext()) {
                Entry entry = (Entry) iterator.next();
                CustomLog.debug("key new:" + entry.getKey() + "," + entry.getValue());
                list_rooms.add("" + entry.getKey());
            }

            FragmentTransaction frag = this.getSupportFragmentManager().beginTransaction();
            //SlideViewer mFrag = new SlideViewer("mood");
            frag.replace(R.id.menu_slide, SlideViewer.newInstance("mood"));
            frag.commit();

            sm = getSlidingMenu();
            sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
            sm.setMode(SlidingMenu.LEFT);
            sm.setShadowWidthRes(R.dimen.shadow_width);
            sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
            sm.setFadeDegree(0.35f);//0.35f
            sm.setBehindScrollScale(0.25f);
            sm.toggle();


            layout_mood = (LinearLayout) findViewById(R.id.layout_mood);
            layout_mood.setVisibility(View.GONE);
            /**
             * Slider action
             */
            img_slider = (ImageView) findViewById(R.id.img_slider);
            img_slider.setOnClickListener(this);

			/*
             * create instance for tab
			 */
            tab_config = (TextView) findViewById(R.id.tab_config);
            tab_config.setOnClickListener(this);

            tab_schedule = (TextView) findViewById(R.id.tab_schedule);
            tab_schedule.setOnClickListener(this);

            tab_status = (TextView) findViewById(R.id.tab_status);
            tab_status.setOnClickListener(this);


            img_tab_config_bottom = (ImageView) findViewById(R.id.img_tab_config_bottom);
            img_tab_config_bottom.setVisibility(View.VISIBLE);

            img_tab_schedule_bottom = (ImageView) findViewById(R.id.img_tab_schedule_bottom);
            img_tab_status_bottom = (ImageView) findViewById(R.id.img_tab_status_bottom);

            progressDlg = new ProgressDialog(this);
            progressDlg.setMessage("Please wait while mood configuration loading..");


            LinearLayout moodMasterOn = (LinearLayout) findViewById(R.id.moodMasterOn);
            moodMasterOn.setOnClickListener(this);

            LinearLayout moodMasterOff = (LinearLayout) findViewById(R.id.moodMasterOff);
            moodMasterOff.setOnClickListener(this);

            moodMorning = (LinearLayout) findViewById(R.id.moodMorning);
            moodMorning.setOnClickListener(this);

            moodEvening = (LinearLayout) findViewById(R.id.moodEvening);
            moodEvening.setOnClickListener(this);

            moodDinner = (LinearLayout) findViewById(R.id.moodDinner);
            moodDinner.setOnClickListener(this);

            imgMoodOn = (ImageView) findViewById(R.id.imgMoodOn);
            imgMoodOff = (ImageView) findViewById(R.id.imgMoodOff);
            imgMoodMorning = (ImageView) findViewById(R.id.imgMoodMorning);
            imgMoodEvening = (ImageView) findViewById(R.id.imgMoodEvening);
            imgMoodDinner = (ImageView) findViewById(R.id.imgMoodDinner);

            ResetMoodIcon();
            imgMoodOn.setImageResource(R.drawable.mood_master);

            callPopup();

			/*
             * Create instance for mood configuration
			 */
            layout_mood_config = (RelativeLayout) findViewById(R.id.layout_mood_config);
            layout_mood_config.setVisibility(View.GONE);

			/*
             * create instance for mood config dimmer ac light view
			 */

          /* test1 view_config_dimmer = (LinearLayout) findViewById(R.id.view_config_dimmer);
            view_config_dimmer.setVisibility(View.GONE);

            view_config_light = (LinearLayout) findViewById(R.id.view_config_light);
            view_config_light.setVisibility(View.GONE);

            view_config_curtain = (LinearLayout) findViewById(R.id.view_config_curtain);
            view_config_curtain.setVisibility(View.GONE);

            view_config_fan = (LinearLayout) findViewById(R.id.view_config_fan);
            view_config_fan.setVisibility(View.GONE);

            view_config_ac = (LinearLayout) findViewById(R.id.view_config_ac);
            view_config_ac.setVisibility(View.GONE);

            view_config_tv = (LinearLayout) findViewById(R.id.view_config_tv);
            view_config_tv.setVisibility(View.GONE);test1*/

            //apply
            ApplyMoodConfig = (TextView) findViewById(R.id.ApplyMoodConfig);
            ApplyMoodConfig.setOnClickListener(this);

			/*
             * create instance for schedule
			 */
            layout_mood_schedule = (LinearLayout) findViewById(R.id.layout_mood_schedule);
            layout_mood_schedule.setVisibility(View.GONE);

            txtMoodAreaValue = (TextView) findViewById(R.id.txtMoodAreaValue);
            txtMoodRoomValue = (TextView) findViewById(R.id.txtMoodRoomValue);
            txtMoodRoomValue.setText(_whichMood);

			/*
             * craete instace for m schedule buttons
			 */
            txtApplyDateMood = (TextView) findViewById(R.id.txtApplyDateMood);
            txtApplyDateMood.setOnClickListener(this);

            txtApplyTimeMood = (TextView) findViewById(R.id.txtApplyTimeMood);
            txtApplyTimeMood.setOnClickListener(this);

            txtApplyDailyMood = (TextView) findViewById(R.id.txtApplyDailyMood);
            txtApplyDailyMood.setOnClickListener(this);

            txtApplyOnceMood = (TextView) findViewById(R.id.txtApplyOnceMood);
            txtApplyOnceMood.setOnClickListener(this);

			/*
			 * get current day, month and year
			 */
            cal = Calendar.getInstance();
            day = cal.get(Calendar.DAY_OF_MONTH);
            month = cal.get(Calendar.MONTH);
            year = cal.get(Calendar.YEAR);

            mHour = cal.get(Calendar.HOUR_OF_DAY);
            mMinute = cal.get(Calendar.MINUTE);

            txtApplyDateMood.setText(App_Variable.getAppendZero("" + day) + "/" + App_Variable.getAppendZero("" + (month + 1)) + "/" + year);
            txtApplyTimeMood.setText(App_Variable.getAppendZero("" + mHour) + ":" + App_Variable.getAppendZero("" + mMinute));

            mHourSelected = mHour;
            mMinSelected = mMinute;

            mDaySelected = day;
            mMonthSelected = month + 1;

			/*
			 * create instance for schedule status
			 */
            layout_mood_status = (ScrollView) findViewById(R.id.layout_mood_status);
            layout_mood_status.setVisibility(View.GONE);

            layout_linear_status = (LinearLayout) findViewById(R.id.layout_linear_status);


            progress = new ProgressDialog(MoodActivity.this);
            //	hashMoodData=App_Variable.hashMoodData;
            //App_Variable.CreateMoodsFind();
            //hashMoodFind=App_Variable.hashMoodFind;
            //callMoodData();


        } catch (Exception e) {
            // TODO: handle exception
        }
    }


    private void callHomeList() {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {

            HomeList home = new HomeList(MoodActivity.this, true);
            home.execute();
        } else {
            App_Variable.ShowNoNetwork(this);
        }
    }


    @Override
    protected void onPause() {
        App_Variable.appMinimize = 0;
        CustomLog.show(tag, "pause:" + App_Variable.appMinimize);
        CustomLog.show(tag, "Ranjith time mood pause:" + moodvalue);
        SharedPreferences SUGGESTION_PREF = getSharedPreferences("SUGGESTION_PREF", Context.MODE_PRIVATE);
        boolean checkvalue = SUGGESTION_PREF.getBoolean("checkboxvalue", false);
        if (checkvalue) {

            if (moodvalue) {
                Intent intent = new Intent(this, UnlockActivity.class);
                startActivity(intent);
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        App_Variable.appMinimize = 1;
        CustomLog.show(tag, "resume:" + App_Variable.appMinimize);
        CustomLog.show(tag, "Ranjith time mood pause:" + moodvalue);

        if (App_Variable.appMinimizeSensor) {
            Intent intentHome = new Intent(MoodActivity.this, HomeActivity.class);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentHome.putExtra("position", -1);
            intentHome.putExtra("room", "");
            intentHome.putExtra("screen", "splash");
            intentHome.putExtra("nextPage", "sensor");
            startActivity(intentHome);
            return;
        }else if(App_Variable.appMinimizeCamera){
            App_Variable.hashListCamera = null;

            Intent intentCamera = new Intent(MoodActivity.this, CameraActivity.class);
            intentCamera.putExtra("page", 1);
            intentCamera.putExtra("screen", "vdb");
            intentCamera.putExtra("vdb_trigger", 1);
            startActivity(intentCamera);
            return;
        }

        if (App_Variable.hashNickName == null) {
            callHomeList();
        } else {
            //callMoodConfigData(false);
        }


    }

    private void callMoodData() {
        //progress=new ProgressDialog(this);
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            if (gmd != null)
                gmd.cancel(true);
            gmd = new GetMooodData(MoodActivity.this, true);
            gmd.setProgress(progress);
            gmd.execute();

        } else {
            App_Variable.ShowNoNetwork(this);
            //callMoodData();
        }
    }

    @Override
    public void getResponseMoodData(HashMap<String, MoodEntity> _hashMoodData,
                                    HashMap<String, String> _hashMoodFind, HashMap<String, String> _hashMoodAppliance, HashMap<String, String> _hashMoodApplRoom) {

        //hashMoodData=_hashMoodData;
        //hashMoodFind=_hashMoodFind;
        //hashMoodAppliance=_hashMoodAppliance;
        //hashMoodApplRoom=_hashMoodApplRoom;

        callMoodConfigData(false);
    }

    private void callMoodConfigData(boolean _isUpdate) {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Schedule", "clicked", _whichMood);
            if (gcd != null)
                gcd.cancel(true);
            gcd = new GetMooodConfigData(_isUpdate);
            gcd.execute();

        } else {
            App_Variable.ShowNoNetwork(this);
        }

    }

    class GetMooodConfigData extends AsyncTask<Void, Void, Void> {

        private String response = "";
        private String errorString = "";
        private boolean isUpadte;

        public GetMooodConfigData(boolean _isUpdate) {
            isUpadte = _isUpdate;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            progressDlg.setMessage("Please wait while mood configuration loading..");
            progressDlg.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            CustomLog.latest("url:" + GetAPI.BASE_URL + GetAPI.MOOD_CONFIG + "?" + System.currentTimeMillis());
            MoodConfigParser mp = new MoodConfigParser(GetAPI.BASE_URL + GetAPI.MOOD_CONFIG + "?" + System.currentTimeMillis());
            listMoodConfig = mp.getMoodConfigList();
            errorString = mp.getException();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDlg.dismiss();
            if (!errorString.isEmpty()) {
                App_Variable.ShowErrorToast(errorString, MoodActivity.this);
            } else {
                if (listMoodConfig != null) {

                    int size = listMoodConfig.size();

                    CustomLog.latest("isUpadte:" + isUpadte);
                    if (isUpadte) {
                        if (!checkSectionEmpty()) {
                            layout_mood_config.setVisibility(View.VISIBLE);
                            addLayoutToDisplayControls();
                            //ShowMoodConfigurationView();
                        } else {
                            layout_mood_config.setVisibility(View.GONE);
                        }
                    } else {
                        progress.dismiss();
                        callPopup();//122
                    }
                }

            }
        }
    }

    private void addLayoutToDisplayControls() {
        //LinearLayout lyMoodConfigContainer=
        LinearLayout lyMoodConfigContainer = (LinearLayout) findViewById(R.id.lyMoodConfigContainer);
        lyMoodConfigContainer.removeAllViewsInLayout();
        lyMoodConfigContainer.removeAllViews();
        lightStatusList = new HashMap<String, String[]>();
        lightListMap = new HashMap<String, ArrayList<MoodEntity>>();
        dimmerStatusList = new HashMap<String, int[]>();
        dimmerListMap = new HashMap<String, ArrayList<MoodEntity>>();
        curtainStatusList = new HashMap<String, String[]>();
        curtainListMap = new HashMap<String, ArrayList<MoodEntity>>();
        acStatusList = new HashMap<String, String[]>();
        acListMap = new HashMap<String, ArrayList<MoodEntity>>();
        if ("Global".equals(selectedSection)) {
            int size = list_rooms.size();
            for (int i = 0; i < size; i++) {
                roomPosition = i + 1;
                // moodPosition = 1;
                _whichSection = list_rooms.get(i);

                View view = getLayoutInflater().inflate(R.layout.mood_config_view, null);
                TextView tvRoomName = (TextView) view.findViewById(R.id.tvRoomName);
                tvRoomName.setText(_whichSection);
                setInstanses(view);
                ShowMoodConfigurationView(view);
                lyMoodConfigContainer.addView(view);
            }
        } else {
            View view = getLayoutInflater().inflate(R.layout.mood_config_view, null);
            setInstanses(view);
            ShowMoodConfigurationView(view);
            lyMoodConfigContainer.addView(view);
        }
    }

    private void setInstanses(View view) {
        view_config_dimmer = (LinearLayout) view.findViewById(R.id.view_config_dimmer);
        view_config_dimmer.setVisibility(View.GONE);

        view_config_light = (LinearLayout) view.findViewById(R.id.view_config_light);
        view_config_light.setVisibility(View.GONE);

        view_config_curtain = (LinearLayout) view.findViewById(R.id.view_config_curtain);
        view_config_curtain.setVisibility(View.GONE);

        view_config_fan = (LinearLayout) view.findViewById(R.id.view_config_fan);
        view_config_fan.setVisibility(View.GONE);

        view_config_ac = (LinearLayout) view.findViewById(R.id.view_config_ac);
        view_config_ac.setVisibility(View.GONE);

        view_config_tv = (LinearLayout) view.findViewById(R.id.view_config_tv);
        view_config_tv.setVisibility(View.GONE);

        /*//apply
        ApplyMoodConfig = (TextView) view.findViewById(R.id.ApplyMoodConfig);
        ApplyMoodConfig.setOnClickListener(this);*/
    }

    public void ShowViewSection(int position, String whichSection) {

        //CURRENT_TAB =CONFIG_TAB;
        _whichSection = whichSection;
        selectedSection = whichSection;

        TextView txt_header=(TextView)findViewById(R.id.txt_header);
        txt_header.setText(selectedSection);
        if ("Global".equals(selectedSection)) {
            tab_schedule.setTextColor(Color.GRAY);
            //tab_schedule.setEnabled(false);
            //tab_schedule.setClickable(false);

            moodMorning.setEnabled(false);
            moodMorning.setClickable(false);
            moodEvening.setEnabled(false);
            moodEvening.setClickable(false);
            moodDinner.setEnabled(false);
            moodDinner.setClickable(false);

            _whichMood = moodNames[0];
            moodPosition = 1;
            ResetMoodIcon();
            imgMoodOn.setImageResource(R.drawable.mood_master);

        } else {
            tab_schedule.setTextColor(Color.WHITE);
            tab_schedule.setEnabled(true);
            tab_schedule.setClickable(true);

            moodMorning.setEnabled(true);
            moodMorning.setClickable(true);
            moodEvening.setEnabled(true);
            moodEvening.setClickable(true);
            moodDinner.setEnabled(true);
            moodDinner.setClickable(true);
        }
        //	SetMoodData(_whichSection);

        if (sm != null)
            sm.toggle();

        if (_whichSection.equalsIgnoreCase("home")) {
            moodvalue = false;
            Intent intentHome = new Intent(MoodActivity.this, HomeActivity.class);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentHome.putExtra("position", -1);
            intentHome.putExtra("room", "");
            intentHome.putExtra("screen", "others");
            startActivity(intentHome);      //
            finish();
            AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Home", "clicked", "");

        } else if (_whichSection.equalsIgnoreCase("settings")) {
            moodvalue = false;
            Intent intentHome = new Intent(MoodActivity.this, HomeActivity.class);
            intentHome.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intentHome.putExtra("position", -1);
            intentHome.putExtra("room", "Settings");
            intentHome.putExtra("screen", "others");
            startActivity(intentHome);      //settings
            finish();
            AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Settings", "clicked", "");

        } else {
            if (_whichSection.equalsIgnoreCase("common area"))
                //moodvalue=false;
                _whichSection = "Global";

            roomPosition = position;
            CURRENT_TAB = CONFIG_TAB;
            updateScreen();

        }

    }


    private MoodEntity SetMoodData(String moodID) {

        String id = moodID.replace("mood", "");
        int roomPos = 1;
        int moodPos = 1;
        //System.out.println("moodID:"+id+","+id.charAt(0)+","+id.charAt(1));
        if (id.length() >= 2) {
            String roomNo = id.substring(0, id.length() - 1);
            roomPos = Integer.parseInt(roomNo);

            moodPos = Integer.parseInt(id.charAt(id.length() - 1) + "");
        }

        if (roomPos == 0)
            roomPos = 1;

        if (moodPos == 0)
            moodPos = 1;


        return createMoodEntity(list_rooms.get(roomPos - 1), moodNames[moodPos - 1]);


		/*
		if(section.toLowerCase().contains("living")&&section.toLowerCase().contains("gf")){
			hashMoodData.put("mood11", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood12", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood13", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood14", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood15", createMoodEntity(section,"Dinner"));

		}else if(section.toLowerCase().contains("living")&&section.toLowerCase().contains("ff")){
			hashMoodData.put("mood21", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood22", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood23", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood24", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood25", createMoodEntity(section,"Dinner"));

		}else if(section.toLowerCase().contains("bed")||section.toLowerCase().contains("mbr")&&section.toLowerCase().contains("gf")){
			hashMoodData.put("mood31", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood32", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood33", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood34", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood35", createMoodEntity(section,"Dinner"));
		}else if(section.toLowerCase().contains("bed")||section.toLowerCase().contains("mbr")&&section.toLowerCase().contains("ff")){
			hashMoodData.put("mood41", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood42", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood43", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood44", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood45", createMoodEntity(section,"Dinner"));
		}else if(section.toLowerCase().contains("bed")||section.toLowerCase().contains("br")&&section.toLowerCase().contains("gf")){
			hashMoodData.put("mood51", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood52", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood53", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood54", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood55", createMoodEntity(section,"Dinner"));
		}else if(section.toLowerCase().contains("bed")||section.toLowerCase().contains("br")&&section.toLowerCase().contains("ff")){
			hashMoodData.put("mood61", createMoodEntity(section,"Master On"));
			hashMoodData.put("mood62", createMoodEntity(section,"Master Off"));
			hashMoodData.put("mood63", createMoodEntity(section,"Morning"));
			hashMoodData.put("mood64", createMoodEntity(section,"Evening"));
			hashMoodData.put("mood65", createMoodEntity(section,"Dinner"));
		}else if(section.toLowerCase().contains("common")||section.toLowerCase().contains("global")){

		}

		 */
    }

    private MoodEntity createMoodEntity(String area, String mood) {
        MoodEntity entity = new MoodEntity();
        entity.setArea(area);
        entity.setMoodName(mood);
        return entity;
    }

    public void MoodApplyConfig() throws JSONException {
        int moodSize = moodNames.length;
        for (int i = 0; i < moodSize; i++) {
            if (_whichMood.equals(moodNames[i])) {
                moodPosition = i + 1;
                break;
            }
        }

        String moodID = "";
        if ("Global".equals(selectedSection)) {
            moodID = "mood0" + moodPosition;
        } else {
            moodID = "mood" + roomPosition + "" + moodPosition;
        }

        JSONObject obj = null;
        //App_Variable.MOOD_RESPONSE="";

        if (App_Variable.MOOD_RESPONSE.trim().equals("")) {
            String data = "{}";
            obj = new JSONObject(data);
        } else {
            obj = new JSONObject(App_Variable.MOOD_RESPONSE);
        }
        if (obj.has(moodID))
            obj.remove(moodID);

        CustomLog.d(tag, "moodID:" + moodID + ", obj::" + obj);

        listToJsonString(moodID, obj);
        AnalyticsTracker.trackEvents(this, screenName, "Mood Configuration", "applied", _whichSection + "/" + moodID + "/" + obj);

        callMoodApplyConfig(obj.toString(), moodID);

        CustomLog.camera(tag, "result:" + obj.toString());

    }

    MoodApply mp;

    private void callMoodApplyConfig(String payload, String moodid) {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            if (mp != null)
                mp.cancel(true);
            mp = new MoodApply(moodid);
            mp.execute(payload);
            //mp.execute(GetAPI.BASE_URL+"/cgi-bin/saya_webAPI.sh?/www/cgi-bin/scripts/atzi_con_embeddedMoods.sh"+moodid);
        } else {
            App_Variable.ShowNoNetwork(this);
        }
    }

    class MoodApply extends AsyncTask<String, Void, Void> {

        private String response = "";
        private String exception = "";
        private String moodval;

        public MoodApply(String moodid) {
            moodval = moodid;
            System.out.println("Mood id 1st:" + moodval);
        }


        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            progress.setMessage("Request for configuration is being sent..");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            BaseParser parser = new BaseParser(GetAPI.BASE_URL + GetAPI.MOOD_CONFIG_POST, params[0].trim());
            response = parser.getPOSTResponse();
            exception = parser.getException().trim();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            CustomLog.latest(tag, "apply config:" + response + ":::");

            progress.dismiss();
            if (!exception.equals("")) {
                //App_Variable.ShowErrorToast(exception, MoodActivity.this);
            } else {
                System.out.println("Mood id 1st:" + moodval);
                System.out.println("Mood id 1st Url:" + GetAPI.BASE_URL + "/cgi-bin/saya_webAPI.sh?/www/cgi-bin/scripts/atzi_con_embeddedMoods.sh+" + moodval);
                MoodApi ap = new MoodApi(GetAPI.BASE_URL + "/cgi-bin/saya_webAPI.sh?/www/cgi-bin/scripts/atzi_con_embeddedMoods.sh+" + moodval);
                ap.execute();
                MoodConfigSucessPopUp();

            }

            super.onPostExecute(result);
        }
    }

    class MoodApi extends AsyncTask<String, Void, Void> {
        public String Url;

        public MoodApi(String url) {
            Url = url;
            System.out.println("Mood id 2nd url:" + url);
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            BaseParser parser = new BaseParser(Url);
            parser.getResponse();
            return null;
        }

    }

    /*
	 * Remove
	 */
    public String listToJsonString(String key, JSONObject obj) {

        try {
            JSONObject styleJSON = new JSONObject();


            if ("Global".equals(selectedSection)) {
                Set entrySetKey;
                Iterator iteratorKey;
                //Set status of dimmer
                entrySetKey = dimmerListMap.entrySet();
                iteratorKey = entrySetKey.iterator();
                while (iteratorKey.hasNext()) {
                    Entry entryKey = (Entry) iteratorKey.next();
                    appendDimmerStatus(entryKey.getKey() + "", styleJSON);
                }
                //Set status of lights
                entrySetKey = lightListMap.entrySet();
                iteratorKey = entrySetKey.iterator();
                while (iteratorKey.hasNext()) {
                    Entry entryKey = (Entry) iteratorKey.next();
                    appendLightStatus(entryKey.getKey() + "", styleJSON);
                }

                entrySetKey = curtainListMap.entrySet();
                iteratorKey = entrySetKey.iterator();
                while (iteratorKey.hasNext()) {
                    Entry entryKey = (Entry) iteratorKey.next();
                    appendCurtenStatus(entryKey.getKey() + "", styleJSON);
                }

                entrySetKey = acListMap.entrySet();
                iteratorKey = entrySetKey.iterator();
                while (iteratorKey.hasNext()) {
                    Entry entryKey = (Entry) iteratorKey.next();
                    appendAcStatus(entryKey.getKey() + "", styleJSON);
                }
            } else {
                appendDimmerStatus(_whichSection, styleJSON);
                appendLightStatus(_whichSection, styleJSON);
                appendCurtenStatus(_whichSection, styleJSON);
                appendAcStatus(_whichSection, styleJSON);
            }

			/*int tvSize=listTV.size();
			for (int i = 0; i < tvSize; i++) {
				String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listTV.get(i));
				if(roomID!=null)
					styleJSON.put(roomID,tvCondition[i]);
			}

			int fanSize=listFan.size();
			for (int i = 0; i < fanSize; i++) {
				String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listFan.get(i));
				if(roomID!=null)
					styleJSON.put(roomID,fanCondition[i]);
			}*/

            obj.put(key, styleJSON);
        } catch (Exception jse) {
            CustomLog.e(tag, "config apply:" + jse);
        }

        CustomLog.latest("post:" + obj.toString());

        return obj.toString();
    }

    private void appendAcStatus(String whichSection, JSONObject styleJSON) throws JSONException {
        //int acSize = listAC.size();
        ArrayList<MoodEntity> acList = acListMap.get(whichSection);
        if(acList!=null && acList.size()>0) {
            int acSize = acList.size();
            String[] status = acStatusList.get(whichSection);
            for (int i = 0; i < acSize; i++) {
                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listAC.get(i));
                MoodEntity entity = acList.get(i);
                String roomID = entity.getRoomID();
                if (roomID != null)
                    styleJSON.put(roomID, status[i]);
            }
        }
    }

    private void appendCurtenStatus(String whichSection, JSONObject styleJSON) throws JSONException {
        //int curtainSize = listCurtains.size();
        ArrayList<MoodEntity> curtainList = curtainListMap.get(whichSection);
        if(curtainList!=null && curtainList.size()>0) {
            int curtainSize = curtainList.size();
            String[] status = curtainStatusList.get(whichSection);
            for (int i = 0; i < curtainSize; i++) {
                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listCurtains.get(i));
                MoodEntity entity = curtainList.get(i);
                String roomID = entity.getRoomID();

                if (roomID != null)
                    styleJSON.put(roomID, status[i]);

            }
        }
    }

    private void appendDimmerStatus(String _whichSection, JSONObject styleJSON) throws JSONException {

        ArrayList<MoodEntity> dimmerLights = dimmerListMap.get(_whichSection);
        if(dimmerLights!=null && dimmerLights.size()>0) {
            int dimmerSize = dimmerLights.size();
            int[] status = dimmerStatusList.get(_whichSection);
            for (int i = 0; i < dimmerSize; i++) {
                MoodEntity entity = dimmerLights.get(i);
                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+entity.getNameMood());
                String roomID = entity.getRoomID();
                if (roomID != null) {
                    if (status[i] == -1) {
                        styleJSON.put(roomID, "null");
                    } else {
                        styleJSON.put(roomID, "level" + status[i]);
                    }
                }
            }
        }
    }

    private void appendLightStatus(String _whichSection, JSONObject styleJSON) throws JSONException {
        ArrayList<MoodEntity> listLights = lightListMap.get(_whichSection);
        if(listLights!=null && listLights.size()>0) {
            int lightSize = listLights.size();
            String[] status = lightStatusList.get(_whichSection);
            for (int i = 0; i < lightSize; i++) {
                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listLights.get(i));
                MoodEntity entity = listLights.get(i);
                String roomID = entity.getRoomID();
                if (roomID != null) {
                    styleJSON.put(roomID, status[i]);
                    // styleJSON.put(roomID, lightStatus[i]);
                }
            }
        }
    }

    public void MoodConfigSucessPopUp() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Mood Configuration Done")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void updateScreen() {
        callMoodConfigData(true);
		/*if(!checkSectionEmpty()){
			layout_mood_config.setVisibility(View.VISIBLE);
			ShowMoodConfigurationView();
		}else{
			layout_mood_config.setVisibility(View.GONE);
		}*/
        txtMoodAreaValue.setText(_whichSection);
    }

    private void ResetMoodIcon() {
        imgMoodOn.setImageResource(R.drawable.mood_master_unselected);
        imgMoodOff.setImageResource(R.drawable.mood_master_unselected);
        imgMoodMorning.setImageResource(R.drawable.mood_morning_unselected);
        imgMoodEvening.setImageResource(R.drawable.mood_evening_unselected);
        imgMoodDinner.setImageResource(R.drawable.mood_dinner_unselected);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tab_config:
                CustomLog.latest("tab_config");
                CURRENT_TAB = CONFIG_TAB;
                img_tab_config_bottom.setVisibility(View.VISIBLE);
                img_tab_schedule_bottom.setVisibility(View.GONE);
                img_tab_status_bottom.setVisibility(View.GONE);
                if (_whichSection.trim().equals("")) {
                    callPopup();
                } else {


                    layout_mood_schedule.setVisibility(View.GONE);
                    layout_mood_status.setVisibility(View.GONE);

                    updateScreen();
                }
                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Tab", "clicked", "configuration/0");

			/*if(!checkSectionEmpty()){
				layout_mood_config.setVisibility(View.VISIBLE);
				ShowMoodConfigurationView();
			}else{
				layout_mood_config.setVisibility(View.GONE);
			}*/


                break;
            case R.id.tab_schedule:

                if ("Global".equals(selectedSection)) {
                    scheduleDisabledAlert();
                }else {
                    CustomLog.latest("tab_schedule");
                    CURRENT_TAB = SCHDULE_TAB;
                    img_tab_config_bottom.setVisibility(View.GONE);
                    img_tab_schedule_bottom.setVisibility(View.VISIBLE);
                    img_tab_status_bottom.setVisibility(View.GONE);
                    if (_whichSection.trim().equals("")) {
                        callPopup();
                    } else {


                        layout_mood_schedule.setVisibility(View.VISIBLE);
                        layout_mood_status.setVisibility(View.GONE);
                        layout_mood_config.setVisibility(View.GONE);

                        ShowScheduleView();
                    }
                    AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Tab", "clicked", "scheduling/1");

                }
                break;
            case R.id.tab_status:
                CustomLog.latest("tab_status");
                CURRENT_TAB = STATUS_TAB;
                img_tab_config_bottom.setVisibility(View.GONE);
                img_tab_schedule_bottom.setVisibility(View.GONE);
                img_tab_status_bottom.setVisibility(View.VISIBLE);
                if (_whichSection.trim().equals("")) {
                    callPopup();
                } else {
                    layout_mood_schedule.setVisibility(View.GONE);
                    layout_mood_config.setVisibility(View.GONE);

                    callMoodStatus();
                }
                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Tab", "clicked", "status/2");

                break;
            case R.id.img_slider:
                if (sm != null)
                    sm.toggle();
                break;
            case R.id.ApplyMoodConfig:
                try {
                    MoodApplyConfig();
                } catch (JSONException e) {
                    System.out.println("apply config1111111111::" + e);
                    e.printStackTrace();
                }

                break;
            case R.id.txtApplyDateMood:
                showDialog(0);

                break;
            case R.id.txtApplyTimeMood:
                showDialog(1);

                break;
            case R.id.txtApplyDailyMood:
			/*
			 * mood schedule daily apply
			 */

                CustomLog.debug(_whichSection + "&&" + _whichMood + "whichmood=" + ("mood" + roomPosition + "" + moodPosition));


                String urlString = GetAPI.BASE_URL + GetAPI.MOOD_SCHEDULE1;
                urlString += App_Variable.getAppendZero("" + mMinSelected) + "%20" + App_Variable.getAppendZero("" + mHourSelected) + "%20-1%20-1%20";
                urlString += GetAPI.MOOD_SCHEDULE2;
                urlString += ("%20" + ("mood" + roomPosition + "" + moodPosition));

                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Scheduling Daily", "applied date",
                        selectedDate + "/" + txtApplyTimeMood.getText());
                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Scheduling Daily", "applied mood",
                        _whichMood + "/" + _whichSection);


                callScheduleApply(urlString);
			/*}else
			{
				Toast.makeText(this, "not possible to select past time", Toast.LENGTH_LONG).show();
			}*/
                break;

            case R.id.txtApplyOnceMood:
                CustomLog.latest("txtApplyOnceMood");
			/*
			 * mood schedule once apply
			 */
                String urlOnceString = GetAPI.BASE_URL + GetAPI.MOOD_SCHEDULE1;
                urlOnceString += App_Variable.getAppendZero("" + mMinSelected) + "%20" + App_Variable.getAppendZero("" + mHourSelected) + "%20" + App_Variable.getAppendZero("" + mDaySelected) + "%20" + App_Variable.getAppendZero("" + mMonthSelected) + "%20";
                urlOnceString += GetAPI.MOOD_SCHEDULE2;
                urlOnceString += ("%20" + ("mood" + roomPosition + "" + moodPosition));

                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Scheduling Once", "applied date",
                        selectedDate + "/" + txtApplyTimeMood.getText());
                AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Scheduling Once", "applied mood",
                        _whichMood + "/" + _whichSection);

                callScheduleApply(urlOnceString);

//			}else {
//			Toast.makeText(MoodActivity.this,"cont set past time",Toast.LENGTH_LONG).show();
//			}


                break;

            case R.id.moodMasterOn:
                _whichMood = moodNames[0];
                moodPosition = 1;
                ResetMoodIcon();
                imgMoodOn.setImageResource(R.drawable.mood_master);

                callMoodConfigData(true);
                //ShowMoodConfigurationView();
                txtMoodRoomValue.setText(_whichMood);
                break;
            case R.id.moodMasterOff:
                _whichMood = moodNames[1];
                moodPosition = 2;
                ResetMoodIcon();
                imgMoodOff.setImageResource(R.drawable.mood_master);

                txtMoodRoomValue.setText(_whichMood);
                //ShowMoodConfigurationView();
                callMoodConfigData(true);
                break;
            case R.id.moodMorning:
                _whichMood = moodNames[2];
                moodPosition = 3;
                txtMoodRoomValue.setText(_whichMood);

                ResetMoodIcon();
                imgMoodMorning.setImageResource(R.drawable.mood_morning);

                //ShowMoodConfigurationView();
                callMoodConfigData(true);
                break;
            case R.id.moodEvening:
                _whichMood = moodNames[3];
                moodPosition = 4;
                txtMoodRoomValue.setText(_whichMood);

                ResetMoodIcon();
                imgMoodEvening.setImageResource(R.drawable.mood_evening);

                //ShowMoodConfigurationView();
                callMoodConfigData(true);
                break;
            case R.id.moodDinner:
                _whichMood = moodNames[4];
                moodPosition = 5;
                txtMoodRoomValue.setText(_whichMood);
                //ShowMoodConfigurationView();
                ResetMoodIcon();
                imgMoodDinner.setImageResource(R.drawable.mood_dinner);
                callMoodConfigData(true);
                break;

            default:
                break;
        }
    }


    private boolean checkSectionEmpty() {
        if (_whichSection.trim().equals("")) {
            return true;
        }
        return false;

    }

    private void ShowMoodConfigurationView(View view) {
        if (CURRENT_TAB == CONFIG_TAB) {
            layout_mood_schedule.setVisibility(View.GONE);
            layout_mood_status.setVisibility(View.GONE);
            layout_mood_config.setVisibility(View.VISIBLE);

            img_tab_config_bottom.setVisibility(View.VISIBLE);
            img_tab_schedule_bottom.setVisibility(View.GONE);
            img_tab_status_bottom.setVisibility(View.GONE);

        } else if (CURRENT_TAB == SCHDULE_TAB) {
            layout_mood_schedule.setVisibility(View.VISIBLE);
            layout_mood_status.setVisibility(View.GONE);
            layout_mood_config.setVisibility(View.GONE);

            img_tab_config_bottom.setVisibility(View.GONE);
            img_tab_schedule_bottom.setVisibility(View.VISIBLE);
            img_tab_status_bottom.setVisibility(View.GONE);

        } else if (CURRENT_TAB == STATUS_TAB) {
            layout_mood_schedule.setVisibility(View.GONE);
            layout_mood_status.setVisibility(View.VISIBLE);
            layout_mood_config.setVisibility(View.GONE);

            img_tab_config_bottom.setVisibility(View.GONE);
            img_tab_schedule_bottom.setVisibility(View.GONE);
            img_tab_status_bottom.setVisibility(View.VISIBLE);
        }


        layout_mood.setVisibility(View.VISIBLE);


        countOfData = 0;
        isExtraData = false;
        listDimmers = new ArrayList<MoodEntity>();
        listCurtains = new ArrayList<MoodEntity>();
        listLights = new ArrayList<MoodEntity>();
        listFan = new ArrayList<String>();
        listAC = new ArrayList<MoodEntity>();
        //listTV=new ArrayList<String>();

        listDimmersID = new ArrayList<String>();
        listLightsID = new ArrayList<String>();

        GetandGroupData(_whichSection);

        ArrayList<String> listOfItems = App_Variable.hashRoomType.get(_whichSection);
        listData = App_Variable.hashRoomOptions.get(_whichSection);

       /* if (App_Variable.hashtabname.get(_whichSection) != null) {
            int sizeOfitem = listOfItems.size() - 1;

            for (int i = 0; i < sizeOfitem; i++) {
                String type = listOfItems.get(i).trim();
                CustomLog.debug("type:::" + type);
                Hashtable<String, HomeEntity> hashEntity = listData.get(i);
                HomeEntity entity = hashEntity.get(type);
                //CustomLog.debug(entity.getId()+","+entity.getChannel());
            }
        } else {
            int sizeOfitem = listOfItems.size();
            for (int i = 0; i < sizeOfitem; i++) {
                String type = listOfItems.get(i).trim();
                CustomLog.debug("type:::" + type);
                Hashtable<String, HomeEntity> hashEntity = listData.get(i);
                HomeEntity entity = hashEntity.get(type);
            }
        }*/


        GroupingTabData(listOfItems);

        SortingData();

        DisplayGroupData();

        DisplayDimmerView(view);

        DisplayLightView(view);

        DisplayCurtainView(view);

        DisplayACView(view);

        DisplayTVView(view);

        DisplayFanView(view);

        //	-------------

    }

    private void GroupingTabData(ArrayList<String> listOfItems) {

        int size;
        if (App_Variable.hashtabname.get(_whichSection) != null) {
            size = listOfItems.size() - 1;
        } else {
            size = listOfItems.size();
        }
        for (int i = 0; i < size; i++) {
            String item = listOfItems.get(i).trim();
            Hashtable<String, HomeEntity> hashEntity = listData.get(i);
            HomeEntity entity = hashEntity.get(listOfItems.get(i));
            String typekey = "";
            if ("projector".equals(item.toLowerCase()))
                typekey = _whichSection + "&&" + (item.substring(0, 1).toUpperCase() + item.substring(1, item.length()));
            else
                typekey = _whichSection + "&&" + item;
            listhashtype = App_Variable.hashtype;
            String typekey1 = listhashtype.get(typekey).toLowerCase();
            MoodEntity entityMood = new MoodEntity();
            entityMood.setNameMood(listOfItems.get(i) + "");
            if (typekey1.contains("ac")) {
                entityMood.setRoomID(CreateControlID(entity, "ac"));
                listAC.add(entityMood);

            } else if (typekey1.contains("dimming")) {
                entityMood.setRoomID(CreateControlID(entity, "dimmer"));
                listDimmers.add(entityMood);
            } else if (typekey1.contains("switching")) {
                entityMood.setRoomID(CreateControlID(entity, "light"));
                listLights.add(entityMood);
            } else if (typekey1.contains("curtain")) {
                entityMood.setRoomID(listOfItems.get(i) + "");
                listCurtains.add(entityMood);
                CustomLog.debug("curtain roomID:" + listOfItems.get(i));
            }
        }
    }

    /**
     * @param entity contains dimmer details
     */
    private String CreateControlID(HomeEntity entity, String type) {
        String id = "";
        String dimmerID = entity.getId();
        String channel = entity.getChannel();

        //	CustomLog.debug("id:::"+entity.g);

		/*if(type.contains("dimmer"))
			id+="srd";
		else if(type.contains("light"))
			id+="srm";
		else if(type.contains("ac"))
			id+="ir";*/
        if (dimmerID.startsWith("SR_D")) {
            id += "srm";
        } else if (dimmerID.startsWith("SD_D")) {
            id += "srd";
        } else if (dimmerID.startsWith("IR")) {
            id += "ir";
        }

        String roomNo = dimmerID.substring(dimmerID.length() - 2);
        int mode = 0;
        if (App_Variable.isNumeric(roomNo)) {
            mode = Integer.parseInt(roomNo);
        }
        //CustomLog.debug("roomNo:"+mode);
        id += mode;

        if (!type.contains("ac")) {
            id += "_ch";
            id += channel;
        }

        CustomLog.debug("roomID:" + id);

        return id;
    }


    /**
     * Grouping data for lights, dimmer,curtains,fan,tv ansd ac views
     */
    int countOfData = 0;

    private void GetandGroupData(String whichSection) {

        //CustomLog.latest("hashMoodFind:"+hashMoodFind);
        CustomLog.latest("whichSection:" + whichSection);
        CustomLog.latest("_whichMood:" + _whichMood);
        String moodID = "";
        if ("Global".equals(selectedSection)) {
            moodID = "mood0" + moodPosition;
        } else {
            moodID = "mood" + roomPosition + "" + moodPosition;
        }
        CustomLog.latest("config mood:" + moodID);
        if (moodID != null) {
            if (listMoodConfig != null) {
                int size = listMoodConfig.size();
                if (size > 0) {
                    LinkedHashMap<String, LinkedHashMap<String, String>> hashMood = listMoodConfig.get(0);

                    hashKey = hashMood.get(moodID);

                    if (hashKey != null) {
                        isData = true;
                        CustomLog.debug("MoodID:" + moodID + "," + "hashKey");
                        //GroupingListData(moodID,hashMood);

                    } else {
                        CustomLog.debug("MoodID:" + moodID + "," + "null");
                        //GroupingEmptyData(moodID,hashMood);
                        //MOOD_SIZE
                    }
                }
            }
        }

    }


    private void SortingData() {

        Collections.sort(listLights, new NameSorter());
        Collections.sort(listDimmers, new NameSorter());
        Collections.sort(listCurtains, new NameSorter());
        Collections.sort(listFan);
        Collections.sort(listAC, new NameSorter());
        Collections.sort(listTV);
    }

    private void DisplayGroupData() {
        if (listLights.size() == 0)
            view_config_light.setVisibility(View.GONE);
        else
            view_config_light.setVisibility(View.VISIBLE);


        if (listDimmers.size() == 0)
            view_config_dimmer.setVisibility(View.GONE);
        else
            view_config_dimmer.setVisibility(View.VISIBLE);

        if (listCurtains.size() == 0)
            view_config_curtain.setVisibility(View.GONE);
        else
            view_config_curtain.setVisibility(View.VISIBLE);

        if (listFan.size() == 0)
            view_config_fan.setVisibility(View.GONE);
        else
            view_config_fan.setVisibility(View.VISIBLE);

        if (listTV.size() == 0)
            view_config_tv.setVisibility(View.GONE);
        else
            view_config_tv.setVisibility(View.VISIBLE);

        if (listAC.size() == 0)
            view_config_ac.setVisibility(View.GONE);
        else
            view_config_ac.setVisibility(View.VISIBLE);

    }


    private void DisplayDimmerView(View view1) {
        HorizontalScrollView scrollViewDimmer = (HorizontalScrollView) view1.findViewById(R.id.scrollViewDimmerConfig);
        scrollViewDimmer.scrollTo(0, 0);

        LinearLayout linearDimmer = (LinearLayout) view1.findViewById(R.id.linearDimmerConfig);
        linearDimmer.removeAllViewsInLayout();
        linearDimmer.removeAllViews();

        int sizeOfDimmer = listDimmers.size();
        final TextView[] dimmerLevel1 = new TextView[sizeOfDimmer];
        final int[] dimmerRange1 = new int[sizeOfDimmer];

        for (int i = 0; i < sizeOfDimmer; i++) {

            dimmerRange1[i] = 0;
            final MoodEntity entity = listDimmers.get(i);
            View view = getLayoutInflater().inflate(R.layout.dimmer_config_view, null);

            /**
             * add don't care
             */
            final CheckBox cbdimmerDCare = (CheckBox) view.findViewById(R.id.cbdimmerDCare);
            cbdimmerDCare.setChecked(false);


            final TextView txtDimmer = (TextView) view.findViewById(R.id.txtDimmer);
            final TextView dimmerMinus = (TextView) view.findViewById(R.id.dimmerMinus);
            final TextView dimmerPlus = (TextView) view.findViewById(R.id.dimmerPlus);
            //txtDimmer.setText(listDimmers.get(i));
            CustomLog.resume("App_Variable.hashNickName:" + App_Variable.hashNickName);
            if (!App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()).equals(""))
                txtDimmer.setText(App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()));
            else
                txtDimmer.setText(entity.getNameMood());


            dimmerLevel1[i] = (TextView) view.findViewById(R.id.dimmerLevel);

            String roomID = entity.getRoomID();
            //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listDimmers.get(i));
            CustomLog.latest("roomID:::" + roomID);
            String value = "level0";//hashKey.get(listDimmers.get(i));
            if (roomID != null) {
                if (hashKey != null) {
                    value = hashKey.get(roomID);
                    if (value == null)
                        value = "level0";
                    CustomLog.latest("value:::" + value);
                }
            }

            CustomLog.latest("dimmer value:::" + value);
            int levelValue = 0;

            if (isExtraData) {
                dimmerLevel1[i].setText("0");
            } else {
                if (isData) {
                    if (value != null) {
                        if (value.contains("level")) {
                            levelValue = Integer.parseInt(value.replace("level", ""));
                            dimmerLevel1[i].setText(levelValue + "");
                            dimmerRange1[i] = levelValue;
                            cbdimmerDCare.setChecked(false);
                        } else {
                            if ("null".equals(value)) {
                                cbdimmerDCare.setChecked(true);
                                dimmerRange1[i] = -1;
                                dimmerPlus.setClickable(false);
                                dimmerMinus.setClickable(false);
                            }
                            dimmerLevel1[i].setText("0");
                        }
                    }
                } else {
                    dimmerLevel1[i].setText("0");
                    cbdimmerDCare.setChecked(false);
                    CustomLog.latest("dimmer value:::" + value);
                }
            }

            //this.dimmerLevel = dimmerLevel1;
            this.dimmerRange = dimmerRange1;

            dimmerStatusList.put(_whichSection, dimmerRange1);
            dimmerListMap.put(_whichSection, listDimmers);


            /**
             * event for dimmer decrement(-)
             */

            dimmerMinus.setTag(i + "/" + _whichSection);
            ;
            final int finalI = i;
            dimmerMinus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (!cbdimmerDCare.isChecked()) {
                        String[] tag = ("" + v.getTag()).split("/");
                        int index = Integer.parseInt(tag[0]);
                        int[] dimmerRange1 = dimmerStatusList.get(tag[1]);

                        // if (dimmerRange1[index] != -1) {
                        int level = Integer.parseInt(dimmerLevel1[index].getText() + "");
                        CustomLog.debug("- " + level);
                        if (level == 0)
                            level = 0;
                        else {
                            level--;
                        }
                        CustomLog.debug("-- " + level);
                        dimmerLevel1[index].setText(level + "");
                        dimmerRange1[index] = level;
                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Dimmer Mood", "minus clicked",
                                _whichSection + "/" + _whichMood + "/" + txtDimmer.getText(), level);
                        // }

                        dimmerStatusList.put(_whichSection, dimmerRange1);
                    }
                }
            });

            /**
             * event for dimmer increment(+)
             */

            dimmerPlus.setTag(i + "/" + _whichSection);

            dimmerPlus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    if (!cbdimmerDCare.isChecked()) {
                        String[] tag = ("" + v.getTag()).split("/");
                        int index = Integer.parseInt(tag[0]);
                        int[] dimmerRange1 = dimmerStatusList.get(tag[1]);
                        //if (dimmerRange1[index] != -1) {
                        int level = Integer.parseInt(dimmerLevel1[index].getText() + "");
                        CustomLog.debug("+ " + level);
                        if (level == 13)
                            level = 13;
                        else {
                            level++;
                        }
                        CustomLog.debug("++ " + level);

                        dimmerLevel1[index].setText(level + "");
                        dimmerRange1[index] = level;

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Dimmer Mood", "plus clicked",
                                _whichSection + "/" + _whichMood + "/" + txtDimmer.getText(), level);
                        // }
                        dimmerStatusList.put(_whichSection, dimmerRange1);
                    }
                }
            });

            final int finalI1 = i;
            cbdimmerDCare.setTag(i + "/" + _whichSection);
            cbdimmerDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String[] tag = ("" + buttonView.getTag()).split("/");
                    int index = Integer.parseInt(tag[0]);
                    int[] dimmerRange1 = dimmerStatusList.get(tag[1]);

                    if (isChecked) {
                        dimmerRange1[index] = -1;
                        dimmerLevel1[index].setText("0");
                        dimmerPlus.setClickable(false);
                        dimmerMinus.setClickable(false);
                    } else {
                        dimmerRange1[index] = 0;
                        dimmerPlus.setClickable(true);
                        dimmerMinus.setClickable(true);
                    }

                    dimmerStatusList.put(_whichSection, dimmerRange1);
                }
            });

            linearDimmer.addView(view);
        }
    }

    //int[] countLight;
    //String lightData = "";
    //String[] lightStatus;

    private void DisplayLightView(View view1) {
        HorizontalScrollView scrollViewLight = (HorizontalScrollView) view1.findViewById(R.id.scrollViewLightConfig);
        scrollViewLight.scrollTo(0, 0);

        LinearLayout linearLight = (LinearLayout) view1.findViewById(R.id.linearLightConfig);
        linearLight.removeAllViewsInLayout();
        linearLight.removeAllViews();

        int sizeOfLight = listLights.size();

        //imgLightStatus =new ImageView[sizeOfLight];
        final int[] countLight = new int[sizeOfLight];
        String[] lightStatus = new String[sizeOfLight];
        String lightData = "";

        for (int i = 0; i < sizeOfLight; i++) {
            View view = getLayoutInflater().inflate(R.layout.light_config_view, null);

            countLight[i] = 0;
            MoodEntity entity = listLights.get(i);

            /**
             * add don't care
             */
            final CheckBox cbLightDCare = (CheckBox) view.findViewById(R.id.cbLightDCare);
            cbLightDCare.setChecked(false);
            final TextView txtLight = (TextView) view.findViewById(R.id.txtLight);
            //txtLight.setText(hashMoodAppliance.get(listLights.get(i)));
            //
            if (!App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()).equals(""))
                txtLight.setText(App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()));
            else
                txtLight.setText(entity.getNameMood());


            /**
             * set light status on or off
             */
            lightData = "";
            final ImageView imgLightStatus = (ImageView) view.findViewById(R.id.imgLightStatus);

            String roomID = entity.getRoomID();
            //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listLights.get(i));
            CustomLog.latest("light roomID:::" + roomID);
            lightData = "off";//lightData =hashKey.get(listLights.get(i));
            if (roomID != null) {
                if (hashKey != null) {
                    lightData = hashKey.get(roomID);
                    CustomLog.latest("light value:::" + lightData);
                }
            }

            if (isExtraData) {
                lightData = "off";
            } else {
                if (!isData) {
                    lightData = "off";
                }
            }
            if (lightData != null) {
                if (lightData.equals("off")) {
                    imgLightStatus.setImageResource(R.drawable.on_icon);
                    cbLightDCare.setChecked(false);
                } else if ("null".equals(lightData)) {
                    imgLightStatus.setImageResource(R.drawable.on_icon);
                    cbLightDCare.setChecked(true);
                    imgLightStatus.setClickable(false);
                } else {
                    imgLightStatus.setImageResource(R.drawable.off_icon);
                    cbLightDCare.setChecked(false);
                }
            } else {
                lightData = "off";
            }

            lightStatus[i] = lightData;

            lightStatusList.put(_whichSection, lightStatus);
            lightListMap.put(_whichSection, listLights);

            /**
             * event for light on or off
             */
            imgLightStatus.setTag(i + "/" + _whichSection);
            cbLightDCare.setTag(i + "/" + _whichSection);
            imgLightStatus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!cbLightDCare.isChecked()) {
                        String[] tag = ("" + v.getTag()).split("/");
                        int index = Integer.parseInt(tag[0]);
                        int pos = countLight[index];
                        String[] lightStatus = lightStatusList.get(tag[1]);
                        System.out.println("inc===" + pos + "," + lightStatus[index] + ", index=" + index);


                        if (lightStatus[index].equals("off")) {
                            lightStatus[index] = "on";
                            imgLightStatus.setImageResource(R.drawable.off_icon);
                        } else if (lightStatus[index].equals("null")) {
                            lightStatus[index] = "on";
                            imgLightStatus.setImageResource(R.drawable.off_icon);
                        } else {
                            lightStatus[index] = "off";
                            imgLightStatus.setImageResource(R.drawable.on_icon);
                        }
                        countLight[index]++;

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Light Mood", lightStatus[index] + " clicked",
                                _whichSection + "/" + _whichMood + "/" + txtLight.getText());
                        lightStatusList.put(tag[1], lightStatus);
                    }
                }
            });

            final int finalI = i;
            cbLightDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    String[] tag = ("" + buttonView.getTag()).split("/");
                    int index = Integer.parseInt(tag[0]);
                    String[] lightStatus = lightStatusList.get(tag[1]);
                    if (isChecked) {
                        lightStatus[index] = "null";
                        imgLightStatus.setImageResource(R.drawable.on_icon);
                        imgLightStatus.setClickable(false);
                    } else {
                        lightStatus[index] = "off";
                        imgLightStatus.setClickable(true);
                    }

                    lightStatusList.put(tag[1], lightStatus);
                }
            });

            linearLight.addView(view);
        }
        // lightStatusList.put(_whichSection,lightStatus);
        //lightListMap.put(_whichSection,listLights);
    }

    int[] countCurtain;
    String curtainStatus = "";
    String[] cutainCondition;

    private void DisplayCurtainView(View view1) {
        HorizontalScrollView scrollViewCurtain = (HorizontalScrollView) view1.findViewById(R.id.scrollViewCurtainConfig);
        scrollViewCurtain.scrollTo(0, 0);

        LinearLayout linearCurtain = (LinearLayout) view1.findViewById(R.id.linearCurtainConfig);
        linearCurtain.removeAllViewsInLayout();
        linearCurtain.removeAllViews();

        int sizeOfCurtains = listCurtains.size();

        //imgLightStatus =new ImageView[sizeOfLight];
        int[] countCurtain = new int[sizeOfCurtains];
        String[] cutainCondition = new String[sizeOfCurtains];
        for (int i = 0; i < sizeOfCurtains; i++) {
            View view = getLayoutInflater().inflate(R.layout.curtain_config_view, null);

            countCurtain[i] = 0;
            MoodEntity entity = listCurtains.get(i);
            /**
             * add don't care
             */
            final CheckBox cbCurtainDCare = (CheckBox) view.findViewById(R.id.cbCurtainDCare);
            cbCurtainDCare.setChecked(false);
            final TextView txtLight = (TextView) view.findViewById(R.id.txtCurtain);
            //txtLight.setText(hashMoodAppliance.get(listCurtains.get(i)));
            //
            if (!App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()).equals(""))
                txtLight.setText(App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()));
            else
                txtLight.setText(entity.getNameMood());

            /**
             * set light status on or off
             */
            curtainStatus = "";
            final ImageView imgCurtainStatus = (ImageView) view.findViewById(R.id.imgCurtainStatus);

            //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listCurtains.get(i));
            String roomID = entity.getRoomID();
            CustomLog.latest("roomID:::" + roomID);
            curtainStatus = "close";//curtainStatus =hashKey.get(listCurtains.get(i));
            if (roomID != null) {
                if (hashKey != null) {
                    curtainStatus = hashKey.get(roomID);
                    CustomLog.latest("value:::" + curtainStatus);
                }
            }

            if (isExtraData) {
                curtainStatus = "close";
            } else {
                if (!isData) {
                    curtainStatus = "close";
                }
            }
            if (curtainStatus != null) {
                if (curtainStatus.trim().equals("close")) {
                    imgCurtainStatus.setImageResource(R.drawable.open_icon);
                    cbCurtainDCare.setChecked(false);
                } else if ("null".equals(curtainStatus)) {
                    imgCurtainStatus.setImageResource(R.drawable.open_icon);
                    cbCurtainDCare.setChecked(true);
                    imgCurtainStatus.setClickable(false);
                } else {
                    imgCurtainStatus.setImageResource(R.drawable.close_icon);
                    cbCurtainDCare.setChecked(false);
                }
            } else {
                curtainStatus = "close";
            }
            cutainCondition[i] = curtainStatus;

            curtainStatusList.put(_whichSection, cutainCondition);
            curtainListMap.put(_whichSection, listCurtains);

            /**
             * event for curtain on or off
             */
            imgCurtainStatus.setTag(i + "/" + _whichSection);
            cbCurtainDCare.setTag(i + "/" + _whichSection);
            imgCurtainStatus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!cbCurtainDCare.isChecked()) {

                        String[] tag = ("" + v.getTag()).split("/");
                        int index = Integer.parseInt(tag[0]);
                        String[] curtainStatus = curtainStatusList.get(tag[1]);

                        if (curtainStatus[index].equals("close")) {
                            curtainStatus[index] = "open";
                            imgCurtainStatus.setImageResource(R.drawable.close_icon);
                        } else if (curtainStatus[index].equals("null")) {
                            curtainStatus[index] = "open";
                            imgCurtainStatus.setImageResource(R.drawable.close_icon);
                        } else {
                            curtainStatus[index] = "close";
                            imgCurtainStatus.setImageResource(R.drawable.open_icon);
                        }

                        String action = "";
                        if (curtainStatus[index].equalsIgnoreCase("close")) {
                            action = "closed";
                        } else {
                            action = "opened";
                        }

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Curtain Mood", action,
                                _whichSection + "/" + _whichMood + "/" + txtLight.getText());
                        curtainStatusList.put(tag[1], curtainStatus);
                    }
                }
            });

            final int finalI = i;
            cbCurtainDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    String[] tag = ("" + buttonView.getTag()).split("/");
                    int index = Integer.parseInt(tag[0]);
                    String[] curtainStatus = curtainStatusList.get(tag[1]);
                    if (curtainStatus.length > finalI)
                        if (isChecked) {
                            curtainStatus[index] = "null";
                            imgCurtainStatus.setImageResource(R.drawable.open_icon);
                            imgCurtainStatus.setClickable(false);
                        } else {
                            curtainStatus[index] = "close";
                            imgCurtainStatus.setClickable(true);
                        }

                    curtainStatusList.put(tag[1], curtainStatus);
                }
            });

            linearCurtain.addView(view);
        }

    }

    int[] countAC;
    String acStatus = "";
    String[] acCondition;

    private void DisplayACView(View view1) {

        HorizontalScrollView scrollViewACConfig = (HorizontalScrollView) view1.findViewById(R.id.scrollViewACConfig);
        scrollViewACConfig.scrollTo(0, 0);

        LinearLayout linearACConfig = (LinearLayout) view1.findViewById(R.id.linearACConfig);
        linearACConfig.removeAllViewsInLayout();
        linearACConfig.removeAllViews();

        int sizeOfAC = listAC.size();
        //imgLightStatus =new ImageView[sizeOfLight];
        int[] countAC = new int[sizeOfAC];
        String[] acCondition = new String[sizeOfAC];

        for (int i = 0; i < sizeOfAC; i++) {
            View view = getLayoutInflater().inflate(R.layout.ac_config_view, null);

            countAC[i] = 0;
            MoodEntity entity = listAC.get(i);
            final TextView txtAC = (TextView) view.findViewById(R.id.txtAC);

            /**
             * add don't care
             */
            final CheckBox cbACDCare = (CheckBox) view.findViewById(R.id.cbACDCare);
            cbACDCare.setChecked(false);
            //txtAC.setText(hashMoodAppliance.get(listAC.get(i)));
            //txtAC.setText(listAC.get(i));
            if (!App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()).equals(""))
                txtAC.setText(App_Variable.hashNickName.get(_whichSection + "&&" + entity.getNameMood()));
            else
                txtAC.setText(entity.getNameMood());

            /**
             * set light status on or off
             */
            acStatus = "";
            final ImageView imgCurtainStatus = (ImageView) view.findViewById(R.id.imgACStatus);


            //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listAC.get(i));
            String roomID = entity.getRoomID();

            CustomLog.latest("roomID:::" + roomID);
            acStatus = "off"; //acStatus =hashKey.get(listAC.get(i));
            if (roomID != null) {
                if (hashKey != null) {
                    acStatus = hashKey.get(roomID);
                    CustomLog.latest("value:::" + acStatus);
                }
            }

            if (isExtraData) {
                acStatus = "off";
            } else {
                if (!isData)
                    acStatus = "off";
            }

            acCondition[i] = acStatus;

            if (acStatus != null) {
                if (acStatus.equals("off")) {
                    imgCurtainStatus.setImageResource(R.drawable.on_icon);
                    cbACDCare.setChecked(false);
                } else if ("null".equals(acStatus)) {
                    imgCurtainStatus.setImageResource(R.drawable.on_icon);
                    cbACDCare.setChecked(true);
                    imgCurtainStatus.setClickable(false);
                } else {
                    imgCurtainStatus.setImageResource(R.drawable.off_icon);
                    cbACDCare.setChecked(false);
                }
            }

            acStatusList.put(_whichSection, acCondition);
            acListMap.put(_whichSection, listAC);


            /**
             * event for curtain on or off
             */
            imgCurtainStatus.setTag(i + "/" + _whichSection);
            cbACDCare.setTag(i + "/" + _whichSection);
            imgCurtainStatus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!cbACDCare.isChecked()) {
                        String[] tag = ("" + v.getTag()).split("/");
                        int index = Integer.parseInt(tag[0]);
                        String[] acStatus = acStatusList.get(tag[1]);
                        // int pos = countAC[index];
                        // System.out.println("inc===" + pos + "," + acCondition[index]);
                        if (acStatus != null) {
                            if (acStatus[index] != null) {
                                if (acStatus[index].equals("off")) {
                                    acStatus[index] = "on";
                                    imgCurtainStatus.setImageResource(R.drawable.off_icon);
                                } else if (acStatus[index].equals("null")) {
                                    acStatus[index] = "on";
                                    imgCurtainStatus.setImageResource(R.drawable.off_icon);
                                } else {
                                    acStatus[index] = "off";
                                    imgCurtainStatus.setImageResource(R.drawable.on_icon);
                                }
                            } else {

                            }
                        }

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "AC Mood", acStatus[index] + " clicked",
                                _whichSection + "/" + _whichMood + "/" + txtAC.getText());

                        acStatusList.put(tag[1], acStatus);
					/*if(acCondition[index].equals("off")){
						if(pos%2==0){
							acCondition[index]="on";
							imgCurtainStatus.setImageResource(R.drawable.off_icon);
						}else{
							acCondition[index]="off";
							imgCurtainStatus.setImageResource(R.drawable.on_icon);
						}
					}else{
						if(pos%2==0){
							acCondition[index]="off";
							imgCurtainStatus.setImageResource(R.drawable.on_icon);
						}else{
							acCondition[index]="on";
							imgCurtainStatus.setImageResource(R.drawable.off_icon);
						}
					}*/
                        //countAC[index]++;
                    }
                }
            });


            final int finalI = i;
            cbACDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String[] tag = ("" + buttonView.getTag()).split("/");
                    int index = Integer.parseInt(tag[0]);
                    String[] acStatus = acStatusList.get(tag[1]);
                    if (isChecked) {
                        acStatus[index] = "null";
                        imgCurtainStatus.setImageResource(R.drawable.on_icon);
                        imgCurtainStatus.setClickable(false);
                    } else {
                        acStatus[index] = "off";
                        imgCurtainStatus.setClickable(true);
                    }

                    acStatusList.put(tag[1], acStatus);
                }
            });


            linearACConfig.addView(view);
        }


    }

    int[] countTV;
    String[] tvCondition;
    String tvStatus = "";

    private void DisplayTVView(View view1) {
        HorizontalScrollView scrollViewTVConfig = (HorizontalScrollView) view1.findViewById(R.id.scrollViewTVConfig);
        scrollViewTVConfig.scrollTo(0, 0);

        LinearLayout linearTVConfig = (LinearLayout) view1.findViewById(R.id.linearTVConfig);
        linearTVConfig.removeAllViewsInLayout();
        linearTVConfig.removeAllViews();

        int sizeOfTV = listTV.size();
        //imgLightStatus =new ImageView[sizeOfLight];
        countTV = new int[sizeOfTV];
        tvCondition = new String[sizeOfTV];

        for (int i = 0; i < sizeOfTV; i++) {
            View view = getLayoutInflater().inflate(R.layout.tv_config_view, null);

            countTV[i] = 0;
            final TextView txtTV = (TextView) view.findViewById(R.id.txtTV);
            final CheckBox cbTVDCare = (CheckBox) view.findViewById(R.id.cbTVDCare);
            cbTVDCare.setChecked(false);
            //txtTV.setText(hashMoodAppliance.get(listTV.get(i)));
            //txtTV.setText(listTV.get(i));
            if (!App_Variable.hashNickName.get(_whichSection + "&&" + listTV.get(i)).equals(""))
                txtTV.setText(App_Variable.hashNickName.get(_whichSection + "&&" + listTV.get(i)));
            else
                txtTV.setText(listTV.get(i));

            /**
             * set light status on or off
             */
            tvStatus = "";
            final ImageView imgCurtainStatus = (ImageView) view.findViewById(R.id.imgTVStatus);

            //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listTV.get(i));
            String roomID = "";
            CustomLog.latest("roomID:::" + roomID);
            tvStatus = "off"; //tvStatus =hashKey.get(listTV.get(i));
            if (roomID != null) {
                if (hashKey != null) {
                    tvStatus = hashKey.get(roomID);
                    CustomLog.latest("value:::" + tvStatus);
                }
            }

            if (isExtraData) {
                tvStatus = "off";
            } else {
                if (!isData)
                    tvStatus = "off";
            }

            if (tvStatus != null) {
                if (tvStatus.equals("off")) {
                    imgCurtainStatus.setImageResource(R.drawable.on_icon);
                    cbTVDCare.setChecked(false);
                } else if ("null".equals(tvStatus)) {
                    imgCurtainStatus.setImageResource(R.drawable.on_icon);
                    cbTVDCare.setChecked(true);
                } else {
                    imgCurtainStatus.setImageResource(R.drawable.off_icon);
                    cbTVDCare.setChecked(false);
                }
            }
            tvCondition[i] = tvStatus;


            /**
             * event for curtain on or off
             */
            imgCurtainStatus.setTag(i);
            imgCurtainStatus.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int index = Integer.parseInt("" + v.getTag());
                    int pos = countTV[index];
                    CustomLog.debug("inc===" + pos);
                    if (tvStatus.equals("off")) {
                        tvCondition[index] = "on";
                        imgCurtainStatus.setImageResource(R.drawable.off_icon);
                    } else {
                        tvCondition[index] = "off";
                        imgCurtainStatus.setImageResource(R.drawable.on_icon);
                    }

                    AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "TV Mood", tvCondition[index] + " clicked",
                            _whichSection + "/" + _whichMood + "/" + txtTV.getText());

					/*if(tvStatus.equals("off")){
						if(pos%2==0){
							tvCondition[index]="on";
							imgCurtainStatus.setImageResource(R.drawable.off_icon);
						}else{
							tvCondition[index]="off";
							imgCurtainStatus.setImageResource(R.drawable.on_icon);
						}
					}else{
						if(pos%2==0){
							tvCondition[index]="off";
							imgCurtainStatus.setImageResource(R.drawable.on_icon);
						}else{
							tvCondition[index]="on";
							imgCurtainStatus.setImageResource(R.drawable.off_icon);
						}
					}*/
                    countTV[index]++;
                }

            });

            final int finalI = i;
            cbTVDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked)
                        tvCondition[finalI] = "null";
                    else
                        tvCondition[finalI] = tvStatus;
                }
            });

            linearTVConfig.addView(view);
        }

    }

    int[] countFan;
    String fanStatus = "";
    String[] fanCondition;

    private void DisplayFanView(View view1) {

        HorizontalScrollView scrollViewFanConfig = (HorizontalScrollView) view1.findViewById(R.id.scrollViewFanConfig);
        scrollViewFanConfig.scrollTo(0, 0);

        LinearLayout linearFanConfig = (LinearLayout) view1.findViewById(R.id.linearFanConfig);
        linearFanConfig.removeAllViewsInLayout();
        linearFanConfig.removeAllViews();

        int sizeOfFan = listFan.size();
        //imgLightStatus =new ImageView[sizeOfLight];
        countFan = new int[sizeOfFan];
        fanLevel = new TextView[sizeOfFan];
        fanCondition = new String[sizeOfFan];

        for (int i = 0; i < sizeOfFan; i++) {
            String status = hashKey.get(listFan.get(i));
            if (!status.startsWith("level")) {

                View view = getLayoutInflater().inflate(R.layout.tv_config_view, null);

                countFan[i] = 0;
                final TextView txtFan = (TextView) view.findViewById(R.id.txtTV);
                //txtFan.setText(hashMoodAppliance.get(listFan.get(i)));
                txtFan.setText(listFan.get(i));
                /**
                 * set light status on or off
                 */
                fanStatus = "";
                final ImageView imgFanStatus = (ImageView) view.findViewById(R.id.imgTVStatus);


                final CheckBox cbTVDCare = (CheckBox) view.findViewById(R.id.cbTVDCare);
                cbTVDCare.setChecked(false);

                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listFan.get(i));
                String roomID = "";
                CustomLog.latest("roomID:::" + roomID);
                fanStatus = "off"; //fanStatus =hashKey.get(listFan.get(i));
                if (roomID != null) {
                    if (hashKey != null) {
                        fanStatus = hashKey.get(roomID);
                        CustomLog.latest("value:::" + fanStatus);
                    }
                }

                if (isExtraData) {
                    fanStatus = "off";
                } else {
                    if (!isData)
                        fanStatus = "off";
                }

                if (fanStatus != null) {
                    if (fanStatus.equals("off")) {
                        imgFanStatus.setImageResource(R.drawable.on_icon);
                        cbTVDCare.setChecked(false);
                    } else if ("null".equals(fanStatus)) {
                        imgFanStatus.setImageResource(R.drawable.on_icon);
                        cbTVDCare.setChecked(true);
                    } else {
                        imgFanStatus.setImageResource(R.drawable.off_icon);
                        cbTVDCare.setChecked(false);
                    }
                }
                fanCondition[i] = fanStatus;

                /**
                 * event for curtain on or off
                 */
                imgFanStatus.setTag(i);
                imgFanStatus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //String[] tag=(""+v.getTag()).split("&&");
                        int index = Integer.parseInt("" + v.getTag());
                        int pos = countFan[index];
                        CustomLog.debug("inc===" + pos);
                        if (fanStatus.equals("off")) {
                            fanCondition[index] = "on";
                            imgFanStatus.setImageResource(R.drawable.off_icon);
                        } else {
                            fanCondition[index] = "off";
                            imgFanStatus.setImageResource(R.drawable.on_icon);
                        }

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Fan Mood", fanCondition[index] + " clicked",
                                _whichSection + "/" + _whichMood + "/" + txtFan.getText());

						/*if(fanStatus.equals("off")){
							if(pos%2==0){
								fanCondition[index]="on";
								imgFanStatus.setImageResource(R.drawable.off_icon);
							}else{
								fanCondition[index]="off";
								imgFanStatus.setImageResource(R.drawable.on_icon);
							}
						}else{
							if(pos%2==0){
								fanCondition[index]="off";
								imgFanStatus.setImageResource(R.drawable.on_icon);
							}else{
								fanCondition[index]="on";
								imgFanStatus.setImageResource(R.drawable.off_icon);
							}
						}*/
                        countFan[index]++;
                    }
                });

                final int finalI = i;
                cbTVDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            fanCondition[finalI] = "null";
                        else
                            fanCondition[finalI] = fanStatus;
                    }
                });

                linearFanConfig.addView(view);
            } else {
                fanCondition[i] = "level0";
                View view = getLayoutInflater().inflate(R.layout.dimmer_config_view, null);

                final TextView txtFan = (TextView) view.findViewById(R.id.txtDimmer);

                final CheckBox cbdimmerDCare = (CheckBox) view.findViewById(R.id.cbdimmerDCare);
                cbdimmerDCare.setChecked(false);

                //txtFan.setText(hashMoodAppliance.get(listFan.get(i)));

                fanLevel[i] = (TextView) view.findViewById(R.id.dimmerLevel);

                int levelValue = 0;
                //String roomID= hashMoodApplRoom.get(_whichSection+"&&"+listFan.get(i));
                String roomID = "";
                CustomLog.latest("roomID:::" + roomID);
                String value = "level0";//String value =hashKey.get(listFan.get(i));
                if (roomID != null) {
                    if (hashKey != null) {
                        value = hashKey.get(roomID);
                        CustomLog.latest("value:::" + value);
                    }
                }

                if (isExtraData) {
                    fanLevel[i].setText("0");
                } else {
                    if (isData) {
                        if (value != null) {
                            if (value.contains("level")) {
                                levelValue = Integer.parseInt(value.replace("level", ""));
                                fanLevel[i].setText(levelValue + "");
                                fanCondition[i] = "level" + levelValue;
                                cbdimmerDCare.setChecked(false);
                            } else {
                                if ("null".equals(value)) {
                                    cbdimmerDCare.setChecked(true);
                                    fanCondition[i] = "level" + -1;
                                }
                                fanLevel[i].setText("0");
                            }
                        }
                    } else {
                        cbdimmerDCare.setChecked(false);
                        fanLevel[i].setText("0");
                    }
                }

                /**
                 * event for dimmer decrement(-)
                 */
                TextView fanMinus = (TextView) view.findViewById(R.id.dimmerMinus);
                fanMinus.setTag(i);
                fanMinus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //String[] split = (""+v.getTag()).split("&&");
                        int index = Integer.parseInt("" + v.getTag());
                        int level = Integer.parseInt(fanLevel[index].getText() + "");
                        CustomLog.debug("- " + level);
                        if (level == 0)
                            level = 0;
                        else {
                            level--;
                        }
                        CustomLog.debug("-- " + level);
                        fanLevel[index].setText(level + "");
                        fanCondition[index] = "level" + level;

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Fan Mood", "minus clicked",
                                _whichSection + "/" + _whichMood + "/" + txtFan.getText(), level);
                    }
                });

                /**
                 * event for dimmer increment(+)
                 */
                TextView fanPlus = (TextView) view.findViewById(R.id.dimmerPlus);
                fanPlus.setTag(i);
                fanPlus.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        //String[] split = (""+v.getTag()).split("&&");
                        int index = Integer.parseInt("" + v.getTag());
                        int level = Integer.parseInt(fanLevel[index].getText() + "");
                        CustomLog.debug("+ " + level);
                        if (level == 13)
                            level = 13;
                        else {
                            level++;
                        }
                        CustomLog.debug("++ " + level);

                        fanLevel[index].setText(level + "");
                        fanCondition[index] = "level" + level;

                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Fan Mood", "plus clicked",
                                _whichSection + "/" + _whichMood + "/" + txtFan.getText(), level);

                    }
                });

                final int finalI1 = i;
                final int finalLevelValue = levelValue;
                cbdimmerDCare.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked)
                            fanCondition[finalI1] = "level" + -1;

                        else
                            fanCondition[finalI1] = "level" + finalLevelValue;
                    }
                });

                linearFanConfig.addView(view);
            }
        }


    }

    private void callMoodStatus() {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            if (gms != null)
                gms.cancel(true);
            gms = new GetMoodStatus();
            gms.execute();
        } else {
            App_Variable.ShowNoNetwork(this);
        }
    }

    class GetMoodStatus extends AsyncTask<Void, Void, Void> {
        //satyam
        private String response = "";
        private String exception = "";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            progress.setMessage("Please wait while mood status loading..");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            BaseParser parser = new BaseParser(GetAPI.BASE_URL + GetAPI.MOOD_STATUS);
            response = parser.getResponse();
            exception = parser.getException().trim();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //Log.d(tag, "apply status:"+response);

            if (!exception.equals("")) {
                App_Variable.ShowErrorToast(exception, MoodActivity.this);
            } else {

                if (response != null) {
                    try {
                        String[] status = response.split("\n");
                        int length = status.length;

                        layout_linear_status.removeAllViewsInLayout();
                        layout_linear_status.removeAllViews();

                        for (int i = 0; i < length; i++) {
                            if (!status[i].startsWith("Content") && !status[i].trim().equals("")) {

                                View vi = getLayoutInflater().inflate(R.layout.mood_status_view, null);

                                TextView txtStatusDay = (TextView) vi.findViewById(R.id.txtStatusDay);
                                TextView txtStatusTime = (TextView) vi.findViewById(R.id.txtStatusTime);
                                TextView txtStatusRoom = (TextView) vi.findViewById(R.id.txtStatusRoom);
                                TextView txtStatusMood = (TextView) vi.findViewById(R.id.txtStatusMood);

                                String[] spliStatus = status[i].split(" ");
                                int splitLength = spliStatus.length;
                                String[] spitMin = null;
                                String day = null;
                                for (int j = 0; j < splitLength; j++) {
                                    if (j == 0) {
                                        spitMin = spliStatus[j].split(":");
                                        //CustomLog.debug("MM:"+spitMin[1]);

                                    } else if (j == 1) {
                                        //CustomLog.debug("HH:"+spliStatus[j]);
                                        txtStatusTime.setText(spliStatus[j] + ":" + spitMin[1]);

                                    } else if (j == 2) {
                                        day = spliStatus[j];
                                        //CustomLog.debug("DD:"+spliStatus[j]);
                                    } else if (j == 3) {
                                        if (day.startsWith("*"))
                                            txtStatusDay.setText("DAILY");
                                        else
                                            txtStatusDay.setText(day + "/" + spliStatus[j]);
                                        //CustomLog.debug("MM:"+spliStatus[j]);
                                    } else if (j == splitLength - 1) {
                                        //CustomLog.debug("Mood:"+spliStatus[j]);
                                        MoodEntity entity = SetMoodData(spliStatus[j]);
                                        if (entity != null) {
                                            txtStatusRoom.setText(entity.getArea());
                                            txtStatusMood.setText(entity.getMoodName());
                                        }
                                    }
                                }
                                final ImageView statusDelete = (ImageView) vi.findViewById(R.id.statusDelete);
                                statusDelete.setTag(spitMin[0]);
                                statusDelete.setOnClickListener(new OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        // TODO Auto-generated method stub
                                        CustomLog.debug("delete id:" + statusDelete.getTag());
                                        String urlString = GetAPI.BASE_URL + GetAPI.MOOD_DELETE + statusDelete.getTag();

                                        AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Mood Status", "deleted", statusDelete.getTag() + "");
                                        callMoodStatusDelete(urlString);

                                    }
                                });
                                layout_linear_status.addView(vi);
                            }
                        }
                        layout_mood_status.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        System.out.println("Mood Status:" + e);
                    }
                }
            }
            progress.dismiss();
            super.onPostExecute(result);
        }
    }

    private void callMoodStatusDelete(String urlString) {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            if (gsd != null)
                gsd.cancel(true);
            gsd = new GetMoodStausDelete();
            gsd.execute(urlString);
        } else {
            App_Variable.ShowNoNetwork(this);
        }
    }

    class GetMoodStausDelete extends AsyncTask<String, Void, Void> {

        private String response = "";
        private String exception = "";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            progress.setMessage("Please wait while scheduling is being removed..");
            progress.setCanceledOnTouchOutside(false);
            progress.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            BaseParser parser = new BaseParser(params[0]);
            response = parser.getResponse();
            exception = parser.getException().trim();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            CustomLog.d(tag, "apply schdule:" + response);
            if (!exception.equals("")) {
                App_Variable.ShowErrorToast(exception, MoodActivity.this);
                progress.dismiss();
            } else {
                progress.dismiss();
                callMoodStatus();
            }

            super.onPostExecute(result);
        }
    }


    private void callScheduleApply(String urlString) {
        if (App_Variable.isNetworkAvailable(MoodActivity.this)) {
            if (gs != null)
                gs.cancel(true);
            gs = new GetMoodScheduleApply();
            gs.execute(urlString);
        } else {
            App_Variable.ShowNoNetwork(this);
        }
    }

    class GetMoodScheduleApply extends AsyncTask<String, Void, Void> {

        private String response = "";
        private String exception = "";

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub


            progress.setMessage("Please wait while applying schedule..");
            progress.setCanceledOnTouchOutside(false);
            progress.show();


            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(String... params) {
            // TODO Auto-generated method stub
            Calendar c = Calendar.getInstance();
            //if(mHour>=(c.get(Calendar.HOUR_OF_DAY))&&(Integer.getInteger(txtApplyDateMood.getText().toString())==c.get(Calendar.DATE))) {

            BaseParser parser = new BaseParser(params[0]);
            response = parser.getResponse();
            exception = parser.getException();
            return null;
            //}else {
            //Toast.makeText(MoodActivity.this,"cont set past time",Toast.LENGTH_LONG).show();
            //}
            //return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            CustomLog.d(tag, "apply schdule:" + response);
            if (!exception.equals("")) {
                App_Variable.ShowErrorToast(exception, MoodActivity.this);
                progress.dismiss();
            } else {
                progress.setMessage("Schedule successfully  updated");
                progress.dismiss();
            }

            super.onPostExecute(result);
        }
    }


    private void ShowScheduleView() {

        txtMoodAreaValue.setText(_whichSection);

    }


    /**
     * Popup contains data like Please select room
     */
    private void callPopup() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Please select room")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sm.toggle();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        AnalyticsTracker.trackEvents(this, screenName, "Select Room Dialog", "shown", _whichSection);
    }

    private void scheduleDisabledAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage("Global not supported for mood scheduling")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        AnalyticsTracker.trackEvents(this, screenName, "Global not supported for mood scheduling", "shown", _whichSection);
    }

    protected Dialog onCreateDialog(int id) {
        if (id == 0) {
            DatePickerDialog dialog = new DatePickerDialog(this, datePickerListener, year, month, day);

            Date min = new Date(year - 1900, month, day);
            dialog.getDatePicker().setMinDate(min.getTime());
            return dialog;
        } else {
            TimePickerDialog tdialog = new TimePickerDialog(this, mTimeSetListener, mHour, mMinute, true);
            return tdialog;
        }
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {

			/*txtApplyDate.setText(selectedDay + " - " + App_Variable.getfMonth((selectedMonth + 1)) + " - "
					+ selectedYear);*/
            mDaySelected = selectedDay;
            mMonthSelected = selectedMonth + 1;
            mYearSelected = selectedYear;
            Log.i("ADebugTag", "Value: " + mYearSelected);
            txtApplyDateMood.setText(App_Variable.getAppendZero("" + selectedDay) + "/" + App_Variable.getAppendZero("" + (selectedMonth + 1)) + "/"
                    + selectedYear);
            selectedDate = selectedYear + "-" + App_Variable.getAppendZero("" + (selectedMonth + 1))
                    + "-" + App_Variable.getAppendZero("" + selectedDay);
            AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Date", "clicked", selectedDate);
        }
    };


    private TimePickerDialog.OnTimeSetListener mTimeSetListener =
            new TimePickerDialog.OnTimeSetListener() {

                // the callback received when the user "sets" the TimePickerDialog in the dialog
                public void onTimeSet(TimePicker view, int hourOfDay, int min) {
                    //Calendar c = Calendar.getInstance();
                    mHourSelected = hourOfDay;
                    mMinSelected = min;
                    //msec=sec;
                    Calendar c = Calendar.getInstance();
                    Log.i("ADebugTag", "mHour: " + mHourSelected);
                    Log.i("ADebugTag", "hour: " + c.get(Calendar.HOUR_OF_DAY));
                    Log.i("ADebugTag", "day: " + c.get(Calendar.DAY_OF_MONTH));
                    Log.i("ADebugTag", "month: " + c.get(Calendar.MONTH));
                    Log.i("ADebugTag", "mday: " + mDaySelected);
                    Log.i("ADebugTag", "mmonth: " + mMonthSelected);
                    Log.i("ADebugTag", "myear: " + mYearSelected);
                    Log.i("ADebugTag", "year: " + c.get(Calendar.YEAR));
                    if ((mHourSelected <= c.get(Calendar.HOUR_OF_DAY)) && (mDaySelected == c.get(Calendar.DAY_OF_MONTH)) && (mMonthSelected == (c.get(Calendar.MONTH) + 1))) {
                        //Toast.makeText(MoodActivity.this,"cant set",Toast.LENGTH_LONG).show();
                        if ((mMinSelected <= c.get(Calendar.MINUTE))) {
                            AlertDialog ad = new AlertDialog.Builder(MoodActivity.this)
                                    .setTitle("Time picker")
                                    .setMessage("you can't set past time")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //img_tab_schedule_bottom.setVisibility(View.VISIBLE);
                                            dialog.dismiss();
                                            showDialog(1);


                                        }
                                    })
                                    .create();
                            ad.setIcon(android.R.drawable.ic_dialog_alert);
                            ad.show();
                        } else {
                            txtApplyTimeMood.setText(App_Variable.getAppendZero("" + hourOfDay) + ":" + App_Variable.getAppendZero("" + min));
                        }
                    } else {
                        txtApplyTimeMood.setText(App_Variable.getAppendZero("" + hourOfDay) + ":" + App_Variable.getAppendZero("" + min));

                        //onTimeSet(view,hourOfDay,min);
                        //mTimeSetListener.TimePickerDialog(this, mTimeSetListener, mHour, mMinute,true);
                    }
                    AnalyticsTracker.trackEvents(MoodActivity.this, screenName, "Time", "clicked", txtApplyTimeMood.getText() + "");

                }
            };

    @Override
    public void callExceptionPopup(String exception) {
        // TODO Auto-generated method stub
        String errorMsg = exception.trim().toLowerCase();

        if (errorMsg.contains("host")) {
            errorMsg = App_Variable.MSG_CONNECT_EXCEPTION;
        } else if (errorMsg.contains("connect")) {
            errorMsg = App_Variable.MSG_CONNECT_EXCEPTION;
        } else {
            errorMsg = App_Variable.MSG_CONNECT_EXCEPTION;
        }

        App_Variable.ShowErrorToast(errorMsg, MoodActivity.this);


    }


    @Override
    public void StartBGService(Context context, String whichScreen) {
        callMoodConfigData(false);

    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        moodvalue = false;
        finish();
        return super.onKeyDown(keyCode, event);
    }


}

/**
 * If hash is empty, compare other rooms data in same mood
 *
 * @isExtraData become true, if any room has no data in all moods.
 * Then compare other room with same room type
 * @isData become true, if any room has no data in some paricular mood.
 * Then compare other moods in same room
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 * <p>
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 *//*
private void GroupingEmptyData(String moodID, LinkedHashMap<String, LinkedHashMap<String, String>> hashMood) {
	try{
		isData=false;

		countOfData++;
		String partialMoodId=moodID.substring(0, moodID.length()-1);

		for (int i = countOfData; i < MOOD_SIZE+1; i++) {
			String newID=partialMoodId+""+i;
			CustomLog.debug("New Mood ID:"+newID);
			if(!moodID.equals(newID)){
				hashKey=hashMood.get(newID);
				if(hashKey!=null){
					GroupingListData(moodID,hashMood);
					break;
				}
			}
		}
		CustomLog.debug("listDimmers size:"+listDimmers.size());
		if(listDimmers.size()==0){
			countOfData =0;
			int roomSize=list_rooms.size();
			for (int i = 0; i < roomSize; i++) {
				System.out.println("rooms name:"+list_rooms.get(i));
				if(!_whichSection.equalsIgnoreCase(list_rooms.get(i))){
					if(_whichSection.split("-")[0].equalsIgnoreCase(list_rooms.get(i).split("-")[0])){
						CustomLog.debug("empty rooms:"+list_rooms.get(i));
						isExtraData=true;
						GetandGroupData(list_rooms.get(i));
						break;
					}

				}

			}
		}
	}catch (Exception e) {
		CustomLog.e(tag, "GroupingEmptyData:"+e);
	}


}*/

/*	*//**
 * Groping data like dimmer, light and etc
 * @param hashMood
 * @param moodID
 *//*
private void GroupingListData(String moodID, LinkedHashMap<String, LinkedHashMap<String, String>> hashMood) {
	listDimmers=new ArrayList<MoodEntity>();
	listCurtains=new ArrayList<String>();
	listLights=new ArrayList<String>();
	listFan=new ArrayList<String>();
	listAC=new ArrayList<String>();
	listTV=new ArrayList<String>();

	boolean hasData=false;
	CustomLog.debug("mood GroupingListData");

	Set entrySetKey = hashKey.entrySet();
	Iterator iteratorKey = entrySetKey.iterator();
	while (iteratorKey.hasNext()) {
		hasData =true;
		Entry entryKey = (Entry) iteratorKey.next();
		String category =hashMoodAppliance.get(entryKey.getKey());

		CustomLog.check("key scdule:"+entryKey.getKey()+":"+hashMoodAppliance.get(entryKey.getKey())+":"+entryKey.getValue());
		if(category!=null){
			category=category.toLowerCase();

			if(category.startsWith("light")){
				listLights.add(entryKey.getKey()+"");
			}else if(category.startsWith("dimmer")){
				listDimmers.add(entryKey.getKey()+"");
			}else if(category.startsWith("curtain")){
				listCurtains.add(entryKey.getKey()+"");
			}else if(category.startsWith("fan")){
				listFan.add(entryKey.getKey()+"");
			}else if(category.startsWith("ac")){
				listAC.add(entryKey.getKey()+"");
			}else if(category.startsWith("tv")){
				listTV.add(entryKey.getKey()+"");
			}
		}

	}

	if(!hasData){
		GroupingEmptyData(moodID,hashMood);
	}
}*/


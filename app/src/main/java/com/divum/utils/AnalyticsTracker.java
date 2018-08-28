package com.divum.utils;

import android.content.Context;
import android.os.Bundle;


import com.divum.silvan.R;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;



import java.net.URLEncoder;


/**
 * Created by Divum on 14-May-15.
 */
public class AnalyticsTracker {
    static Tracker mTracker = null;


    synchronized public static Tracker getDefaultTracker(Context mContext) {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.analytics);

            mTracker.enableAdvertisingIdCollection(true);

        }
        return mTracker;
    }



    public static void trackEvents(Context context, String screenName, String category, String action, String label, boolean non_interaction) {

      /*  Tracker mTracker = getDefaultTracker(context);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setNonInteraction(non_interaction)
                //  .set("&cd", screenName)
                .build());*/
    }


    public static void trackEvents(Context context, String screenName, String category, String action, String label) {

        /*Tracker mTracker = getDefaultTracker(context);
        // mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                //  .set("&cd", screenName)
                .build());*/
    }

    public static void trackEvents(Context context, String screenName, String category, String action, String label, int value) {

       /* Tracker mTracker = getDefaultTracker(context);
        // mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                // .set("&cd", screenName)
                .build());*/
    }

    public static void trackEvents(Context context, String screenName, String category, String action, String label, int value, boolean non_interaction) {

      /*  Tracker mTracker = getDefaultTracker(context);
        // mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .setNonInteraction(non_interaction)
                // .set("&cd", screenName)
                .build());*/

        //trackMixPanelEvents(context, screenName, category, action, label, value + "");

        //trackFbEvents(context, screenName, category, action, label, value + "");
    }


    public static void trackScreen(Context context, String screenName) {
        /*Tracker mTracker = getDefaultTracker(context);
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

    }

    public static void trackUser(Context context, String userId) {
       /* Tracker mTracker = getDefaultTracker(context);

        mTracker.set("&uid", userId);

        // This hit will be sent with the User ID value and be visible in User-ID-enabled views (profiles).
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());*/

    }



    public static void trackDimension(Context mContext, String screenName, int dimension, String value, String key) {
        /*Tracker mTracker = getDefaultTracker(mContext);
        // mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCustomDimension(dimension, value)
                .build()
        );*/
    }


    public static void trackCampaign(Context mContext, String screenName, String mail, String buyerId) {
        Tracker mTracker = getDefaultTracker(mContext);
        mTracker.setScreenName(screenName);

        String campaignData = "https://play.google.com/store/apps/details?id=" +
                "com.divumlabs.terraa" +
                "&referrer=utm_source%3Dgoogle%26utm_medium%3Dcpc%26" + "utm_content%3D" + buyerId +
                "utm_campaign%3D" + URLEncoder.encode(mail) + "%26anid%3Dadmob";

// Campaign data sent with this hit.
        mTracker.send(new HitBuilders.ScreenViewBuilder()
                .setCampaignParamsFromUrl(campaignData)
                .build()
        );
    }//https://play.google.com/store/apps/details?id=com.divum.baby&referrer=utm_source%3Dgoogle%26utm_medium%3Dcpc%26utm_content%3D123436%26utm_campaign%3Dranjith%2540divum.in%26anid%3Dadmob




}
package com.divum.utils;

import com.divum.parser.BaseParser;
import com.divum.silvan.HomeActivity;
import com.divum.silvan.R;
import com.divum.silvan.R.string;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

public class CommonPingURL extends AsyncTask<String, Void, Void> {

    ProgressDialog dialog;
    private String loadingSection = "";
    private String hooterMsg = "Please wait while hooter is being applied..";
    private String panicMsg = "Please wait while panic is being applied..";
    private String moodMsg = "Please wait while the selected mood is being applied..";
    private String globalmoodMsg = "Please wait while the global mood is being applied..";

    private Context context;
    private String exception = "";
    private Dialog globalDialog;

    public CommonPingURL(String string, Context _context) {
        loadingSection = string;
        context = _context;
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        dialog =new ProgressDialog(context);
        if (!loadingSection.equals("others")) {
            if (loadingSection.equals("hooter"))
                dialog.setMessage(hooterMsg);
            else if (loadingSection.equals("panic"))
                dialog.setMessage(panicMsg);
            else if (loadingSection.equals("moodRoom"))
                dialog.setMessage(moodMsg);


            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
        }
        if (loadingSection.equals("others")) {
            if(App_Variable.globalClicked){
                dialog.setMessage(globalmoodMsg);
                App_Variable.globalClicked=false;
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        }
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... params) {
        BaseParser parser = new BaseParser(params[0]);
        parser.getResponse();
        exception = parser.getException().trim();
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        // TODO Auto-generated method stub
        CustomLog.e("URL:", "http://192.168.1.231/cgi-bin/saya_webAPI.sh?/www/cgi-bin/scripts/ completed:" + exception);

        /*if (!loadingSection.equals("others")) {
            if (null != dialog)
                dialog.dismiss();
        }
        if (loadingSection.equals("others")) {
            if (null != dialog)
                dialog.dismiss();
        }*/
        if (null != dialog)
            dialog.dismiss();
        if (!exception.equals(""))
            App_Variable.ShowErrorToast(exception, context);
        super.onPostExecute(result);
    }

    private void callGlobalPopup() {
        globalDialog = new Dialog(context);
        globalDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        globalDialog.getWindow().setLayout(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        globalDialog.setCancelable(true);
        globalDialog.setContentView(R.layout.global_switch_progress);
        //dialog.setTitle(getResources().getString(R.string.appntmtnt_accepted));
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(globalDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;

        globalDialog.getWindow().setAttributes(lp);


        globalDialog.show();
    }


}

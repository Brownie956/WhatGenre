/*Author: Chris Brown
* Date: 27/03/2016
* Description: Utils class for generic functions*/
package com.cfbrownweb.whatgenre;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public abstract class Utils {

    public static boolean isConnected(Context context){
        //Get network information
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void netErrorToast(Context context){
        Toast netErrorToast = Toast.makeText(context, R.string.net_fail, Toast.LENGTH_LONG);
        TextView toastText = (TextView) netErrorToast.getView().findViewById(android.R.id.message);
        if(toastText != null) {
            toastText.setGravity(Gravity.CENTER);
        }
        netErrorToast.show();
    }

    public static void serverErrorToast(Context context){
        Toast serverErrorToast = Toast.makeText(context, R.string.server_error, Toast.LENGTH_LONG);
        TextView toastText = (TextView) serverErrorToast.getView().findViewById(android.R.id.message);
        if(toastText != null) {
            toastText.setGravity(Gravity.CENTER);
        }
        serverErrorToast.show();
    }
}

package org.applab.ledgerlink.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by JCapito on 8/6/2016.
 */
public class InboundChatReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Intent background = new Intent(context, InboundChatService.class);
//        context.startService(background);
    }
}

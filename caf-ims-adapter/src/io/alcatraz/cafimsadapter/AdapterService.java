/*
 * Copyright (c) 2025, Alcatraz323 <alcatraz32323@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 */

package io.alcatraz.cafimsadapter;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;
import android.os.IBinder;
import android.os.UserHandle;
import android.util.Log;

import androidx.annotation.Nullable;

public class AdapterService extends Service {
    private static final String TAG = "CAFIMSAdapter";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(TelephonyManager.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED);
        registerReceiver(new DataSubChangedReceiver(), intentFilter);
        return super.onStartCommand(intent, flags, startId);
    }

    public static void onBoot(Context context) {
        context.startServiceAsUser(new Intent(context, AdapterService.class), UserHandle.SYSTEM);
    }
}

/*
 * SPDX-FileCopyrightText: 2025 Alcatraz323 <alcatraz32323@gmail.com>
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package io.alcatraz.cafimsadapter

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.os.UserHandle
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log

class AdapterService : Service() {
    private val dataSubChangedReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action != TelephonyManager.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED) {
                    return
                }

                val ddsSubId = SubscriptionManager.getDefaultDataSubscriptionId()
                val ddsPhoneId = SubscriptionManager.getPhoneId(ddsSubId)

                if (ddsSubId == defaultDataSubId && ddsPhoneId == defaultDataPhoneId) {
                    Log.d(TAG, "Default data subscription did not change")
                    return
                }

                Log.i(
                    TAG,
                    "Default data subscription changed from $defaultDataSubId/" +
                        "$defaultDataPhoneId to $ddsSubId/$ddsPhoneId",
                )
                defaultDataSubId = ddsSubId
                defaultDataPhoneId = ddsPhoneId
                pendingDdsSubId = ddsSubId
                if (!SubscriptionManager.isValidSubscriptionId(ddsSubId)) {
                    Log.w(TAG, "Ignoring invalid default data subscription $ddsSubId")
                    return
                }

                notifyDdsSwitchDoneIfReady()
            }
        }

    private val activeDataSubscriptionListener =
        object : TelephonyCallback(), TelephonyCallback.ActiveDataSubscriptionIdListener {
            override fun onActiveDataSubscriptionIdChanged(subId: Int) {
                Log.d(TAG, "Active data subscription changed to $subId")
                activeDataSubId = subId
                notifyDdsSwitchDoneIfReady()
            }
        }

    private lateinit var telephonyManager: TelephonyManager
    private var activeDataSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID
    private var defaultDataPhoneId = SubscriptionManager.INVALID_PHONE_INDEX
    private var defaultDataSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID
    private var pendingDdsSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        telephonyManager = getSystemService(TelephonyManager::class.java)
        activeDataSubId = SubscriptionManager.getActiveDataSubscriptionId()
        defaultDataSubId = SubscriptionManager.getDefaultDataSubscriptionId()
        defaultDataPhoneId = SubscriptionManager.getPhoneId(defaultDataSubId)

        val intentFilter =
            IntentFilter(TelephonyManager.ACTION_DEFAULT_DATA_SUBSCRIPTION_CHANGED)
        registerReceiver(dataSubChangedReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        telephonyManager.registerTelephonyCallback(
            mainExecutor,
            activeDataSubscriptionListener,
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onDestroy() {
        telephonyManager.unregisterTelephonyCallback(activeDataSubscriptionListener)
        unregisterReceiver(dataSubChangedReceiver)
        super.onDestroy()
    }

    private fun notifyDdsSwitchDoneIfReady() {
        // PhoneSwitcher publishes the active data sub only after its RIL command succeeds.
        if (
            !SubscriptionManager.isValidSubscriptionId(pendingDdsSubId) ||
                pendingDdsSubId != activeDataSubId ||
                pendingDdsSubId != SubscriptionManager.getDefaultDataSubscriptionId()
        ) {
            return
        }

        val ddsSubId = pendingDdsSubId
        pendingDdsSubId = SubscriptionManager.INVALID_SUBSCRIPTION_ID

        val intent =
            Intent(ACTION_DDS_SWITCH_DONE).apply {
                addFlags(Intent.FLAG_RECEIVER_INCLUDE_BACKGROUND)
                SubscriptionManager.putSubscriptionIdExtra(this, ddsSubId)
            }
        Log.i(TAG, "Data switch completed for subscription $ddsSubId")
        sendBroadcast(intent)
    }

    companion object {
        private const val TAG = "CAFIMSAdapter"
        private const val ACTION_DDS_SWITCH_DONE =
            "org.codeaurora.intent.action.ACTION_DDS_SWITCH_DONE"

        fun onBoot(context: Context) {
            context.startServiceAsUser(
                Intent(context, AdapterService::class.java),
                UserHandle.SYSTEM,
            )
        }
    }
}

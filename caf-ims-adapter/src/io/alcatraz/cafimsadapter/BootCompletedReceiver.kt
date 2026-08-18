/*
 * SPDX-FileCopyrightText: 2025 Alcatraz323 <alcatraz32323@gmail.com>
 * SPDX-FileCopyrightText: 2026 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package io.alcatraz.cafimsadapter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        Log.d(TAG, "Starting")
        AdapterService.onBoot(context)
    }

    companion object {
        private const val TAG = "CAFIMSAdapter"
    }
}

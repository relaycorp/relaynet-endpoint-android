package tech.relaycorp.awaladroid.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.common.Logging.logger

internal class NotificationBroadcastReceiver : BroadcastReceiver() {

    internal var coroutineContext: CoroutineContext = Dispatchers.IO

    override fun onReceive(context: Context?, intent: Intent?) {
        logger.info("Received notification")
        CoroutineScope(coroutineContext).launch {
            GatewayClient.checkForNewMessages()
        }
    }
}

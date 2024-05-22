package Login.ui

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import java.util.*

class TimerService : Service() {

    private val timer = Timer()
    private var time = 0.0

    // Return null because this service is not designed for binding
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Starts the timer when the service is started
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            time = intent.getDoubleExtra(TIME_EXTRA, 0.0)
        }
        timer.scheduleAtFixedRate(TimeTask(), 0, 1000)
        return START_NOT_STICKY
    }

    // Cancels the timer when the service is destroyed
    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    // TimerTask to increment the time and broadcast the updated time every second
    private inner class TimeTask : TimerTask() {
        override fun run() {
            time++
            val intent = Intent(TimerService.TIMER_UPDATED)
            intent.putExtra(TimerService.TIME_EXTRA, time)
            LocalBroadcastManager.getInstance(this@TimerService).sendBroadcast(intent)
        }
    }

    companion object {
        const val TIMER_UPDATED = "code.with.cal.timeronservicetutorial.TIMER_UPDATED"
        const val TIME_EXTRA = "code.with.cal.timeronservicetutorial.TIME_EXTRA"
    }
}

package com.braze.advancedsamples

import android.app.Application
import com.braze.BrazeActivityLifecycleCallbackListener
import com.facebook.drawee.backends.pipeline.Fresco

class BrazeDemoApplication : Application(){

    override fun onCreate() {
        super.onCreate()
        Fresco.initialize(this)
        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
        val manager = BrazeManager.getInstance(this)
        manager.configureCustomInAppMessageViewFactory()
        manager.registerForContentCardUpdates()
    }
}
package com.brigadka.app

import android.os.Build

class AndroidPlatform : Platform {
//    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val name: String = "android"
}

actual fun getPlatform(): Platform = AndroidPlatform()
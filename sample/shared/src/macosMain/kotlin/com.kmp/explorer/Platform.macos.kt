package com.kmp.explorer


class MacOsPlatform: Platform {
    override val name: String = "MacOs"
}

actual fun getPlatform(): Platform = MacOsPlatform()
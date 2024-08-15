package com.kmp.explorer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
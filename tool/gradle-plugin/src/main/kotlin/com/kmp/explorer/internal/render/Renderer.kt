package com.kmp.explorer.internal.render

import java.io.File

internal interface Renderer {
    fun render(output: File)
}
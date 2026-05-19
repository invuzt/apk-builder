package com.builder.utils

object NativeLib {
    init {
        System.loadLibrary("rust_engine")
    }

    external fun processFileWithRust(
        filePath: String, 
        rotationDegrees: Int, 
        useHdr: Boolean, 
        useLossless: Boolean
    )
}

package com.builder.utils

object NativeLib {
    init {
        System.loadLibrary("rust_engine")
    }

    /**
     * Kotlin hanya kasih 'Link' (Path) ke file foto.
     * Rust yang akan kerja keras buka, proses, dan kompres filenya.
     */
    external fun processFileWithRust(filePath: String)
}

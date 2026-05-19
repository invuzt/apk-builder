package com.builder.utils

object NativeLib {
    init {
        // Nama library harus sama dengan name di Cargo.toml
        System.loadLibrary("rust_engine")
    }

    /**
     * Memanggil fungsi Rust untuk memproses HDR dan Kompresi
     */
    external fun processHDRAndCompress(inputData: ByteArray): ByteArray
}

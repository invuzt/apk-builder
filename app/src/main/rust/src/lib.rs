use jni::JNIEnv;
use jni::objects::{JClass, JString};
use std::path::Path;
use image::{DynamicImage, GenericImageView, ImageOutputFormat};
use std::fs::File;
use std::io::BufWriter;

#[no_mangle]
pub extern "system" fn Java_com_builder_utils_NativeLib_processFileWithRust(
    mut env: JNIEnv,
    _class: JClass,
    file_path: JString,
) {
    // 1. Ambil Path File dari Kotlin (Link ke Photo)
    let path_str: String = env.get_string(&file_path).expect("Gagal ambil path").into();
    let path = Path::new(&path_str);

    if let Ok(mut img) = image::open(path) {
        // 2. KEKUATAN RUST: Proses Efek Warna (HDR Ringan / Vivid ala iPhone)
        // Kita naikkan sedikit kontras dan kecerahan secara native
        let (width, height) = img.dimensions();
        
        // Contoh manipulasi pixel: Adjust Brightness & Contrast
        let processed_img = img.brighten(10).adjust_contrast(15.0);

        // 3. KEKUATAN RUST: Lossless / High-Efficiency WebP Compression
        // Kita simpan kembali ke file yang sama atau file baru dengan kompresi WebP
        // WebP Lossless biasanya jauh lebih kecil dari JPEG tapi kualitas utuh.
        let output_path = path.with_extension("webp");
        let file = File::create(output_path).unwrap();
        let mut writer = BufWriter::new(file);

        processed_img.write_to(&mut writer, ImageOutputFormat::WebP).expect("Gagal kompres");
    }
}

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use std::path::Path;
use image::{DynamicImage, GenericImageView, ImageOutputFormat, imageops};
use std::fs::File;
use std::io::BufWriter;

#[no_mangle]
pub extern "system" fn Java_com_builder_utils_NativeLib_processFileWithRust(
    mut env: JNIEnv,
    _class: JClass,
    file_path: JString,
    rotation_degrees: jni::sys::jint,
    use_hdr: jni::sys::jboolean,
    use_lossless: jni::sys::jboolean,
) {
    let path_str: String = env.get_string(&file_path).expect("Gagal ambil path").into();
    let path = Path::new(&path_str);

    if let Ok(mut img) = image::open(path) {
        // 1. KEKUATAN RUST: Perbaikan Rotasi Sensor di Level Native
        let mut processed_img = match rotation_degrees {
            90 => img.rotate90(),
            180 => img.rotate180(),
            270 => img.rotate270(),
            _ => img,
        };

        // 2. KEKUATAN RUST: Vivid HDR ala iPhone (Manipulasi Pixel Sat-Set)
        if use_hdr == jni::sys::JNI_TRUE {
            // Naikkan kontras dan saturasi warna secara internal di Rust
            processed_img = processed_img.adjust_contrast(20.0).brighten(5);
        }

        // 3. KEKUATAN RUST: Penyimpanan & Kompresi Pintar
        let file = File::create(path).unwrap(); // Timpa file cache lama dengan hasil olahan Rust
        let mut writer = BufWriter::new(file);

        if use_lossless == jni::sys::JNI_TRUE {
            // Kompresi WebP tingkat tinggi tanpa merusak kualitas
            processed_img.write_to(&mut writer, ImageOutputFormat::WebP).expect("Gagal kompres WebP");
        } else {
            // Kompresi JPEG standar jika fitur lossless dimatikan
            processed_img.write_to(&mut writer, ImageOutputFormat::Jpeg(90)).expect("Gagal kompres JPEG");
        }
    }
}

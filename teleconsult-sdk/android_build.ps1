# Android Build Script for Teleconsult SDK

$targets = @("aarch64-linux-android", "armv7-linux-androideabi", "i686-linux-android", "x86_64-linux-android")

foreach ($target in $targets) {
    Write-Host "Building for $target..."
    cargo ndk --target $target build --release
}

Write-Host "Generating Kotlin bindings..."
cargo run --release --features=uniffi/cli -- uniffi-bindgen generate src/lib.rs --language kotlin --out-dir out/android

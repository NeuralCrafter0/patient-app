#!/bin/bash
# iOS Build Script for Teleconsult SDK (Run this on a Mac)

targets=("aarch64-apple-ios" "x86_64-apple-ios" "aarch64-apple-ios-sim")

for target in "${targets[@]}"
do
    echo "Building for $target..."
    cargo build --target $target --release
done

echo "Generating Swift bindings..."
cargo run --release --features=uniffi/cli -- uniffi-bindgen generate src/lib.rs --language swift --out-dir out/ios

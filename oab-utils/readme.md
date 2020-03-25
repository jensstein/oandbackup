compile
======
download the android ndk from https://developer.android.com/ndk/downloads/
./build/tools/make\_standalone\_toolchain.py --api 18 --install-dir=$toolchain\_directory --arch=arm

e.g. add armv7 target:
```
rustup target add aarch64-linux-android
```

add to the global cargo config (.cargo/config):
```
[target.armv7-linux-androideabi]
linker = "Android/Sdk/ndk/_version/$toolchains/llvm/prebuilt/linux-x86_64/bin/armv7a-linux-androideabi29-clang"

[target.aarch64-linux-android]
linker = "Android/Sdk/ndk/_version/toolchains/llvm/prebuilt/linux-x86_64/bin/aarch64-linux-android29-clang"

[target.i686-linux-android]
linker = "Android/Sdk/ndk/_version/toolchains/llvm/prebuilt/linux-x86_64/bin/i686-linux-android29-clang"

[target.x86_64-linux-android]
linker = "Android/Sdk/ndk/_version/toolchains/llvm/prebuilt/linux-x86_64/bin/x86_64-linux-android29-clang"

```

build with Cargo:
```
cargo build --release --target armv7-linux-androideabi
cargo build --release --target aarch64-linux-android
```

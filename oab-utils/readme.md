compile
======
download the android ndk from https://developer.android.com/ndk/downloads/
./build/tools/make\_standalone\_toolchain.py --api 18 --install-dir=$toolchain\_directory --arch=arm

add armv7 target:
```
rustup target add armv7-linux-androideabi
```

add to the global cargo config (.cargo/config):
```
[target.armv7-linux-androideabi]
linker = "$toolchain\_directory/bin/arm-linux-androideabi-clang"
```

build with
```
cargo build --release --target armv7-linux-androideabi
```

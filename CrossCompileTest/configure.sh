#!/bin/bash

arch=armeabi-v7a

while [[  $# -gt 0 ]]
do
key="$1"
case $key in
    --arch )
    arch="$2"
    shift #past argumemt
    ;;
    *)
          # unknown option
    ;;
esac
shift # past argument or value
done

if [[ $arch =~ armeabi-v7a ]]; then
    abi="armeabi-v7a"
    toolchain=arm-linux-androideabi-4.9
elif [[ $arch =~ arm64-v8a ]]; then
    abi="arm64-v8a"
    toolchain=aarch64-linux-android-4.9
elif [[ $arch =~ x86 ]]; then
    abi="x86"
    toolchain=x86-4.9
else
    echo "arch must be armv7a, arm64, or x86"
    exit 1
fi

build_path=build/android/$arch

mkdir -p $build_path || exit 1
cd $build_path       || exit 1

echo Build Target=$abi


/Users/chenfeifei/Library/Android/sdk/cmake/3.6.4111459/bin/cmake \
../../../proj.cmake \
-DANDROID_ABI=$abi \
-DANDROID_PLATFORM=android-15 \
-DANDROID_TOOLCHAIN_NAME=$toolchain \
-DCMAKE_BUILD_TYPE=Debug \
-DANDROID_NDK=/Users/chenfeifei/Android/android-ndk-r16b \
-DCMAKE_TOOLCHAIN_FILE=/Users/chenfeifei/Android/android-ndk-r16b/build/cmake/android.toolchain.cmake \
-DANDROID_TOOLCHAIN=clang || exit 1

make -j4 || exit 1
cd ../../.. || exit 1

exit 0


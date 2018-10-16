cmake_minimum_required(VERSION 3.4.1)

if (WIN32)
    MESSAGE("Build Windows")
elseif (ANDROID)
    MESSAGE("Build Android")
    if (ARMEABI_V7A)
        set(ABI armeabi-v7a)
        MESSAGE("Target armv7a")
    endif()
    if (ARM64_V8A)
        set(ABI arm64-v8a)
        MESSAGE("Target arm64")
    endif()
    if (X86)
        set(ABI x86)
        MESSAGE("Target x86")
    endif()
elseif (IOS)
    MESSAGE("Build iOS")
elseif (APPLE)
    MESSAGE("Build Mac")
elseif (UNIX)
    MESSAGE("Build Linux")
endif()


add_library(myso SHARED ../src/Native.c)

set_target_properties(myso PROPERTIES LIBRARY_OUTPUT_DIRECTORY "${CMAKE_CURRENT_SOURCE_DIR}/../lib/${ABI}")

#include_directories(/home/hm/JDK/jdk1.8.0_151/include /home/hm/JDK/jdk1.8.0_151/include/linux)

target_link_libraries(myso log)


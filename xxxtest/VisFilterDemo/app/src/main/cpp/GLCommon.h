//
// Created by 陈飞飞 on 2018/9/17.
//

#ifndef VISFILTERDEMO_GLCOMMON_H
#define VISFILTERDEMO_GLCOMMON_H

#include <GLES2/gl2.h>
#include <GLES2/gl2ext.h>
#include <GLES/gl.h>

#include <android/log.h>

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG ,  "VisFilter", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO  ,  "VisFilter", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR  , "VisFilter", __VA_ARGS__)

#define VAssert(x, text) \
    do { if (!(x)) LOGE("ASSERTION FAILED at %s:%d: %s", __FILE__, __LINE__, text); } while(0)


#endif //VISFILTERDEMO_GLCOMMON_H

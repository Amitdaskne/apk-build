#include "Includes.h"



extern "C" JNIEXPORT jobject JNICALL
Java_com_thunder_kuroapi_Sapi_getheaders(JNIEnv *env, jobject /* this */) {
    std::vector<std::string> headers = {
            OBFUSCATE("Content-Type"),
            OBFUSCATE("application/x-www-form-urlencoded"),
            OBFUSCATE("Accept"),
            OBFUSCATE("application/json"),
            OBFUSCATE("Charset"),
            OBFUSCATE("UTF-8"),
            OBFUSCATE("User-Agent"),
            OBFUSCATE("public-loder"),
            OBFUSCATE("PUBG"),
            OBFUSCATE("user_key"),
            OBFUSCATE("serial"),
    };
    jclass arrayListClass = env->FindClass("java/util/ArrayList");
    jmethodID arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    jobject headerList = env->NewObject(arrayListClass, arrayListConstructor);
    jmethodID addMethod = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    for (const auto &header: headers) {
        jstring jheader = env->NewStringUTF(header.c_str());
        env->CallBooleanMethod(headerList, addMethod, jheader);
        env->DeleteLocalRef(jheader);
    }

    return headerList;
}


extern "C"
JNIEXPORT jstring JNICALL
Java_com_thunder_kuroapi_Sapi_getbaseurl(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("https://venomkey.com/connect"));//KEY PANEL
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_thunder_kuroapi_Sapi_libdownloadlink(JNIEnv *env, jobject thiz) {
    return env->NewStringUTF(OBFUSCATE("https://zlibs.shop/CREATOP/folders/THUNDER/Matrix.zip"));//UI LIB LINK
}

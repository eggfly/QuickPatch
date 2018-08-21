#include <jni.h>
#include <android/log.h>

#define  LOG_TAG    "QuickPatchNativeBridge"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_quickpatch_sdk_NativeBridge_callNonvirtualVoidMethodTest(
        JNIEnv *env,
        jclass type,
        jobject obj) {
    LOGD("callNonvirtualVoidMethodTest");
    jclass superClass = env->FindClass("quickpatch/example/SuperClass");
    jclass subClass = env->FindClass("quickpatch/example/SubClass");
    jclass subSubClass = env->FindClass("quickpatch/example/SubSubClass");

    jmethodID superClassMethod = env->GetMethodID(superClass, "foo", "()V");
    jmethodID subClassMethod = env->GetMethodID(subClass, "foo", "()V");
    jmethodID subSubClassMethod = env->GetMethodID(subSubClass, "foo", "()V");
    env->CallNonvirtualVoidMethod(obj, superClass, superClassMethod);
    env->CallNonvirtualVoidMethod(obj, subClass, subClassMethod);
    env->CallNonvirtualVoidMethod(obj, subSubClass, subSubClassMethod);
}

extern "C"
JNIEXPORT void JNICALL
Java_quickpatch_sdk_NativeBridge_callNonvirtualVoidMethod(JNIEnv *env, jclass type, jobject obj,
                                                          jstring classNameOfMethod,
                                                          jstring methodName,
                                                          jstring methodSignature,
                                                          jobjectArray invokeArgs) {
    LOGD("callNonvirtualVoidMethod");
    const char *classNameOfMethodStr = env->GetStringUTFChars(classNameOfMethod, 0);
    const char *methodNameStr = env->GetStringUTFChars(methodName, 0);
    const char *methodSignatureStr = env->GetStringUTFChars(methodSignature, 0);

    jclass classOfMethod = env->FindClass(classNameOfMethodStr);
    jmethodID method = env->GetMethodID(classOfMethod, methodNameStr, methodSignatureStr);
    int argsCount = env->GetArrayLength(invokeArgs);
    jvalue argsArray[argsCount];
    for (int i = 0; i < argsCount; i++) {
        jobject item = env->GetObjectArrayElement(invokeArgs, i);
        jvalue value = {.l = item};
        argsArray[i] = value;
    }

    env->CallNonvirtualVoidMethodA(obj, classOfMethod, method, argsArray);

    for (int i = 0; i < argsCount; i++) {
        env->DeleteLocalRef(argsArray[i].l);
    }
    env->DeleteLocalRef(classOfMethod);

    env->ReleaseStringUTFChars(classNameOfMethod, classNameOfMethodStr);
    env->ReleaseStringUTFChars(methodName, methodNameStr);
    env->ReleaseStringUTFChars(methodSignature, methodSignatureStr);
}

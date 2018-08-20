#include <jni.h>

extern "C"
JNIEXPORT
void JNICALL
Java_quickpatch_sdk_NativeBridge_callNonvirtualVoidMethod(
        JNIEnv *env,
        jobject /* this */,
        jobject obj) {
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
JNIEXPORT
void JNICALL
Java_quickpatch_sdk_NativeBridge_callNonvirtualVoidMethodHelper(
        JNIEnv *env,
        jobject /* this */,
        jobject obj,
        jstring classNameOfMethod,
        jstring methodName,
        jstring methodSignature) {
    const char *classNameStr = env->GetStringUTFChars(classNameOfMethod, JNI_FALSE);
    const char *methodNameStr = env->GetStringUTFChars(methodName, JNI_FALSE);
    const char *methodSignatureStr = env->GetStringUTFChars(methodSignature, JNI_FALSE);
    jclass classOfMethod = env->FindClass(classNameStr);
    jmethodID method = env->GetMethodID(classOfMethod, methodNameStr, methodSignatureStr);
    env->CallNonvirtualVoidMethod(obj, classOfMethod, method);
}

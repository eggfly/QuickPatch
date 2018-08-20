#include <jni.h>
#include <stdio.h>

int i;

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

    char a[256] = {'\0'};
    sprintf(a, "LEAK? %d", i++);
    jstring leak = env->NewStringUTF(a);
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
        jstring methodSignature,
        jobjectArray invokeArgs) {
    const char *classNameStr = env->GetStringUTFChars(classNameOfMethod, NULL);
    const char *methodNameStr = env->GetStringUTFChars(methodName, NULL);
    const char *methodSignatureStr = env->GetStringUTFChars(methodSignature, NULL);
    jclass classOfMethod = env->FindClass(classNameStr);
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
    env->ReleaseStringUTFChars(classNameOfMethod, classNameStr);
    env->ReleaseStringUTFChars(methodName, methodNameStr);
    env->ReleaseStringUTFChars(methodSignature, methodSignatureStr);
}

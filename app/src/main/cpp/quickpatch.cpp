#include <jni.h>
#include <android/log.h>

#define  LOG_TAG    "QuickPatch::ReflectionBridge"
// #define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
// #define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


inline jobject
callNonVirtualMethod(JNIEnv *env, jobject obj, jchar returnType, jclass classOfMethod,
                     jmethodID method, jvalue *argsArray);

template<typename ...T>
inline jobject construct(JNIEnv *env, const char *className, const char *sig, T... params) {
    jclass cls = env->FindClass(className);
    if (!cls)
        return nullptr;

    jmethodID ctor = env->GetMethodID(cls, "<init>", sig);
    if (!ctor) {
        env->DeleteLocalRef(cls);
        return nullptr;
    }

    jobject result = env->NewObject(cls, ctor, params...);
    env->DeleteLocalRef(cls);
    return result;
}

extern "C"
JNIEXPORT jobject JNICALL
Java_quickpatch_sdk_ReflectionBridge_callNonVirtualMethod(JNIEnv *env, jclass type, jobject obj,
                                                          jstring classNameOfMethod,
                                                          jstring methodName,
                                                          jstring methodSignature, jchar returnType,
                                                          jobjectArray invokeArgs) {
    LOGD("callNonVirtualMethod");
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

    jobject returnObj = callNonVirtualMethod(env, obj, returnType, classOfMethod, method,
                                             argsArray);

    for (int i = 0; i < argsCount; i++) {
        env->DeleteLocalRef(argsArray[i].l);
    }
    env->DeleteLocalRef(classOfMethod);

    env->ReleaseStringUTFChars(classNameOfMethod, classNameOfMethodStr);
    env->ReleaseStringUTFChars(methodName, methodNameStr);
    env->ReleaseStringUTFChars(methodSignature, methodSignatureStr);
    return returnObj;
}

inline jobject callNonVirtualMethod(JNIEnv *env, jobject obj, jchar returnType,
                                    jclass classOfMethod, jmethodID method,
                                    jvalue *argsArray) {
    jobject returnObj = nullptr;
    switch (returnType) {
        case 'V': { // void
            env->CallNonvirtualVoidMethodA(obj, classOfMethod, method, argsArray);
            break;
        }
        case 'Z': { // boolean
            jboolean r = env->CallNonvirtualBooleanMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Boolean", "(Z)V", r);
            break;
        }
        case 'B': { // byte
            jbyte r = env->CallNonvirtualByteMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Byte", "(B)V", r);
            break;
        }
        case 'C': { // char
            jbyte r = env->CallNonvirtualByteMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Character", "(C)V", r);
            break;
        }
        case 'S': { // short
            jshort r = env->CallNonvirtualShortMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Short", "(S)V", r);
            break;
        }
        case 'I': { // int
            jint r = env->CallNonvirtualIntMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Integer", "(I)V", r);
            break;
        }
        case 'J': { // long
            jlong r = env->CallNonvirtualLongMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Long", "(J)V", r);
            break;
        }
        case 'F': { // float
            jfloat r = env->CallNonvirtualFloatMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Float", "(F)V", r);
            break;
        }
        case 'D': { // double
            jdouble r = env->CallNonvirtualDoubleMethodA(obj, classOfMethod, method, argsArray);
            returnObj = construct(env, "java/lang/Double", "(D)V", r);
            break;
        }
        case '[':   // array
        case 'L': { // object
            returnObj = env->CallNonvirtualObjectMethodA(obj, classOfMethod, method, argsArray);
            break;
        }
        default:
            break;
    }
    return returnObj;
}


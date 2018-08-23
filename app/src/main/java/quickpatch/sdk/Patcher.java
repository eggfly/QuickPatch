package quickpatch.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public final class Patcher {

    private static final String TAG = Patcher.class.getSimpleName();
    private volatile ClassLoader mPatchClassLoader;
    private volatile boolean mHasGlobalPatch = false;

    private Patcher() {
    }

    public void unloadPatch(Context context) {

    }

    private static class SingletonHolder {
        private static Patcher instance = new Patcher();
    }

    public static Patcher getInstance() {
        return SingletonHolder.instance;
    }

    public void testLoadPatch(Context context) {
        String apkPath = getSelfApkPath(context);
        Log.d(TAG, apkPath);
        File dex = new File(Environment.getExternalStorageDirectory(), "patch.dex");
        if (dex.exists()) {
            File privateDex = new File(context.getCacheDir(), "patch.dex");
            try {
                Utils.copy(dex, privateDex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClassLoader parent = Patcher.class.getClassLoader();
            Log.d(TAG, "" + parent);
            mPatchClassLoader = new DexClassLoader(privateDex.getAbsolutePath(),
                    context.getCacheDir().getAbsolutePath(), null, parent);
            privateDex.delete();
            Log.d(TAG, "" + mPatchClassLoader);
            mHasGlobalPatch = true;
        } else {
            Log.d(TAG, "patch not exist.");
        }
    }

    private String getSelfApkPath(Context context) {
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.publicSourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 为保证性能在第一层检查，判断全局是否有热修复补丁
     * TODO: 需要性能测试，也许new Object数组在有逃逸检测优化后，不需要在堆上分配也不需要gc
     *
     * @return
     */
    public static boolean hasGlobalPatch() {
        return getInstance().mHasGlobalPatch;
    }

    public static ProxyResult proxy(Object thisObject,
                                    String className,
                                    String methodName,
                                    String methodSignature,
                                    Object[] args) {
        return getInstance().invokeProxy(thisObject, className, methodName, methodSignature, args);
    }

    private ProxyResult invokeProxy(Object thisObject, String className, String methodName, String methodSignature, Object[] args) {
        // TODO: 判断是否有patch的逻辑、性能问题、各版本机型适配
        if (mHasGlobalPatch && mPatchClassLoader != null) {
            try {
                Class<?> patchClass = mPatchClassLoader.loadClass(className + "Patch");
                Method patchMethod = patchClass.getDeclaredMethod(methodName, new Class[]{Object.class, Bundle.class});
                patchMethod.setAccessible(true);
                final Object returnValue = patchMethod.invoke(null, thisObject, null);
                return ProxyResult.createActiveProxyResult(returnValue);
            } catch (ClassNotFoundException e) {
                // it's normally ok
            } catch (NoSuchMethodException e) {
                // it's normally ok
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return ProxyResult.NO_PROXY;
        } else {
            // do nothing
            return ProxyResult.NO_PROXY;
        }
    }
}

package quickpatch.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.lang.reflect.Proxy;

import dalvik.system.PathClassLoader;
import quickpatch.example.MainActivity;

public final class Patcher {

    private static final String TAG = Patcher.class.getSimpleName();

    private Patcher() {
    }

    private static class SingletonHolder {
        private static Patcher instance = new Patcher();
    }

    public static Patcher getInstance() {
        return SingletonHolder.instance;
    }

    public volatile boolean mHasGlobalPatch = false;
    private volatile PatchInterface mPatchInterfaceProxyInstance;

    public void testLoadPatch(Context context) {
        String apkPath = getSelfApkPath(context);
        Log.d(TAG, apkPath);
        PathClassLoader classLoader = new PathClassLoader(apkPath, null);

        try {
            Class<?> clazz = classLoader.loadClass("quickpatch.sdk.Patcher");
            Log.d(TAG, "loadClass: " + clazz);
            clazz = classLoader.loadClass("java.lang.Object");
            Log.d(TAG, "loadClass: " + clazz + "eq?" + (clazz == Object.class));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private String getSelfApkPath(Context context) {
        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return ai.publicSourceDir;
        } catch (Throwable x) {
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
        final ProxyResult result;
        // TODO: 判断是否有patch的逻辑、性能问题、各版本机型适配
        if (mHasGlobalPatch) {
            // TODO classloader是否可以优化?
            final PatchInterface patch;
            if (mPatchInterfaceProxyInstance == null) {
                mPatchInterfaceProxyInstance = (PatchInterface) Proxy.newProxyInstance(Patcher.class.getClassLoader(), new Class[]{PatchInterface.class}, new PatchInvocationHandler());
            }
            final Object returnValue = mPatchInterfaceProxyInstance.invoke(thisObject, MainActivity.class, methodName, methodSignature, args);
            result = ProxyResult.createActiveProxyResult(returnValue);
        } else {
            // do nothing
            result = ProxyResult.NO_PROXY;
        }
        return result;
    }
}

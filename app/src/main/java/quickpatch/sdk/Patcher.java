package quickpatch.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexClassLoader;
import quickpatch.example.MainActivity_QPatch;

public final class Patcher {

    private static final String TAG = Patcher.class.getSimpleName();
    private volatile ClassLoader mPatchClassLoader;
    private volatile boolean mHasGlobalPatch = false;
    private volatile Set<String> mQPatchClassNames;

    private Patcher() {
    }

    public void unloadPatch(Context context) {
        // TODO
    }


    private static class SingletonHolder {
        private static Patcher instance = new Patcher();
    }

    public static Patcher getInstance() {
        return SingletonHolder.instance;
    }

    public String loadPatch(Context context) {
        if (mHasGlobalPatch) {
            Log.w(TAG, "global patch already exists");
            return null;
        }
        String apkPath = getSelfApkPath(context);
        Log.d(TAG, apkPath);
        File dex = new File(Environment.getExternalStorageDirectory(), "patch.dex");
        if (dex.exists()) {
            File patchDir = new File(context.getCacheDir(), SdkConstants.QUICK_PATCH_DIR);
            patchDir.mkdirs();
            File privateDex = new File(patchDir, "patch.dex");
            try {
                FileUtils.copy(dex, privateDex);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ClassLoader parent = Patcher.class.getClassLoader();
            Log.d(TAG, "" + parent);
            mQPatchClassNames = ClassUtils.findPatchClassesInDex(dex.getPath(), SdkConstants.QPATCH_CLASS_SUFFIX);
            // parent classloader可以设置null，但是为了方便native库的加载，借用了父classloader的native库加载路径
            mPatchClassLoader = new DexClassLoader(privateDex.getAbsolutePath(),
                    context.getCacheDir().getAbsolutePath(), null, parent);
            privateDex.delete();
            Log.d(TAG, "" + mPatchClassLoader);
            mHasGlobalPatch = true;
            return dex.getAbsolutePath();
        } else {
            Log.d(TAG, "patch not exist.");
            return null;
        }
    }

    /**
     * TODO: 临时实现需要删除
     *
     * @param context
     */
    public void simulateLoadPatch(Context context) {
        mPatchClassLoader = Patcher.class.getClassLoader();
        // TODO 临时实现
        mQPatchClassNames = new HashSet<>();
        mQPatchClassNames.add(MainActivity_QPatch.class.getCanonicalName());
        mHasGlobalPatch = true;
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
        // TODO: 性能问题、各版本机型适配
        if (mHasGlobalPatch && mPatchClassLoader != null && mQPatchClassNames != null) {
            String fullQPatchClassName = className + SdkConstants.QPATCH_CLASS_SUFFIX;
            if (mQPatchClassNames.contains(fullQPatchClassName)) {
                try {
                    Class<?> patchClass = mPatchClassLoader.loadClass(fullQPatchClassName);
                    Method[] methods = patchClass.getDeclaredMethods();
                    for (Method method : methods) {
                        if (TextUtils.equals(methodName, method.getName())
                                && TextUtils.equals(ClassUtils.getSignature(method), methodSignature)) {
                            Class<?> clazz = Class.forName(className);
                            Constructor<?> constructor = patchClass.getDeclaredConstructor(clazz);
                            constructor.setAccessible(true);
                            Object patchInstance = constructor.newInstance(thisObject);
                            final Object returnValue = method.invoke(patchInstance, args);
                            return ProxyResult.createActiveProxyResult(returnValue);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    // it's normally ok
                } catch (NoSuchMethodException e) {
                    // it's normally ok
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
            return ProxyResult.NO_PROXY;
        } else {
            // do nothing
            return ProxyResult.NO_PROXY;
        }
    }
}

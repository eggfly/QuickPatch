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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import dalvik.system.DexClassLoader;
import quickpatch.example.MainActivity_QPatch;
import quickpatch.example.SecondActivity_QPatch;

public final class Patcher {

    private static final String TAG = Patcher.class.getSimpleName();
    private volatile ClassLoader mPatchClassLoader;
    private volatile Set<String> mQPatchClassNames;

    private Patcher() {
    }

    public void unloadPatch(Context context) {
        // TODO 不支持
    }


    private static class SingletonHolder {
        private static Patcher instance = new Patcher();
    }

    public static Patcher getInstance() {
        return SingletonHolder.instance;
    }

    public String loadPatch(Context context) {
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
            // parent classloader可以设置null
            // 但是为了 1.方便native库的加载 2.方便找到原来的老类并插入静态stub, 所以借用了父classloader的native库加载路径
            mPatchClassLoader = new DexClassLoader(privateDex.getAbsolutePath(),
                    context.getCacheDir().getAbsolutePath(), null, parent);
            privateDex.delete();
            Log.d(TAG, "" + mPatchClassLoader);
            mQPatchClassNames = ClassUtils.findPatchClassesInDex(dex.getPath(), SdkConstants.QPATCH_CLASS_SUFFIX);
            updateStubFields();
            return dex.getAbsolutePath();
        } else {
            Log.d(TAG, "patch not exist.");
            return null;
        }
    }

    private void updateStubFields() {
        for (String newClassName : mQPatchClassNames) {
            int index = newClassName.lastIndexOf(SdkConstants.QPATCH_CLASS_SUFFIX);
            if (index > 0) {
                final String oldClassName = newClassName.substring(0, index);
                try {
                    Class<?> oldClass = mPatchClassLoader.loadClass(oldClassName);
                    Field[] fields = oldClass.getDeclaredFields();
                    Field stubField = null;
                    for (Field field : fields) {
                        if (TextUtils.equals(field.getType().getCanonicalName(), QuickPatchStub.class.getCanonicalName()) && TextUtils.equals(field.getDeclaringClass().getCanonicalName(), oldClass.getCanonicalName())) {
                            stubField = field;
                            break;
                        }
                    }
                    if (stubField == null) {
                        Log.d(TAG, "ERROR: found a new patch class but cannot find stub field on " + oldClass);
                    } else {
                        Class<?> newClass = mPatchClassLoader.loadClass(newClassName);
                        Constructor<QuickPatchStubImpl> constructor = QuickPatchStubImpl.class.getDeclaredConstructor(Class.class, Class.class);
                        QuickPatchStubImpl stubImplObj = constructor.newInstance(oldClass, newClass);
                        stubField.set(null, stubImplObj);
                        Log.d(TAG, "SUCCESS: set stub field and patched for class: " + oldClassName);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * TODO: 临时测试
     *
     * @param context
     */
    public void simulateLoadPatch(Context context) {
        mPatchClassLoader = Patcher.class.getClassLoader();
        mQPatchClassNames = new HashSet<>();
        mQPatchClassNames.add(MainActivity_QPatch.class.getCanonicalName());
        mQPatchClassNames.add(SecondActivity_QPatch.class.getCanonicalName());
        updateStubFields();
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
}

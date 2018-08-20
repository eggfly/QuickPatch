package quickpatch.gradleplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;

public class CodePatcher {
    public static void insertCode(List<CtClass> box, File jarFile) throws IOException, CannotCompileException {
        ZipOutputStream outStream = new JarOutputStream(new FileOutputStream(jarFile));
        for (CtClass ctClass : box) {
            if (isNeedInsertClass(ctClass.getName())) {
                // change class modifier
                ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));
                if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
                    // skip the unsatisfied class
                    System.out.println("skip class: " + ctClass.getName());
                    zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                    continue;
                }

                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    if (!isQualifiedMethod(ctBehavior)) {
                        continue;
                    }
                    // here comes the method will be inserted code
                    try {
                        if (ctBehavior.getMethodInfo().isMethod()) {
                            CtMethod ctMethod = (CtMethod) ctBehavior;
                            boolean isStatic = (ctMethod.getModifiers() & AccessFlag.STATIC) != 0;
                            CtClass returnType = ctMethod.getReturnType();
                            String returnTypeString = returnType.getName();
                            //construct the code will be inserted in string format
                            String body = "Object argThis = null;";
                            if (!isStatic) {
                                body += "\nargThis = $0;";
                            }
                            body += "\nfinal quickpatch.sdk.ProxyResult proxyResult = ";
                            body += String.format("quickpatch.sdk.Patcher.proxy(argThis, \"%s\", \"%s\", \"%s\", $args); ",
                                    ctClass.getName(), ctMethod.getName(), ctMethod.getSignature());
                            body += "\nif (proxyResult.isPatched) {\n  " + getReturnStatement(returnTypeString) + "\n}";
                            System.out.println("Patching method: " + ctMethod.getName()
                                    + ", signature: " + ctMethod.getSignature()
                                    + ", genericSignature: " + ctMethod.getGenericSignature()
                                    + ", returnType: " + returnTypeString + ", body: \n" + body);
                            if (ctMethod.getGenericSignature() != null) {
                                System.out.println("===============================\nGENERIC SIGNATURE:: " + ctMethod.getGenericSignature());
                            }
                            ctBehavior.insertBefore(body);
                        }
                    } catch (Throwable t) {
                        // here we ignore the error
                        t.printStackTrace();
                        System.out.println("ctClass: " + ctClass.getName() + " error: " + t.getMessage());
                    }
                }
            }
            // zip the inserted-classes into output file
            zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
        }
        outStream.close();
    }

    private static boolean isNeedInsertClass(String className) {
        //这样可以在需要埋点的剔除指定的类
        if (className.startsWith("quickpatch.sdk")) {
            System.err.println(className);
            return false;
        }
//        for (String exceptName : exceptPackageList) {
//            if (className.startsWith(exceptName)) {
//                return false;
//            }
//        }
        for (String name : Constants.sHotFixPackageList) {
            if (className.startsWith(name)) {
                return true;
            }
        }
        // 默认不插桩
        return false;
    }

    private static void zipFile(byte[] classBytesArray, ZipOutputStream zos, String entryName) {
        try {
            ZipEntry entry = new ZipEntry(entryName);
            zos.putNextEntry(entry);
            zos.write(classBytesArray, 0, classBytesArray.length);
            zos.closeEntry();
            zos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean isQualifiedMethod(CtBehavior it) throws CannotCompileException {
        if (it.getMethodInfo().isStaticInitializer()) {
            return false;
        }

        // synthetic 方法暂时不aop 比如AsyncTask 会生成一些同名 synthetic方法,对synthetic 以及private的方法也插入的代码，主要是针对lambda表达式
        if ((it.getModifiers() & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(it.getModifiers())) {
            return false;
        }
        if (it.getMethodInfo().isConstructor()) {
            return false;
        }

        if ((it.getModifiers() & AccessFlag.ABSTRACT) != 0) {
            return false;
        }
        if ((it.getModifiers() & AccessFlag.NATIVE) != 0) {
            return false;
        }
        if ((it.getModifiers() & AccessFlag.INTERFACE) != 0) {
            return false;
        }
//
//        if (it.getMethodInfo().isMethod()) {
//            if (AccessFlag.isPackage(it.getModifiers())) {
//                it.setModifiers(AccessFlag.setPublic(it.getModifiers()));
//            }
//            boolean flag = isMethodWithExpression((CtMethod) it);
//            if (!flag) {
//                return false;
//            }
//        }
//        //方法过滤
//        if (isExceptMethodLevel && exceptMethodList != null) {
//            for (String exceptMethod : exceptMethodList) {
//                if (it.getName().matches(exceptMethod)) {
//                    return false;
//                }
//            }
//        }
//
//        if (isHotfixMethodLevel && hotfixMethodList != null) {
//            for (String name : hotfixMethodList) {
//                if (it.getName().matches(name)) {
//                    return true;
//                }
//            }
//        }
//        return !isHotfixMethodLevel;
        return true;
    }

    /**
     * 根据传入类型判断调用PathProxy的方法
     *
     * @param type 返回类型
     * @return 返回return语句
     */
    private static String getReturnStatement(String type) {
        switch (type) {
            case Constants.CONSTRUCTOR:
                return "";
            case Constants.LANG_VOID:
                return "return null;";
            case Constants.VOID:
                return "return;";
            case Constants.LANG_BOOLEAN:
                return "return ((java.lang.Boolean)proxyResult.returnValue);";
            case Constants.BOOLEAN:
                return "return ((java.lang.Boolean)proxyResult.returnValue).booleanValue();";
            case Constants.INT:
                return "return ((java.lang.Integer)proxyResult.returnValue).intValue();";
            case Constants.LANG_INT:
                return "return ((java.lang.Integer)proxyResult.returnValue);";
            case Constants.LONG:
                return "return ((java.lang.Long)proxyResult.returnValue).longValue();";
            case Constants.LANG_LONG:
                return "return ((java.lang.Long)proxyResult.returnValue);";
            case Constants.DOUBLE:
                return "return ((java.lang.Double)proxyResult.returnValue).doubleValue();";
            case Constants.LANG_DOUBLE:
                return "return ((java.lang.Double)proxyResult.returnValue);";
            case Constants.FLOAT:
                return "return ((java.lang.Float)proxyResult.returnValue).floatValue();";
            case Constants.LANG_FLOAT:
                return "return ((java.lang.Float)proxyResult.returnValue);";
            case Constants.SHORT:
                return "return ((java.lang.Short)proxyResult.returnValue).shortValue();";
            case Constants.LANG_SHORT:
                return "return ((java.lang.Short)proxyResult.returnValue);";
            case Constants.BYTE:
                return "return ((java.lang.Byte)proxyResult.returnValue).byteValue();";
            case Constants.LANG_BYTE:
                return "return ((java.lang.Byte)proxyResult.returnValue);";
            case Constants.CHAR:
                return "return ((java.lang.Character)proxyResult.returnValue).charValue();";
            case Constants.LANG_CHARACTER:
                return "return ((java.lang.Character)proxyResult.returnValue);";
            default:
                return "return ((" + type + ")proxyResult.returnValue);";
        }
    }
}

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
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.MethodInfo;

public class CodePatcher {
    public static void insertCode(List<CtClass> box, File jarFile) throws IOException, CannotCompileException {
        ZipOutputStream outStream = new JarOutputStream(new FileOutputStream(jarFile));
        for (CtClass ctClass : box) {
            if (isNeedInsertClass(ctClass.getName())) {
                // change class modifier
                // ctClass.setModifiers(AccessFlag.setPublic(ctClass.getModifiers()));
                if (ctClass.isInterface() || ctClass.getDeclaredMethods().length < 1) {
                    // skip the unsatisfied class
                    System.out.println("ignore class: " + ctClass.getName());
                    zipFile(ctClass.toBytecode(), outStream, ctClass.getName().replaceAll("\\.", "/") + ".class");
                    continue;
                } else {
                    System.out.println("Current class: " + ctClass.getName());
                }

                for (CtBehavior ctBehavior : ctClass.getDeclaredBehaviors()) {
                    MethodInfo methodInfo = ctBehavior.getMethodInfo();
                    if (!isQualifiedMethod(methodInfo)) {
                        continue;
                    }
                    // here comes the method will be inserted code
                    try {
                        if (methodInfo.isMethod() || methodInfo.isConstructor()) {
                            CtMethod ctMethod = null;
                            if (methodInfo.isMethod()) {
                                ctMethod = (CtMethod) ctBehavior;
                            }
                            boolean isConstructor = !methodInfo.isMethod();
                            boolean isStatic = (ctBehavior.getModifiers() & AccessFlag.STATIC) != 0;
                            CtClass returnType = ctMethod != null ? ctMethod.getReturnType() : null;
                            String returnTypeString = returnType != null ? returnType.getName() : Constants.CONSTRUCTOR;
                            String body = "Object argThis = null;";
                            if (!isStatic) {
                                body += "\nargThis = $0;";
                            }
                            body += "\nfinal quickpatch.sdk.ProxyResult proxyResult = ";
                            body += String.format("quickpatch.sdk.Patcher.proxy(argThis, \"%s\", \"%s\", \"%s\", $args); ",
                                    ctClass.getName(), ctBehavior.getName(), ctBehavior.getSignature());
                            body += "\nif (proxyResult.isPatched) {\n  " + getReturnStatement(returnTypeString) + "\n}";
                            System.out.println("Patching method: " + ctBehavior.getName()
                                    + ", signature: " + ctBehavior.getSignature()
                                    + ", genericSignature: " + ctBehavior.getGenericSignature()
                                    + ", returnType: " + returnTypeString);
                            if (ctBehavior.getGenericSignature() != null) {
                                System.out.println("FOUND GENERIC SIGNATURE: " + ctBehavior.getGenericSignature());
                            }
                            if (isConstructor) {
                                CtConstructor constructor = (CtConstructor) ctBehavior;
                                // TODO: 构造函数插桩支持替换第一行的super或者this
                                // TODO: 构造函数插桩是否必要?
                                // TODO: 构造函数的名字没写成<init> 有问题..
                                constructor.insertBeforeBody(body);
                            } else {
                                ctBehavior.insertBefore(body);
                            }
                            // System.out.println("Patching with code body: \n" + body);
                            System.out.println("Patched OK");
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
            System.out.println("skip sdk class: " + className);
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
        return true;
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

    private static boolean isQualifiedMethod(MethodInfo methodInfo) {
        final int accessFlags = methodInfo.getAccessFlags();
        System.err.println("Method info: " + methodInfo + ", " + methodInfo.getName() + ", " + methodInfo.getDescriptor());

        if (methodInfo.isConstructor()) {
            System.err.println("Found constructor: " + methodInfo.getName());
        }

        final int isPublic = (accessFlags & AccessFlag.PUBLIC) != 0 ? 1 : 0;
        final int isPrivate = (accessFlags & AccessFlag.PRIVATE) != 0 ? 1 : 0;
        final int isProtected = (accessFlags & AccessFlag.PROTECTED) != 0 ? 1 : 0;
        final int isStatic = (accessFlags & AccessFlag.STATIC) != 0 ? 1 : 0;
        final int isFinal = (accessFlags & AccessFlag.FINAL) != 0 ? 1 : 0;
        final int isSynchronized = (accessFlags & AccessFlag.SYNCHRONIZED) != 0 ? 1 : 0;
        final int isVolatileOrBridge = (accessFlags & AccessFlag.VOLATILE) != 0 ? 1 : 0;
        final int isTransientOrVarArgs = (accessFlags & AccessFlag.TRANSIENT) != 0 ? 1 : 0;
        final int isNative = (accessFlags & AccessFlag.NATIVE) != 0 ? 1 : 0;
        final int isInterface = (accessFlags & AccessFlag.INTERFACE) != 0 ? 1 : 0;
        final int isAbstract = (accessFlags & AccessFlag.ABSTRACT) != 0 ? 1 : 0;
        final int isStrict = (accessFlags & AccessFlag.STRICT) != 0 ? 1 : 0;
        final int isSynthetic = (accessFlags & AccessFlag.SYNTHETIC) != 0 ? 1 : 0;
        final int isAnnotation = (accessFlags & AccessFlag.ANNOTATION) != 0 ? 1 : 0;
        final int isEnum = (accessFlags & AccessFlag.ENUM) != 0 ? 1 : 0;
        final int isMandated = (accessFlags & AccessFlag.MANDATED) != 0 ? 1 : 0;

        System.err.println(String.format("flags: isPublic:%d isPrivate:%d "
                        + "isProtected:%d isStatic:%d isFinal:%d isSynchronized:%d isVolatileOrBridge:%d "
                        + "isTransientOrVarArgs:%d isNative:%d isInterface:%d isAbstract:%d isStrict:%d isSynthetic:%d "
                        + "isAnnotation:%d isEnum:%d isMandated:%d",
                isPublic, isPrivate,
                isProtected, isStatic, isFinal, isSynchronized, isVolatileOrBridge,
                isTransientOrVarArgs, isNative, isInterface, isAbstract, isStrict, isSynthetic,
                isAnnotation, isEnum, isMandated));

        if (methodInfo.isStaticInitializer()) {
            return false;
        }

        // TODO: 不懂为啥SYNTHETIC还要判断是不是private
        if ((accessFlags & AccessFlag.SYNTHETIC) != 0 && !AccessFlag.isPrivate(accessFlags)) {
            return false;
        }

        if (methodInfo.isConstructor()) {
            return true;
        }

        if ((accessFlags & AccessFlag.ABSTRACT) != 0) {
            return false;
        }
        if ((accessFlags & AccessFlag.NATIVE) != 0) {
            return false;
        }
        if ((accessFlags & AccessFlag.INTERFACE) != 0) {
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
     * 根据传入类型来生成返回语句
     *
     * @param type 返回类型
     * @return 返回return语句
     */
    private static String getReturnStatement(String type) {
        switch (type) {
            case Constants.CONSTRUCTOR: // TODO
                return "return;";
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
                // array or class name, etc..
                return "return ((" + type + ")proxyResult.returnValue);";
        }
    }
}

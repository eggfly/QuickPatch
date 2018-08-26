package quickpatch.gradleplugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AccessFlag;
import javassist.bytecode.MethodInfo;

public class CodePatcher {
    public static void insertCode(List<CtClass> box, File jarFile) throws IOException, CannotCompileException {
        ZipOutputStream outStream = new JarOutputStream(new FileOutputStream(jarFile));
        for (CtClass ctClass : box) {
            // TODO: isNeedInsertClass在没有任何函数的类里 就跳过了
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

                // add static field for every class
                try {
                    ctClass.getDeclaredField(Constants.STATIC_QPATCH_STUB_FIELD_NAME);
                } catch (NotFoundException e) {
                    ClassPool classPool = ctClass.getClassPool();
                    CtClass type = classPool.getOrNull(Constants.STATIC_QPATCH_STUB_TYPE_NAME);
                    CtField ctField = new CtField(type, Constants.STATIC_QPATCH_STUB_FIELD_NAME, ctClass);
                    ctField.setModifiers(AccessFlag.PUBLIC | AccessFlag.STATIC);
                    ctClass.addField(ctField);
                    System.err.println("added static field");
                }

                // add a instance field: Object[] _QFieldStub for every class
                try {
                    ctClass.getDeclaredField(Constants.QPATCH_OBJECT_ARRAY_STUB_FIELD_NAME);
                } catch (NotFoundException e) {
                    ClassPool classPool = ctClass.getClassPool();
                    CtClass type = classPool.getOrNull("java.lang.Object[]");
                    CtField ctField = new CtField(type, Constants.QPATCH_OBJECT_ARRAY_STUB_FIELD_NAME, ctClass);
                    ctField.setModifiers(AccessFlag.PUBLIC);
                    ctClass.addField(ctField);
                    System.err.println("added non-static field");
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
                            String body = "if (_QPatchStub != null) {\n";
                            body += "  Object argThis = null;\n";
                            if (!isStatic) {
                                body += "  argThis = $0;\n";
                            }
                            String methodName = isConstructor ? "__init__" : ctBehavior.getName();
                            body += String.format("  final quickpatch.sdk.MethodProxyResult proxyResult = _QPatchStub.proxy(argThis, \"%s\", \"%s\", $args); \n",
                                    methodName, ctBehavior.getSignature());
                            body += "  if (proxyResult.isPatched) {\n    " + getReturnStatement(returnTypeString) + "\n  }\n";
                            body += "}";
                            System.out.println("Patching method: " + methodName
                                    + ", signature: " + ctBehavior.getSignature()
                                    + ", genericSignature: " + ctBehavior.getGenericSignature()
                                    + ", returnType: " + returnTypeString);
                            if (ctBehavior.getGenericSignature() != null) {
                                System.out.println("FOUND GENERIC SIGNATURE: " + ctBehavior.getGenericSignature());
                            }
                            if (isConstructor) {
                                CtConstructor constructor = (CtConstructor) ctBehavior;
                                // insertBeforeBody 所以构造函数插桩不支持替换第一行的super或者this
                                // TODO: 构造函数插桩是否必要?
                                // TODO: 构造函数的名字没写成<init> 有问题..
                                constructor.insertBeforeBody(body);
                            } else {
                                ctBehavior.insertBefore(body);
                            }
                            System.out.println("Patching with code body: \n" + body);
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
        // 在需要埋点的剔除指定的类
        if (className.endsWith(Constants.QPATCH_CLASS_SUFFIX)) {
            System.out.println("skip patch class: " + className);
            return false;
        }
        if (className.startsWith("quickpatch.sdk")) {
            System.out.println("skip sdk class: " + className);
            return false;
        }
//        for (String exceptName : exceptPackageList) {
//            if (class Name.startsWith(exceptName)) {
//                return false;
//            }
//        }
        for (String name : Constants.sHotFixPackageList) {
            if (className.startsWith(name)) {
                return true;
            }
        }
        // TODO: 需要改成其他类库默认不插桩
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

        // 为何SYNTHETIC还要判断是不是private，SYNTHETIC的情况有哪些？
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
            case Constants.CONSTRUCTOR:
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

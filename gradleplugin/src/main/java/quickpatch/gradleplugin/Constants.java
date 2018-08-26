package quickpatch.gradleplugin;

import java.util.ArrayList;

public class Constants {
    @SuppressWarnings("SpellCheckingInspection")
    public static final String STATIC_QPATCH_STUB_TYPE_NAME = "quickpatch.sdk.QuickPatchStub";
    @SuppressWarnings("SpellCheckingInspection")
    public static final String STATIC_QPATCH_STUB_FIELD_NAME = "_QPatchStub";
    public static final String QPATCH_CLASS_SUFFIX = "_QPatch";

//    static {
//        RFileClassSet.add("R$array");
//        RFileClassSet.add("R$xml");
//        RFileClassSet.add("R$styleable");
//        RFileClassSet.add("R$style");
//        RFileClassSet.add("R$string");
//        RFileClassSet.add("R$raw");
//        RFileClassSet.add("R$menu");
//        RFileClassSet.add("R$layout");
//        RFileClassSet.add("R$integer");
//        RFileClassSet.add("R$id");
//        RFileClassSet.add("R$drawable");
//        RFileClassSet.add("R$dimen");
//        RFileClassSet.add("R$color");
//        RFileClassSet.add("R$bool");
//        RFileClassSet.add("R$attr");
//        RFileClassSet.add("R$anim");
//    }

    public static ArrayList<String> sHotFixPackageList = new ArrayList<>();

    static {
        sHotFixPackageList.add("quickpatch.example");
    }

    public static final String CONSTRUCTOR = "Constructor";
    public static final String LANG_VOID = "java.lang.Void";
    public static final String VOID = "void";
    public static final String LANG_BOOLEAN = "java.lang.Boolean";
    public static final String BOOLEAN = "boolean";
    public static final String LANG_INT = "java.lang.Integer";
    public static final String INT = "int";
    public static final String LANG_LONG = "java.lang.Long";
    public static final String LONG = "long";
    public static final String LANG_DOUBLE = "java.lang.Double";
    public static final String DOUBLE = "double";
    public static final String LANG_FLOAT = "java.lang.Float";
    public static final String FLOAT = "float";
    public static final String LANG_SHORT = "java.lang.Short";
    public static final String SHORT = "short";
    public static final String LANG_BYTE = "java.lang.Byte";
    public static final String BYTE = "byte";
    public static final String LANG_CHARACTER = "java.lang.Character";
    public static final String CHAR = "char";
}

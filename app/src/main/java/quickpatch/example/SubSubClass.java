package quickpatch.example;

public class SubSubClass extends SubClass {
    public Object[] _STUB;

    public void foo() {
        super.foo();
        System.out.println("SubSubClass.foo() called");
    }
}

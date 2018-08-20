package quickpatch.example;

public class SubSubClass extends SubClass {
    public void foo() {
        super.foo();
        System.out.println("SubSubClass.foo() called");
    }
}

package pkg001;

public class ClassA {

   ClassB classB = new ClassB();

   void foo() {
      m1();
   }

   void bar() {
      m2();
   }

   void m1() {
      m2();
   }

   void m2() {
      m3();
   }

   void m3() {
      classB.m4();
   }
}

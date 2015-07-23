package test;

public class Main {

	static void p() {
		System.out.println();
	}

	static void p(String msg) {
		System.out.println(msg);
	}

	public static void main(String args[]) {
		new A();
		p();
		B.privateStaticMethod(42);
		B b = new B();
		b.directMethod();
		b.virtualMethod("data");
		p();
		new Derived().method();
		p();
		new C().print();
		p();
		new D().print();
		p();
		new E("data").print();
		p();
		new Concrete1().interfaceMethod();
		p();
		new Concrete2().interfaceMethod();
		p();
		new Concrete3().interfaceMethod();
	}

	public static class A {
		static { p("original A::<clinit>"); }
		public A() { p("original A::<init>"); }
	}

	public static class B {
		@SuppressWarnings("unused")
		private String privateKey = "my-key";
		static private void privateStaticMethod(int i) { p("B::staticMethod: " + i); }
		private void directMethod() { p("original B::directMethod"); }
		public void virtualMethod(String data) { p("original B::virtualMethod: " + data); }
	}

	public static class Base {
		public void method() { p("original Base::method"); }
	}

	public static class Derived extends Base{
		@Override
		public void method() {
			p("entering original Derived::method");
			super.method();
			p("exiting original Derived::method");
		}
	}

	public static class C {
		private static int sourceStaticField = 100;
		private int sourceField = 100;
		private static int redefinedSourceStaticField = 100;
		private int redefinedSourceField = 100;
		static { p("original C::<clinit>"); }
		public C() { p("original C::<init>"); }
		public void print() {
			p("C::sourceStaticField: " + sourceStaticField);
			p("C::sourceField: " + sourceField);
			p("C::redefinedSourceStaticField: " + redefinedSourceStaticField);
			p("C::redefinedSourceField: " + redefinedSourceField);
		}
	}

	public static class D {
		private static int sourceStaticField = 100;
		private int sourceField = 100;
		private static int redefinedSourceStaticField = 100;
		private int redefinedSourceField = 100;
		static { p("original D::<clinit>"); }
		public D() { p("original D::<init>"); }
		public void print() {
			p("D::sourceStaticField: " + sourceStaticField);
			p("D::sourceField: " + sourceField);
			p("D::redefinedSourceStaticField: " + redefinedSourceStaticField);
			p("D::redefinedSourceField: " + redefinedSourceField);
		}
	}

	public static class E {
		private static int sourceStaticField = 100;
		private int sourceField = 100;
		private static int redefinedSourceStaticField = 100;
		private int redefinedSourceField = 100;
		static { p("original E::<clinit>"); }
		public E(String data) { p("original E::<init>: " + data); }
		public void print() {
			p("E::sourceStaticField: " + sourceStaticField);
			p("E::sourceField: " + sourceField);
			p("E::redefinedSourceStaticField: " + redefinedSourceStaticField);
			p("E::redefinedSourceField: " + redefinedSourceField);
		}
	}

	public interface Interface {
		public void interfaceMethod();
	}

	public static abstract class Abstract {
		public abstract void abstractMethod();
	}

	public static class Concrete1 extends Abstract implements Interface {
		@Override
		public void interfaceMethod() {
			p("Concrete1::interfaceMethod");
			abstractMethod();
		}
		@Override
		public void abstractMethod() {
			p("Concrete1::abstractMethod");
			method();
		}
		public void method() { p("original Concrete1::method"); }
	}

	public static class Concrete2 extends Abstract implements Interface {
		@Override
		public void interfaceMethod() {
			p("Concrete2::interfaceMethod");
			abstractMethod();
		}
		@Override
		public void abstractMethod() {
			p("Concrete2::abstractMethod");
			method();
		}
		public void method() { p("original Concrete2::method"); }
	}

	public static class Concrete3 extends Abstract implements Interface {
		@Override
		public void interfaceMethod() {
			p("Concrete3::interfaceMethod");
			abstractMethod();
		}
		@Override
		public void abstractMethod() {
			p("Concrete3::abstractMethod");
			method();
		}
		public void method() { p("original Concrete3::method"); }
	}

}

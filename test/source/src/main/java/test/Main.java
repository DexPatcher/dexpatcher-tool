/*
 * DexPatcher - Copyright 2015-2017 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

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
		new B().print();
		p();
		new Derived().method();
		p();
		new C().print();
		p();
		new D().print();
		p();
		new E("data").print();
		p();
		new F().print();
		p();
		new G().print();
		p();
		new H().print();
		p();
		new Concrete1().interfaceMethod();
		p();
		new Concrete2().interfaceMethod();
		p();
		new Concrete3().interfaceMethod();
		p();
		CrossClassAHelper.print();
		p();
		CrossClassBHelper.print();
		p();
		new CrossClassC();
	}

	public static class A {
		static { p("original A::<clinit>"); }
		public A() { p("original A::<init>"); }
	}

	public static class B {
		static private void privateStaticMethod(int i) {
			try { p("B::privateStaticMethod: " + i + " (" + B.class.getDeclaredMethod("privateStaticMethod", int.class) + ")"); }
			catch (NoSuchMethodException e) { throw new RuntimeException(e); }
		}
		private String privateKey = "my-key";
		private void directMethod() { p("original B::directMethod"); }
		public void virtualMethod(String data) { p("original B::virtualMethod: " + data); }
		public void wrapTestMethod(String data) { p("original B::wrapTestMethod: " + data); }
		public void prependTestMethod(String data) { p("original B::prependTestMethod: " + data); }
		public void appendTestMethod(String data) { p("original B::appendTestMethod: " + data); }
		public void print() {
			privateStaticMethod(42);
			try { p("B::privateKey: " + privateKey + " (" + this.getClass().getDeclaredField("privateKey") + ")"); }
			catch (NoSuchFieldException e) { throw new RuntimeException(e); }
			directMethod();
			virtualMethod("data");
			wrapTestMethod("data");
			prependTestMethod("data");
			appendTestMethod("data");
		}
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

	public static class F {
		private static int sourceStaticField = 100;
		private int sourceField = 100;
		private static int redefinedSourceStaticField = 100;
		private int redefinedSourceField = 100;
		static { p("original F::<clinit>"); }
		public F() { p("original F::<init>"); }
		public void print() {
			p("F::sourceStaticField: " + sourceStaticField);
			p("F::sourceField: " + sourceField);
			p("F::redefinedSourceStaticField: " + redefinedSourceStaticField);
			p("F::redefinedSourceField: " + redefinedSourceField);
		}
	}

	public static class G {
		private static int sourceStaticField = 100;
		private static int redefinedSourceStaticField = 100;
		static { p("original G::<clinit>"); }
		public G() { p("original G::<init>"); }
		public void print() {
			p("G::sourceStaticField: " + sourceStaticField);
			p("G::redefinedSourceStaticField: " + redefinedSourceStaticField);
		}
	}

	public static class H {
		public H() { p("original H::<init>"); }
		public void print() {}
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

	public static class CrossClassA {
		private String data;
		public CrossClassA(String data) { this.data = data; }
		public void go() {
			p("original CrossClassA::go: " + data + " (" + this.getClass() + ")");
			help1(this);
			CrossClassAHelper.help2(this);
		}
		public static void help1(CrossClassA a) { p("original CrossClassA::help1 (" + a.getClass() + ")"); }
	}
	public static class CrossClassAHelper {
		public static void print() { new CrossClassA("data").go(); }
		public static void help2(CrossClassA a) { p("original CrossClassAHelper::help2(CrossClassA)"); }
	}

	public static class CrossClassB {
		private String data;
		public CrossClassB(String data) { this.data = data; }
		public void go() { p("original CrossClassB::go: " + data + " (" + this.getClass() + ")"); }
	}
	public static class CrossClassBHelper {
		public static void print() { new CrossClassB("data").go(); }
	}

	public static class CrossClassC {
		static { p("original CrossClassC::<clinit>"); }
		public CrossClassC() { p("original CrossClassC::<init>"); }
	}

}

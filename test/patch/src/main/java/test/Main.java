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

import lanchon.dexpatcher.annotation.*;

// DexPatcher Sample

// A DexPatcher patch is an android dex or apk file that defines modifications
// to be applied to a source dex or apk file, a.k.a. the original file. A patch
// file must reference and include the 'dexpatcher-annotation-NNN.jar' library
// that contains the definitions of the DexPatcher annotations and tags.

// The DexPatcher annotations can be used to tag packages, classes, fields and
// methods in the patch file. The available annotations are:
//   @DexAdd: add this patch item to the source.
//   @DexEdit: modify the targeted item in the source as described by this item.
//   @DexReplace: replace the targeted item in the source with this item.
//   @DexRemove: remove the targeted item from the source.
//   @DexIgnore: ignore this patch item; do nothing.

// Untagged classes in the patch are added by default to the source, allowing
// off-the-shelf Java sources and libs to be included in the patch.

// Ignore the 'Main' class in the patch:
// Note: Classes at the JVM or DalvikVM level are never nested. Nested/inner
// classes in Java are really just syntactic sugar understood and handled by the
// compiler, and converted to completely independent bytecode classes. At the
// bytecode level, only fields and methods can be members of classes. Given that
// we do not want to modify any of the members of the 'Main' class (we only want
// to modify its nested classes) we can simply ignore the class.
@DexIgnore
public class Main {

	// We declare this method here because we want to use it from the patch
	// code but the real method invoked at runtime will be the one in the source.
	// Class 'Main' in the patch and all its bytecode members will be ignored.
	static void p(String msg) {}

	// Completely replace class 'A' with a new class:
	// The targeted item must be a class and can be defined in various ways:
	//@DexReplace(target = "A")			// using the nested class name
	//@DexReplace(target = "Main$A")	// using the full bytecode class name
	//@DexReplace(target = "test.Main$A")	// using the fully qualified name
	//@DexReplace(target = "Ltest/Main$A;")	// using the type descriptor
	//@DexReplace(targetClass = A.class)	// using a class reference
	@DexReplace		// or implicitly, given that the names match
	public static class A {
		static { p("replaced A::<clinit>"); }
		public A() { p("replaced A::<init>"); }
	}

	// Modify members of class 'B':
	// Note: By default untagged members of a @DexEdit-tagged patch class will
	// trigger an error. The default behavior can be changed by adding a
	// 'defaultAction' element to the @DexEdit tag.
	@DexEdit(staticConstructorAction = DexAction.ADD)
	public static class B {

		// Add a static field:
		// Note: Do not use an initializer unless you are also modifying the
		// class constructor, as the initializer could be embedded there. In
		// this case, this static field can trigger the generation of a static
		// constructor by the compiler, and then it would be added to the
		// source because of the 'staticConstructorAction' setting in @DexEdit.
		// Or else, the compiler can embed the initial value in the field
		// declaration itself, which would also be added to the source as part
		// of the field, again due to the 'staticConstructorAction' setting.
		// Either way the field will be properly initialized.
		@DexAdd
		static int staticField = 100;

		// Make sure we have a static constructor no matter what compiler we
		// use, otherwise an error would be triggered if no static constructor
		// is found and 'staticConstructorAction' is defined.
		static {}

		// Add an instance field:
		// Note: Do not use an initializer unless you are also modifying all
		// the class constructors; the instance initializers are embedded in
		// every constructor. In this case we are not, so the field will *not*
		// be properly initialized.
		@DexAdd
		int instanceField = 100;

		// Ignore the constructor of 'B' present in the patch:
		// Note: The compiler would generate a default constructor if none is
		// defined, and that would trigger an error due to 'defaultAction'
		// being undefined in @DexEdit. Define a constructor and explicitly
		// ignore it to avoid the problem.
		@DexIgnore
		private B() {}

		// Add a method:
		@DexAdd
		public void addedMethod() { p("B::addedMethod"); }

		// Increase visibility of a private field:
		// Note: Instance field initializers are embedded in every constructor.
		@DexEdit
		public String privateKey;

		// Increase visibility of a private method:
		// Note: The actual method code in the source is not modified if you
		// use @DexEdit; only the prototype is.
		@DexEdit
		static public void privateStaticMethod(int i) { p("THIS CODE IS IGNORED!"); }

		// Replace an instance direct (ie: non-virtual) method:
		@DexReplace
		private void directMethod() {
			p("replaced B::directMethod");
			p("B::staticField: " + staticField + "   <-- initialized");
			p("B::instanceField: " + instanceField + "   <-- *not* initialized");
			addedMethod();
			p("B::privateKey: " + privateKey);
		}

		// Replace a method, invoking the replaced method at will:
		// Part 1: Rename the target method, optionally reducing visibility:
		@DexEdit(target = "virtualMethod")
		protected void source_virtualMethod(String data) {}
		// Part 2: Add a new method:
		// Note: Cannot replace method here, items can be targeted only once.
		@DexAdd
		public void virtualMethod(String data) {
			p("entering replaced B::virtualMethod: " + data);
			String filteredData = "filtered " + data;
			source_virtualMethod(filteredData);
			p("exiting replaced B::virtualMethod");
		}

	}

	// Declare class 'Base':
	@DexIgnore
	public static class Base {}

	// Modify members of class 'Derived':
	// Note: The generated default constructor is implicitly ignored in this
	// case by specifying a default action.
	@DexEdit(defaultAction = DexAction.IGNORE)
	public static class Derived extends Base {

		// Replace a method that overrides and invokes a base method via super:
		// Note: The renamed source_method correctly invokes Base::method via
		// super (not Base::source_method).
		@DexEdit(target = "method")
		protected void source_method() {}
		@DexAdd
		public void method() {
			p("entering replaced Derived::method");
			source_method();
			p("exiting replaced Derived::method");
		}

	}

	// Modify members of class 'C' keeping its original static constructor:
	@DexEdit(staticConstructorAction = DexAction.IGNORE)
	public static class C {

		@DexEdit
		private static int redefinedSourceStaticField = 200;
		@DexEdit
		private int redefinedSourceField = 200;
		@DexAdd
		private static int patchStaticField = 200;
		@DexAdd
		private int patchField = 200;

		// Ignore the static constructor:
		// Note: Ignoring the static constructor of a class is dangerous.
		// Class initialization, possibly including some static field
		// initializers, will be skipped. The compiler might synthesize
		// static fields that require initialization.
		static {
			// This static block is part of the static constructor that
			// initializes all static fields declared in the patch.
			// The static constructor will not execute.
			p("THIS CODE IS IGNORED!");
		}

		// Ignore the constructor:
		@DexIgnore
		public C() {
			// This constructor that initializes all instance fields declared
			// in the patch will not execute.
			p("THIS CODE IS IGNORED!");
		}

		@DexEdit(target = "print")
		protected void source_print() {}
		@DexAdd
		public void print() {
			source_print();
			p("C::patchStaticField: " + patchStaticField);
			p("C::patchField: " + patchField);
		}

	}

	// Modify members of class 'D' replacing its static constructor:
	// Note: There is no way to annotate static initializers in Java. This
	// is why the action to apply to the static constructor of the class
	// must be specified in the enclosing @DexEdit tag.
	@DexEdit(staticConstructorAction = DexAction.REPLACE)
	public static class D {

		@DexEdit
		private static int redefinedSourceStaticField = 200;
		@DexEdit
		private int redefinedSourceField = 200;
		@DexAdd
		private static int patchStaticField = 200;
		@DexAdd
		private int patchField = 200;

		// Replace the static constructor:
		// Note: Replacing the static constructor of a class is dangerous.
		// Class initialization, possibly including some static field
		// initializers, will be skipped. The compiler might synthesize
		// static fields that require initialization.
		static {
			// This static constructor will initialize all static fields
			// declared in the patch. The source static fields will *not* be
			// initialized.
			p("replaced D::<clinit>");
		}

		// Replace a constructor:
		// Note: Replacing a constructor can have unexpected side effects.
		// The patch constructor will not initialize instance fields of the
		// source class unless they are also declared and initialized in the
		// patch class.
		@DexReplace
		public D() {
			// Because this constructor does not invoke another via 'this()'
			// it will initialize all instance fields declared in the patch.
			// The source instance fields will *not* be initialized.
			p("replaced D::<init>");
		}

		@DexEdit(target = "print")
		protected void source_print() {}
		@DexAdd
		public void print() {
			source_print();
			p("D::patchStaticField: " + patchStaticField);
			p("D::patchField: " + patchField);
		}

	}

	// Modify members of class 'E'
	// Note: The default value of 'staticConstructorAction' is the value of
	// 'defaultAction' if one is defined. Otherwise it is undefined.
	@DexEdit(defaultAction = DexAction.ADD)
	public static class E {

		@DexEdit
		private static int redefinedSourceStaticField = 200;
		@DexEdit
		private int redefinedSourceField = 200;
		@DexAdd
		private static int patchStaticField = 200;
		@DexAdd
		private int patchField = 200;

		// Replace the static constructor, invoking the replaced item at will:
		// Part 1: Rename the source static constructor:
		@DexEdit(target = DexTarget.STATIC_CONSTRUCTOR)
		private static void source_static() {}
		// Part 2: Add a new static constructor:
		// Note: Cannot replace item here, items can be targeted only once.
		// The static constructor action in this case is ADD by default.
		static {
			// When this static block is invoked, the patch static fields have
			// already been initialized. Manually invoking the source static
			// constructor initializes the source static fields. The static
			// fields that are declared on both classes are initialized twice,
			// and end up with the vaules set later by the source.
			p("entering replaced E::<clinit>");
			source_static();
			p("exiting replaced E::<clinit>");
		}

		// Extend a constructor:
		// Part 1: Change the prototype of the source constructor:
		// Note: Constructors do not have names in Java, so you cannot rename
		// the source constructor. Instead you can add a tag argument to
		// distinguish the original and replacing constructors via overloading.
		// The argument can be of any type and must be tagged with @DexIgnore.
		// When you invoke the original constructor, the value of the extra
		// argument is ignored.
		// Note: DexPatcher 1.0 used the DexTag type to tag constructor
		// arguments. This has been deprecated but a backwards compatible mode
		// can be enabled in newer DexPatcher versions. See older revisions
		// of this file for more details on DexTag.
		@DexEdit
		private E(String data, @DexIgnore Void tag) {}
		// Part 2: Add a new constructor that invokes the source constructor:
		// Note: Because this constructor invokes another constructor via the
		// 'this()' syntax, this constructor does *not* initialize instance
		// fields (they should be initialized during the nested invocation).
		// This means that instance fields added in the patch will *not* be
		// initialized by this constructor.
		@DexAdd
		public E(String data) {
			// Because this constructor invokes another via 'this()', it will
			// not initialize any instance fields. The fields are assumed to
			// be initialized by the invoked constructor. In this case, the
			// invoked constructor will initialize all source instance fields.
			this("filtered " + data, (Void) null);
			p("continuing on replaced E::<init>: " + data);
		}

		@DexEdit(target = "print")
		protected void source_print() {}
		@DexAdd
		public void print() {
			source_print();
			p("E::patchStaticField: " + patchStaticField);
			p("E::patchField: " + patchField);
		}

	}

	// Note: In a proper setup, types like Interface and Abstract would be
	// imported form the source and be available to the patch at compile time.
	// But given that this is a simple test mock up we just redefine them here
	// instead of importing them.
	@DexIgnore
	public interface Interface {
		public void interfaceMethod();
	}
	@DexIgnore
	public static abstract class Abstract {
		public abstract void abstractMethod();
	}

	// Modify a concrete class that must implement abstract methods:
	// Option 1: The verbose and brittle way:
	// Note: By default @DexEdit redefines the class so you can do stuff like
	// adding implemented interfaces or changing the base class. But usually
	// you do not want to do any such change, and this sometimes forces you to
	// implement many abstract methods that do not interest you just to make
	// the patch class compile, as in this example.
	@DexEdit
	public static class Concrete1 extends Abstract implements Interface {
		@DexIgnore
		private Concrete1() {}
		@DexReplace
		public void method() {
			p("replaced Concrete1::method");
		}
		// We only need this abstract method implemented here so that the
		// concrete class compiles.
		@DexIgnore
		@Override
		public void interfaceMethod() {}
		// Same case here. We can avoid defining a method body by declaring
		// the method native, but it is still verbose and annoying.
		// Note: It is not recommended to use 'native' as an alternative to
		// defining a method body that will be ignored by DexPatcher for two
		// reasons:
		// 1) It cannot be used in constructors.
		// 2) The presence of a native method disables IDE warnings that
		//    depend on class-global analysis (eg: 'unused field' warning).
		@DexIgnore
		@Override
		public native void abstractMethod();
	}
	// Option 2: Declaring the class 'abstract':
	// Note: You can make @DexEdit not redefine the target class by setting
	// its 'onlyEditMembers' element to true. This gives you several options
	// for modifying the class so that it compiles without its abstract
	// methods being implemented, such as declaring the class 'abstract'.
	// Note that you cannot use the 'abstract' trick if you need to
	// instantiate the class in the patch code.
	@DexEdit(onlyEditMembers = true)
	public static abstract class Concrete2 extends Abstract implements Interface {
		@DexIgnore
		private Concrete2() {}
		@DexReplace
		public void method() {
			p("replaced Concrete2::method");
		}
	}
	// Option 3: Stripping out the hierarchy:
	// Note: You can also strip out interfaces or even the base from the
	// class, but this has complex implications for the type system and the
	// constructors of the class.
	// Note: It is not recommended to strip out the hierarchy because it
	// cripples IDE's understanding of the code and disables features such
	// as auto-completion.
	@DexEdit(onlyEditMembers = true)
	public static class Concrete3 {
		@DexIgnore
		private Concrete3() {}
		@DexReplace
		public void method() {
			p("replaced Concrete3::method");
		}
	}

	// Mini FAQ

	// Q) My IDE outputs classes that clash with classes in my source app
	// and I cannot tag them. What do I do?
	//
	// A)
	//     @DexRemove(targetClass = com.example.R.class)
	//     class Remove_R {}

	// Q) I would like to update a library that is bundled in the source app
	// because I want a bugfix or I need a newer version for the patch code.
	// How can I do it?
	//
	// A)
	//     @DexRemove(recursive = true)
	//     package com.acme.util;
	//
	// The optional 'recursive' element also causes removal of subpackages.
	// Or you can use @DexReplace instead to add custom package annotations:
	//
	//     @Comment("this annotation is included in the patched app")
	//     @DexReplace(recursive = true)    // <-- but not this one
	//     package com.acme.util;
	//
	// If you are dropping in an off-the-shelf replacement library and it
	// already includes its own package annotations, you can do this:
	//
	//     @DexRemove(target = "com.acme.util", recursive = true)
	//     package remove_com_acme_util;

}

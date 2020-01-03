/*
 * DexPatcher - Copyright 2015-2020 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package test;

import lanchon.dexpatcher.annotation.*;

// DexPatcher Tool Sample

// A DexPatcher patch is an android dex or apk file that defines modifications
// to be applied to a source dex or apk file, a.k.a. the original file. A patch
// file must reference the 'dexpatcher-annotation-<version>.jar' library that
// contains the definitions of the DexPatcher annotations and constants.

// The DexPatcher annotations can be used to tag packages, classes, fields and
// methods in the patch file. The available annotations are:
//   @DexAdd: add this patch item to the source.
//   @DexEdit: modify the targeted item in the source as described by this item.
//   @DexReplace: replace the targeted item in the source with this item.
//   @DexRemove: remove the targeted item from the source.
//   @DexIgnore: ignore this patch item; do nothing.
// DexPatcher tool v1.3.0 adds:
//   @DexWrap: replace the targeted method, invoking the original at will.
// DexPatcher tool v1.4.0 adds:
//   @DexPrepend: prepend code to the targeted method.
//   @DexAppend: append code to the targeted method.

// Untagged classes in the patch are added by default to the source, allowing
// off-the-shelf Java sources and libs to be included in the patch.

// This file is the main piece of the DexPatcher tool test suite, and it also
// doubles as documentation for the tool. What follows are patching cases and
// their explanation. Please note that main intent of these cases is exercising
// the tool rather than showcasing good patch design patterns.

// Ignore the 'Main' class in the patch:
// Note: Classes at the JVM or DalvikVM level are never nested. Nested/inner
// classes in Java are really just syntactic sugar understood and handled by the
// compiler, and converted to completely independent bytecode classes. At the
// bytecode level, only fields and methods can be members of classes. Given that
// we do not want to modify any of the members of the 'Main' class (we only want
// to modify its nested classes) we can simply ignore the class.
@DexIgnore
public class Main {

	// We declare these methods because here in the patch we want to use the
	// methods in the source that match these signatures, so the compiler needs
	// these symbols to be present while building the patch. But the real
	// methods invoked at runtime will be the ones in the source. Class 'Main'
	// in the patch and all its bytecode members will be ignored: they will not
	// be included in the resulting patched code and will be discarded.
	// Note: The recommended way to populate unused method bodies in the patch
	// is with a single 'throw null;' statement.
	static void p() { throw null; }
	static void p(String message) { throw null; }
	static void pClass(String message) { throw null; }
	static void pMethod(String message) { throw null; }

	// Completely replace class 'A' with a new class:
	// The targeted item must be a class and can be defined in various ways:
	//@DexReplace(target = "A")			// using the nested class name
	//@DexReplace(target = "Main$A")	// using the full bytecode class name
	//@DexReplace(target = "test.Main$A")	// using the fully qualified name
	//@DexReplace(target = ".test.Main$A")	// using the explicit FQN
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
	// 'defaultAction' element to the @DexEdit tag. The use of 'defaultAction'
	// is strongly discouraged except in cases where there is no other way to
	// tag compiler-generated members. Explicitly tagging all patch members
	// is safer than relying on default actions.
	// Note: There is no way to annotate static initializers in Java. This
	// is why the action to apply to the static constructor of the class
	// must be specified in the enclosing @DexEdit tag.
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

		// Increase visibility of a private static method:
		// Note: The actual method code in the source is not modified if you
		// use @DexEdit; only the prototype is. The unused body of this patch
		// method would typically consist of a 'throw null;' statement.
		@DexEdit
		static public void privateStaticMethod(int i) { p("THIS CODE IS IGNORED!"); }

		// Add an instance field:
		// Note: Do not use an initializer unless you are also modifying all
		// the class constructors; the instance initializers are embedded in
		// every constructor. In this case we are not, so the field will *not*
		// be properly initialized.
		@DexAdd
		int instanceField = 100;

		// Increase visibility of a private field:
		// Note: Instance field initializers are embedded in every constructor.
		// The constructor in the source still initializes this field properly.
		@DexEdit
		public String privateKey;

		// Ignore the constructor of 'B' present in the patch:
		// Note: The compiler would generate a default constructor if none is
		// defined, and that would trigger an error due to 'defaultAction'
		// being undefined in @DexEdit. Define a constructor and explicitly
		// ignore it to avoid the problem.
		// Note: DexPatcher tool v1.3.1 introduces the ability to implicitly
		// ignore trivial default constructors for which no action is defined.
		// In this context trivial means that the constructor bears no code
		// except for a call to the default constructor of the superclass.
		// This capability automatically takes care of most compiler-generated
		// default constructors. However, an automatic default constructor
		// for this class would include code to initialize 'instanceField',
		// making it non-trivial. Thus, either a constructor with an explicit
		// action or a default action for the class must be defined.
		@DexIgnore
		private B() { throw null; }

		// Add a method:
		@DexAdd
		public void addedMethod() { p("B::addedMethod"); }

		// Replace an instance direct (ie: non-virtual) method:
		@DexReplace
		private void directMethod() {
			p("replaced B::directMethod");
			p("B::staticField: " + staticField + "   <-- initialized");
			p("B::instanceField: " + instanceField + "   <-- *not* initialized");
			addedMethod();
		}

		// Replace a method, invoking the replaced method at will:
		// Part 1: Rename the target method, optionally reducing visibility:
		@DexEdit(target = "virtualMethod")
		private void source_virtualMethod(String data) { throw null; }
		// Part 2: Add a new method:
		// Note: Cannot replace method here, items can be targeted only once.
		@DexAdd
		public void virtualMethod(String data) {
			p("entering replaced B::virtualMethod: " + data);
			String filteredData = "filtered " + data;
			source_virtualMethod(filteredData);
			p("exiting replaced B::virtualMethod");
		}

		// Replace a method, invoking the replaced method at will:
		// Note: The verbose @DexEdit/@DexAdd idiom presented above is commonly
		// used to modify methods. DexPatcher tool v1.3.0 introduces @DexWrap,
		// a more concise alternative to accomplish the same thing. @DexWrap
		// converts recursive calls in the patch to invocations of the original
		// source method. (Recursion is not supported; use the @DexEdit/@DexAdd
		// idiom if recursion is needed.)
		// Note: @DexWrap cannot be used on instance or static constructors,
		// as instance constructors cannot recursively call themselves using
		// the 'this(...)' syntax and static constructors can never be
		// explicitly invoked.
		@DexWrap
		public void wrapTestMethod(String data) {
			p("entering wrapper B::wrapTestMethod: " + data);
			String filteredData = "filtered " + data;
			wrapTestMethod(filteredData);    // invokes the replaced method
			p("exiting wrapper B::wrapTestMethod");
		}

		// Prepend code to the existing code of a method:
		// Note: DexPatcher tool v1.4.0 introduces @DexPrepend and @DexAppend,
		// which are tags that add code to existing methods. These tags can
		// only be applied to methods that return void. They are a convenient
		// shorthand when applicable, but the rationale for their existence is
		// improving the handling of static constructors (on which the use of
		// @DexWrap is not allowed).
		// Note: In their present form, @DexPrepend and @DexAppend cannot be
		// applied to instance constructors. This limitation might be partially
		// relaxed in the future.
		@DexPrepend
		public void prependTestMethod(String data) {
			p("prepended B::prependTestMethod: " + data);
		}

		// Append code to the existing code of a method:
		@DexAppend
		public void appendTestMethod(String data) {
			p("appended B::appendTestMethod: " + data);
		}

	}

	// Declare class 'Base':
	@DexIgnore
	public static class Base {}

	// Modify members of class 'Derived':
	// Note: The generated default constructor of this class is ignored due to
	// the specified default action. Keep in mind that defining default actions
	// is not recommended.
	@DexEdit(defaultAction = DexAction.IGNORE)
	public static class Derived extends Base {

		// Replace a method that overrides and invokes a base method via super:
		// Note: The renamed source_method correctly invokes Base::method via
		// super (not Base::source_method).
		// Note: The less verbose @DexWrap could have been used here instead.
		@DexEdit(target = "method")
		private void source_method() { throw null; }
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

		@DexAppend
		public void print() {
			p("C::patchStaticField: " + patchStaticField);
			p("C::patchField: " + patchField);
		}

	}

	// Modify members of class 'D' replacing its static constructor:
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

		@DexAppend
		public void print() {
			p("D::patchStaticField: " + patchStaticField);
			p("D::patchField: " + patchField);
		}

	}

	// Modify members of class 'E':
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
		private static void source_static() { throw null; }
		// Part 2: Add a new static constructor:
		// Note: Cannot replace item here, items can be targeted only once.
		// The static constructor action in this case is ADD by default.
		static {
			// When this static block is invoked, the patch static fields have
			// already been initialized. Manually invoking the source static
			// constructor initializes the source static fields. The static
			// fields that are declared on both classes are initialized twice,
			// and end up with the values set later by the source.
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
		@DexEdit
		private E(String data, @DexIgnore Void tag) { throw null; }
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

		@DexAppend
		public void print() {
			p("E::patchStaticField: " + patchStaticField);
			p("E::patchField: " + patchField);
		}

	}

	// Modify members of class 'F' appending code to its static constructor:
	@DexEdit(staticConstructorAction = DexAction.APPEND)
	public static class F {

		@DexEdit
		private static int redefinedSourceStaticField = 200;
		@DexEdit
		private int redefinedSourceField = 200;
		@DexAdd
		private static int patchStaticField = 200;
		@DexAdd
		private int patchField = 200;

		// Append code to the existing static constructor:
		static {
			// The source static constructor runs fully before any part of the
			// patch static constructor is executed, including its static field
			// initializers. The static fields that are declared on both source
			// and patch classes are initialized twice, and end up with the
			// values set later by the patch.
			p("appended F::<clinit>");
		}

		// Ignore the constructor:
		@DexIgnore
		public F() {
			// This constructor that initializes all instance fields declared
			// in the patch will not execute.
			throw null;
		}

		@DexAppend
		public void print() {
			p("F::patchStaticField: " + patchStaticField);
			p("F::patchField: " + patchField);
		}

	}

	// Modify members of class 'G' implicitly handling static constructors:
	// Note: DexPatcher tool v1.4.0 introduces the ability to implicitly
	// handle static constructors for which no action is defined. A static
	// constructor in the patch is appended to the corresponding one in the
	// source if it exists, or otherwise it is added to the output as is.
	// This implicit handling guarantees that code in static constructors
	// will never be discarded and provides a safe default behavior. Prior
	// to v1.4.0, failure to define actions for static constructors resulted
	// in errors.
	@DexEdit
	public static class G {

		@DexEdit
		private static int redefinedSourceStaticField = 200;
		@DexAdd
		private static int patchStaticField = 200;

		// Implicitly append code to an existing static constructor:
		static {
			p("appended G::<clinit>");
		}

		@DexAppend
		public void print() {
			p("G::patchStaticField: " + patchStaticField);
		}

	}

	// Modify members of class 'H' implicitly handling static constructors:
	// Note: Setting 'defaultAction' defines a default action for all members
	// including static constructors, thus disabling their implicit handling.
	// DexPatcher tool v1.6.3 adds the new 'DexAction.NONE' value which can
	// be assigned to 'staticConstructorAction' to restore implicit handling
	// of static constructors when using 'defaultAction'.
	@DexEdit(defaultAction = DexAction.WRAP, staticConstructorAction = DexAction.NONE)
	public static class H {

		@DexAdd
		private static int patchStaticField = 200;

		// Implicitly add a static constructor where none previously existed:
		static {
			p("added H::<clinit>");
		}

		@DexIgnore
		H() { throw null; }

		@DexAppend
		public void print() {
			p("H::patchStaticField: " + patchStaticField);
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
	// Note: The automatically generated default constructor for this class is
	// trivial and will be implicitly ignored if no default action is defined.
	// There is no need to explicitly define a constructor and @DexIgnore it.
	@DexEdit
	public static class Concrete1 extends Abstract implements Interface {
		@DexReplace
		public void method() {
			p("replaced Concrete1::method");
		}
		// We only need this abstract method implemented here so that the
		// concrete class compiles.
		@DexIgnore
		@Override
		public void interfaceMethod() { throw null; }
		// Same case here. We can avoid defining a method body by declaring
		// the method native, but it is still verbose and annoying.
		// Note: It is not recommended to use 'native' as an alternative to
		// defining a method body that will be ignored by DexPatcher for two
		// reasons:
		// 1) It cannot be used in constructors.
		// 2) The presence of a native method disables IDE warnings that
		//    depend on class-wide analysis (eg: 'unused field' warnings).
		@DexIgnore
		@Override
		public native void abstractMethod();
	}
	// Option 2: Declaring the class 'abstract':
	// Note: You can make @DexEdit not redefine the target class by setting
	// its 'contentOnly' element to true. This gives you several options
	// for modifying the class so that it compiles without its abstract
	// methods being implemented, such as declaring the class 'abstract'.
	// Keep in mind that you cannot use the 'abstract' trick if you need to
	// instantiate the class in the patch code.
	// Note: Before DexPatcher tool v1.5.0, the 'contentOnly' element was
	// called 'onlyEditMembers'. The name was changed to allow extending the
	// usage of the element to other tags besides @DexEdit and to other items
	// besides classes. The new DexPatcher tool accepts both the old and new
	// names of the element, so binary patches compiled for an older version
	// of the tool will continue to apply using the new version. However,
	// recompilation of the patches will require adapting them to the new name.
	@DexEdit(contentOnly = true)
	public static abstract class Concrete2 extends Abstract implements Interface {
		@DexReplace
		public void method() {
			p("replaced Concrete2::method");
		}
	}
	// Option 3: Stripping out the hierarchy:
	// Note: You can also strip out interfaces or even the superclass from the
	// class, but this has complex implications affecting the type system and
	// the constructors of the class. It is generally not recommended.
	@DexEdit(contentOnly = true)
	public static class Concrete3 {
		@DexReplace
		public void method() {
			p("replaced Concrete3::method");
		}
	}

	// Replace a class, using the replaced class at will:
	// Part 1: Rename the target class, optionally reducing visibility:
	// Note: All references to the type of the class within the declarations
	// and code of the class are rewritten to account for the change of type.
	// In particular, the 'this' references within the class change type.
	// Note: DexPatcher tool v1.5.0 introduces the ability to reliably perform
	// cross-class edits, where the patch and target class names are different.
	// (Before v1.5.0 these edits were accepted by the tool but the resulting
	// bytecode was invalid due to intricacies such as the type of the 'this'
	// references in the code not matching the type of the containing class).
	@DexEdit(targetClass = CrossClassA.class)
	static class source_CrossClassA {
		@DexIgnore
		public source_CrossClassA(String data) { throw null; }
		@DexIgnore
		public void go() { throw null; }
	}
	// Part 2: Adapt code outside of the class to the change of type if
	// self-references (such as 'this') escape the class:
	@DexEdit
	public static class CrossClassAHelper {
		@DexAdd
		public static void help2(source_CrossClassA a) { p("added CrossClassAHelper::help2(source_CrossClassA)"); }
	}
	// Part 3: Add a new class compatible with the one being replaced:
	// Note: Cannot replace the class here, items can be targeted only once.
	@DexAdd		// This tag can be omitted, as classes are added by default.
	public static class CrossClassA {
		private source_CrossClassA a1;
		private source_CrossClassA a2;
		public CrossClassA(String data) {
			a1 = new source_CrossClassA("filtered " + data + " for a1");
			a2 = new source_CrossClassA("filtered " + data + " for a2");
		}
		public void go() {
			pClass("entering replaced %s::go");
			a1.go();
			a2.go();
			pClass("exiting replaced %s::go");
		}
	}

	// Modify members of class 'CrossClassB' from a different class:
	// Note: This can be used to organize code by collecting simple related
	// changes to a set of classes as static nested classes within a single
	// patch class (that could itself be added or ignored). It is also useful
	// to handle name clashes between packages and classes in obfuscated code.
	// Note: The patch class is renamed to match the target behind-the-scenes
	// and all references to the type of the class within the declarations
	// and code of the class are rewritten to account for the change of type.
	// In particular, the 'this' references within the patch class change type.
	@DexEdit(target = "CrossClassB", contentOnly = true)
	public static class CrossClassBPatcher {
		@DexWrap
		public void go() {
			pClass("entering wrapper %s::go (originally in CrossClassBPatcher)");
			new CrossClassBPatcher[] {}.hashCode();     // array rewrite test
			go();
			pClass("exiting wrapper %s::go (originally in CrossClassBPatcher)");
		}
	}

	// Completely replace class 'CrossClassC' from a different class:
	// Note: The patch class is renamed to match the target behind-the-scenes
	// and all references to the type of the class within the declarations
	// and code of the class are rewritten to account for the change of type.
	// In particular, the 'this' references within the patch class change type.
	@DexReplace(target = "CrossClassC", contentOnly = true)
	public static class CrossClassCPatcher {
		static { pClass("replaced %s::<clinit> (originally in CrossClassCPatcher)"); }
		public CrossClassCPatcher() { pClass("replaced %s::<init> (originally in CrossClassCPatcher)"); }
	}

	// DexPatcher tool v1.8.0 adds support for patching obfuscated code via
	// optional identifier decode code transform passes. They allow using the
	// Java language to work with identifiers that are illegal in the language
	// but legal in bytecode.

	// Modify members of supposedly obfuscated class using identifier codes:
	// Note: The patch dex file must be decoded using '--decode-patches'.
	@DexEdit
	public static class __My_label_for_this_class_$$_IllegalName__ {
		@DexReplace
		public void __my_label_for_this_method_$$_illegalName__() { pMethod("replaced %s"); }
	}

	// Test identifier codes:
	@DexEdit
	public static class IdentifierCodes {

		// Identifier codes:

		// Identifier codes can be part of names of packages, types, fields,
		// methods, method parameters and method local variables, and of values
		// of string-valued annotation elements.

		// Identifier codes are recognized by the presence of a code marker,
		// which by default is '_$$_' but can be configured. Code markers must
		// be at least two characters in length, must start with an underscore,
		// and must not start or end with double underscore.

		// Identifier codes can take one of two forms:
		// - without label: _<code-marker><escaped-string>__
		// - with label:    __<label><code-marker><escaped-string>__

		// When using the default code marker, these forms become:
		// - without label: __$$_<escaped-string>__
		// - with label:    __<label>_$$_<escaped-string>__

		// When instructed to decode dex files, DexPatcher will replace each
		// occurrence of an identifier code with the unescaped version of its
		// corresponding <escaped-string> field, ignoring the rest of the code
		// (including its label). This mechanism allows using the Java language
		// to work with identifiers that are illegal in the language but legal
		// in bytecode. This is often required when patching obfuscated code.

		// The <label> field of codes is optional and used for disambiguation
		// and documentation purposes. If present, it must not be zero-length,
		// must not contain the code marker, must not start or end with an
		// underscore, and must not contain double underscore.

		// The <escaped-string> field of codes is mandatory. It must not be
		// zero-length, must not contain the code marker, must not contain
		// underscore, and can only contain dollar signs to signal the start
		// of escape sequences.

		// The allowed escape sequences are:
		// - dollar sign: $S is replaced with the dollar sign character ($).
		// - underscore: $U is replaced with the underscore character (_).
		// - ASCII / Latin-1: $aNN is replaced with Unicode code unit U+00NN.
		// - Unicode code unit: $uNNNN is replaced with code unit U+NNNN.
		// - Unicode code point: $pNNNNNN is replaced with code point U+NNNNNN.

		// Note that patches can be decoded at patch time, as would be typical
		// during development, or ahead of time before publishing them, so
		// users can apply them without specifying transforms.

		// Test valid identifier codes:
		@DexAdd void    __$$_withoutLabel__() { pMethod("added %s"); };
		@DexAdd void __ok_$$_with$SdollarEscape__() { pMethod("added %s"); };
		@DexAdd void __ok_$$_with$UunderscoreEscape__() { pMethod("added %s"); };
		@DexAdd void __ok_$$_with$aB1asciiLatin1Escape__() { pMethod("added %s"); };
		@DexAdd void __ok_$$_with$u00B1unicodeCodeUnitEscape__() { pMethod("added %s"); };
		@DexAdd void __ok_$$_with$p0000B1unicodeCodePointEscape__() { pMethod("added %s"); };
		@DexAdd void __prefix_$$_1____infix_$$_2__and__postfix_$$_3__() { pMethod("added %s"); };

		// Test invalid identifier codes:
		// Note: The patch dex file must be decoded using '--no-decode-errors'.
		// Note: Invalid codes within identifiers are skipped during decoding,
		// but valid codes within the affected identifiers are still decoded.
		@DexAdd int  _bad_$$_noStartMark__;
		@DexAdd int __bad_$$_noEndMark_;
		@DexAdd int __bad_emptyCode_$$__;
		@DexAdd int __bad_$$_invalid_character__;
		@DexAdd int __bad_$$_invalid$sescape__;
		@DexAdd int __bad_$$_invalid$aX1asciiLatin1Escape__;
		@DexAdd int __bad_$$_invalid$uB1unicodeCodeUnitEscape__;
		@DexAdd int __bad_$$_invalid$p00B1unicodeCodePointEscape__;
		@DexAdd int __bad_$$_invalid$p110000unicodeCodePointEscape__;
		@DexAdd int __bad_$$_truncatedEscape$__;
		@DexAdd int __bad_$$_truncatedAsciiLatin1Escape$aB__;
		@DexAdd int __bad_$$_truncatedUnicodeCodeUnitEscape$uB1__;
		@DexAdd int __bad_$$_truncatedUnicodeCodePointEscape$p00B1__;
		@DexAdd void __prefix_$$_1____bad_infix_$$_$2__and__postfix_$$_3__() { pMethod("added %s"); };

		// Print the decoded identifiers:
		@DexReplace
		public void print() {
			__$$_withoutLabel__();
			__ok_$$_with$SdollarEscape__();
			__ok_$$_with$UunderscoreEscape__();
			__ok_$$_with$aB1asciiLatin1Escape__();
			__ok_$$_with$u00B1unicodeCodeUnitEscape__();
			__ok_$$_with$p0000B1unicodeCodePointEscape__();
			__prefix_$$_1____infix_$$_2__and__postfix_$$_3__();
			__prefix_$$_1____bad_infix_$$_$2__and__postfix_$$_3__();
		}

	}

	// DexPatcher tool v1.8.0 adds support for processing anonymous classes via
	// optional code transform passes that deanonymize and reanonymize them.
	// They allow easier patching of existing anonymous classes in the source.
	// And they also allow defining new anonymous classes in the patch without
	// risk of them name-clashing with existing ones in the source.

	// The deanonymize and reanonymize transforms apply a configurable plan,
	// which is a string that represents an anonymous class renaming template
	// and has the form '<prefix>[<infix>]<suffix>'. The infix cannot be empty,
	// and the prefix and suffix cannot be both empty. The brackets are literal
	// characters in the string, and the characters immediately adjacent to the
	// brackets cannot be digits.

	// During deanonymization, an anonymous class named number 'N' in bytecode
	// and nested 'L' levels deep will be renamed to '<prefix>N<suffix>' if L
	// is 1 or to '<prefix>N<infix>L<suffix>' if L is larger than 1. The
	// nesting level is included in the new name to support patching of nested
	// anonymous classes and to avoid clashes with existing class names that
	// happen to match the plan, should they exist.

	// For example, to allow defining new anonymous classes in a patch, the
	// patch could be deanonymized with plan '[_]_patch' to rename its
	// anonymous classes and make sure they do not clash with the names of
	// anonymous classes already existing in the source. In this case, a patch
	// anonymous class named 'Main$1' would end up being called 'Main$1_patch'.

	// Also, to make it easier to patch existing anonymous classes, the source
	// could be deanonymized with plan 'Anon[_Level]'. This would cause a
	// source anonymous class named 'Main$3' to be renamed to 'Main$Anon3' for
	// easy targeting with DexPatcher tags. Additionally, the original
	// anonymous class names can be restored after patching by reanonymizing
	// the output using the same plan used to deanonymize the source.

	// Finally, when it is time to publish the patch, the patch itself could be
	// pre-transformed (so that it can be applied to the source by users
	// without needing any further transforms) using this command:

	// dexpatcher patch.dex --output release-patch.dex
	//     --deanon-source-alt --reanon-source --main-plan Anon[_Level]

	// Modify members of an anonymous class:
	// Note: For this to work, the source dex file must be deanonymized using
	// '--deanon-source --main-plan Anon[_Level]'. It is recommended to
	// reanonymize the output dex using '--reanon-output'. This restores the
	// original names of the anonymous classes right after the patching step.
	@DexEdit
	public static class AnonymousClasses {
		@DexEdit(contentOnly = true)
		public static class Anon1 {
			@DexEdit(contentOnly = true)
			public static class Anon1_Level2 implements Runnable {
				@DexReplace
				@Override public void run() {
					p("replaced AnonymousClasses::<anon>::<anon>::run");
				}
			}
		}

		// Use anonymous classes in the patch:
		// Note: Anonymous classes in the patch can name-clash with anonymous
		// classes in the source. To avoid clashes, the patch dex file can be
		// deanonymized using '--deanon-patch'.
		@DexAppend
		public static void print() {
			new Runnable() {
				@Override
				public void run() {
					pClass("added class %s");
				}
			}.run();
		}

		// Test unexpected anonymous classes during reanonymization:
		// Note: The output must be reanonymized using '--no-reanon-errors'.
		// Note: When reanonymizing the output, all classes are expected to be
		// deanonymized. This inserts an non-deanonymized anonymous class named
		// '42' in the output to test the handling of this error condition.
		static class __UnexpectedAnonymousClass_$$_42__ {}
	}

	// Modify members of a class that has been deobfuscated with a map file.
	// Note: The source dex must be mapped with '--map <map-file> --map-source'
	// and the output should typically be inverse-mapped with '--unmap-output'.
	@DexEdit
	public static class Thing {
		@DexIgnore
		public int field;
		@DexReplace
		public void someMethod() { pMethod("replaced %s"); }
		@DexReplace
		public float[][] anotherMethodWithArgs(int i, String s, Object... args) { pMethod("replaced %s"); return null; }
		@DexReplace
		public void makeFriendsWithOtherThings(Thing[] things) { pMethod("replaced %s"); }
		@DexPrepend
		public void print() { field = 42; }
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

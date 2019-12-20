/*
 * DexPatcher - Copyright 2015-2019 Rodrigo Balerdi
 * (GNU General Public License version 3 or later)
 *
 * DexPatcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 */

package lanchon.dexpatcher.transform.anonymizer;

public final class TypeAnonymizer {

	public interface ErrorHandler {
		void onError(String type, String message);
	}

	//public static final String DEFAULT_MARKER_INFIX = "_";

	public static final ErrorHandler NULL_ERROR_HANDLER = new ErrorHandler() {
		@Override
		public void onError(String type, String message) {}
	};

	public static boolean isValidPlan(String plan) {
		if (plan.contains(";") || plan.contains(".") || plan.contains("/")) return false;
		int open = plan.indexOf('[');
		if (open < 0 || open != plan.lastIndexOf('[')) return false;
		int close = plan.indexOf(']');
		//int minInfixLength = 0;
		int minInfixLength = 1;
		if (close - open - 1 < minInfixLength || close != plan.lastIndexOf(']')) return false;
		int length = plan.length();
		if (length - close + open - 1 == 0) return false;
		if (open - 1 >= 0 && isDigit(plan.charAt(open - 1))) return false;
		if (isDigit(plan.charAt(open + 1)) || isDigit(plan.charAt(close - 1))) return false;
		if (close + 1 < length && isDigit(plan.charAt(close + 1))) return false;
		return true;
	}

	private final String prefix;
	private final String infix;
	private final String suffix;
	private final boolean reanonymize;

	public TypeAnonymizer(String plan, boolean reanonymize) {
		if (!isValidPlan(plan)) {
			throw new IllegalArgumentException("plan");
		}
		int open = plan.indexOf('[');
		int close = plan.indexOf(']');
		prefix = plan.substring(0, open);
		String in = plan.substring(open + 1, close);
		//if (in.length() == 0) in = DEFAULT_MARKER_INFIX;
		infix = in;
		suffix = plan.substring(close + 1);
		this.reanonymize = reanonymize;
	}

	public String getPlan() {
		return prefix + '[' + infix + ']' + suffix;
	}

	public boolean isReanonymizer() {
		return reanonymize;
	}

	public String anonymizeType(String type) {
		return anonymizeType(type, NULL_ERROR_HANDLER);
	}

	public String anonymizeType(String type, ErrorHandler errorHandler) {

		int length = type.length();

		// Find the simple name of the type (without package).
		final int minSimpleLength = 3;
		if (length < minSimpleLength + 2 || type.charAt(0) != 'L' || type.charAt(length - 1) != ';') return type;
		int simpleStart = type.lastIndexOf('/') + 1;
		if (simpleStart == 0) simpleStart = 1;
		if (length - simpleStart < minSimpleLength + 1) return type;
		// The simple name is type.substring(simpleStart, length - 1) and is non-empty.

		// Find the first inner class delimiter.
		int outerEnd = findFirstDollarChar(type, simpleStart + 1); // skip first character of simple name
		if (outerEnd < 0) return type;

		// Rewrite the type if needed.
		return anonymizeTypeTail(type, 1, simpleStart, 0, outerEnd, errorHandler);

	}

	private String anonymizeTypeTail(String type, int level, int simpleStart, int start, int outerEnd,
			ErrorHandler errorHandler) {

		int length = type.length();

		// Find the first inner class name.
		int innerStart = findFirstNonDollarChar(type, outerEnd + 1);
		if (innerStart < 0) return type.substring(start);
		int innerEnd = findFirstDollarChar(type, innerStart + 1);
		boolean hasNestedInner = (innerEnd >= 0);
		if (!hasNestedInner) innerEnd = length - 1;
		// The inner class name is type.substring(innerStart, innerEnd) and is non-empty.

		// Detect an anonymous inner class and find its anonymous class number and deanonymization level.
		boolean anonymous = false;
		int currentLevel = 0;
		int numberStart, numberEnd;
		if (isAllDigits(type, innerStart, innerEnd)) {
			anonymous = true;
			numberStart = innerStart;
			numberEnd = innerEnd;
		} else {
			// Detect a deanonymized inner class and its deanonymization level.
			numberStart = innerStart + prefix.length();
			numberEnd = innerEnd - suffix.length();
			if (numberStart < numberEnd && type.startsWith(prefix, innerStart) && type.startsWith(suffix, numberEnd)) {
				if (isAllDigits(type, numberStart, numberEnd)) {
					anonymous = true;
					currentLevel = 1;
				} else {
					int levelEnd = numberEnd;
					numberEnd = type.indexOf(infix, numberStart);
					int levelStart = numberEnd + infix.length();
					if (numberStart < numberEnd && levelStart < levelEnd) {
						if (isAllDigits(type, numberStart, numberEnd) && isAllDigits(type, levelStart, levelEnd)) {
							String levelString = type.substring(levelStart, levelEnd);
							try {
								currentLevel = Integer.parseInt(levelString);
							} catch (NumberFormatException e) {}
							// Test levelString reconstruction.
							if (currentLevel > 1 && levelString.equals(Integer.toString(currentLevel))) {
								anonymous = true;
							}
						}
					}
				}
			}
		}
		// If an anonymous class was detected, its number is type.substring(numberStart, numberEnd) and is non-empty.
		//if (anonymous && !(numberStart < numberEnd)) throw new AssertionError("Bad anonymous class number indexes");

		// Decide whether this inner class name has to be rewritten.
		int newLevel = reanonymize ? currentLevel - level : currentLevel + level;
		if (anonymous) {
			if (newLevel < 0) {
				String message = reanonymize ? "cannot reanonymize '" : "cannot deanonymize '";
				errorHandler.onError(type, message + type.substring(simpleStart, innerEnd) + "' by " +
						level + (level == 1 ? " level" : "levels"));
				anonymous = false;
			}
		}

		// If not rewriting this inner class name, then rewrite the type tail if needed.
		if (!anonymous) {
			return hasNestedInner ? anonymizeTypeTail(type, level, simpleStart, start, innerEnd, errorHandler) :
					type.substring(start);
		}

		// Else rewrite the type, tail first if needed.
		String rewrittenTail = hasNestedInner ?
				anonymizeTypeTail(type, level + 1, simpleStart, innerEnd, innerEnd, errorHandler) : null;
		int rewrittenLength = hasNestedInner ? innerEnd + rewrittenTail.length() : length;
		String newLevelString = newLevel > 1 ? Integer.toString(newLevel) : null;
		int currentInnerLength = innerEnd - innerStart;
		int newInnerLength = numberEnd - numberStart + (
				newLevel == 0 ? 0 :
				newLevel == 1 ? prefix.length() + suffix.length() :
						prefix.length() + infix.length() + newLevelString.length() + suffix.length()
		);
		int newLength = rewrittenLength - currentInnerLength + newInnerLength - start;
		StringBuilder sb = new StringBuilder(newLength);
		sb.append(type, start, innerStart);
		if (newLevel == 0) {
			sb.append(type, numberStart, numberEnd);
		} else {
			sb.append(prefix);
			sb.append(type, numberStart, numberEnd);
			if (newLevel != 1) sb.append(infix).append(newLevelString);
			sb.append((suffix));
		}
		if (hasNestedInner) {
			sb.append(rewrittenTail);
		} else {
			sb.append(type, innerEnd, length);
		}
		//if (sb.length() != newLength) throw new AssertionError("Bad precalculated string length");
		return sb.toString();

	}

	private static int findFirstDollarChar(String type, int start) {
		int i = start;
		loop:
		for (;;) {
			switch (type.charAt(i)) {
				case ';': return -1;
				case '$': break loop;
				default:  break;
			}
			i++;
		}
		return i;
	}

	private static int findFirstNonDollarChar(String type, int start) {
		int i = start;
		loop:
		for (;;) {
			switch (type.charAt(i)) {
				case ';': return -1;
				case '$': break;
				default:  break loop;
			}
			i++;
		}
		return i;
	}

	private static boolean isAllDigits(String type, int start, int end) {
		for (int i = start; i < end; i++) {
			if (!isDigit(type.charAt(i))) return false;
		}
		return true;
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

}

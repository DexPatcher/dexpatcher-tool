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

	public static final String NUMBER_MARKER = "[]";

	public static final ErrorHandler NULL_ERROR_HANDLER = new ErrorHandler() {
		@Override
		public void onError(String type, String message) {}
	};

	private static final int NUMBER_MARKER_LENGTH = NUMBER_MARKER.length();

	public static boolean isValidPlan(String plan) {
		int length = plan.length();
		if (length < NUMBER_MARKER_LENGTH + 1 || plan.contains(";")) return false;
		int marker = plan.indexOf(NUMBER_MARKER);
		if (marker < 0 || plan.lastIndexOf(NUMBER_MARKER) != marker) return false;
		int beforeMarker = marker - 1;
		if (beforeMarker >= 0 && isDigit(plan.charAt(beforeMarker))) return false;
		int afterMarker = marker + NUMBER_MARKER_LENGTH;
		if (afterMarker < length && isDigit(plan.charAt(afterMarker))) return false;
		return true;
	}

	private final String prefix;
	private final String suffix;
	private final int prefixLength;
	private final int suffixLength;
	private final int totalLength;
	private final boolean reanonymize;

	public TypeAnonymizer(String plan, boolean reanonymize) {
		if (!isValidPlan(plan)) {
			throw new IllegalArgumentException("plan");
		}
		int marker = plan.indexOf(NUMBER_MARKER);
		prefix = plan.substring(0, marker);
		suffix = plan.substring(marker + NUMBER_MARKER_LENGTH);
		prefixLength = prefix.length();
		suffixLength = suffix.length();
		totalLength = prefixLength + suffixLength;
		this.reanonymize = reanonymize;
	}

	public String getPlan() {
		return prefix + NUMBER_MARKER + suffix;
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

		// Find the space where the anonymous class number should be.
		int currentLevel = 0;
		int numberStart = innerStart;
		int numberEnd = innerEnd;
		while (matchesPlan(type, numberStart, numberEnd)) {
			currentLevel++;
			numberStart += prefixLength;
			numberEnd -= suffixLength;
		}
		// The space where the number should be is type.substring(numberStart, numberEnd) and is non-empty.
		if (!(numberStart < numberEnd)) throw new AssertionError("Invalid anonymous class number index range");

		if (isAllDigits(type, numberStart, numberEnd)) {
			if (reanonymize && currentLevel < level) {
				errorHandler.onError(type, "class '" + type.substring(simpleStart, innerEnd) +
						"' cannot be " + level + "-level reanonymized");




			} else {

				// Rewrite the type, tail first if needed.
				String rewrittenTail = hasNestedInner ?
						anonymizeTypeTail(type, level + 1, simpleStart, innerEnd, innerEnd, errorHandler) : null;
				int rewrittenLength = hasNestedInner ? innerEnd + rewrittenTail.length() : length;
				int lengthChange = reanonymize ? -totalLength : +totalLength;
				StringBuilder sb = new StringBuilder(rewrittenLength - start + lengthChange);
				sb.append(type, start, innerStart);
				if (reanonymize) {
					sb.append(type, innerStart + level * prefixLength, innerEnd - level * suffixLength);
				} else {
					for (int i = 0; i < level; i++) sb.append(prefix);
					sb.append(type, innerStart, innerEnd);
					for (int i = 0; i < level; i++) sb.append(suffix);
				}
				if (hasNestedInner) {
					sb.append(rewrittenTail);
				} else {
					sb.append(type, innerEnd, length);
				}
				return sb.toString();

			}
		}
		return hasNestedInner ? anonymizeTypeTail(type, level, simpleStart, start, innerEnd, errorHandler) :
				type.substring(start);

	}

	private boolean matchesPlan(String type, int start, int end) {
		return end - start > totalLength && type.startsWith(prefix, start) &&
				type.startsWith(suffix, end - suffixLength);
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

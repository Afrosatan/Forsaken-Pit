package com.fpit.util;

import java.util.Random;

/**
 * 
 */
public class StringUtil {
	private static final char[] ALLOW_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
			.toCharArray();

	/**
	 * Generate a string randomly with the provided number of characters from lower and upper case letters (a-z and A-Z) and numbers (0-9). 
	 */
	public static String randomAlphaNum(int size) {
		Random rand = new Random();
		char[] chars = new char[size];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = ALLOW_CHARS[rand.nextInt(ALLOW_CHARS.length)];
		}
		return new String(chars);
	}
}

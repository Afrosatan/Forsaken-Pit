package com.fpit.util;

public class Util {
	public static int mymod(int x) {
		if (x > 99) {
			x -= 100;
		} else if (x < 0) {
			x += 100;
		}
		return x;
	}
}

package com.fpit.util;

import com.fpit.Constants;

public class Util {
	public static int mymod(int x) {
		if (x >= Constants.MAP_SIZE) {
			x -= Constants.MAP_SIZE;
		} else if (x < 0) {
			x += Constants.MAP_SIZE;
		}
		return x;
	}
}

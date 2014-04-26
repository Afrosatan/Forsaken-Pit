var pitModule = angular.module("pitModule", [ "pitweb" ]);

function ssort(arr, key) {
	arr.sort(function(a, b) {
		var ak = a[key];
		var bk = b[key];
		if (ak < bk) {
			return -1;
		}
		if (ak > bk) {
			return 1;
		}
		return 0;
	});
}

function shuffle(o) {
	for ( var j, x, i = o.length; i; j = Math.floor(Math.random() * i), x = o[--i], o[i] = o[j], o[j] = x)
		;
	return o;
}
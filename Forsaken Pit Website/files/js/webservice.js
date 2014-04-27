var pitwebModule = angular.module("pitweb", []);

function PitwebCall() {
	this.running = false;
	this.runId = 0;
	this.complete = function(data, status, statusText) {
		alert("Webservice call complete not handled");
	};
	this.error = function(data, status, statusText) {
		if (data.message) {
			alert(data.message);
		} else if (statusText) { //statusText is added in some version past 1.2.15 of angular
			alert("Error contacting webservice (" + statusText + ")");
		} else if (status) {
			alert("Error contacting webservice (" + status + ")");
		} else {
			alert("Error contacting webservice (" + data + ")");
		}
	};
}

function callWrapper(http, call) {
	call.running = true;
	var runId = ++call.runId;
	http.success(function(data, status, headers, config, statusText) {
		if (runId === call.runId) {
			call.running = false;
			call.complete(data, status, statusText);
		}
	}).error(function(data, status, headers, config, statusText) {
		if (runId === call.runId) {
			call.running = false;
			call.error(data, status, statusText);
		}
	})
}

function pitwebFactory($http) {
	var instance = {};

	instance.createNewPlayer = function(name, type, call) {
		var http = $http.post("/pitapi/player/create", {
			"name" : name,
			"type" : type
		});
		callWrapper(http, call);
	};

	instance.update = function(player_key, call) {
		var http = $http.post("/pitapi/update", {
			"player_key" : player_key
		});
		callWrapper(http, call);
	};

	instance.move = function(player_key, x, y, call) {
		var http = $http.post("/pitapi/move", {
			"player_key" : player_key,
			"x" : x,
			"y" : y
		});
		callWrapper(http, call);
	};

	instance.attack = function(player_key, actor_id, call) {
		var http = $http.post("/pitapi/attack", {
			"player_key" : player_key,
			"actor_id" : actor_id
		});
		callWrapper(http, call);
	};

	instance.rest = function(player_key, call) {
		var http = $http.post("/pitapi/rest", {
			"player_key" : player_key
		});
		callWrapper(http, call);
	};

	return instance;
}

pitwebModule.factory('$pitweb', [ '$http', pitwebFactory ]);
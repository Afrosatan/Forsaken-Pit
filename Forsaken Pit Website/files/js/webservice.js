var pitwebModule = angular.module("pitweb", []);

function PitWebCall() {
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

	instance.logIn = function(account, pass, call) {
		var http = $http.post("/api/login/authenticate", {
			"accountName" : account,
			"password" : pass
		});
		callWrapper(http, call);
	};

	return instance;
}

pitwebModule.factory('$pitweb', [ '$http', pitwebFactory ]);
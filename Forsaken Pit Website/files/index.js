function IndexCtrl($scope, $pitweb) {
	$scope.newName = "";
	$scope.newType = "";
	$scope.newCharacterMessage = "";

	$scope.cellRows = [];
	for ( var i = 0; i < 11; i++) {
		var cellRow = [];
		for ( var j = 0; j < 11; j++) {
			if (i === 5 && j === 5) {
				cellRow.push({
					obj : {
						type : "player",
						name : "It's You!, dumb fuck"
					}
				});
			} else {
				cellRow.push({
					obj : null
				});
			}
		}
		$scope.cellRows.push(cellRow);
	}

	$scope.newPlayerCall = new PitwebCall();
	$scope.newPlayerCall.complete = function(data) {

	};

	$scope.createNewCharacter = function() {
		$scope.newCharacterMessage = "";
		var name = $scope.newName.trim();
		if (name.length === 0) {
			$scope.newCharacterMessage = "Enter a name";
			return;
		}
		var type = $scope.newType;
		if (type.length === 0) {
			$scope.newCharacterMessage = "Pick a color/type";
			return;
		}
		$pitweb.createNewPlayer(name, type, $scope.newPlayerCall);
	};
};


function IndexCtrl($scope, $pitweb, $timeout) {
	$scope.newName = "";
	$scope.newType = "";
	$scope.newCharacterMessage = "";

	player_key = null;
	player_id = null;
	$scope.player_type = null;
	$scope.player_name = null;
	$scope.depth = null;

	$scope.cellRows = [];
	for ( var i = -5; i <= 5; i++) {
		var cellRow = [];
		for ( var j = -5; j <= 5; j++) {
			cellRow.push({
				objs : [],
				x : j,
				y : i
			});
		}
		$scope.cellRows.push(cellRow);
	}

	var updatePromise = null;
	$scope.updateCall = new PitwebCall();
	$scope.updateCall.complete = function(data) {
		player_id = data.player_id;
		$scope.player_type = data.player_type.charAt(0).toUpperCase()
				+ data.player_type.slice(1);
		$scope.player_name = data.player_name;
		$scope.depth = data.depth;

		for ( var i = 0; i < $scope.cellRows.length; i++) {
			var cellRow = $scope.cellRows[i];
			for ( var j = 0; j < cellRow.length; j++) {
				var cell = cellRow[j];
				cell.objs = [];
			}
		}
		for ( var i = 0; i < data.objs.length; i++) {
			var obj = data.objs[i];
			$scope.cellRows[obj.y][obj.x].objs.push(obj);
		}

		if (updatePromise) {
			$timeout.cancel(updatePromise);
			updatePromise = null;
		}
		//updatePromise = $timeout(update, 5000);
	};

	function update() {
		if (updatePromise) {
			$timeout.cancel(updatePromise);
			updatePromise = null;
		}
		$pitweb.update(player_key, $scope.updateCall);
	}

	$scope.newPlayerCall = new PitwebCall();
	$scope.newPlayerCall.complete = function(data) {
		player_key = data.player_key;
		update();
	};

	$scope.createNewCharacter = function() {
		$scope.newCharacterMessage = "";
		var name = $scope.newName.trim();
		if (name.length === 0) {
			$scope.newCharacterMessage = "Enter a name";
			return;
		}
		var type = $scope.newType.toLowerCase();
		if (type.length === 0) {
			$scope.newCharacterMessage = "Pick a color/type";
			return;
		}
		$pitweb.createNewPlayer(name, type, $scope.newPlayerCall);
	};

	$scope.selectedCell = null;
	$scope.showingDropdown = false;
	$scope.dropdownStyle = {};
	$scope.dropdownActions = [];
	$scope.onCellClick = function($event, cell) {
		$scope.selectedCell = cell;
		$scope.dropdownActions = [];
		$scope.dropdownActions.push({
			"title" : "Move Here",
			"action" : "move"
		});

		$scope.showingDropdown = true;
		$scope.dropdownStyle.top = ($event.target.parentElement.offsetTop + 25)
				+ "px";
		$scope.dropdownStyle.left = ($event.target.parentElement.offsetLeft + 25)
				+ "px";
	};

	var movePromise = null;
	$scope.moveCall = new PitwebCall();
	$scope.moveCall.complete = function(data) {
		if (data.moved) {
			if ($scope.moveX < 0) {
				$scope.moveX++;
			} else if ($scope.moveX > 0) {
				$scope.moveX--;
			}
			if ($scope.moveY < 0) {
				$scope.moveY++;
			} else if ($scope.moveY > 0) {
				$scope.moveY--;
			}
			update();
			if (movePromise) {
				$timeout.cancel(movePromise);
				movePromise = null;
			}
			movePromise = $timeout(move, data.waitMillis);
		} else {
			if (movePromise) {
				$timeout.cancel(movePromise);
				movePromise = null;
			}
			movePromise = $timeout(move, data.waitMillis);
		}
	};

	function move() {
		var x = 0;
		var y = 0;
		if ($scope.moveX < 0) {
			x = -1;
		} else if ($scope.moveX > 0) {
			x = 1;
		}
		if ($scope.moveY < 0) {
			y = -1;
		} else if ($scope.moveY > 0) {
			y = 1;
		}
		if (x !== 0 || y !== 0) {
			if (movePromise) {
				$timeout.cancel(movePromise);
				movePromise = null;
			}
			$pitweb.move(player_key, x, y, $scope.moveCall);
		}
	}

	$scope.moveX = 0;
	$scope.moveY = 0;
	$scope.doAction = function(action) {
		if (action.action === "move") {
			$scope.moveX = $scope.selectedCell.x;
			$scope.moveY = $scope.selectedCell.y;
			move();
		}
		$scope.selectedCell = null;
		$scope.showingDropdown = false;
	};
};
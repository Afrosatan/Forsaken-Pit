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
				y : i,
				grounded : Math.random() >= 0.5,
			});
		}
		$scope.cellRows.push(cellRow);
	}
	$scope.events = [];
	$scope.leaderboard = [];

	var updatePromise = null;
	function resetUpdate() {
		if (updatePromise) {
			$timeout.cancel(updatePromise);
			updatePromise = null;
		}
	}

	$scope.updateCall = new PitwebCall();
	$scope.updateCall.complete = function(data) {
		player_id = data.player_id;
		$scope.player_type = data.player_type.charAt(0).toUpperCase()
				+ data.player_type.slice(1);
		$scope.player_name = data.player_name;
		$scope.depth = data.depth;
		$scope.firepower = data.firepower;
		$scope.health = data.health;
		$scope.max_health = data.max_health;
		$scope.points = data.points;

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
		$scope.events = data.events;
		$scope.leaderboard = data.leaderboard;
		resetUpdate();
		updatePromise = $timeout(update, 1000);
	};

	function update() {
		resetUpdate();
		$pitweb.update(player_key, $scope.updateCall);
	}

	$scope.newPlayerCall = new PitwebCall();
	$scope.newPlayerCall.complete = function(data) {
		player_key = data.player_key;
		update();
		$scope.newName = "";
		$scope.newType = "";
		$scope.newCharacterMessage = "";
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
			title : "Move Here",
			action : "move"
		});

		for ( var i = 0; i < cell.objs.length; i++) {
			var obj = cell.objs[i];
			if (obj.id !== player_id) {
				$scope.dropdownActions.push({
					title : "Attack " + obj.name,
					action : "attack",
					obj : obj
				});
			} else {
				$scope.dropdownActions.push({
					title : "Rest",
					action : "rest"
				});
			}
		}

		$scope.showingDropdown = true;

		//i don't know why, but when these 6 lines were combined into just 2, eclipse javascript formatter didn't work -_- i need a better IDE for this shit
		var wtf = $event.target.parentElement;
		var lol = $event.target.parentElement.parentElement.parentElement;
		var rofl = wtf.offsetTop + lol.offsetTop + 25;
		var omg = wtf.offsetLeft + lol.offsetLeft + 25;
		$scope.dropdownStyle.top = rofl + "px";
		$scope.dropdownStyle.left = omg + "px";
	};

	function shiftCellGrounded(sx, sy) {
		if (sx > 0) {
			for ( var i = 0; i < $scope.cellRows.length; i++) {
				var cellRow = $scope.cellRows[i];
				for ( var j = cellRow.length - 1; j > 0; j--) {
					cellRow[j].grounded = cellRow[j - 1].grounded;
				}
				cellRow[0].grounded = Math.random() >= 0.5;
			}
		} else if (sx < 0) {
			for ( var i = 0; i < $scope.cellRows.length; i++) {
				var cellRow = $scope.cellRows[i];
				for ( var j = 0; j < cellRow.length - 1; j++) {
					cellRow[j].grounded = cellRow[j + 1].grounded;
				}
				cellRow[cellRow.length - 1].grounded = Math.random() >= 0.5;
			}
		}

		if (sy > 0) {
			for ( var i = $scope.cellRows.length - 1; i > 0; i--) {
				var cellRow = $scope.cellRows[i];
				var nextRow = $scope.cellRows[i - 1];
				for ( var j = 0; j < cellRow.length; j++) {
					cellRow[j].grounded = nextRow[j].grounded;
				}
			}
			var firstRow = $scope.cellRows[0];
			for ( var j = 0; j < firstRow.length; j++) {
				firstRow[j].grounded = Math.random() >= 0.5;
			}
		} else if (sy < 0) {
			for ( var i = 0; i < $scope.cellRows.length - 1; i++) {
				var cellRow = $scope.cellRows[i];
				var nextRow = $scope.cellRows[i + 1];
				for ( var j = 0; j < cellRow.length; j++) {
					cellRow[j].grounded = nextRow[j].grounded;
				}
			}
			var firstRow = $scope.cellRows[$scope.cellRows.length - 1];
			for ( var j = 0; j < firstRow.length; j++) {
				firstRow[j].grounded = Math.random() >= 0.5;
			}
		}
	}

	$scope.moveCall = new PitwebCall();
	$scope.moveCall.complete = function(data) {
		if (data.moved) {
			var sx = 0;
			var sy = 0;
			if ($scope.moveX < 0) {
				sx++;
				$scope.moveX++;
			} else if ($scope.moveX > 0) {
				sx--;
				$scope.moveX--;
			}
			if ($scope.moveY < 0) {
				sy++;
				$scope.moveY++;
			} else if ($scope.moveY > 0) {
				sy--;
				$scope.moveY--;
			}
			shiftCellGrounded(sx, sy);
			update();
		}
		resetAction();
		$scope.currentAction = "Moving";
		actionPromise = $timeout(move, data.waitMillis);
	};

	function move() {
		resetAction();
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
			$scope.currentAction = "Moving";
			$pitweb.move(player_key, x, y, $scope.moveCall);
		}
	}

	$scope.attackCall = new PitwebCall();
	$scope.attackCall.complete = function(data) {
		if (data.attacked) {
			update();
		}
		if (!data.invalidTarget) {
			resetAction();
			$scope.currentAction = "Attacking";
			actionPromise = $timeout(attack, data.waitMillis);
		}
	};

	function attack() {
		resetAction();
		$scope.currentAction = "Attacking";
		$pitweb.attack(player_key, $scope.attackId, $scope.attackCall);
	}

	$scope.restCall = new PitwebCall();
	$scope.restCall.complete = function(data) {
		update();
		resetAction();
		$scope.currentAction = "Resting";
		actionPromise = $timeout(rest, data.waitMillis);
	};

	function rest() {
		resetAction();
		$scope.currentAction = "Resting";
		$pitweb.rest(player_key, $scope.restCall);
	}

	$scope.moveX = 0;
	$scope.moveY = 0;
	$scope.attackId = null;

	var actionPromise = null;
	$scope.currentAction = "Waiting";
	function resetAction() {
		$scope.currentAction = "Waiting";
		if (actionPromise) {
			$timeout.cancel(actionPromise);
			actionPromise = null;
		}
	}

	$scope.doAction = function(action) {
		resetAction();
		if (action.action === "move") {
			$scope.moveX = $scope.selectedCell.x;
			$scope.moveY = $scope.selectedCell.y;
			move();
		} else if (action.action === "attack") {
			$scope.attackId = action.obj.id;
			attack();
		} else if (action.action === "rest") {
			rest();
		}
		$scope.selectedCell = null;
		$scope.showingDropdown = false;
	};
}
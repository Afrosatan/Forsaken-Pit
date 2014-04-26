function IndexCtrl($scope, $pitweb) {
	$scope.cellRows = [];
	for ( var i = 0; i < 11; i++) {
		var cellRow = [];
		for ( var j = 0; j < 11; j++) {
			if (i === 5 && j === 5) {
				cellRow.push({
					obj : {
						type : "player"
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
};


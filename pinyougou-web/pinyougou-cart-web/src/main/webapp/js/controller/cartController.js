app.controller('cartController', function ($scope, baseService, $controller) {
    //继承baseController
    $controller('baseController', {$scope: $scope});


    /** 初始化购物车 */
    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart").then(function (response) {
            $scope.carts = response.data;
            /** 定义总计对象 */
            $scope.totalEntity = {totalNum: 0, totalMoney: 0.00, num: 0};
            for (var i = 0; i < $scope.carts.length; i++) {
                $scope.totalEntity.totalNum += $scope.carts[i].orderItems.length;
                for (var j = 0; j < $scope.carts[i].orderItems.length; j++) {
                    //总金额
                    $scope.totalEntity.totalMoney += $scope.carts[i].orderItems[j].totalFee;
                    //总数量
                    $scope.totalEntity.num += $scope.carts[i].orderItems[j].num;
                }
            }


        });
    };


    /** 购物车的增减 */
    $scope.addCart = function (itemId, num) {
        baseService.sendGet("/cart/addCart", "itemId=" + itemId + "&num=" + num).then(function (response) {
            if (response.data == true) {
                $scope.findCart();
            } else {
                alert("操作失败!")
            }
        });
    };

    /** 购物车填写数字数量 */
    $scope.addCart1=function (itemId, num) {
        baseService.sendGet("/cart/addCart1", "itemId=" + itemId + "&num=" + num).then(function (response) {
            if (response.data == true) {
                $scope.findCart();
            } else {
                alert("操作失败!")
            }
        });
    }


});
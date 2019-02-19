app.controller('itemController', function ($scope, $controller, $http) {

    $controller('baseController', {$scope: $scope});

    //初始化需要购买的数量
    $scope.num = 1;
    //增加或减少数量的方法
    $scope.addNum = function (num) {
        // ng-model 绑定的数据都是字符串
        $scope.num = parseInt($scope.num);
        $scope.num += num;
        if ($scope.num <= 0) {
            $scope.num = 1;
        }
    };

    /** 记录用户选择的规格选项 */
    $scope.specItems = {};

    //定义用户选择规格选项的方法
    $scope.selectSpec = function (key, value) {
        $scope.specItems[key] = value;
        /** 查找对应的SKU商品 */
        searchSku();

    };

    //判断某个规格选项是否被选中
    $scope.isSelected = function (key, value) {
        return $scope.specItems[key] == value;
    };

    //加载默认的SKU
    $scope.loadSku = function () {
        //取出默认的sku -->已排序   itemList=${itemList}的内容
        $scope.sku = itemList[0];
        //获取SKU商品选择的选项规格
        $scope.specItems = JSON.parse($scope.sku.spec);

    };

    /** 查找对应的SKU商品 */
    var searchSku = function () {
        for (i = 0; i < itemList.length; i++) {
            if (itemList[i].spec == JSON.stringify($scope.specItems)) {
                $scope.sku = itemList[i];
                return;
            }
        }
    };

    /** 加入购物车事件绑定 */
    $scope.addToCart = function () {

        $http.get("http://cart.pinyougou.com/cart/addCart?itemId=" +
            $scope.sku.id + "&num=" + $scope.num, {"withCredentials": true}).then(function (response) {
            if (response.data) {
                /** 跳转到购物车页面 */
                location.href = 'http://cart.pinyougou.com/cart.html';
            } else {
                alert("操作失败!")
            }
        });
    };

});


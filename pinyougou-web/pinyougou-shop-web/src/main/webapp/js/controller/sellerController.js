/** 定义控制器层 */
app.controller('sellerController', function ($scope, baseService) {


    /** 添加或修改 */
    $scope.save = function () {
        /** 发送post请求 */
        baseService.sendPost("/seller/save", $scope.seller)
            .then(function (response) {
                if (response.data) {
                    /*跳转到登录页面*/
                    location.href = "/shoplogin.html";
                } else {
                    alert("操作失败！");
                }
            });
    };


});
// 定义基础模块
app.controller("baseController",function ($scope,baseService) {
    //定于获取用户名方法
    //定义重定向URL
    $scope.redirectUrl = window.encodeURIComponent(location.href);

    $scope.showName=function () {
        baseService.sendGet("/user/showName").then(function (response) {
            $scope.loginName = response.data.loginName;
        })
    }
});
app.controller('indexController', function ($scope, baseService) {

    //定于获取用户名方法
    $scope.showName=function () {
        baseService.sendGet("/user/showName").then(function (response) {
            $scope.loginName = response.data.loginName;
        })
    }

});
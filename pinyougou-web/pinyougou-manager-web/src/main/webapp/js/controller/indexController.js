app.controller("indexController", function ($scope,baseService) {

    /*获取登录名*/
    $scope.getLoginName=function () {
        baseService.sendGet("/login/getLoginName").then(function (value) {
            $scope.loginName = value.data.loginName;
        });
    }


});
app.controller('baseController', function ($http, $scope) {
    $scope.loadUserName = function () {
        //定义重定向URL
        $scope.redirectUrl = window.encodeURIComponent(location.href);
        $http.get("/user/showName").then(function (response) {
            $scope.loginName = response.data.loginName;
        });
    };
});
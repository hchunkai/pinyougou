app.controller('indexController', function ($scope,$controller) {

    //继承指定的controller
    $controller('baseController',{$scope:$scope});
});
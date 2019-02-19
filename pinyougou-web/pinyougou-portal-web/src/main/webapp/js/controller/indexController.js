/** 定义首页控制器层 */
app.controller("indexController", function ($scope, baseService,$controller) {
    $controller('baseController', {$scope: $scope});


    //查找轮播图资源
    $scope.findContentByCategoryId = function (categoryId) {
        baseService.sendGet("/content/findContentByCategoryId?categoryId=" + categoryId).then(function (response) {
            $scope.contentList = response.data;
        })
    };
    /** 定义搜索参数对象 */
    $scope.searchParam = {};

    //搜索跳转到搜索页面
    $scope.search = function () {
        var keyword = $scope.keywords ? $scope.keywords : "";
        location.href = "http://search.pinyougou.com?keywords=" + keyword;
    }

});
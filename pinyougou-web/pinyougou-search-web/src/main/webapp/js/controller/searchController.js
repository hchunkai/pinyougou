/** 定义搜索控制器 */
app.controller("searchController", function ($scope, baseService, $sce ,$location,$controller) {
    $controller('baseController',{$scope: $scope});


    /** 定义搜索参数对象 */
    $scope.searchParam = {'keywords': '', 'category': '', 'brand': '', 'spec': {}, 'price': '',
                             page: 1, rows: 10,sortField : '', sort : ''};

    $scope.Search = function () {
        baseService.sendPost("/Search", $scope.searchParam).then(function (value) {
            /** 获取搜索结果 */
            $scope.resultMap = value.data;
            $scope.initPageNum();
        });
    };

    // 将文本转化成html
    $scope.trustHtml = function (html) {
        return $sce.trustAsHtml(html);
    };

    //添加选项搜索
    $scope.addSearchItem = function (key, value) {
    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {//如果点击的是分类或者是品牌或者是价格
            $scope.searchParam[key] = value;
        } else { //规格选项
            $scope.searchParam.spec[key] = value;
        }
        //点击条件时默认为第一页
        $scope.searchParam.page = 1;
        $scope.jumpPage = 1;
        $scope.Search();
    };

    //移除选项列表
    $scope.removeSearchItem = function (key) {
        if (key == 'category' || key == 'brand' || key == 'price') {//如果点击的是分类或者是品牌或者是价格
            $scope.searchParam[key] = '';
        } else {
            delete $scope.searchParam.spec[key];
        }
        //点击条件时默认为第一页
        $scope.searchParam.page = 1;
        $scope.jumpPage = 1;
        $scope.Search();
    };

    //分页显示搜索结果
    $scope.initPageNum = function () {
        //初始化页数集合
        $scope.pageNums = [];
        //总页数
        var totalPage = $scope.resultMap.totalPage;
        //开始页码
        var firstPage = 1;
        //结束页码
        var lastPage = totalPage;

        if (totalPage > 5) {
            if ($scope.searchParam.page <= 3) { //当前页码在前三页
                lastPage = 5;
                $scope.firstDot = false; //前面无点
                $scope.lastDot = true;
            } else if ($scope.searchParam.page > totalPage - 3) {//当前页码在后三页
                firstPage = totalPage - 4;
                $scope.firstDot = true;
                $scope.lastDot = false;  //后面无点
            } else { //页码在中间
                firstPage = $scope.searchParam.page - 2;
                lastPage = $scope.searchParam.page + 2;
                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        }else {
            $scope.firstDot = false;
            $scope.lastDot = false;
        }



        /** 循环产生页码 */
        for (var i = firstPage; i <= lastPage; i++){
            $scope.pageNums.push(i);
        }
    };

    //点击页数搜索方法
    $scope.pageSearch = function (page) {
         page = parseInt(page);

        if (page <= 0) {  //不允许当前页低于0
            return;
        }
        if (page > $scope.resultMap.totalPage) { //不允许当前页超过最大页数
            return;
        }
        $scope.searchParam.page = page;
        $scope.jumpPage = $scope.searchParam.page;  //绑定跳转框的页码
        $scope.Search();
    };

    //排序
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchParam.sort = sort;
        $scope.searchParam.sortField = sortField;
        $scope.Search();

    };

    //获得首页跳转来的参数
    $scope.getkeywords = function () {
        $scope.searchParam.keywords = $location.search().keywords;
        $scope.Search();
    };


});

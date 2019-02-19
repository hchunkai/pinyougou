/** 定义控制器层 */
app.controller('typeTemplateController', function ($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    //查询品牌列表
    $scope.findBrandIds = function () {
        baseService.sendGet("/brand/brandIDandName").then(function (value) {
            $scope.brandList = {data: value.data}
        })
    };
    // 查询规格列表
    $scope.findSpecIds = function () {
        baseService.sendGet("/specification/specIDandName").then(function (value) {
            $scope.specList = {data: value.data};
        })
    };

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function (page, rows) {
        baseService.findByPage("/typeTemplate/findByPage", page,
            rows, $scope.searchEntity)
            .then(function (response) {
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 添加或修改 */
    $scope.saveOrUpdate = function () {
        var url = "save";
        if ($scope.entity.id) {
            url = "update";
        }
        /** 发送post请求 */
        baseService.sendPost("/typeTemplate/" + url, $scope.entity)
            .then(function (response) {
                if (response.data) {
                    /** 重新加载数据 */
                    $scope.reload();
                } else {
                    alert("操作失败！");
                }
            });
    };

    /** 显示修改 */
    $scope.show = function (entity) {
        /** 把json对象转化成一个新的json对象 */
        $scope.entity = JSON.parse(JSON.stringify(entity));
        $scope.entity.brandIds =JSON.parse(entity.brandIds);
        $scope.entity.specIds =JSON.parse(entity.specIds);
        $scope.entity.customAttributeItems = JSON
            .parse(entity.customAttributeItems);

    };

    /** 批量删除 */
    $scope.delete = function () {
        if ($scope.ids.length > 0) {
            baseService.deleteById("/typeTemplate/delete", $scope.ids)
                .then(function (response) {
                    if (response.data) {
                        /** 重新加载数据 */
                        $scope.reload();
                    } else {
                        alert("删除失败！");
                    }
                });
        } else {
            alert("请选择要删除的记录！");
        }
    };

    /** 新增行 */
    $scope.addTableRow = function () {
        $scope.entity.customAttributeItems.push({});
    };
    /** 删除行 */
    $scope.delTableRow = function (index) {
        $scope.entity.customAttributeItems.splice(index, 1);
    }
});
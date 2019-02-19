/** 定义控制器层 */
app.controller('itemCatController', function ($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});


    // 查询商品分类列表
    $scope.findByParentId = function (id) {
        $scope.ParentId = id;
        baseService.sendGet("/itemCat/findByParentId?parentId=" + id).then(function (value) {
            $scope.dataList = value.data;
        });
    };

    $scope.grade = 1;
    //面包屑导航
    $scope.selectList = function (entity, grade) {
        $scope.grade = grade;
        if (grade == 1) {
            $scope.itemCat1 = null;
            $scope.itemCat2 = null;
        }
        if (grade == 2) {
            $scope.itemCat1 = entity;
            $scope.itemCat2 = null;
        }
        if (grade == 3) {
            $scope.itemCat2 = entity;
        }
         $scope.findByParentId(entity.id);
    };


    /** 添加或修改 */
    $scope.saveOrUpdate = function () {
        var url = "save";
        if ($scope.entity.id) {
            url = "update";
        }
        $scope.entity.parentId = $scope.ParentId;
        /** 发送post请求 */
        baseService.sendPost("/itemCat/" + url, $scope.entity)
            .then(function (response) {
                if (response.data) {
                    /** 重新加载数据 */
                    $scope.findByParentId($scope.ParentId);
                } else {
                    alert("操作失败！");
                }
            });

        $scope.findByParentId($scope.ParentId);
    };

    /** 显示修改 */
    $scope.show = function (entity) {
        /** 把json对象转化成一个新的json对象 */
        $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function () {
        if ($scope.ids.length > 0) {
            baseService.deleteById("/itemCat/delete", $scope.ids)
                .then(function (response) {
                    if (response.data) {
                        /** 重新加载数据 */
                        $scope.findByParentId($scope.ParentId);
                    } else {
                        alert("删除失败！");
                    }
                });
        } else {
            alert("请选择要删除的记录！");
        }
    };

    //查询类型模板
    $scope.findTypeTemplateList = function () {
        baseService.sendGet("/typeTemplate/findTypeTemplateList").then(function (value) {
            $scope.typeTemplateList = value.data;
        })
    };

});
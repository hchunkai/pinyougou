/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    /** 添加或修改 */
    $scope.saveOrUpdate = function(){
        var url = "save";
        if ($scope.entity.id){
            url = "update";
        }
        /** 发送post请求 */
        baseService.sendPost("/goods/" + url, $scope.entity)
            .then(function(response){
                if (response.data){
                    /** 重新加载数据 */
                    $scope.reload();
                }else{
                    alert("操作失败！");
                }
            });
    };

    /** 显示修改 */
    $scope.show = function(entity){
       /** 把json对象转化成一个新的json对象 */
       $scope.entity = JSON.parse(JSON.stringify(entity));
    };

    /** 批量删除 */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.deleteById("/goods/delete", $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };


    // 定义数组显示状态
    $scope.status = ['未审核', '审核通过', '审核未通过', '关闭'];
    //定义数组显示分级信息
    //显示分类数组
    $scope.catItemList = [];
    $scope.findCatItemList=function () {
        baseService.sendGet("/itemCat/findCatItemList").then(function (value) {
            var res = value.data;
            for (var i = 0; i < res.length; i++) {
                $scope.catItemList[res[i].id] = res[i].name;
            }
        })
    };

    // 审核状态-->通过,驳回
    $scope.updateStatus=function (audit_status) {
        baseService.sendGet("/goods/updateStatus?auditStatus=" + audit_status+"&ids="+$scope.ids).then(function (value) {
            if (value.data) {
                alert("修改成功");
                $scope.reload();
            } else {
                alert("修改失败");
            }

        });
    }

});
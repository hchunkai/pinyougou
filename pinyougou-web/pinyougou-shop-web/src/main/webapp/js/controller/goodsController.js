/** 定义控制器层 */
app.controller('goodsController', function ($scope, $controller, baseService) {

    /** 指定继承baseController */
    $controller('baseController', {$scope: $scope});

    //根据父id查找分级类别
    $scope.findByParentId = function (parentId, name) {
        baseService.sendGet("/itemCat/findByParentId",
            "parentId=" + parentId).then(function (response) {
            $scope[name] = response.data;
        });
    };
    $scope.$watch('goods.category1Id', function (newv, oldv) {
        if (newv) {
            /** 根据选择的值查询二级分类 */
            $scope.findByParentId(newv, "itemCatList2");
        } else {
            $scope.itemCatList2 = [];
        }

    });
    $scope.$watch('goods.category2Id', function (newv, oldv) {
        if (newv) {
            /** 根据选择的值查询二级分类 */
            $scope.findByParentId(newv, "itemCatList3");
        } else {
            $scope.itemCatList3 = [];
        }

    });


    //监视category3Id的变化，是否获得模板id的值
    $scope.$watch('goods.category3Id', function (newV, oldV) {
        if (newV) {
            for (var i = 0; i < $scope.itemCatList3.length; i++) {
                var itemCat = $scope.itemCatList3[i];
                if (itemCat.id == newV) {
                    $scope.goods.typeTemplateId = itemCat.typeId;
                    break;
                }
            }
        }
        else {
            $scope.goods.typeTemplateId = null;
        }

    });

    //监视模板id，有值则查询品牌列表
    $scope.$watch('goods.typeTemplateId', function (newV, oldV) {
        if (!newV) {
            //清空下拉列表品牌的值
            $scope.brandIds = [];
            $scope.goods.goodsDesc.customAttributeItems = [];
            $scope.specList = [];
            return;
        }

        //查询商品品牌和扩展属性
        baseService.sendGet("/typeTemplate/findOne", "id=" + newV).then(function (value) {
            //品牌
            $scope.brandIds = JSON.parse(value.data.brandIds);
            //扩展属性
            $scope.goods.goodsDesc.customAttributeItems = JSON.parse(value.data.customAttributeItems);
        });

        //查询规格选项列表
        baseService.sendGet("/typeTemplate/findSpec?id=" + newV).then(function (value) {
            $scope.specList = value.data;
        });
    });


    //添加checkbox的信息，更新SpecAttr属性绑定goods.goodsDesc.specificationItems
    $scope.updateSpecAttr = function ($event, name, value) {
        //value="联通4G"
        // obj={"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
        var obj = $scope.findJsonByKey($scope.goods.goodsDesc.specificationItems, 'attributeName', name);
        if (obj) {
            //选中添加
            if ($event.target.checked) {
                obj.attributeValue.push(value);
            } else {
                obj.attributeValue.splice(obj.attributeValue
                    .indexOf(value), 1);

                //都没有选中
                if (obj.attributeValue.length == 0) {
                    $scope.goods.goodsDesc.specificationItems.splice($scope.goods.goodsDesc.specificationItems.indexOf(obj), 1);
                }
            }
        } else {
            /** 如果为空，则新增数组元素 */
            $scope.goods.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }

    };


    $scope.createItems = function () {
        /** 定义SKU数组，并初始化 */
        $scope.goods.items = [{
            spec: {}, price: 0, num: 9999,
            status: '0', isDefault: '0'
        }];

        /** 定义选中的规格选项数组 */
        var specItems = $scope.goods.goodsDesc.specificationItems;
        //循环specItems是中的值创建新的数组
        for (var i = 0; i < specItems.length; i++) {
            specItem = specItems[i];//{"attributeValue":["联通4G","移动4G","电信4G"],"attributeName":"网络"}
            /** 扩充原SKU数组方法 */
            $scope.goods.items = swapItems($scope.goods.items,
                specItem.attributeName,
                specItem.attributeValue);
        }
        //清空复选框
        if ($scope.goods.goodsDesc.specificationItems.length == 0) {
            $scope.goods.items = [];
        }
    };

    //使用specItems的值和旧的数组创建新的数组
    var swapItems = function (items, attrName, attrValue) {
        /** 创建新的SKU数组 */
        var newItems = new Array();
        for (var i = 0; i < items.length; i++) {
            /** 获取一个SKU商品 */
            var item = items[i];  //{spec: {}, price: 0, num: 9999,status: '0', isDefault: '0'}

            for (var j = 0; j < attrValue.length; j++) {
                //深克隆一个SKU
                var newItem = JSON.parse(JSON.stringify(item));
                newItem.spec[attrName] = attrValue[j];
                newItems.push(newItem);
            }
        }
        return newItems;
    };


    //初始化itemImages，还有specificationItems
    $scope.goods = {goodsDesc: {itemImages: [], specificationItems: []}};


    //上传图片
    $scope.uploadFile = function () {
        baseService.uploadFile().then(function (value) {
            if (value.status == 200) {
                $scope.picEntity.url = value.data.url;
            } else {
                alert("上传失败");
            }
        })
    };

    /** 数组中移除图片 */
    $scope.removePic = function (index) {
        $scope.goods.goodsDesc.itemImages.splice(index, 1);
    };

    //清空上传文件名
    $scope.clearFileName = function () {
        var file = document.getElementById('file');
        file.value = '';
    };

    //显示上传的图片
    $scope.addPic = function () {
        $scope.goods.goodsDesc.itemImages.push($scope.picEntity);
    };


    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function (page, rows) {
        baseService.findByPage("/goods/findByPage", page,
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
        if ($scope.goods.id) {
            url = "update";
        }
        $scope.goods.goodsDesc.introduction = editor.html();

        /** 发送post请求 */
        baseService.sendPost("/goods/" + url, $scope.goods)
            .then(function (response) {
                if (response.data) {
                    $scope.goods = {};
                    /** 清空富文本编辑器中的内容 */
                    editor.html('');
                    alert("操作成功!")
                } else {
                    alert("操作失败！");
                }
            });
    };


  //##############################  商品管理  ##############################################

    // 定义显示状态的数组
    $scope.status = ['未审核', '已审核', '审核不通过', '已关闭'];

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

    //更新上下架状态
    $scope.updateMarketStatus=function (is_marketable) {
        //  检查是否审核状态上架的商品
        if ($scope.ids.length > 0) {
            for (var i = 0; i < $scope.dataList.length; i++) {
                var goods = $scope.dataList[i];
                var status = $scope.ids.indexOf(goods.id);
                if (status >= 0) {
                    if (goods.auditStatus != "1") {
                        alert("请选择审核通过的产品!!")
                        return;
                    }
                }
            }
            baseService.sendGet("/goods/updateStatus?isMarketable=" + is_marketable+"&ids="+$scope.ids).then(function (value) {
                if (value.data) {
                    alert("修改成功");
                    $scope.reload();
                    // 清空ids数组
                    $scope.ids = [];
                } else {
                    alert("修改失败");
                }

            });
        }else {
            alert("请选择要上下架的商品!")
        }
    };





});
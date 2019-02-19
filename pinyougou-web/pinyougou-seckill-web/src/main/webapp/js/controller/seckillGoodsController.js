/** 定义秒杀商品控制器 */
app.controller("seckillGoodsController", function ($scope, $controller, baseService, $location,$timeout) {

    /** 指定继承cartController */
    $controller("baseController", {$scope: $scope});

    //查找秒杀商品
    $scope.findSeckillGoods = function () {
        baseService.sendGet("secKill/findSeckillGoods").then(function (response) {
            $scope.seckillGoodsList = response.data;
        });
    };

    //获地址栏发送过来的id
    var id = $location.search().id;
    //查找商品详情
    $scope.findOne = function () {
        baseService.sendGet("/secKill/findOne?id=" + id).then(function (response) {
            //得到商品详情
            $scope.entity = response.data;
            $scope.downcount($scope.entity.endTime);
        });
    };

    /**跳转到详情页 */
    $scope.jumpItem = function (id) {
        location.href = "/seckill-item.html?id=" + id;
    };

    /** 定义倒计时方法 */
    $scope.downcount = function (endTime) {
        //计算出相差时间
        var milliSeconds = new Date(endTime).getTime() - new Date().getTime();
        var seconds = milliSeconds / 1000;  //转换成秒
        if (seconds > 0) {  //秒杀没有结束
            //计算出天数,小时,分
            var minutes = Math.floor(seconds / 60);
            var hours = Math.floor(minutes / 60);
            var days = Math.floor(hours / 24);
            /** 定义resArr封装最后显示的时间 */
            var resArr = new Array();
            if (days > 0) {
                resArr.push(days + "天  ")
            }
            if (hours > 0) {
                resArr.push(hours - (days * 24) + ":");
            }
            if (minutes > 0) {
                resArr.push(minutes - (hours * 60) + ":");
            }
            if (seconds > 0) {
                resArr.push((seconds - minutes * 60).toFixed(0));
            }
            $scope.timeStr = resArr.join("");

            //定时任务
            $timeout(function () {
                $scope.downcount(endTime);
            },1000);
        } else {
            $scope.timeStr = "秒杀结束";
        }
    };

    /** 立即抢购 */
    $scope.submitOrder =function () {
        if ($scope.loginName) {//已登陆
            baseService.sendGet("/secKill/submitOrder?id=" + $scope.entity.id).then(function (reponse) {
                if (reponse.data) {
                    location.href = "/order/pay.html";
                } else {
                    alert("抢购失败");
                }
            });
        } else { // 没有登陆
            //跳转到登录页面
            location.href = "http://sso.pinyougou.com?service=" + $scope.redirectUrl;
        }
    }

});
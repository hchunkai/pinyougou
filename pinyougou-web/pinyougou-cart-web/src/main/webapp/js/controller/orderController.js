app.controller('orderController', function (baseService, $scope, $controller, $interval, $location) {
    $controller('cartController', {$scope: $scope});

    /**
     * 获得地址列表
     */
    $scope.findAddressByUser = function () {
        baseService.sendGet("/order/findAddressByUser").then(function (response) {
            $scope.addressList = response.data;
            for (var i in response.data) {
                if (response.data[i].isDefault == 1) {
                    $scope.address = response.data[i];
                    break;
                }
            }
        });
    };

    /**
     * 用户选中的地址
     * @param address
     */
    $scope.selectAddress = function (address) {
        $scope.address = address;
    };

    /**
     * 选中样式
     * @param address
     * @returns {boolean}
     */
    $scope.isSelectedAddress = function (address) {
        return $scope.address == address;
    };

    //初始化$scope.newAddress
    $scope.newAddress = {};

    //保存或者更新地址
    $scope.saveOrUpdate = function () {
        var url = "saveNewAddress";
        if ($scope.newAddress.id) {
            url = "updateNewAddress"
        }

        baseService.sendPost("/order/" + url, $scope.newAddress).then(function (response) {
            if (response.data) {
                $scope.findAddressByUser();
            } else {
                alert("操作失败");
            }
        });
    };

    //编辑显示当前地址
    $scope.show = function (address) {
        $scope.newAddress = JSON.parse(JSON.stringify(address));
    };

    //删除地址
    $scope.delete = function (id) {
        baseService.sendGet("/order/delete?id=" + id).then(function (response) {
            if (response.data) {
                $scope.findAddressByUser();
            } else {
                alert("操作失败");
            }
        });
    };

    // 定义order对象封装参数
    $scope.order = {paymentType: "1"};  //默认是微信支付
    //选择支付方式
    $scope.selectPayType = function (payType) {
        $scope.order.paymentType = payType;
    };


    /** 提交订单 */
    $scope.saveOrder = function () {
        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiver = $scope.address.contact;
        $scope.order.receiverMobile = $scope.address.mobile;
        //发送请求
        baseService.sendPost("/order/saveOrder", $scope.order).then(function (response) {
            if (response.data) {
                location.href = "/order/pay.html";
            } else {
                alert("订单提交失败");
            }
        });
    };

    /**
     * 获得微信支付二维码
     */
    $scope.genPayCode = function () {
        baseService.sendGet("/order/genPayCode").then(function (response) {
            //获取金额(分转化成元)
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            //获取订单交易号
            $scope.outTradeNo = response.data.outTradeNo;
            // 生成二维码
            var qr = new QRious({
                element: document.getElementById("qrious"),
                size: 250,
                level: 'H',
                value: response.data.codeUrl
            });


            /**
             * 开启定时器
             * 第一个参数：调用的函数
             * 第二个参数：时间毫秒数(3000毫秒也就是3秒)
             * 第三个参数：调用的总次数(60次)
             * */
            var timer = $interval(function () {
                // 发送请求，查询支付状态
                baseService.sendGet("/order/queryPayStatus?outTradeNo="
                    + $scope.outTradeNo).then(function (response) {
                    if (response.data.status == 1) { // 支付成功
                        // 取消定时器
                        $interval.cancel(timer);
                        location.href = "/order/paysuccess.html?money=" + $scope.money;
                    }
                    if (response.data.status == 3) { //支付失败
                        // 取消定时器
                        $interval.cancel(timer);
                        location.href = "/order/payfail.html"
                    }
                    if (response.data.status == 2) { //未支付
                        //过期倒计时
                        $scope.leftTime -= 1;
                        if ($scope.leftTime == 0) {
                            //显示二维码过期
                            $scope.fail = true;
                        }
                    }
                });
            }, 1000, 60);
            //初始化二维码过期时间
            $scope.leftTime = 60;
            // 执行60次(3分钟)之后需要回调的函数
            timer.then(function () {
                alert("微信支付二维码失效！");
            });
        });
    };

    /** 获取支付总金额 */
    $scope.getMoney = function(){
        return $location.search().money;
    };


});
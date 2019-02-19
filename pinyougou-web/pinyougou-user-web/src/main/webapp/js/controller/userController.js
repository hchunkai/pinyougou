/** 定义控制器层 */
app.controller('userController', function ($scope, baseService,$timeout) {


    //初始化user对象
    $scope.user = {};
    $scope.code = "";
    $scope.password = "";
    //保存注册用户
    $scope.save = function () {
        if ($scope.user.password != $scope.password) {
            alert("两次输入的密码不正确");
            return;
        }
        baseService.sendPost("/user/save?code="+$scope.code,$scope.user).then(function (response) {
            if (response.data) {
                alert("注册成功！");
                //清空注册页面
                $scope.user = {};
                $scope.password = "";
                $scope.code = "";
                $scope.checkCode = {};
            } else {
                alert("注册失败!");
            }
        });
    };

    // 定义按钮是否禁用的变量
    $scope.disabled = false;
    $scope.tip = "获取短信验证码";
    // $scope.checkCode={code: "123456"};  //测试用

    //获取手机验证码
    $scope.sendCode = function () {
        // 判断手机号码
        if (!$scope.user.phone || !/^1[3|4|8|5|6|2]\d{9}$/.test($scope.user.phone)) {
            alert("手机号码格式不正确！");
        }
        else {
            $scope.disabled = true;
            // 发送异步请求
            baseService.sendGet("/user/sendCode?phone=" + $scope.user.phone).then(function (response) {
                //response.data={success:true|false,code:xxxxxx}
                $scope.checkCode = response.data;
                if ($scope.checkCode.success) {
                    //调用倒计时
                    $scope.downCount(90);
                } else {
                    alert("发送失败");
                }
            });
        }
    };

    /** 倒计时功能 */
    $scope.downCount = function (seconds) {
        seconds--;
        if (seconds >= 0) {
            $scope.tip = seconds + "s后,可重新获取验证码!";
            /**
             * 开启定时器
             * 第一个参数：回调的函数
             * 第二个参数：时间毫秒数
             */
            $timeout(function () {
                $scope.downCount(seconds);
            }, 1000);
        } else {
            $scope.disabled = false;
            $scope.tip = "获取短信验证码";
        }
    };

});
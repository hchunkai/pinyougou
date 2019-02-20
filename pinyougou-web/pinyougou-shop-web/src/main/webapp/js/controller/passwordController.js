// 定义 密码  控制层
app.controller('passwordController',function ($scope,baseService) {
    // 修改密码
    // 定义密码对象
    $scope.pass = {};
    $scope.updatePassword=function () {

        if ($scope.pass.newPassword1 == $scope.pass.newPassword2){
           baseService.sendPost("/password/updatePassword",$scope.pass).then(function (response) {
               var maps = response.data;
               if (maps.falg==1){
                   alert("修改成功");
                   location.href="/logout";
               }else if (maps.falg == 0) {
                   alert("原密码输入错误");
               }else if (maps.falg ==2) {
                  alert("密码不能为空");
               }else {
                   alert("系统错误");
               }
           });
        } else{
            alert('两次输入密码不一致');
            $scope.pass.newPassword1='';
            $scope.pass.newPassword2='';
        }
    }



});
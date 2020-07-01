$(function () {

    $("#uploadForm").submit(upload);

})

function upload() {

    $.ajax({
        url: "http://upload.qiniup.com",
        type: 'post',
        processData: false,
        contentType: false,
        data: new FormData($("#uploadForm")[0])
    }).done(function (data) {
        console.log(data);
        // 成功了更新头像访问路径
        $.ajax({
            url: CONTEXT_PATH + "/user/header/url",
            type: 'post',
            data: {"fileName": $("input[name='key']").val()},
            dataType: 'json',
        }).done(function (data) {
            window.location.reload();
        }).fail(function(data){
            // 在提示框中显示返回的消息
            let errors = Object.values(data.responseJSON.data);
            $("#hintBody").text(errors);

            // 显示提示框
            $("#hintModal").modal("show");
            // 2秒后，自动隐藏提示框
            setTimeout(function () {
                $("#hintModal").modal("hide");
            }, 2000);
        });
    }).fail(function(data){
        console.log(data);
        alert("上传头像失败！")
    });
    // 返回假阻止提交事件向下执行
    return false;

}
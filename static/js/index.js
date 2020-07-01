$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");

    window.editor.sync();
    // 获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();

    // 发送 ajax 请求之前，将csrf令牌设置到请求的头中.
    // 每个需要 ajax 请求的页面都需要这样设置
    // var token = $("meta[name='_csrf']").attr("content");
    // var header = $("meta[name='_csrf_header']").attr("content");
    // $(document).ajaxSend(function (e, xhr, options) {
    //     xhr.setRequestHeader(header, token);
    // })

    $.ajax({
        url: CONTEXT_PATH + "/discuss",
        type: 'post',
        data: {"title": title, "content": content},
        dataType: 'json',
    }).done(function (data) {
        // 在提示框中显示返回的消息
        $("#hintBody").text(data.message);

        // 显示提示框
        $("#hintModal").modal("show");
        // 2秒后，自动隐藏提示框
        setTimeout(function () {
            $("#hintModal").modal("hide");
            // 刷新页面
            if (data.status === 200) {
                window.location.reload();
            }
        }, 2000);
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

}
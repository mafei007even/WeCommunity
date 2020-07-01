$(function () {

    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
    $("#restoreBtn").click(restorePost);
    $("#shareBtn").click(sharePost);

})

function errorHandler(data) {
    // 是否登陆
    if (data.responseJSON.status === 403) {
        let goLogin = confirm("您还没有登陆，没有权限！是否现在登陆？");
        if (goLogin === true){
            location.href = data.responseJSON.data;
        }
        return;
    }
    // 在提示框中显示返回的消息
    let errors;
    if (data.responseJSON.data != null) {
        errors = Object.values(data.responseJSON.data);
    } else{
        errors = data.responseJSON.message;
    }
    alert(errors);
}

var count = 1;

function repeatGetImg(img) {
    setTimeout(function () {
        var originSrc = $(img).attr("src");
        $(img).attr("src", originSrc + count++)
    }, 1000);
}

function sharePost() {
    var htmlUrl = location.href;

    // 显示提示
    $("#toastBody").text("正在生成分享图...")
    $("#toast").toast("show");

    $.ajax({
        url: CONTEXT_PATH + "/share",
        type: "get",
        dataType: 'json',
        data: {"htmlUrl": htmlUrl}
    }).done(function (data) {

        // x秒后,显示提示框
        setTimeout(function () {
            $("#toast").toast("hide");
            $("#shareImg").attr("src", data.data + "?");
            $("#resultModal").modal("show");
        }, 1000);

    }).fail(function (data) {
        $("#toast").toast("hide");
        errorHandler(data);
    })
}

function setTop() {
    var tip;
    var type; //'0-普通; 1-置顶;'
    var successMsg;
    var btnVal;

    if ($("#topBtn").text() === "置顶") {
        tip = "确定将该帖子置顶吗？";
        type = 1;
        successMsg = "置顶成功！"
        btnVal = "取消置顶"
    } else{
        tip = "确定将该帖子取消置顶吗？";
        type = 0;
        successMsg = "取消置顶成功！"
        btnVal = "置顶"
    }

    if (!confirm(tip)){
        return;
    }
    let postId = $("#postId").val();
    $.ajax({
        url: CONTEXT_PATH + "/discuss/top",
        type: "post",
        dataType: 'json',
        data: {"postId": postId, 'type': type}
    }).done(function (data) {
        $("#topBtn").text(btnVal)
        alert(successMsg)
    }).fail(function (data) {
        errorHandler(data);
    })
}

function setWonderful() {
    var tip;
    var status; //'0-正常; 1-精华; 2-拉黑;'
    var successMsg;
    var btnVal;

    if ($("#wonderfulBtn").text() === "加精") {
        tip = "确定将该帖子加精吗？";
        status = 1;
        successMsg = "加精成功！"
        btnVal = "取消加精"
    } else{
        tip = "确定将该帖子取消加精吗？";
        status = 0;
        successMsg = "取消加精成功！"
        btnVal = "加精"
    }

    if (!confirm(tip)){
        return;
    }
    let postId = $("#postId").val();
    $.ajax({
        url: CONTEXT_PATH + "/discuss/wonderful",
        type: "post",
        dataType: 'json',
        data: {"postId": postId, 'status': status}
    }).done(function (data) {
        $("#wonderfulBtn").text(btnVal);
        alert(successMsg)
    }).fail(function (data) {
        errorHandler(data);
    })
}

function setDelete() {
    if (!confirm("确定将该帖子删除吗？")){
        return;
    }
    let postId = $("#postId").val();
    $.ajax({
        url: CONTEXT_PATH + "/discuss/delete",
        type: "post",
        dataType: 'json',
        data: {"postId": postId}
    }).done(function (data) {
        alert("删除成功！")
        location.href = CONTEXT_PATH + "/index";
    }).fail(function (data) {
        errorHandler(data);
    })
}

function restorePost() {
    if (!confirm("确定将该已删除的帖子恢复吗？")){
        return;
    }
    let postId = $("#postId").val();
    $.ajax({
        url: CONTEXT_PATH + "/discuss/restore",
        type: "post",
        dataType: 'json',
        data: {"postId": postId}
    }).done(function (data) {
        alert("恢复成功！");
        window.location.reload()
    }).fail(function (data) {
        errorHandler(data);
    })
}

function like(btn, entityType, entityId, entityUserId, postId) {

    $.ajax({
        url: CONTEXT_PATH + "/like",
        type: "post",
        dataType: 'json',
        data: {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId, "postId": postId}
    }).done(function (data) {
        // 改变dom： 赞还是已赞，点赞数
        $(btn).children("i").text(data.data.likeCount);
        $(btn).children("b").text(data.data.likeStatus == 1 ? "已赞" : "赞");
    }).fail(function (data) {
        // 是否登陆
        if (data.responseJSON.status === 403) {
            let goLogin = confirm("您还没有登陆，不能点赞！是否现在登陆？");
            if (goLogin === true){
                location.href = data.responseJSON.data;
            }
            return;
        }
        alert(data.responseJSON.message);
    })

}

function goProfile(userId){
    location.href = CONTEXT_PATH + "/profile/" + userId;
}
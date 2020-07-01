$(function(){
	$(".follow-btn").click(follow);
});

function follow() {
	var btn = this;
	if($(btn).hasClass("btn-info")) {
		// 关注TA
		$.ajax({
			url: CONTEXT_PATH + "/follow",
			type: 'post',
			dataType: 'json',
			data: {"entityType" : 3, 'entityId': $(this).prev().val()}
		}).done(function (data) {
			window.location.reload();
		}).fail(function (data) {
			// 是否登陆
			if (data.responseJSON.status === 403) {
				let goLogin = confirm("您还没有登陆，不能关注！是否现在登陆？");
				if (goLogin === true) {
					location.href = data.responseJSON.data;
				}
				return;
			}
			console.log(data);
		});
		// $(btn).text("已关注").removeClass("btn-info").addClass("btn-secondary");
	} else {
		// 取消关注
		$.ajax({
			url: CONTEXT_PATH + "/unfollow",
			type: 'post',
			dataType: 'json',
			data: {"entityType" : 3, 'entityId': $(this).prev().val()}
		}).done(function (data) {
			window.location.reload();
		}).fail(function (data) {
			// 是否登陆
			if (data.responseJSON.status === 403) {
				let goLogin = confirm("您还没有登陆，不能取消关注！是否现在登陆？");
				if (goLogin === true) {
					location.href = data.responseJSON.data;
				}
				return;
			}
			console.log(data);
		});
		// $(btn).text("关注TA").removeClass("btn-secondary").addClass("btn-info");
	}
}
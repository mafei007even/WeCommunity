KindEditor.plugin('image', function (K) {
	var self = this, name = 'image',
		allowImageUpload = K.undef(self.allowImageUpload, true),
		allowImageRemote = K.undef(self.allowImageRemote, true),
		formatUploadUrl = K.undef(self.formatUploadUrl, true),
		allowFileManager = K.undef(self.allowFileManager, false),
		extraParams = K.undef(self.extraFileUploadParams, {
			'token': '',//添加token
			'key':''
		}), //七牛云上传需要返回的token和key参数
		// filePostName = K.undef(self.filePostName, 'file'),
		uploadJson = K.undef(self.uploadJson, 'http://up-z2.qiniup.com') //七牛云上传url华南区
		imageTabIndex = K.undef(self.imageTabIndex, 0),
		imgPath = self.pluginsPath + 'image/images/',
		filePostName = K.undef(self.filePostName, 'file'), //七牛云上传文件类型为file
		fillDescAfterUploadImage = K.undef(self.fillDescAfterUploadImage, false),
		lang = self.lang(name + '.');
	self.plugin.imageDialog = function (options) {
		var imageUrl = options.imageUrl,
			imageWidth = K.undef(options.imageWidth, ''),
			imageHeight = K.undef(options.imageHeight, ''),
			imageTitle = K.undef(options.imageTitle, ''),
			imageAlign = K.undef(options.imageAlign, ''),
			showRemote = K.undef(options.showRemote, true),
			showLocal = K.undef(options.showLocal, true),
			tabIndex = K.undef(options.tabIndex, 0),
			clickFn = options.clickFn;
		var target = 'kindeditor_upload_iframe_' + new Date().getTime();
		var qiniuToken = '';
		var hiddenElements = [];
		for (var k in extraParams) {
			hiddenElements.push('<input type="hidden" name="' + k + '" value="' + extraParams[k] + '" />');
		}
        //html就是点击图片小按钮弹出框的html代码。'<form class="ke-upload-area ke-form"就是点击上传时的form,里面就是上传时需要的各种参数。
		var html = [
			'<div style="padding:20px;">',
			'<div class="tabs"></div>',
			'<div class="tab1" style="display:none;">',
			'<div class="ke-dialog-row">',
			'<label for="remoteUrl" style="width:60px;">' + lang.remoteUrl + '</label>',
			'<input type="text" id="remoteUrl" class="ke-input-text" name="url" value="" style="width:200px;" /> &nbsp;',
			'</div>',
			'<div class="ke-dialog-row">',
			'<label for="remoteWidth" style="width:60px;">' + lang.size + '</label>',
			lang.width + ' <input type="text" id="remoteWidth" class="ke-input-text ke-input-number" name="width" value="" maxlength="4" /> ',
			lang.height + ' <input type="text" class="ke-input-text ke-input-number" name="height" value="" maxlength="4" /> ',
			'<img class="ke-refresh-btn" src="' + imgPath + 'refresh.png" width="16" height="16" alt="" style="cursor:pointer;" title="' + lang.resetSize + '" />',
			'</div>',
			'<div class="ke-dialog-row">',
			'<label style="width:60px;">' + lang.align + '</label>',
			'<input type="radio" name="align" class="ke-inline-block" value="" checked="checked" /> <img name="defaultImg" src="' + imgPath + 'align_top.gif" width="23" height="25" alt="" />',
			' <input type="radio" name="align" class="ke-inline-block" value="left" /> <img name="leftImg" src="' + imgPath + 'align_left.gif" width="23" height="25" alt="" />',
			' <input type="radio" name="align" class="ke-inline-block" value="right" /> <img name="rightImg" src="' + imgPath + 'align_right.gif" width="23" height="25" alt="" />',
			'</div>',
			'<div class="ke-dialog-row">',
			'<label for="remoteTitle" style="width:60px;">' + lang.imgTitle + '</label>',
			'<input type="text" id="remoteTitle" class="ke-input-text" name="title" value="" style="width:200px;" />',
			'</div>',
			'</div>',
			'<div class="tab2" style="display:none;">',
			'<iframe name="' + target + '" style="display:none;"></iframe>',
			'<form class="ke-upload-area ke-form" enctype="multipart/form-data" method="post" target="' + target + '" action="' + uploadJson + '">',
			'<div class="ke-dialog-row">',
			hiddenElements.join(''),
			'<label style="width:60px;">' + lang.localUrl + '</label>',
			'<input type="text" name="localUrl" class="ke-input-text" tabindex="-1" style="width:200px;" readonly="true" /> &nbsp;',
			'<input type="button" class="ke-upload-button" value="' + lang.upload + '" />',
			'</div>',
			'</form>',
			'</div>',
			'</div>'
		].join('');
		var dialogWidth = showLocal || allowFileManager ? 450 : 400,
			dialogHeight = showLocal && showRemote ? 300 : 250;
		var dialog = self.createDialog({
			name: name,
			width: dialogWidth,
			height: dialogHeight,
			title: self.lang(name),
			body: html,
			yesBtn: {
				name: self.lang('yes'),
				click: function (e) {
					if (dialog.isLoading) {
						return;
					}
					if (showLocal && showRemote && tabs && tabs.selectedIndex === 1 || !showRemote) {
						if (uploadbutton.fileBox.val() == '') {
							alert(self.lang('pleaseSelectFile'));
							return;
						}
						dialog.showLoading(self.lang('uploadLoading'));
						uploadbutton.submit();
						localUrlBox.val('');
						return;
					}
					var url = K.trim(urlBox.val()),
						width = widthBox.val(),
						height = heightBox.val(),
						title = titleBox.val(),
						align = '';
					alignBox.each(function () {
						if (this.checked) {
							align = this.value;
							return false;
						}
					});
					if (url == 'http://' || K.invalidUrl(url)) {
						alert(self.lang('invalidUrl'));
						urlBox[0].focus();
						return;
					}
					if (!/^\d*$/.test(width)) {
						alert(self.lang('invalidWidth'));
						widthBox[0].focus();
						return;
					}
					if (!/^\d*$/.test(height)) {
						alert(self.lang('invalidHeight'));
						heightBox[0].focus();
						return;
					}
					clickFn.call(self, url, title, width, height, 0, align);
				}
			},
			beforeRemove: function () {
				widthBox.unbind();
				heightBox.unbind();
				refreshBtn.unbind();
			}
		}),
			div = dialog.div;
		var urlBox = K('[name="url"]', div),
			localUrlBox = K('[name="localUrl"]', div),
			widthBox = K('.tab1 [name="width"]', div),
			heightBox = K('.tab1 [name="height"]', div),
			refreshBtn = K('.ke-refresh-btn', div),
			titleBox = K('.tab1 [name="title"]', div),
			alignBox = K('.tab1 [name="align"]', div);
		var tabs;
		if (showRemote && showLocal) {
			tabs = K.tabs({
				src: K('.tabs', div),
				afterSelect: function (i) { }
			});
			tabs.add({
				title: lang.remoteImage,
				panel: K('.tab1', div)
			});
			tabs.add({
				title: lang.localImage,
				panel: K('.tab2', div)
			});
			tabs.select(tabIndex);
		} else if (showRemote) {
			K('.tab1', div).show();
		} else if (showLocal) {
			K('.tab2', div).show();
		}
        //点击上传按钮的事件上传到upload-z2.qiniu.com，我直接在afterUpload里将返回的url插入到编辑器self.insertHtml('<img src="'+ IMGURL + data.key +'" />').hideDialog().focus();
		var uploadbutton = K.uploadbutton({
			button: K('.ke-upload-button', div)[0],
			fieldName: filePostName,
			form: K('.ke-form', div),
			target: target,
			width: 60,
			afterUpload: function (data) {
				console.log(data);
				dialog.hideLoading();
				if (data.error === 0) {
					var url = data.url;
					if (formatUploadUrl) {
						url = K.formatUrl(url, 'absolute');
					}
					if (self.afterUpload) {
						self.afterUpload.call(self, url, data, name);
					}
					if (!fillDescAfterUploadImage) {
						clickFn.call(self, url, data.title, data.width, data.height, data.border, data.align);
					} else {
						K(".ke-dialog-row #remoteUrl", div).val(url);
						K(".ke-tabs-li", div)[0].click();
						K(".ke-refresh-btn", div).click();
					}
				} else {
					// alert(IMGURL + data.key);
					self.insertHtml('<img src="'+ IMGURL + data.key +'" />').hideDialog().focus();
				}
			},
			afterError: function (html) {
				dialog.hideLoading();
				self.errorDialog(html);
			}
		});
        //uploadbutton.fileBox就是选择文件后的那个input框，获取选择的图片的名字
		uploadbutton.fileBox.change(function (e) {
			localUrlBox.val(uploadbutton.fileBox.val());
            //这是自己的后台获取token的接口，将token和key的值插入到html中extra的input中
			$.ajax({
				url: APIURL + '/rest/upload/image/token',
				type: 'post',
				xhrFields: {
					withCredentials: true
				},
				data: { name: localUrlBox.val() },
				success: function (resp) {
					if (resp.code === 200) {
						qiniuToken = resp.result.upToken;
						K('[name="token"]', div).val(resp.result.upToken);
						K('[name="key"]', div).val(resp.result.upKey);
					}
				}
			})
		});
 
		var originalWidth = 0, originalHeight = 0;
		function setSize(width, height) {
			widthBox.val(width);
			heightBox.val(height);
			originalWidth = width;
			originalHeight = height;
		}
		refreshBtn.click(function (e) {
			var tempImg = K('<img src="' + urlBox.val() + '" />', document).css({
				position: 'absolute',
				visibility: 'hidden',
				top: 0,
				left: '-1000px'
			});
			tempImg.bind('load', function () {
				setSize(tempImg.width(), tempImg.height());
				tempImg.remove();
			});
			K(document.body).append(tempImg);
		});
		widthBox.change(function (e) {
			if (originalWidth > 0) {
				heightBox.val(Math.round(originalHeight / originalWidth * parseInt(this.value, 10)));
			}
		});
		heightBox.change(function (e) {
			if (originalHeight > 0) {
				widthBox.val(Math.round(originalWidth / originalHeight * parseInt(this.value, 10)));
			}
		});
		urlBox.val(options.imageUrl);
		setSize(options.imageWidth, options.imageHeight);
		titleBox.val(options.imageTitle);
		alignBox.each(function () {
			if (this.value === options.imageAlign) {
				this.checked = true;
				return false;
			}
		});
		if (showRemote && tabIndex === 0) {
			urlBox[0].focus();
			urlBox[0].select();
		}
		return dialog;
	};
	self.plugin.image = {
		edit: function () {
			var img = self.plugin.getSelectedImage();
			self.plugin.imageDialog({
				imageUrl: img ? img.attr('data-ke-src') : 'http://',
				imageWidth: img ? img.width() : '',
				imageHeight: img ? img.height() : '',
				imageTitle: img ? img.attr('title') : '',
				imageAlign: img ? img.attr('align') : '',
				showRemote: allowImageRemote,
				showLocal: allowImageUpload,
				tabIndex: img ? 0 : imageTabIndex,
				clickFn: function (url, title, width, height, border, align) {
					if (img) {
						img.attr('src', url);
						img.attr('data-ke-src', url);
						img.attr('width', width);
						img.attr('height', height);
						img.attr('title', title);
						img.attr('align', align);
						img.attr('alt', title);
					} else {
						self.exec('insertimage', url, title, width, height, border, align);
					}
					setTimeout(function () {
						self.hideDialog().focus();
					}, 0);
				}
			});
		},
		'delete': function () {
			var target = self.plugin.getSelectedImage();
			if (target.parent().name == 'a') {
				target = target.parent();
			}
			target.remove();
			self.addBookmark();
		}
	};
	self.clickToolbar(name, self.plugin.image.edit);
});
var App = (function () {
    function UI(name, opts, callback) {
        //tab 切换效果封装
        var tabToggle = {
            "init"     : function () {
                this.dom = opts.dom;
                this.activeClass = opts.activeClass;
                this.defaultClass = opts.defaultClass || null;
                this.bindEvent();
            },
            "bindEvent": function (e) {
                var _self = this;
                var _activeClass = _self.activeClass;
                var _defaultClass = _self.defaultClass;
                var tabItem = _self.dom.children();
                tabItem.each(function (index) {
                    $(this).on('click', function (e) {
                        var me = $(this);
                        var dataFor = me.attr('data-for');
                        var currentTabContent = $('#' + dataFor);
                        if ( me.hasClass(_activeClass) ) {
                            _defaultClass && me.removeClass(_defaultClass);
                        }
                        me.addClass(_activeClass);
                        currentTabContent.show();
                        tabItem.each(function (index) {
                            var me = $(this);
                            if ( me.attr('data-for') !== dataFor ) {
                                me.removeClass(_activeClass);
                                _defaultClass && me.addClass(_defaultClass);
                                $('#' + me.attr('data-for')).hide();
                            }
                        });
                    });
                });
            }
        };
        //画廊效果封装
        var gallary = {
            "activeImg"   : null,
            "init"        : function () {
                this.dom = opts.dom;
                this.bindEvent();
            },
            "bindEvent"   : function () {
                var _self = this;
                var _dom = _self.dom;
                _dom.on('click', 'img', function () {
                    _self.toggleImg($(this));
                })
            },
            "toggleImg"   : function (curImg) {
                var _self = this;
                var dataActive = curImg.attr('data-active') - 0;
                dataActive ? _self.reductionImg(curImg) : _self.enlargeImg(curImg);
            },
            "enlargeImg"  : function (curImg) {
                var _self = this;
                curImg.animate({
                    'position': 'absolute',
                    'top'     : '15%',
                    'left'    : '10%',
                    'width'   : '80%',
                    'z-index' : '111'
                }, 500, 'ease');
                curImg.attr('data-active', 1);
                _self.showLayer();
                _self.activeImg = curImg;
            },
            "reductionImg": function (curImg) {
                var _self = this;
                curImg.animate({
                    'position': 'static',
                    'top'     : '0',
                    'left'    : '0',
                    'width'   : '100%',
                    'z-index' : 'none'
                }, 500, 'linner');
                curImg.attr('data-active', 0);
                _self.hideLayer();
            },
            "showLayer"   : function () {
                var _self = this;
                var layer = '<div class="layer"></div>';
                $('.app-bd').append(layer);
                $('.layer').on('click', function () {
                    _self.reductionImg(_self.activeImg);
                    $(this).remove();
                })
            },
            "hideLayer"   : function () {
                $('.layer').remove();
            }
        };
        //加载更多效果封装
        var roadMore = {
            "init"       : function () {
                this.dom = opts.dom;//更多按钮
                this.listWrap = opts.listwrap;//需追加列表容器
                this.mode = opts.mode; //模块名称
                this.url = opts.url;//数据请求地址
                this.params = this.data;//数据请求参数
                this.bindEvent();
            },
            "getNewHtml" : function (data) {
                var slist = '';
                var alist = [];
                var l;
                switch ( this.mode ) {
                    case 'eat':
                        var data = [//测试数据
                            {
                                "title"  : "新内容标题1",
                                "content": "内容内容内容内容内容"
                            },
                            {
                                "title"  : "新内容标题1",
                                "content": "内容内容内容内容内容"
                            },
                            {
                                "title"  : "新内容标题1",
                                "content": "内容内容内容内容内容"
                            }
                        ];
                        data && (l = data.length);
                        //拿到返回的data拼接新数据
                        for ( var i = 0; i < l; i++ ) {
                            slist = [
                                '<div class="p2">',
                                '    <h1 class="t2">【' + data[i].title + '】</h1>',
                                '    <p>' + data[i].content + '</p>',
                                '</div>'].join("");
                            alist.push(slist);
                        }
                        break;
                    case 'comment':
                        var data = [//测试数据
                            {
                                "title"  : "new title gjsdljfs",
                                "time"   : "2014-01-01",
                                "content": "新简述新简述新简述新简述新简述新简述新简述新简述"
                            },
                            {
                                "title"  : "new title gjsdljfs",
                                "time"   : "2014-01-01",
                                "content": "新简述新简述新简述新简述新简述新简述"
                            },
                            {
                                "title"  : "new title gjsdljfs",
                                "time"   : "2014-01-01",
                                "content": "新简述新简述新简述新简述"
                            }
                        ];
                        data && (l = data.length);
                        //拿到返回的data拼接新数据
                        for ( var i = 0; i < l; i++ ) {
                            slist = [
                                '<li>',
                                '    <h1 class="t1">' + data[i].title + '</h1>',
                                '    <h2>',
                                '        <i class="ui-ico-02 ui-ico-02-star"></i>',
                                '        <i class="ui-ico-02 ui-ico-02-star"></i>',
                                '        <i class="ui-ico-02 ui-ico-02-star"></i>',
                                '        <i class="ui-ico-02 ui-ico-02-star"></i>',
                                '        <i class="ui-ico-02 ui-ico-02-star-empty"></i>',
                                '        <em class="txt01 txt01-01 fr">' + data[i].time + '</em>',
                                '    </h2>',
                                '    <p class="p1">' + data[i].content + '</p>',
                                '</li>'].join("");
                            alist.push(slist);
                        }
                        break;
                    default :
                        console.log('这是一个彩蛋，还愣着干嘛，买彩票去啊~');
                }
                return alist.join("");
            },
            "bindEvent"  : function () {
                var _self = this;
                this.dom.on('click', function () {//点击更多后触发
                    _self.showLoading();//显示加载，即转圈圈
                    //发起要加载的信息请求
                    _self.getAjaxData(_self.url, _self.params, function (data) {//请求成功回调
                        _self.hideLoading();//隐藏转圈圈
                        var newHtml = _self.getNewHtml(data);//拼接html
                        _self.listWrap.append(newHtml); //插入列表
                    });
                    /*setTimeout(function () { //模拟请求行为，执行请求回调逻辑，实际使用应注释掉
                        _self.hideLoading();
                        var newHtml = _self.getNewHtml();
                        _self.listWrap.append(newHtml);
                    }, 2000);*/
                });
            },
            "showLoading": function () {
                var load = '<div class="ui-loading"></div>'
                this.listWrap.append(load);
            },
            "hideLoading": function () {
                $('.ui-loading').remove();
            },
            "getAjaxData": function (url, params, callback, type) {
                $.ajax({ //登录验证请求
                    type    : type || 'POST',
                    url     : url,
                    data    : params,
                    dataType: 'json'
                }).done(function (data) {
                    if ( data ) {
                        callback && callback(data);
                    }
                }).fail(function (data) {
                    console.log('ajax error!!');
                });
            }
        };
        var moduleNameMap = {
            "tabToggle": tabToggle,
            "gallary"  : gallary,
            "roadMore" : roadMore
        };
        name && moduleNameMap[name].init();
    }

    return {
        "UI": UI
    };
})();
$(function () {
    var tabDetails = $('#tab-details');
    tabDetails.length && App.UI('tabToggle', { //tab切换效果
        'dom'        : tabDetails,
        'activeClass': 'active'
    });
    var tabCircle = $('#tab-circle');
    tabCircle.length && App.UI('tabToggle', { //旅行圈tab切换效果
        'dom'        : tabCircle,
        'activeClass': 'active'
    });
    var picList = $('#gallery');
    picList.length && App.UI('gallary', { //画廊
        'dom': picList
    });
    var eatLoadmore = $('#eat_loadmore');
    var eatListwrap = $('#eat_listwrap');
    eatListwrap.length && App.UI('roadMore', { //吃什么 加载更多
        "dom"     : eatLoadmore,
        "listwrap": eatListwrap,
        "mode"    : 'eat',
        "url"     : '/ca/getlistjson.json',//数据请求地址
        "data"    : {//数据请求参数
            "param01": 'p1'
        }
    });
    var commentLoadmore = $('#comment_loadmore');
    var commentListwrap = $('#comment_listwrap');
    /*var params={
        'pageId':'我是页面id值'
    };*/
    commentListwrap.length && App.UI('roadMore', { //信息中心 加载更多
        "dom"     : commentLoadmore,
        "listwrap": commentListwrap,
        "mode"    : 'comment',
        "url"     : 'http://api.taozilvxing.cn/taozi/poi/view-spots/548040a89fb7882b7882b6dca5fa2/comments',
        "data"    : modulename.XXXparams
    });
})
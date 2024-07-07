// 初始化
document.addEventListener("DOMContentLoaded", function() {
    window.$aside = document.querySelector('aside');
    window.$main = document.querySelector('main');
    window.$photoWall = document.querySelector('.photo-wall');
    window.pathname = location.pathname;
    activeNavigation();
    initRoutes();
});

// 页面回退事件
window.addEventListener('popstate', function() {
    loadMainPage(location.pathname);
});

// 激活导航菜单
function activeNavigation($currLink) {
    const $activedLink = $aside.querySelector('a.actived');
    $activedLink && $activedLink.removeClass('actived');
    $currLink = $currLink || $aside.querySelector(`[data-path="${pathname}"]`);
    if ($aside.contains($currLink)) {
        $currLink.addClass('actived');
    }
}

// 初始化单页路由
function initRoutes(selector) {
    selector = selector || document;
    const $links = selector.querySelectorAll('[data-path]:not([data-path=""])');
    for (const $link of $links) {
        $link.onclick = () => {
            loadMainPage($link.dataset.path);
            activeNavigation($link);
        };
    }
}

// 根据路由加载页面
async function loadMainPage(path) {
    progress.start($main);
    const response = await fetch(path, { headers: { 'x-http-request': true } });
    setHTMLWithScript($main, await response.text());
    initRoutes($main);
    changeHistoryState(path);
    progress.done();
}

// 设置 innerHTML 并执行其中的脚本
function setHTMLWithScript(el, html) {
    const event = new Event("unload");
    el.dispatchEvent(event);
    el.innerHTML = html;
    const scripts = el.querySelectorAll('script');

    for (const script of scripts) {
        if (script.text) {
            const newScript = document.createElement('script');
            newScript.text = '(function() { '+script.text+' })()';
            script.replaceWith(newScript);
        }
    }
}

// 初始化 PhotoSwipe 组件
function initPhotoSwipe() {
    // 勾选元素捕获 Photoswipe 点击缩略图事件以防止传播
    const checkboxes = document.querySelectorAll('.photo-wall input[type="checkbox"]');
    for (const checkbox of checkboxes) {
        checkbox.onclick = (e) => e.stopPropagation();
    }

    // 初始化 PhotoSwipe Lightbox 实例
    const lightbox = new PhotoSwipeLightbox({
        gallery: '.photo-wall',
        children: 'a',
        pswpModule: PhotoSwipe,
        loop: false,
        zoom: false,
        wheelToZoom: true,
    });
    lightbox.init();
}

// 动态设置地址栏
function changeHistoryState(path) {
    if (pathname != path) {
        pathname = path;
        history.pushState(null, null, path);
    }
}

// 进度条组件
const progress = {
    start(container) {
        if (this.status) return;
        const rect = container.getBoundingClientRect();
        this.$instance = Thyme.util.createElement(`<div style="
            position: fixed; z-index: 999; left: ${rect.x}px; top: ${rect.y-1}px; width: 0; height: 1px;
            background: #b3261e; transition: width .3s linear"></div>`);
        document.body.append(this.$instance);

        this._observe();
        this.status = 1;
        this._trickle = setInterval(() => {
            if (this.status < 99) {
                this.status += Math.round(((100 - this.status) / 3) * Math.random());
            }
        }, 300);
    },

    done() {
        if (!this.status) return;
        this.status = 100;
        clearInterval(this._trickle);

        setTimeout(() => {
            this.status = 0;
            this.$instance.remove();
        }, 300);
    },

    _observe() {
        if (this._observed) return;
        this._observed = true;

        let value = this.status;
        Object.defineProperty(this, 'status', {
            get: () => value,
            set: v => {
                value = v;
                this.$instance.style.width = v + '%';
            }
        });
    }
}
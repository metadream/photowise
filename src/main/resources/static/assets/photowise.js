// 初始化
const viewModes = ['brick', 'grid'];
const $aside = document.querySelector('aside');
const $segment = document.querySelector('#segment');
const $photoWall = document.querySelector('.photo-wall');
let pathname = location.pathname;
let modeIndex = 0;
activeNavigation();
initRoutes();

// 页面回退事件
window.addEventListener('popstate', function() {
    loadSegment(location.pathname);
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
            loadSegment($link.dataset.path);
            activeNavigation($link);
        };
    }
}

// 切换视图模式
function toggleView() {
    const preMode = viewModes[modeIndex];
    modeIndex = modeIndex^1;
    const newMode = viewModes[modeIndex];
    $photoWall.classList.replace(preMode, newMode);
}

// 根据路由加载页面
async function loadSegment(path) {
    progress.start();
    const response = await fetch(path, { headers: { 'x-http-request': true } });
    $segment.innerHTML = await response.text();
    initRoutes($segment);
    changeHistoryState(path);
    progress.done();
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
    start() {
        if (this.status) return;
        const rect = $segment.getBoundingClientRect();
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
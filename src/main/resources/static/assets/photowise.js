document.addEventListener('DOMContentLoaded', function() {
    initRoutes();
    window.pathname = location.pathname;
    window.$aside = document.querySelector('aside');
    window.$segment = document.querySelector('#segment');
});

window.addEventListener('popstate', function() {
    loadSegment(location.pathname);
});

function initRoutes(selector) {
    selector = selector || document;
    const $links = selector.querySelectorAll('[data-path]:not([data-path=""])');
    for (const $link of $links) {
        $link.onclick = () => {
            loadSegment($link.dataset.path);
            checkNavigationState($link);
        };
    }
}

function checkNavigationState($currLink) {
    const $activedLink = $aside.querySelector('a.actived');
    $activedLink && $activedLink.removeClass('actived');
    if ($aside.contains($currLink)) {
        $currLink.addClass('actived');
    }
}

async function loadSegment(path) {
    progress.start($segment);
    setTimeout(async () => {
        const response = await fetch(path, { headers: { 'x-http-request': true } });
        $segment.innerHTML = await response.text();
        initRoutes($segment);
        changeHistoryState(path);
        progress.done();
    }, 500);
}

function changeHistoryState(path) {
    if (pathname != path) {
        pathname = path;
        history.pushState(null, null, path);
    }
}

const progress = {
    start(container) {
        if (this.status) return;

        this.$instance = Thyme.util.createElement(`<div style="
            position: absolute; z-index: 999; left: 0; top: -1px; width: 0; height: 1px;
            background: #b3261e; transition: width .3s linear"></div>`);
        container = container || document.body;
        container.append(this.$instance);

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
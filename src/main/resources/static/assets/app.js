class App {
    static instance;
    routes = {};
    currPage = null;
    
    constructor() {
        window.addEventListener('popstate', () => this.#loadPage());
    }

    static getInstance() {
        App.instance = App.instance || new App();
        return App.instance;
    }

    static mount(target) {
        const app = App.getInstance();
        app.$mount = document.querySelector(target);
        app.#initRoutes();
        app.#loadPage();
        return app;
    }
    
    static Page(options = {}) {
        const app = App.getInstance();
        const path = location.pathname;

        let page = app.routes[path];
        if (!page) {
            page = new Page(app, options);
            app.routes[path] = page;
        }
        app.currPage = page.load();
        return page;
    }
    
    // Init elements with 'data-path' attribute
    #initRoutes(scope) {
        scope = scope || document;
        const $routes = scope.querySelectorAll('[data-path]:not([data-path=""])');
        for (const $route of $routes) {
            $route.onclick = () => {
                const path = $route.dataset.path;
                history.pushState(null, null, path);
                this.#loadPage(path);
            };
        }
    }
    
    // Load single page in async
    async #loadPage(path) {
        progress.start(this.$mount);
        path = path || location.pathname;
        const response = await fetch(path, { headers: { 'x-spa-request': true } });
        const html = await response.text();
        
        this.currPage && this.currPage.unload();
        this.#setInnerHTML(this.$mount, html);
        this.#initRoutes(this.$mount);
        this.#activeRoutes(path);
        progress.done();
    }
    
    // Set inner html and run script code
    #setInnerHTML(el, html) {
        el.innerHTML = html;
        const scripts = el.querySelectorAll('script');
        for (const script of scripts) {
            if (script.text) {
                const newScript = document.createElement('script');
                newScript.text = script.text;
                script.replaceWith(newScript);
            }
        }
    }
    
    // Active routes style
    #activeRoutes(path) {
        const $activedRoutes = document.querySelectorAll('[data-path].actived');
        for (const $route of $activedRoutes) {
            $route.removeClass('actived');
        }
        const $routes = document.querySelectorAll(`[data-path="${path}"]`);
        for (const $route of $routes) {
            $route.addClass('actived');
        }
    }
}

class Page {
    constructor(app, options = {}) {
        this.app = app;
        this.options = options;
    }
    
    load() {
        const { onLoad } = this.options;
        onLoad && onLoad();
        return this;
    }
    
    unload() {
        const { onUnload } = this.options;
        onUnload && onUnload();
        return this;
    }
}

// Progress Component
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
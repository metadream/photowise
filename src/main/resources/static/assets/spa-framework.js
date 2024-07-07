const App = {
    $mount: null,
    currentRoute: null,
    routes: {}, // { path: { load, unload } }
    
    navigate(path) {
        const route = this.routes[path];
        this.currentRoute = route;    
        history.pushState(null, null, path);
        route.load();
    },
    
    mount(target) {
        this.$mount = document.querySelector(target);
        this._initRoutes();
    },
    
    onPageUnload(fn) {
        const path = location.pathname;
        this.routes[path].unload = fn;
    },
    
    _initRoutes(scope) {
        scope = scope || document;
        const $routes = scope.querySelectorAll('[data-path]:not([data-path=""])');
        for (const $route of $routes) {
            const path = $route.dataset.path;
            this.routes[path] = {
                load: () => this._loadPage(path),
                unload: () => console.log('----unload', path)
            }
            $route.onclick = () => this.navigate();
        }
    },
    
    async _loadPage(path) {
        progress.start(this.$mount);
        path = path || location.pathname;
        const response = await fetch(path, { headers: { 'x-spa-request': true } });
        const html = await response.text();
        
        this.currentRoute.unload();
        
        this.#setInnerHTML(this.$mount, html);
        this.#initRoutes(this.$mount);
        this.#activeRoutes(path);
        progress.done();
    }
}
class Spa {
    $mount = null;
    route = null;
    router = new Router();

    // Add unload handler to route
    set onPageUnload(fn) {
        this.router.update(location.pathname, fn);
    }

    // Mount target and init
    mount(target) {
        this.$mount = document.querySelector(target);
        this.#initRoutes();

        const path = location.pathname;
        if (!this.router.find(path)) {
            this.router.add(path, () => this.#loadPage(path));
        }

        this.navigate(path);
        window.addEventListener('popstate', () => {
            this.navigate(location.pathname);
        });
    }

    // Init routes by elements with 'data-path' attribute
    #initRoutes(scope) {
        scope = scope || document;
        const $routes = scope.querySelectorAll('[data-path]:not([data-path=""])');

        for (const $route of $routes) {
            const path = $route.dataset.path;
            this.router.add(path, () => this.#loadPage(path));

            $route.onclick = () => {
                history.pushState(null, null, path);
                this.navigate(path);
            };
        }
    }

    // Navigate specified path
    navigate(path) {
        this.route && this.route.unload();
        this.route = this.router.find(path);
        if (!this.route) throw new Error('Route not found.');

        Thyme.Progress.start(this.$mount);
        this.route.load().then(() => {
            this.#initRoutes(this.$mount);
            this.#activeRoutes(path);
            Thyme.Progress.done();
        });
    }

    // Add actived style to route elements
    #activeRoutes(path) {
        let $routes = document.querySelectorAll('[data-path].actived');
        for (const $route of $routes) {
            $route.classList.remove('actived');
        }
        $routes = document.querySelectorAll(`[data-path="${path}"]`);
        for (const $route of $routes) {
            $route.classList.add('actived');
        }
    }

    // Load page content in async
    async #loadPage(path) {
        const response = await fetch(path, { headers: { 'x-spa-request': true } });
        const html = await response.text();
        this.#renderPage(this.$mount, html);
    }

    // Render page and run scripts
    #renderPage(el, html) {
        el.innerHTML = html;
        const scripts = el.querySelectorAll('script');

        for (const script of scripts) {
            const newScript = document.createElement('script');
            newScript.text = script.text;

            for (const name of script.getAttributeNames()) {
                newScript.setAttribute(name, script.getAttribute(name));
            }
            script.replaceWith(newScript);
        }
    }
}

class Router {
    routes = {};

    add(path, load) {
        this.routes[path] = { load, unload: () => {} };
    }

    update(path, unload) {
        const route = this.find(path);
        if (!route) throw new Error('Route not found');
        route.unload = unload;
    }

    find(path) {
        return this.routes[path];
    }
}

export default new Spa();
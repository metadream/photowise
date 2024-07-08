class Spa {
    $mount = null;
    route = null;
    router = new Router();

    // Mount target and init
    mount(target) {
        this.$mount = document.querySelector(target);
        this.#initRoutes();

        this.#navigate(location.pathname);
        window.addEventListener('popstate', () => {
            this.#navigate(location.pathname);
        });
    }

    // Add unload handler to route
    set onPageUnload(fn) {
        this.router.update(location.pathname, fn);
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
                this.#navigate(path);
            };
        }
    }

    // Navigate specified path
    #navigate(path) {
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
            if (script.text) {
                const newScript = document.createElement('script');
                const attrNames = script.getAttributeNames();

                for (const name of attrNames) {
                    newScript.setAttribute(name, script.getAttribute(name));
                }
                newScript.text = script.text;
                script.replaceWith(newScript);
            }
        }
    }
}

class Router {
    routes = {};

    add(path, load) {
        this.routes[path] = { load, unload: ()=>{} };
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
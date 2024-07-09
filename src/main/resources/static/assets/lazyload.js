class LazyLoad {
    loadImage(img) {
        const dataSrc = img.getAttribute('data-src');
        if (dataSrc) {
            img.removeAttribute('data-src');
            img.src = dataSrc;
            img.onload = () => {
                console.log('===========loaded');
            }
            img.onerror = () => {
                console.log('===========error');
            }
        }
    }

    observeImages(scope) {
        const imgs = scope.querySelectorAll('img[data-src]');
        for (const img of imgs) {
            iObserver.observe(img);
        }
    }

    observe(root) {
        root = document.querySelector(root);
        this.iObserver = new IntersectionObserver((entries, observer) => {
            for (const entry of entries) {
                const { target, isIntersecting, intersectionRatio } = entry;

                if (isIntersecting || intersectionRatio > 0) {
                    loadImage(target);
                    observer.unobserve(target);
                }
            }
        }, { root });

        const mObserver = new MutationObserver(mutations => {
            for (let mutation of mutations) {
                for (const node of mutation.addedNodes) {
                    if (node.nodeType === 1) observeImages(node);
                }
            }
        });
        mObserver.observe(root, { attributes: true, childList: true, subtree: true });
    }
}

export default new LazyLoad();
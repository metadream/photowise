/**
 * 进度条组件
 * @example Progress.start();
 */
export const Progress = {
    start(container) {
        if (this.status) return;

        container = container || document.body;
        const rect = container.getBoundingClientRect();
        this.$instance = document.createElement('div');
        this.$instance.setAttribute('style', `
            position: fixed; z-index: 999; background: #b3261e; transition: width .3s linear;
            left: ${rect.x}px; top: ${rect.y - 1}px; max-width: ${rect.width}px; width: 0; height: 1px;
        `);
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
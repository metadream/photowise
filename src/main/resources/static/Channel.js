/**
 * WebSocket 封装
 * @example 
 * const channel = new Channel();
 * channel.on('message', fn);
 * channel.on('custom-event', fn);
 */
export class Channel {

    #endpoint = null;
    #socket = null;
    #locked = false;
    #retryInterval = 5000;

    constructor(path) {
        if (/^wss?:\/\//.test(path)) {
            this.#endpoint = path;
        } else {
            const protocol = location.protocol == 'https:' ? 'wss:' : 'ws:';
            this.#endpoint = protocol + '//' + location.host + (path ?? '');
        }
        this.#createWebSocket();
    }

    on(event, fn) {
        addEventListener(this.#alias(event), e => {
            fn.call(this, e.detail);
        });
        return this;
    }

    send(message) {
        this.#socket.send(message);
    }

    close() {
        this.#socket.close();
    }

    #createWebSocket() {
        this.#socket = new WebSocket(this.#endpoint);
        this.#socket.onerror = () => this.#reconnect();
        this.#socket.onopen = () => console.log('Channel is opened.');

        this.#socket.onmessage = e => {
            const { event, data } = JSON.parse(e.data);
            dispatchEvent(new CustomEvent(this.#alias(event), { detail: data }));
        };
    }

    #reconnect() {
        if (this.#locked) return;
        this.#locked = true;

        setTimeout(() => {
            this.#createWebSocket();
            this.#locked = false;
        }, this.#retryInterval);
    }

    #alias(e) {
        return e === 'message' ? '__' : e;
    }

}
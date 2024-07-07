class Channel {
    
    static #ENDPOINT = 'ws://127.0.0.1:9000/channel/';
    #endpoint = null;
    #socket = null;
    #locked = false;
    #retryInterval = 5000;

    static subscribe(channel) {
        return new Channel(Channel.#ENDPOINT + channel);
    }

    constructor(endpoint) {
        this.#endpoint = endpoint;
        this.#createWebSocket();
    }
    
    close() {
        this.#socket.close();
    }

    #createWebSocket() {
        this.#socket = new WebSocket(this.#endpoint);
        this.#socket.onopen = () => console.log('Channel is opened.');
        this.#socket.onerror = () => this.#reconnect();

        this.#socket.onmessage = e => {
            const { event, data } = JSON.parse(e.data);
            dispatchEvent(new CustomEvent(this.#alias(event), { detail: data }));
        };
    }

    #reconnect() {
        if(this.#locked) return;
        this.#locked = true;

        setTimeout(() => {
            this.#createWebSocket();
            this.#locked = false;
        }, this.#retryInterval);
    }

    #alias(e) {
        return e === 'message' ? '__' : e;
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

}
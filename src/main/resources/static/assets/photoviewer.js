let rect,
    x = 0,
    y = 0,
    scale = 1,
    minScale = 0.2,
    maxScale = 16,
    isPointerdown = false, // 按下标识
    lastPointermove = { x: 0, y: 0 }; // 用于计算diff

    
class PhotoViewer {
    
    scale = 1;
    minScale = 1;
    maxScale = 5;
    
    constructor(s) {
        this.addElementPrototype();

        const container = typeof s === 'string' ? document.querySelector(s) : s;
        this.items = container.querySelectorAll('a');
        this.items.forEach((item, index) => {
            item.style.cursor = 'pointer';
            item.thumbnail = item.querySelector('img');
            item.original = item.getAttribute('href');
            item.removeAttribute('href');
            item.removeAttribute('target');

            item.addEventListener('click', e => {
                this.index = index;
                this.createMask();
                this.createControls();
                this.createImage();
                this.preview(item);
            });
        });
    }

    createMask() {
        this.mask = document.createElement('div');
        document.body.append(this.mask);
        setTimeout(() => this.mask.style.opacity = 1, 0);

        this.mask.onclick = e => this.close(e);
        this.mask.setStyle({
            position: 'fixed',
            zIndex: 990,
            top: 0,
            bottom: 0,
            left: 0,
            right: 0,
            background: 'rgba(0, 0, 0, 0.8)',
            transition: 'all .3s',
            opacity: 0,
        });

        document.addEventListener('keyup', this.escape = e => {
            if (e.keyCode == 27) this.close(e); // ESC key
        })
    }

    createControls() {
        this.controls = document.createElement('div');
        document.body.append(this.controls);
        setTimeout(() => this.controls.style.opacity = 1, 0);

        this.controls.setStyle({
            transitionDelay: '.3s',
            opacity: 0,
        });

        const close = document.createElement('div');
        close.onclick = e => this.close(e);
        close.innerHTML = '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="24" height="24"><path d="M811 171q18 0 30 12t12 30q0 18-12 31L572 512l269 268q12 13 12 31t-12 30-30 12q-18 0-31-12L512 572 244 841q-13 12-31 12t-30-12-12-30q0-18 12-31l269-268-269-268q-12-13-12-31t12-30 30-12q18 0 31 12l268 269 268-269q13-12 31-12z" fill="#fff"/></svg>';
        close.setStyle({
            position: 'fixed',
            zIndex: 992,
            top: 0,
            right: 0,
            color: '#fff',
            padding: '20px',
            cursor: 'pointer',
        });

        const slideBtnCss = {
            position: 'fixed',
            zIndex: 992,
            top: '45%',
            color: '#fff',
            padding: '20px',
            cursor: 'pointer',
        };

        // 向左箭头
        const prev = document.createElement('div');
        prev.innerHTML = '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="22" height="22"><path d="M409 512l404 404a63 63 0 11-90 89L275 557a63 63 0 010-90L723 19a63 63 0 1190 89L409 512z" fill="#eee"/></svg>';
        prev.setStyle(Object.assign({}, slideBtnCss, {
            left: 0,
            display: this.index ? 'block' : 'none'
        }));
        prev.onclick = () => {
            this.preview(this.items[--this.index])
        };

        // 向右箭头
        const count = this.items.length;
        const next = document.createElement('div');
        next.innerHTML = '<svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg" width="22" height="22"><path d="M648 512L270 134a59 59 0 1184-85l421 421a59 59 0 010 84L354 975a59 59 0 11-84-85l378-378z" fill="#eee"/></svg>';
        next.setStyle(Object.assign({}, slideBtnCss, {
            right: 0,
            display: this.index < count - 1 ? 'block' : 'none'
        }));
        next.onclick = () => {
            this.preview(this.items[++this.index])
        };

        this.controls.append(close);
        this.controls.append(prev);
        this.controls.append(next);

        // 监听index变化设置按钮可见性
        let value = this.index;
        Object.defineProperty(this, 'index', {
            get: () => value,
            set: v => {
                value = v;
                prev.style.display = v ? 'block' : 'none';
                next.style.display = v < count - 1 ? 'block' : 'none';
            }
        });
    }

    createImage() {
        if (this.image) return;
        this.image = new Image();
        this.image.setStyle({
            position: 'fixed',
            zIndex: 991,
        });
        document.body.append(this.image);
        // 监听手势
        //this.addTouchListeners()
        this.image.addEventListener('wheel', e => this.scalingImage(e), { passive: false });
    }

    scalingImage(e) {
        e.preventDefault();
        this.scale += e.deltaY * -0.01;
        this.scale = Math.min(Math.max(this.minScale, this.scale), this.maxScale);
        
        const image = e.target;
        image.style.transformOrigin = `${e.offsetX}px ${e.offsetY}px`;
        image.style.transform = `scale(${this.scale})`;
    }

    preview(item) {
        // 复制缩略图
        const thumb = item.thumbnail;
        this.image.src = thumb.src;

        // 缩略图在页面中的坐标和尺寸
        const bound = thumb.getBoundingClientRect();

        // 缩略图宽高比
        const ratio = bound.width / bound.height;
        // 原图在窗口中允许的最大显示尺寸
        let width = innerHeight * ratio;
        let height = innerHeight;
        if (width > innerWidth) {
            width = innerWidth;
            height = innerWidth / ratio;
        };

        // 缩放后的位移
        const x = bound.x - (innerWidth - bound.width) / 2;
        const y = bound.y - (innerHeight - bound.height) / 2;
        // 缩略图相对于原图的缩放比
        const scale = bound.width / width;
        this.oTransform = {
            x,
            y,
            scale
        };

        // 定位到缩略图原来的位置（取消动画）
        this.image.setStyle({
            left: (innerWidth - width) / 2 + 'px',
            top: (innerHeight - height) / 2 + 'px',
            width: width + 'px',
            height: height + 'px',
            transform: `translate3d(${x}px, ${y}px, 0) scale(${scale})`
        });

        // 放大到适应窗口
        this.animate(this.transform = {
            x: 0,
            y: 0,
            scale: 1
        });
        // 设置原图
        this.image.src = item.original;
    }

    addTouchListeners() {
        const hammer = new Hammer(this.image)
        hammer.on('tap', e => {
            this.transform.scale = this.transform.scale == 1 ? this.MAX_SCALE : 1
            this.transform.x = (this.image.offsetWidth / 2 + this.image.offsetLeft - e.center.x) * (this.transform.scale - 1)
            this.transform.y = (this.image.offsetHeight / 2 + this.image.offsetTop - e.center.y) * (this.transform.scale - 1)
            this.animate(this.transform)
        })
        hammer.on('swipeleft', e => {
            if (Math.abs(e.velocityX) < 1) return
            if (this.index == this.items.length - 1) {
                this.close(e)
            } else {
                this.preview(this.items[++this.index])
            }
        })
        hammer.on('swiperight', e => {
            if (Math.abs(e.velocityX) < 1) return
            if (this.index == 0) {
                this.close(e)
            } else {
                this.preview(this.items[--this.index])
            }
        })
        hammer.on('panstart', e => {
            if (this.transform.scale === 1) return
            this.image.style.cursor = 'grab'
            this.image.style.transition = 'none'
        })
        hammer.on('panmove', e => {
            if (this.transform.scale === 1) return
            this.image.style.transform = `translate3d(${this.transform.x + e.deltaX}px, ${this.transform.y + e.deltaY}px, 0) scale(${this.transform.scale})`
        })
        hammer.on('panend', e => {
            if (this.transform.scale === 1) return
            this.transform.x += e.deltaX
            this.transform.y += e.deltaY
            this.image.style.cursor = this.transform.scale == 1 ? 'zoom-in' : 'zoom-out'
        })
    }

    animate(t) {
        setTimeout(() => {
            this.image.setStyle({
                cursor: t.scale == 1 ? 'zoom-in' : 'zoom-out',
                transition: 'all .3s',
                transform: `translate3d(${t.x}px, ${t.y}px, 0) scale(${t.scale})`,
            })
        }, 0)
    }

    close(e) {
        document.removeEventListener('keyup', this.escape)
        this.mask.style.opacity = 0
        this.controls.remove()
        this.animate(this.oTransform)
        this.image.addEventListener('transitionend', () => {
            this.mask.remove()
            this.image.remove()
            this.image = null
        })
    }

    addElementPrototype() {
        Element.prototype.setStyle = function(css, ext) {
            for (let key in css) this.style[key] = css[key]
        }
    }
}
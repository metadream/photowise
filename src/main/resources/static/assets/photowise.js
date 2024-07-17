import PhotoSwipeLightbox from '/assets/photoswipe-lightbox@5.4.4.js';
import PhotoSwipeVideo from '/assets/photoswipe-video@1.0.2.js';

// 照片信息侧边栏
class ExifSidebar {
    static width = '300px';
    
    static {
        this.sidebar = Thyme.util.createElement('<div class="exif-sidebar"></div>');
        this.sidebar.style.transform = `translateX(${this.width})`;
        this.sidebar.style.width = this.width;
        this.sidebar.append(document.querySelector('#exif-sidebar').content);
        document.body.append(this.sidebar);
    }

    static show() {
         this.sidebar = document.querySelector('.exif-sidebar');
         this.sidebar.style.transition = 'none';
         this.sidebar.style.transform = `translateX(0)`;
    }

    static hide() {
        this.sidebar.style.transition = '333ms';
        this.sidebar.style.transform = `translateX(${this.width})`;
    }
    
    static bind(data) {
        console.log(data);
        const $ = (s) => this.sidebar.querySelector(s);
        $('[name="photoTime"]').innerHTML = data.photoTime;
        $('[name="makeModel"]').innerHTML = data.cameraInfo.makeModel;
    }
}

// 照片墙初始化
export function initPhotoSwipe() {
    // 勾选元素捕获 Photoswipe 点击缩略图事件以防止传播
    const checkboxes = document.querySelectorAll('.photo-wall input[type="checkbox"]');
    for (const checkbox of checkboxes) {
        checkbox.onclick = (e) => e.stopPropagation();
    }

    // 初始化 PhotoSwipe 组件
    const lightbox = new PhotoSwipeLightbox({
        pswpModule: () => import ('/assets/photoswipe@5.4.4.js'),
        gallery: '.photo-wall',
        children: 'a',
        mainClass: 'pswp-with-perma-preloader',
        loop: false,
        zoom: false,
        wheelToZoom: true,

        // 图片视口根据容器大小适配（侧边栏带来的调整）
        getViewportSizeFn: function(options, pswp) {
            if (!pswp.element) return;
            const rect = pswp.element.getBoundingClientRect();
            return {
                x: rect.width,
                y: window.innerHeight
            };
        }
    });
    
    // 关闭时恢复容器大小并隐藏侧边栏
    lightbox.on('close', () => {
        lightbox.pswp.element.style.right = '0';
        ExifSidebar.hide();
    });

    // 自定义UI组件
    lightbox.on('uiRegister', function() {
        // 添加图片数据指示
        lightbox.pswp.ui.registerElement({
            name: 'image-indicator',
            order: 5,
            html: '<span id="resolution"></span><span id="length"></span><span id="zoom-level"></span>',

            onInit: (el, pswp) => {
                const $resolution = el.querySelector('#resolution');
                const $length = el.querySelector('#length');
                const $zoomLevel = el.querySelector('#zoom-level');

                // 分辨率和大小
                pswp.on('change', () => {
                    const { width, height, element } = pswp.currSlide.data;
                    const { length } = element.dataset;
                    $resolution.innerText = width + '×' + height;
                    $length.innerText = Thyme.util.formatBytes(length);
                });
                // 缩放百分比
                pswp.on('zoomPanUpdate', (e) => {
                    if (e.slide === pswp.currSlide) {
                        $zoomLevel.innerText = Math.round(pswp.currSlide.currZoomLevel * 100) + '%';
                    }
                });
            }
        });

        // 添加信息图标
        lightbox.pswp.ui.registerElement({
            name: 'info-button',
            order: 9,
            isButton: true,
            tagName: 'button',
            html: '<svg class="icon info"><use xlink:href="/icons/icons.svg#info"></use></svg>',

            onInit: (el, pswp) => {
                // 根据侧边栏调整容器大小
                el.onclick = () => {
                    pswp.element.style.width = 'auto';
                    pswp.element.style.right = ExifSidebar.width;
                    pswp.updateSize();
                    ExifSidebar.show();
                }
                // 设置侧边栏字段数据
                pswp.on('change', () => {
                    const { photoId } = pswp.currSlide.data.element.dataset;
                    Thyme.http.get('/photo/'+photoId).then(res => ExifSidebar.bind(res));
                });
            }
        });
    });

    // 添加视频插件
    new PhotoSwipeVideo(lightbox, {});
    lightbox.init();
}
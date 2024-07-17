import PhotoSwipeLightbox from '/assets/photoswipe-lightbox@5.4.4.js';
import PhotoSwipeVideo from '/assets/photoswipe-video@1.0.2.js';

// 元数据侧边栏
class ExifSidebar {
    static width = '300px';

    static show() {
         let sidebar = document.querySelector('.exif-sidebar');
         if (!sidebar) {
             sidebar = Thyme.util.createElement('<div class="exif-sidebar"></div>');
             sidebar.style.width = this.width;
             sidebar.append(document.querySelector('#exif-sidebar').content);
             document.body.append(sidebar);
         }
         sidebar.style.transition = 'none';
         sidebar.style.transform = `translateX(0)`;
         sidebar.addClass('visible');
    }

    static hide() {
        const sidebar = document.querySelector('.exif-sidebar');
        sidebar.style.transition = '333ms';
        sidebar.style.transform = `translateX(${this.width})`;
    }
}

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
        getViewportSizeFn: function(options, pswp) {
            if (pswp.element) {
                const rect = pswp.element.getBoundingClientRect();
                return {
                    x: rect.width,
                    y: window.innerHeight
                };
            }
        }
    });
    
    lightbox.on('close', () => {
        const { element } = lightbox.pswp;
        element.style.right = '0';
        ExifSidebar.hide();
    });

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
                el.onclick = () => {
                    pswp.element.style.width = 'auto';
                    pswp.element.style.right = ExifSidebar.width;
                    pswp.updateSize();
                    ExifSidebar.show();
                }
                pswp.on('change', () => {
                    const { photoId } = pswp.currSlide.data.element.dataset;
                    console.log("photoId=", photoId);
                });
            }
        });
    });

    // 添加视频插件
    new PhotoSwipeVideo(lightbox, {});
    lightbox.init();
}
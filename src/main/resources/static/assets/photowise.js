import PhotoSwipeLightbox from '/assets/photoswipe-lightbox@5.4.4.js';
import PhotoSwipeVideo from '/assets/photoswipe-video@1.0.2.js';

export function initPhotoSwipe() {
    // 勾选元素捕获 Photoswipe 点击缩略图事件以防止传播
    const checkboxes = document.querySelectorAll('.photo-wall input[type="checkbox"]');
    for (const checkbox of checkboxes) {
        checkbox.onclick = (e) => e.stopPropagation();
    }

    // 初始化 PhotoSwipe 组件
    const lightbox = new PhotoSwipeLightbox({
        pswpModule: () => import('/assets/photoswipe@5.4.4.js'),
        gallery: '.photo-wall',
        children: 'a',
        mainClass: 'pswp-with-perma-preloader',
        loop: false,
        zoom: false,
        wheelToZoom: true,
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
            html: '<img class="icon" src="/icons/info.svg"/>',

            onInit: (el, pswp) => {
                el.onclick = () => {
                    alert('1111');
                }
                pswp.on('change', () => {
                    const { photoId } = pswp.currSlide.data.element.dataset;
                    console.log("photoId=",photoId);
                });
            }
        });
    });
    
    // 添加视频插件
    new PhotoSwipeVideo(lightbox, {});
    lightbox.init();
}
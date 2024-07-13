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
        // 添加缩放百分比指示
        lightbox.pswp.ui.registerElement({
            name: 'zoom-level-indicator',
            order: 5,
            onInit: (el, pswp) => {
                pswp.on('zoomPanUpdate', (e) => {
                    if (e.slide === pswp.currSlide) {
                        el.innerText = Math.round(pswp.currSlide.currZoomLevel * 100) + '%';
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
                    console.log(pswp.currSlide.data.element.dataset.photoId);
                });
            }
        });
    });

    // 添加视频插件
    new PhotoSwipeVideo(lightbox, {});
    lightbox.init();
}
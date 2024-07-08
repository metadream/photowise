import PhotoSwipeLightbox from '/assets/photoswipe-lightbox@5.4.4.js';

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
        loop: false,
        zoom: false,
        wheelToZoom: true,
    });
    lightbox.init();
}
// 勾选元素捕获 Photoswipe 点击缩略图事件以防止传播
const checkboxes = document.querySelectorAll('.photo-wall input[type="checkbox"]');
for (const checkbox of checkboxes) {
    checkbox.onclick = (e) => e.stopPropagation();
}

// 初始化 PhotoSwipe Lightbox 实例
const lightbox = new PhotoSwipeLightbox({
  gallery: '.photo-wall',
  children: 'a',
  pswpModule: PhotoSwipe,
  loop: false,
  zoom: false,
  wheelToZoom: true,
});
lightbox.init();
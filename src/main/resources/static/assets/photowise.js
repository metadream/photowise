import PhotoSwipeLightbox from './photoswipe-lightbox@5.4.4.js';
import PhotoSwipeVideo from './photoswipe-video@1.0.2.js';

/** 照片信息侧边栏 */
class ExifSidebar {
    static width = '300px';
    static isHidden = true;
    static element = null;
    static photoIndex = null;

    static {
        this.element = Thyme.util.createElement('<div class="exif-sidebar"></div>');
        this.element.style.transform = `translateX(${this.width})`;
        this.element.style.width = this.width;
        this.element.append(document.querySelector('#exif-sidebar').content);
        document.body.append(this.element);

        const input = this.element.querySelector('input[name="title"]');
        input.onblur = () => {
            input.value = input.value.trim();
            if (!input.value) return;
            if (this.photoIndex.title === input.value) return;

            this.photoIndex.title = input.value;
            Thyme.http.put('/photo', this.photoIndex).then(() => {
                Thyme.success('保存成功');
            });
        }
    }

    static show() {
        this.element.style.transition = 'none';
        this.element.style.transform = `translateX(0)`;
        this.isHidden = false;
    }

    static hide() {
        this.element.style.transition = '.5s';
        this.element.style.transform = `translateX(${this.width})`;
        this.isHidden = true;
    }

    static bind(data) {
        this.photoIndex = data;
        const { mediaInfo, cameraInfo, geoLocation } = data;
        const { latitude, longitude, altitude } = geoLocation ?? {};

        Thyme.form.setJsonObject(this.element, {
            title: data.title,
            timeZone: data.timeZone,
            photoTime: Thyme.util.formatDate(new Date(data.photoTime), 'yyyy-MM-dd hh:mm:ss'),
            makeModel: cameraInfo.makeModel ?? '',
            apertureValue: cameraInfo.apertureValue ? 'ƒ/' + cameraInfo.apertureValue : '',
            shutterSpeed: cameraInfo.shutterSpeed ? cameraInfo.shutterSpeed + 's' : '',
            focalLength: cameraInfo.focalLength ? cameraInfo.focalLength + 'mm' : '',
            isoEquivalent: cameraInfo.isoEquivalent ? 'ISO' + cameraInfo.isoEquivalent : '',
            path: data.path.split('/').pop(),
            size: mediaInfo.width + ' × ' + mediaInfo.height,
            length: Thyme.util.formatBytes(data.fileLength),
            latilong: latitude && longitude ? latitude?.toFixed(4) + ', ' + longitude?.toFixed(4) : '',
            altitude: altitude ? altitude.toFixed(1) + 'm' : ''
        });
    }
}

/** 照片墙初始化 */
export function initPhotoSwipe() {
    // 勾选元素捕获 Photoswipe 点击缩略图事件以防止传播
    const checkboxes = document.querySelectorAll('.photo-wall input[type="checkbox"]');
    for (const checkbox of checkboxes) {
        checkbox.onclick = e => e.stopPropagation();
    }

    // 初始化 PhotoSwipe 组件
    const lightbox = new PhotoSwipeLightbox({
        pswpModule: () => import ('./photoswipe@5.4.4.js'),
        gallery: '.photo-wall',
        children: 'a',
        mainClass: 'pswp-with-perma-preloader',
        loop: false,
        zoom: false,
        wheelToZoom: true,
        trapFocus: false,

        // 图片视口根据容器大小适配（侧边栏带来的调整）
        getViewportSizeFn: (options, pswp) => {
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
        // 添加缩放百分比指示
        lightbox.pswp.ui.registerElement({
            name: 'zoom-level',
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
            html: '<svg class="icon info"><use xlink:href="/icons/icons.svg#info"></use></svg>',

            onInit: (el, pswp) => {
                // 根据侧边栏调整容器大小
                el.onclick = () => {
                    if (ExifSidebar.isHidden) {
                        pswp.element.style.width = 'auto';
                        pswp.element.style.right = ExifSidebar.width;
                        ExifSidebar.show();
                    } else {
                        pswp.element.style.right = '0';
                        ExifSidebar.hide();
                    }
                    pswp.updateSize();
                }
                // 设置侧边栏字段数据
                pswp.on('change', () => {
                    const { photoId } = pswp.currSlide.data.element.dataset;
                    Thyme.http.get('/photo/' + photoId).then(res => ExifSidebar.bind(res));
                });
            }
        });
    });

    // 添加视频插件
    new PhotoSwipeVideo(lightbox, {});
    lightbox.init();
}
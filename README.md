# Photowise

## TODO
- 移动端适配
- 用户管理：guest/public/只读权限、菜单setting与权限
- 地图添加点：leaflet
- 国际化
- 时间轴
- 设置：语言、回收站清理周期（1天、1周、1月）;public,protected,private
- 清空回收站：同时删除索引、trash文件、缩略图
- 存储空间：usedSpace/(usedSpace+file.getUsableSpace())
- thyme.http finally写法
- library 显示：当前照片库路径：
- 所有从配置文件读取的library改为从数据库读取
- setting缓存
- photowall独立成组件，可自由关联controlbar，type module和window.分开写
- thyme radio 访问模式

```
@GetMapping("/test/video/stream")
public void streaming(HttpServletResponse response) throws IOException {
    response.setHeader("Content-Type", "image/jpeg");
    VideoCapture capture = new VideoCapture("./video.mp4");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);

    Mat mat = new Mat();
    if (capture.read(mat)) {
        MatOfByte buff = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, buff);
        byte[] bytes = buff.toArray();

        resourceHandler.copy(new ByteArrayInputStream(bytes), response.getOutputStream());

    }
}
```

## Dependencies

Build opencv and create opencv_java.jar and libopencv_java4100.so
```
sudo apt update && sudo apt install -y cmake g++ wget unzip
wget -O opencv-4.10.0.zip https://github.com/opencv/opencv/archive/4.10.0.zip
unzip opencv-4.10.0.zip

mkdir -p build && cd build
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
cmake -DBUILD_SHARED_LIBS=OFF ../opencv-4.10.0
make -j8
```

## Deployment

```
# vi /etc/systemd/system/photowise.service

[Unit]
Description=Photowise Service
After=syslog.target

[Service]
WorkingDirectory=/root/photowise
ExecStart=/usr/bin/java -jar /root/photowise/photowise-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143
Restart=always
RestartSec=30

[Install]
WantedBy=multi-user.target
```
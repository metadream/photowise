# Photowise

## TODO
- 用户管理/public/只读权限、菜单setting与权限
- 地图添加点：leaflet
- 国际化
- 时间线
- 缩略图测试（ffmpeg、thumbnaitor、opencv）

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
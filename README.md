# Photowise

## TODO
- 缩略图测试（ffmpeg、thumbnaitor）
- 地图添加点：leaflet
- 图片预览 photoswipe
- 国际化
- 时间线
- 懒加载 loading=lazy
- 当前导航不允许重复点击；
- 从地址栏进入时自动激活对应的导航；
- 

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
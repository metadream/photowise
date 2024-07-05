# Photowise

## TODO
- 缩略图测试（ffmpeg、thumbnaitor）
- 地图添加点：leaflet
- 图片预览参考 photoswipe
- 国际化
- 时间线
- 懒加载 loading=lazy
- Files.walkFileTree
- 用户管理/public/只读权限、菜单setting与权限


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
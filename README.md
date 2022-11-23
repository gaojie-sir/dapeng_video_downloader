# dapeng_video_downloader
大鹏教育网站视频下载工具，该软件原理按理说可实现所有采用m3u8视频流方式的视频下载到本地，已满足对未开放视频下载功能的部分网站进行视频下载需求
# 项目运行方式
## maven模式
该项目包含私服依赖，部分依赖无法直接下载，项目所依赖所有库包含再lib文件夹，可将文件夹上传至maven库，修改pom文件运行
## 本地依赖模式
lib文件夹包含该项目所有依赖，可直接使用本地依赖方式直接运行
# 软件实现原理
内嵌浏览器，通过拦截网页请求返回值，解析返回参数，获取视频信息，拼凑m3u8视频链接，再通过ffmpeg程序命令将m3u8流视频合并成MP4可播放视频

# 使用教程
1. 使用账号登录
2. 网页切换至有视频的页面，软件自动拦截请求，右侧可下载列表会自动刷新当前网页中可下载的所有视频
3. 支持单个视频下载并支持批量下载功能
4. 针对未完成下载功能的视频，下次打开软件开始自动下载

# WeCommunity
> 微博、微信、微X......各种微，我们也跟上潮流叫微社区，英文名是 WeCommunity！是不是和微信(WeChat)很像？

微社区是一个用户交流平台，类似论坛，提供有：帖子管理、评论、关注、点赞、搜索、私信、数据统计等功能。

项目目前部署在阿里云1核2G云服务器上，所有的组件都是单机模式运行。

访问网址：[http://community.aatroxc.club](http://community.aatroxc.club)




### 依葫芦画瓢的项目架构图

![项目架构设计图](https://i.loli.net/2020/07/01/umXATrRW2PCLhEI.png "项目架构设计图")


### 功能描述

1. 用户管理

   &emsp;&emsp;用户可以注册、登陆、退出，修改头像，查看某个用户的主页，其包括某个用户的关注、粉丝、获得的点赞数、发布的帖子，用户自己能查看自己发表的评论。

2. 帖子管理

   &emsp;&emsp;普通用户可以发布帖子、修改帖子，管理员可以删除帖子、恢复删除的帖子，版主可以将帖子置顶/取消置顶、加精华/取消加精华。

3. 关注

   &emsp;&emsp;用户可以关注/取消关注某个用户，假如A关注了B，那么A的关注列表中就有了B，B的粉丝列表中就有了A。

4. 评论

   &emsp;&emsp;用户可以对帖子进行评论，还可以对评论进行回复。

5. 点赞

   &emsp;&emsp;用户可以对帖子、评论进行点赞。

6. 系统通知

   &emsp;&emsp;某个用户评论、点赞了帖子，或者关注了某个用户，那么被评论、点赞、关注的用户会收到一条通知。目前系统中有3种通知：评论通知、点赞通知、被关注通知。

7. 数据统计

   &emsp;&emsp;管理员可以查看网站指定日期范围的UV（独立访客）、DAU（日活跃用户）数据。

8. 私信

   &emsp;&emsp;用户可以对网站内的其他用户发送私信，双方互发消息，只有他们自己能看到自己的私信。

9. 定时调度

   &emsp;&emsp;每个帖子都有个权重分，影响帖子的展示排名，定时调度主要是定时更新帖子的权重。

10. 搜索

    &emsp;&emsp;搜索系统使用Elasticsearch实现，支持对帖子标题、帖子内容的搜索。



### 技术选型

- Spring Boot
- SpringMVC
- Spring
- MyBatis3、通用mapper
- Spring Security：安全框架
- Redis：缓存及数据存储
- Kafka：消息队列
- Elasticsearch-6.3.0：分布式搜索引擎
- Quartz：定时调度框架
- Nginx
- Thymeleaf：模板引擎
- Caffeine：Java本地缓存库
- MySQL
- 七牛云：第三方文件存储服务



### 各个功能模块所对应的技术点

![功能模块对应的技术点](https://i.loli.net/2020/07/01/u3DRnvrxfUNKhtc.jpg)





### 界面设计

![主页](https://i.loli.net/2020/07/01/VbQYPd9wvWzxjy8.jpg "主页")

![帖子发布](https://i.loli.net/2020/07/01/ANeDU75GaMB36ZT.jpg "帖子发布")

![私信列表_私信详情](https://i.loli.net/2020/07/01/p6HQtoPlJXNGdwz.jpg "私信列表_私信详情")

![系统通知_](https://i.loli.net/2020/07/01/mbvtnlCgZyYWqLS.jpg "系统通知_")





### 文件说明

- wecommunity.sql：数据库文件
- wecommunity文件夹：Maven 项目源码
- static文件夹：前端静态资源，需独立部署





### 本地开发运行部署

- 下载zip直接解压或安装git后执行克隆命令 `git clone https://github.com/AatroxC/WeCommunity.git`

- 安装各组件并启动：Redis、ZooKeeper、Kafka、Elasticsearch

- Kafka 运行后要创建项目中需要用到的topic，进入 Kafka 的 bin 目录执行以下命令：

  ```shell
  ./kafka-topics.sh -create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic like
  ./kafka-topics.sh -create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic comment
  ./kafka-topics.sh -create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic follow
  ./kafka-topics.sh -create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic publish
  ./kafka-topics.sh -create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic delete
  ```

- MySQL 新建 `wecommunity` 数据库，运行 sql 文件

- 修改 `Nginx` 配置文件：

  ```nginx
  upstream tomcat-community {
      server localhost:8080 max_fails=5 fail_timeout=20s;
  }
  server {
      listen       80;
      server_name  community.aatroxc.club;
  
      location /{
          proxy_pass   http://tomcat-community;
      }
  }
  server {
      listen       80;
      server_name  static.aatroxc.club;
      location /{
          # 静态资源
          root  /myapps/java/wecommunity/static;
      }
  }
  ```

  根据域名适当修改

- 将项目配置文件 `application.yml` 中的地址、密码、七牛云 key 等配置好，还有静态资源中的 `global.js` 配置七牛云存储空间的 url

- 进入 Maven 项目目录执行打 war 包命令：`mvn clean package -Dmaven.test.skip=true` 放到 Tomcat 中启动，根据在 Nginx 中配置的域名访问即可







### 计划的部署模型

![系统架构图](https://i.loli.net/2020/07/01/aeb4DlWr6GcAOJ8.jpg "系统架构图")
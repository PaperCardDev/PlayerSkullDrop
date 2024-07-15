# PlayerSkullDrop

我的世界服务器插件，当被其他玩家击杀或被闪电苦力怕炸死时，有概率掉落自己的头颅。

## 特点

- 可配置：可自由配置头颅掉概率

- 支持 [Folia](https://papermc.io/software/folia) 系列核心

- 支持的MC版本：1.20及其以上，如需要支持其它版本，请创建Issue

## 命令

- `/player-skull-drop reload` 重载配置文件

- `/player-skukk-drop get <玩家名或UUID> [在线玩家名]` 获取指定玩家的头颅给另外一个在线玩家（不指定默认为自己）

## 配置

配置文件：config.yml

- `probability.player`：整数类型，被其它玩家杀死掉落头颅的概率，默认为10，
  小于等于0代表永远不会掉落，大于等于100代表必定掉落

- `probability.creeper`：整数类型，被闪电苦力怕炸死掉落头颅的概率，默认为10，
  小于等于0代表永远不会掉落，大于等于100代表必定掉落

## 使用方法

1. 下载插件后(一个.jar文件)放置于服务端根目录下的`plugins`文件夹下，重新启动服务器即可。

2. 若需要在运行时修改一些配置，则编辑插件文件夹(`/plugins/PlayerSkullDrop`)下的`config.yml`文件，然后使用上面的重载命令即可。
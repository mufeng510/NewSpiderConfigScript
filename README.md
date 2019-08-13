# NewSpiderConfigScript
##更新日志

####5.4.8

1.修复抓包会打开两个网页

2.修复离开WiFi不获取配置的问题

3.支持非王卡抓包

4.支持shell获取

5.提升稳定性

6.去除一些没必要的东西

####5.4.7

1.去除失效ip

2.修复抓包无法唤醒QQ浏览器

3.其余优化

####5.4.5

1.修正通知栏字体颜色

2.安装脚本将会自动关闭脚本

3.修复安装脚本后修改权限失败问题

4.修复无法唤醒QQ浏览器

5.修复无法冻结解冻QQ浏览器问题

####5.4.4

优化获取配置逻辑

####5.3.6
1.提高获取配置稳定性

2.修复无法关闭脚本的问题

####5.3.5

1.再次修复下载脚本失败问题

2.修改域名，旧版本不受影响

3.提高获取配置功能的稳定性

####5.3.4

修复脚本下载失败问题

####5.3.1

支持通知栏快捷开关（磁贴）

####5.3.0

1.支持检测状态仅检测ip

2.取消wifi状态下获取配置

3.无感模式

4.更新ip

5.自动服务逻辑优化

####5.2.9

1.修复获取配置会检查更新的问题

2.修复wifi状态下一直关闭脚本

####5.2.8

1.整体功能小幅优化

2.自动冻结QQ浏览器

####5.2.7

1.加密接口

2.去除一些没用的提示

3.加入终极防跳

####5.2.6

1.助手主页不能关闭软件

####5.2.5

1.更新接口

2.修正安装脚本后没有模式的问题

3.wifi状态下亮屏不再检测状态

####5.2.1

1.新增ip查询接口

2.优化自动抓包，修复因锁屏无法唤醒QQ浏览器导致的抓包失败

3.自动抓包逻辑跳转，无论抓包成功与否最后都会开启脚本

4.内置工具包，解决部分机型无法自动抓包和shell检测ip的问题

5.修复状态栏获取配置首次必失败的问题

6.助手主页按两下返回可以快速自尽

7.脚本检测与ip检测合并显示，可关闭亮屏后的检测

####5.2.0

1.查看uid美化，排序显示(并不完美,英文中文分开排序)

2.支持关闭定时任务

3.支持更多脚本，安装脚本无需再输入后缀名

4.添加ip查询方式

5.缩短脚本检测状态显示时间

6.定时任务可选自动抓包

7.解决了自诞生之初就存在的防跳编辑乱码问题，并且优化了滑动

8.还乱七八糟改了一些，也忘了啥了

####5.1.2
1.通知栏美化

2.获取配置及抓包重置定时任务

3.关闭脚本时停止定时任务

####5.1.1
1.修复部分机型后台fc问题

2.修复部分通知栏空白问题

3.自动抓包结束后关闭QQ浏览器

4.通知栏美化，点击图标可将助手调到前台(miui貌似不行)

####5.1.0
1.紧急修复自动抓包问题，避免因脚本关闭不彻底导致抓取到旧包

2.后台服务优化，避免系统误杀

####5.0.3
1.优化检测状态功能

2.获取配置功能优化

2.手动获取配置和自动抓包时自动关闭脚本

3.通知栏添加快捷功能

4.其他一些小修小补

####5.0.2
1.更换脚本解压方法，无需busybox支持

2.开启脚本后ip检测问题

3.关闭脚本没有提示

4.部分情况下打开放跳编辑闪退

5.新增解锁后检测ip

####5.0.0
1.更换脚本安装方式

2.通过shel查询ip，放行助手也没影响啦

3.ui调整，去除标题栏，状态栏沉浸，自定义背景壁纸

4.当服务器配置剩余时间少于0分钟时将不再写入

5.去除首页一键复制功能

6.重写多种方法，兼容性更好

7.自动抓包

8.新添了未知bug

####4.2.3
1.执行脚本方法替换为旧方法，解决部分机型执行脚本实际无效果的问题

2.新增设置开启关闭脚本是否自动检测状态

3.修改时间显示方式

4.修复手动抓包配置不上传的问题

5.主界面加了个背景

####4.2.0
1.更换服务器及api

2.修复4.0版本以来手动抓包不上传的问题

3.优化细节，提高稳定性

4.去除了一些无关紧要的东西

5.修复获取配置卡顿问题

6.新增执行脚本后的返回信息

7.支持脚本卸载

8.支持脚本状态检测

9.添加了ZJL2.0beat12脚本

10.新增配置剩余有效时间

11.换包名

####4.1.1脚本版
1.普通版内容基本都有

2.新增查看uid

3.连接wifi自动关闭脚本，断开wifi自动打开脚本

4.可编辑模式
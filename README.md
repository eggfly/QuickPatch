# QuickPatch

#### 项目介绍

年轻人的第一个Android插桩热修复框架

基于函数插桩，兼容性好（Android版本升级不需要做修改），支持热更新无需重启app，参考了美团的Robust插桩热修复框架，精简了很多实现细节，代码可读性高

#### 设计思路

- 暂未实现自动生成补丁
- QuickPatch和美团Robust的区别是，Robust的编译和dex阶段分别使用ASM和Smali做了处理，QuickPatch仅在gradle编译java到class阶段使用Javassist处理，逻辑简单
- 对于super的处理使用native调用CallNonVirtual##TYPE##Method()系列方法实现
- 计划支持构造函数和增加成员函数的热修复
- 可能计划支持非Android的纯Java代码的热修复

#### demo
![demo](demo.gif)

#### 使用说明

- 打开app/build.gradle中的一行apply plugin: 'quickpatch.gradleplugin'
- 然后使用AndroidStudio或者./gradlew执行下面任务:

```bash
./gradlew gradleplugin:uploadArchives # 编译插桩插件
./gradlew app:installDebug # 使用插件编译app代码并插桩
```

- 为了方便点击app内的Enable Patch按钮可以模拟补丁加载效果，实际上是使用原本的ClassLoader加载了apk内打包好的的QPatch类
- 同包名下后缀是_QPatch的类是补丁类，如MainActivity类的补丁类对应名字是MainActivity_QPatch
- 接下来框架代码会使用一个新的ClassLoader加载dex，然后反射识别并查找相应的函数是否存在，如果存在则新的函数里面的逻辑会被调用
- 补丁文件名一般是patch.dex, 生成dex需要手动使用命令，比如dx --dex --output=patch.dex MainActivity_QPatch.class
- 补丁文件需要手动放置到sd卡下，比如adb push patch.dex /sdcard/
- 然后点击app内的Enable Patch按钮即可实时加载补丁，看到pid不会有变化

#### Task list

- [x] 使用ASM或Javassist插桩哪个更方便？分场景使用gradle或者脚本执行 -- 使用Javassist，暂时使用gradle插件
- [x] 是整个apk范围使用还是只有jar类库范围，两种使用场景 -- 暂时apk范围，在class时候全部搞定
- [x] 桌面jvm动态加载class文件、dalvik动态加载dex文件 -- 暂时只考虑Android App
- [x] super的处理问题 -- 暂时使用native的CallNonVirtual<TYPE>Method()系列方法
- [x] multidex问题 -- 估计没问题
- [x] ClassLoader的preverify问题 -- 插桩热修复 没这个问题了
- [x] ClassLoader类加载和类卸载问题，是否存在OOM？-- 插桩热修复 没这个问题了
- [x] 需要热修复的部分代码的Annotation标记 -- 暂时不实现，为了简单手动编写QPatch类的相应代码
- [x] 构造函数支持热修复 -- TODO
- [ ] 支持增加成员变量 -- TODO
- [x] 支持增加函数 -- 直接在QPatch里手动调用新的函数即可
- [ ] 基类和子类是否均需要增加Object[]作为成员变量的问题 -- TODO 需要
- [ ] 混淆可能带来的name mapping问题 -- TODO: 混淆前就确定类名/函数名/函数签名，但是调用的时候需要考虑用反射
- [ ] 类的动态加载带来的安全问题 -- TODO
- [ ] apk/jar原始代码的完整性问题，版本控制问题 -- TODO
- [ ] 热修复包的来源验证问题 -- TODO
- [ ] method id, return value, 反射性能问题 -- TODO优化
- [x] 是否支持热启动并动态替换？ -- 支持
- [ ] 是否支持多进程？ -- TODO
- [ ] 其他安全问题（版本号对应补丁、hash校验、补丁加载的安全问题）？— TODO
- [ ] 多android、多机型的兼容性和如何测试的问题 -- TODO 需要找办法测试
- [ ] 崩溃率和修复率如何统计 -- TODO
- [ ] lambda的语法糖问题？内部类支持？ -- TODO
- [ ] 使用application做初始化，或者用static块确保加载（block住启动，是否影响性能）-- 
- [ ] 范型问题，需要测试 -- TODO

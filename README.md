# lombok-support
删除冗余代码，替换成注解

对于老项目，想全面拥抱lombok并不容易，需要把老代码翻出来自己替换一遍，费时费力。

因此做了一个小工具，删除冗余代码，替换成lombok注解，解放呆板的模板代码。
## 使用方法
```
git clone https://github.com/xinluke/lombok-support.git
mvn clean package -Dmaven.test.skip
```
将编译出来的包丢到项目的根路径下，以当前jar为基点，自动搜索下面全部的java文件进行替换。

## 温馨提示
如果pom.xml中未引用lombok的依赖的需要自己引入哦
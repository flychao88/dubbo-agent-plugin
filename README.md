# dubbo-agent-plugin

无侵入式数据信息采集项目,采用探针无侵入的方式,目前支持Dubbo和Http,正在开发Hessian的支持。
采用此agent-plugin是APM系统的采集端,是APM数据准确性的第一层保障。

此采集agent-plugin可以用来做很多业务系统的数据采集端,比如通道路由自动切换系统等。

# 使用方法
```
-javaagent:/路径/dubbo-agent-plugin-1.0-SNAPSHOT.jar
```

工程打完包以后，包括：config文件夹和dubbo-agent-plugin-1.0-SNAPSHOT.jar

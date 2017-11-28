# dubbo-agent-plugin

无侵入式数据信息采集项目,采用探针无侵入的方式,目前支持Dubbo和Http,正在开发Hessian的支持。
采用此agent-plugin是APM系统的采集端,是APM数据准确性的第一层保障。

此采集agent-plugin可以用来做很多业务系统的数据采集端,比如通道路由自动切换系统等。

### 使用说明
在使用探针之前需要先在应用项目的POM文件中添加如下依赖:

```
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dubbo</artifactId>
    <version>2.8.5</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka_2.10</artifactId>
    <version>0.9.0.0</version>
    <scope>provided</scope>
</dependency>

<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.41</version>
    <scope>provided</scope>
</dependency>
```





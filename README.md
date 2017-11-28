# dubbo-agent-plugin

基于Dubbo项目的agent探针应用，采集指定应用程序的相关信息。

后续会做成APM系统

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

<!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.41</version>
    <scope>provided</scope>
</dependency>
```
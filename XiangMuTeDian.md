#为啥用TreapDB?

==特点==：

  1. 支持大数据量（1亿以内的key-value pair，在SATA硬盘，64位服务器上8200tps随机key写入,12000tps顺序key写入，16000tps读取）
    * 最新采用的批量更新技术，可以达到1.7万条随机写入每秒
    * key是"thing1"~"thing10000000"这样的字符串，value是100字节的字符串
  1. 不仅支持GET和PUT操作，还支持取前缀，取区间（开区间、闭区间），K个最小值，K个最大值。[点击看所有操作](http://code.google.com/p/treapdb/wiki/Operations)
  1. 支持主从复制，从而实现读写分离和灾备
  1. 同时支持Memcached的协议和基于Thrfit的协议（后者可以支持轻松生成各种语言的客户端）
  1. 可以把TreapDB的索引数据结构嵌入自己的代码，就一个jar包，完全可以不用使用服务器

## 常见问题 ##

> http://code.google.com/p/treapdb/issues/list

## 开发日志 ##

> http://code.google.com/p/treapdb/wiki/ChangeLog
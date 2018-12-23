一个点对点的rpc,大部分代码来自https://my.oschina.net/huangyong/blog/361751,一个依赖zookeeper的rpc
现在把zookeeper去掉,做成一个点对点的rpc,目的是为之后可能会实现的一个raft协议服务.
client:
	- 长连接池:FixedChannelPool,ChannelPoolHandler
	- 异步:主要来自http://www.cnblogs.com/luxiaoxun/p/5272384.html
	
- 接下来要做的:
	- 心跳检测.
	- 连接失效管理.
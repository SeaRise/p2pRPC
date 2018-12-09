一个点对点的rpc,大部分代码来自https://my.oschina.net/huangyong/blog/361751
原来的是一个依赖zookeeper的一个rpc,现在把zookeeper去掉,做成一个点对点的,目的是为之后可能会实现的一个raft协议服务.
现在想做的改进:
- rpc client的长连接管理.
- rpc client异步改进
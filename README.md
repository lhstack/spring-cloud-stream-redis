# spring-cloud-stream-redis

#### 介绍
`spring-cloud-stream-redis,集成redis到spring-cloud-stream模块`
### spring-cloud-stream-redis
`spring-cloud-stream-redis使用redis订阅发布模型，只支持StreamListener订阅方式，使用方式参考spring-cloud-stream-redis-tests模块`
### spring-cloud-stream-redis2
`spring-cloud-stream-redis2使用redis-stream模型，支持StreamListener,PollableMessageSource,PollableChannel三种方式，StreamListener是push模型，是自动订阅，PollableMessageSource同StreamListener类似，只不过是手动实现messageHandler，PollableChannel是拉模型，是自己手动拉取消息消费,使用方式参考spring-cloud-stream-redis2`

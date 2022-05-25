# 服务发现和注册
## 服务发现
1. DiscoverClient：服务发现抽象服务类
2. ServiceInstance：服务实例bean的封装
3. 参考实现：nacos
    1. EnableAutoConfiguration的扩展类：NacosDiscoveryAutoConfiguration、NacosServiceAutoConfiguration等等自动注入：NacosDiscoveryClient
    2. NacosServiceInstance implement NacosServiceInstance
    3. NacosDiscoveryClient implement DiscoverClient
    4. NacosDiscoveryProperties：nacos服务properties参数
    5. NacosServiceDiscovery：nacosDiscovery的服务类
## 服务注册
1. Registration：注册实例bean的封装
2. ServiceRegistry：注册服务service类
3. 参考实现：nacos
    1. EnableAutoConfiguration的扩展类：NacosServiceRegistryAutoConfiguration、NacosServiceAutoConfiguration等等自动注入NacosServiceRegistry、NacosRegistration
    2. NacosRegistration implement Registration
    3. NacosServiceRegistry implement ServiceRegistry
# 配置服务
1. PropertySourceLocator: 外部配置资源加载器
2. BootstrapImportSelector: 加载BootstrapConfiguration的import
3. BootstrapConfiguration: 加载BootStrap资源的Import扩展spi
4. PropertySourceBootstrapConfiguration：外部资源加载入本地资源处理器

# 负载均衡
1. LoadBalancerClient: 负载客户端
2. LoadBalancedRetryPolicy: 客户端负载策略（例如是否重新尝试、下一个尝试服务实例、重试的http响应码）
3. LoadBalancerRequest：负载请求包装类
   1. 实现：BlockingLoadBalancerClient，包装类：FeignBlockingLoadBalancerClient
# 断路由
1. CircuitBreaker:feign没有实现这个接口

#Feign实现负载均衡以及断路由
## Feign对rpc的封装
1. Client：客户端执行器
   1. FeignBlockingLoadBalancerClient implement Client
   2. BlockingLoadBalancerClient implement LoadBalancerClient实现负载功能
   3. ApacheHttpClient implement Client实现http请求功能
2. Request：http请求request
3. Response：http请求response
4. Targeter: 执行目标的封装
   1. 具体的实现：HystrixTargeter implement Targeter
5. Feign：Feign用于创建关于某个（@FeignClient）方法执行时生成的动态代理对象
   1. 具体实现：ReflectiveFeign
6. Feign.Builder用于生成Feign，实现：HystrixFeign.Builder
7. InvocationHandlerFactory：被@FeignClient标注的接口上某个被@Get/@Post方法标注的方法代理的处理器工厂类
   1. HystrixInvocationHandler implement InvocationHandler
   2. SynchronousMethodHandler implement MethodHandler
## Feign与SpringBoot集成
1. @EnableFeignClients注解上有个@Import(FeignClientsRegistrar.class)
2. FeignClientsRegistrar类中两个主要方法：registerDefaultConfiguration()注册@EnableFeignClients表明的FeignClient客户端、registerFeignClients()通过ClassPathScanningCandidateComponentProvider扫描器扫描根包下面的被@FeignClient注解的类
3. 获取到@FeignClient注解的类类时通过FeignClientsRegistrar.registerFeignClient()注入到容器中
4. registerFeignClient()注入的核心代码：BeanDefinitionBuilder.genericBeanDefinition(FeignClientFactoryBean.class);
5. 在spring反射创建@FeignClient注解的时会调用FeignClientFactoryBean.getObject()

## 如果想个性化设置Feign的断路由时：
1. 自定义实现SetterFactory
2. 自定义实现hystrix断路由参数的接收器：比如HystrixCommandProperties参数和HystrixThreadPoolProperties参数
3. 将接收器注入到SetterFactory中，然后再将SetterFactory注入到HystrixFeign.Build()中
4. 可以自定义环境变量用于接收hystrix断路由参数


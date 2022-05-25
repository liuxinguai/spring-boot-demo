# springboot自动装配
1. @SpringBootApplication注解被EnableAutoConfiguration标注
2. @EnableAutoConfiguration使用了@Import(AutoConfigurationImportSelector)
3. spring源码分析中执行ConfigurationClassParser.doProcessConfigurationClass()时
4. 会执行AutoConfigurationImportSelector.selectImports->getAutoConfigurationEntry()
5. getAutoConfigurationEntry->读取META-INF/spring.interface文件中EnableAutoConfiguration扩展的spi
6. 根据@EnableAutoConfiguration注解的exclute去过滤不需要装配的bean
7. 若excute中没有过滤掉，同样可以根据运行时@Condition注解上实现了SpringCondition接口来实现对@EnableAutoConfiguration已生效的过滤
8. 过滤流程：
   1. getAutoConfigurationEntry->getConfigurationClassFilter.filter()
   2. filter()->AutoConfigurationImportFilter.match()->FilteringSpringBootCondition.match()
   3. FilteringSpringBootCondition.match()->getOutcomes()
   4. getOutcomes()->分别去调用实现了SpringCondition的FilteringSpringBootCondition抽象类
   5. 例如：OnBeanCondition extend FilteringSpringBootCondition
   6. FilteringSpringBootCondition.matchs()->OnBeanCondition.getOutcomes()
   7. 先判断使用@ConditionalOnBean注解上的bean对应的class文件在classpath中是否已存在，若不存在就直接过滤掉
   8. 若存在则将这个Bean放入到待实例化的列表中
   9. spring源码分析中，当ConfigurationClassBeanDefinitionReader.loadBeanDefinitions()->loadBeanDefinitionsForConfigurationClass()->loadBeanDefinitionsForBeanMethod()时
   10. loadBeanDefinitionsForBeanMethod()->ConditionEvaluator.shouldSkip()->Condition.matches()
   11. Condition.matches()-> SpringBootCondition.matches()->SpringBootCondition.getMatchOutcome()
   12. OnBeanCondition.getMatchOutCome()->OnBeanCondition.getMatchingBeans()->getBeanNamesForType()获取容器中是否存在对应的Bean
   13. 从而完成了整个OnBeanCondition的过滤
## 自动装备主要类图
![AutoConfigurationImportSelector](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\AutoConfigurationImportSelector.png)
![Condition](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\Condition.png)
# springboot 自动装配内嵌（tomcat、jetty）服务器
## springboot 对内嵌服务器的封装
1. WebServer表示对servlet运行时容器server的封装
2. WebServerFactory:工厂方法用于创建webServer
3. WebServerFactoryCustomizer：用于个性化配置WebServerFactory
4. WebServerFactoryCustomizerBeanPostProcessor：用于当初始化WebServerFactory时调用WebServerFactoryCustomizer.customize()对WebServerFactory进行个性化配置
5. SpringApplication.run()->createApplicationContext()->初始化AnnotationConfigServletWebServerApplicationContext容器
6. SpringApplication.run()->createApplicationContext()->refreshContext(context)
7. refreshContext()->AnnotationConfigServletWebServerApplicationContext.fresh()->AbstractApplicationContext.onRefresh()->ServletWebServerApplicationContext.onRefresh()->createWebServer()->getWebServerFactory()
8. getWebServerFactory()->getBeanFactory().getBeanNamesForType(ServletWebServerFactory.class)
9. getBeanNamesForType(ServletWebServerFactory.class)初始化WebServerFactory时调用WebServerFactoryCustomizerBeanPostProcessor.postProcessBeforeInitialization()
10. postProcessBeforeInitialization()->getWebServerFactoryCustomizerBeans()获取WebServerFactoryCustomizer，调用WebServerFactoryCustomizer.customize()方法对WebServerFactory进行个性化配置
11. 7步骤获取到WebServerFactory后调用WebServerFactory.getWebServer()创建WebServer，将WebServer注入到WebServerStartStopLifecycle中，并将WebServerGracefulShutdownLifecycle注入到spring容器中
12. AbstractApplicationContext.onRefresh()->AbstractApplicationContext.finishRefresh()->getLifecycleProcessor().onRefresh()->WebServerGracefulShutdownLifecycle.onRefresh()
13. DefaultLifecycleProcessor.onRefresh()->startBeans()->LifecycleGroup.start()->doStart()->WebServerStartStopLifecycle.start()->WebServer.start()

## spring自动装配只需要在spring容器启动过程中动态地注入WebServerFactory、WebServerFactoryCustomizer、WebServerFactoryCustomizerBeanPostProcessor即可
1. spring-autoconfiguration模块的META-INF/spring.interfaces中配置了ServletWebServerFactoryAutoConfiguration 
2. @ConditionalOnClass(ServletRequest.class)判断是否是javaEE中sevlet规范
3. @ConditionalOnWebApplication(type = Type.SERVLET)判断当前服务器模式是阻塞模式而不是响应式模式
4. @EnableConfigurationProperties(ServerProperties.class)植入服务器ip、port、ssl、是否是http2等http协议信息
5. 导入ServletWebServerFactoryAutoConfiguration.BeanPostProcessorsRegistrar
   1. 注册WebServerFactoryCustomizerBeanPostProcessor，用于自定义WebServerFactory
6. 导入ServletWebServerFactoryConfiguration.EmbeddedTomcat、ServletWebServerFactoryConfiguration.EmbeddedJetty

##主要类图
![TomcatServletWebServerFactory](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\TomcatServletWebServerFactory.png)
![WebServerFactoryCustomizerBeanPostProcessor](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\WebServerFactoryCustomizerBeanPostProcessor.png)
![ServletWebServerFactoryCustomizer](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\ServletWebServerFactoryCustomizer.png)
![TomcatServletWebServerFactoryCustomizer](D:\work-spaces\spring-boot-demo\boot-demo\src\main\resources\TomcatServletWebServerFactoryCustomizer.png)
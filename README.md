# spring-memcached集成

spring　memcached集成。走过了一个坑。使用了google的ssm库。simple-spring-management
aop和memcached冲突。如果有自定义aop，则会导致缓存失效。解决方法，把自定义的aop去掉（LogAop.class）

## gradle依赖
```
    compile("com.google.code.simple-spring-memcached:xmemcached-provider:3.5.0")
    compile("com.google.code.simple-spring-memcached:simple-spring-memcached:3.5.0")
    compile("com.google.code.simple-spring-memcached:spring-cache:3.5.0")
    compile("org.springframework:spring-aop:4.1.5.RELEASE")
	compile("org.aspectj:aspectjrt:1.6.11")
	compile("org.aspectj:aspectjweaver:1.6.11")
	compile("aopalliance:aopalliance:1.0")
```

## 注解配置
```
@Configuration
@EnableCaching
@EnableAspectJAutoProxy
public class MemCacheConfig extends CachingConfigurerSupport {
	@Bean
	public CacheManager cacheManager()
	{
	    MemcacheClientFactoryImpl cacheClientFactory = new MemcacheClientFactoryImpl();
	    AddressProvider addressProvider = new DefaultAddressProvider("www.hollysys.xyz:11211");
	    com.google.code.ssm.providers.CacheConfiguration cacheConfiguration = new com.google.code.ssm.providers.CacheConfiguration();
	    cacheConfiguration.setConsistentHashing(true); //TODO check this

	    CacheFactory cacheFactory = new CacheFactory();
	    cacheFactory.setCacheName           ( "userCache"        );
	    cacheFactory.setCacheClientFactory  ( cacheClientFactory    );
	    cacheFactory.setAddressProvider     ( addressProvider       );
	    cacheFactory.setConfiguration       ( cacheConfiguration    );

	    Cache object = null;
	    try {
	        object = cacheFactory.getObject();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

	    SSMCache ssmCache = new SSMCache(object, 10000, true); //TODO be very carefully here, third param allow remove all entries!!

	    ArrayList<SSMCache> ssmCaches = new ArrayList<SSMCache>();
	    ssmCaches.add(0, ssmCache);

	    SSMCacheManager ssmCacheManager = new SSMCacheManager();
	    ssmCacheManager.setCaches(ssmCaches);
	    
	    return ssmCacheManager;
	}

}
```
注意：
* EnableAspectJAutoProxy　驱动缓存扫描。这句话不能少

##　冲突的配置
```
@Aspect
@Component
@EnableAutoConfiguration
public class LogAop 
{
/*	@Around("execution(* com.niejinkun.spring.springcache.controller.UserController.getUser(..)) && args(user_id)")
	public void logStatics(ProceedingJoinPoint joinPoint,int user_id) throws Throwable{
		
		long start = System.currentTimeMillis();
		
		Object user = joinPoint.proceed();
		
		System.out.println("log user: " + user.toString());
		long end = System.currentTimeMillis();
		
		System.out.println("use time : "  + (end - start));
	}
	
	@Before("execution(* com.niejinkun.spring.springcache.controller.UserController.getUser(..)) && args(user_id)")
	public void logBefore(int user_id) throws Throwable{
		
		System.out.println("Before log user: " + user_id);
	}
	
	@After("execution(* com.niejinkun.spring.springcache.controller.UserController.getUser(..)) && args(user_id)")
	public void logEnd(int user_id) throws Throwable{
		System.out.println("After log user: " + user_id);

	}*/
}
```
说明：
* 这个aop会导致getUser上面的@cacheable失效，因此，这里注释掉了。

## 控制器配置
```
	@Cacheable(value = NAMESPACE, key="#user_id", unless="#result == null")
	@RequestMapping("/user2/{user_id}")
	@ResponseBody
	public  User getUser( @PathVariable  int user_id) {
		// 方法内部实现不考虑缓存逻辑，直接实现业务
		System.out.println("real query user." + user_id);
		return userDAO.getUser(user_id);
	}
```
　说明：
* unless="#result == null" 表示如果返回结果为空，则不放入memcached
## 编译
```
gradle install --info
```

##　运行
```
gradle bootrun
```

## 说明
如果没有注释掉AOP,则会报下面的错误
```
2016-03-23 20:51:49.111  INFO 9660 --- [nio-8080-exec-5] com.google.code.ssm.spring.SSMCache      : Cache miss. Get by key 988 from cache userCache
```



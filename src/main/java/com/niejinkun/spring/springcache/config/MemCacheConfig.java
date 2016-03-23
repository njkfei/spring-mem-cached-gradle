package com.niejinkun.spring.springcache.config;

import java.util.ArrayList;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.google.code.ssm.Cache;
import com.google.code.ssm.CacheFactory;
import com.google.code.ssm.config.AddressProvider;
import com.google.code.ssm.config.DefaultAddressProvider;
import com.google.code.ssm.providers.xmemcached.MemcacheClientFactoryImpl;
import com.google.code.ssm.spring.SSMCache;
import com.google.code.ssm.spring.SSMCacheManager;


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

package com.aatroxc.wecommunity.config;

import com.aatroxc.wecommunity.quartz.PostScoreRefreshJob;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * @author mafei007
 * @date 2020/5/18 18:26
 */

@Configuration
public class QuartzConfig {

	@Value("${postScoreRefreshInterval}")
	private long postScoreRefreshInterval;

	/**
	 * 刷新帖子权重的任务
	 * <p>
	 * 配置 JobDetail
	 * <p>
	 * Spring 中有很多 FactoryBean，可简化 Bean 的实例化过程；
	 * 1. 通过 FactoryBean 封装 Bean 的实例化过程
	 * 2. 将 FactoryBean 装配到 Spring 容器里
	 * 3. 将 FactoryBean 注入给其它的 Bean
	 * 4. 该 Bean 得到的是 FactoryBean 所管理的对象实例
	 *
	 * @return
	 */
	@Bean
	public JobDetailFactoryBean postScoreRefreshJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(PostScoreRefreshJob.class);
		factoryBean.setName("postScoreRefreshJob");
		factoryBean.setGroup("communityJobGroup");
		// 这个 Job 是持久的，哪怕触发器都不在执行了，也留着
		factoryBean.setDurability(true);
		// Job 是不是可恢复的
		factoryBean.setRequestsRecovery(true);
		return factoryBean;
	}

	/**
	 * 刷新帖子权重的任务
	 * <p>
	 * 配置 Trigger，需要用到 JobDetail
	 * (SimpleTriggerFactoryBean, CronTriggerFactoryBean)
	 *
	 * @param postScoreRefreshJobDetail 上面定义的同名 Bean
	 * @return
	 */
	@Bean
	public SimpleTriggerFactoryBean postScoreRefreshTrigger(JobDetail postScoreRefreshJobDetail) {
		SimpleTriggerFactoryBean factoryBean = new SimpleTriggerFactoryBean();
		factoryBean.setJobDetail(postScoreRefreshJobDetail);
		factoryBean.setName("postScoreRefreshTrigger");
		factoryBean.setGroup("communityTriggerGroup");
		// 每 1 分钟触发一次
		factoryBean.setRepeatInterval(postScoreRefreshInterval);
		// Trigger 需要存储 Job 的状态，这里配置用什么来存储
		factoryBean.setJobDataMap(new JobDataMap());
		return factoryBean;
	}

}

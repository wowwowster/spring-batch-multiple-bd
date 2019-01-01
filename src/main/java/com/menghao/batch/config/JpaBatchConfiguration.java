package com.menghao.batch.config;

import com.menghao.batch.entity.db.JpaUser;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.SimpleJobExplorer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.naming.NamingException;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;


@Configuration
@EnableBatchProcessing
public class JpaBatchConfiguration {


    @Bean
    public BatchConfigurer configurer( EntityManagerFactory entityManagerFactory ){
        return new CustomBatchConfigurer( entityManagerFactory );
    }


    /*@Autowired
    private final StepBuilderFactory stepBuilderFactory;

    @Autowired
    private final JobBuilderFactory jobBuilderFactory;*/

    /*@Autowired
    public JpaBatchConfiguration(StepBuilderFactory stepBuilderFactory, JobBuilderFactory jobBuilderFactory) {
        this.stepBuilderFactory = stepBuilderFactory;
        this.jobBuilderFactory = jobBuilderFactory;
    }
*/

    @Bean
    public ItemReader<JpaUser> jpaItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<JpaUser> itemReader = new JpaPagingItemReader<>();
        itemReader.setEntityManagerFactory(entityManagerFactory);
        itemReader.setQueryString("select u from JpaUser u");
        return itemReader;
    }



    @Bean
    public ItemProcessor<JpaUser, JpaUser> processor() {
        return item -> {
            item.setAge(item.getAge() + 1);
            item.setDescription("have deal");
            return item;
        };
    }

    @Bean
    public ItemWriter<JpaUser> jpaItemWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<JpaUser> itemWriter = new JpaItemWriter<>();
        itemWriter.setEntityManagerFactory(entityManagerFactory);
        return itemWriter;
    }

    @Bean
    public Step step(StepBuilderFactory stepBuilderFactory, ItemReader<JpaUser> jpaItemReader, ItemProcessor<JpaUser, JpaUser> processor,
                     ItemWriter<JpaUser> jpaItemWriter) {
        return stepBuilderFactory.get("addAge")
                .<JpaUser, JpaUser>chunk(2)
                .reader(jpaItemReader)
                .processor(processor)
                .writer(jpaItemWriter)
                .build();
    }


    @Bean
    public Job job( JobBuilderFactory jobBuilderFactory, Step step) {
        return jobBuilderFactory.get("addJob")
                .listener(new JobExecutionListener() {
                    private Long time;

                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        time = System.currentTimeMillis();
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        System.out.println(String.format("public Job job(Step step) : %sms", System.currentTimeMillis() - time));
                    }
                })
                .flow(step)
                .end()
                .build();
    }
}

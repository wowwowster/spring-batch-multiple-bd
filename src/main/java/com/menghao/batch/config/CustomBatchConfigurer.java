package com.menghao.batch.config;

import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;


public class CustomBatchConfigurer implements BatchConfigurer {

     private final EntityManagerFactory entityManagerFactory;

    private PlatformTransactionManager transactionManager;

    private JobRepository jobRepository;

    private JobLauncher jobLauncher;

    private JobExplorer jobExplorer;

    // Created by TaskExecutorConfiguration if it is used. If an alternative TaskExecutor is configured,
    // it will be injected here.
    @Autowired
    private TaskExecutor taskExecutor;


    /**
     * Create a new {@link CustomBatchConfigurer} instance.
     * @param entityManagerFactory the entity manager factory
     */
    public CustomBatchConfigurer(EntityManagerFactory entityManagerFactory ) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public JobRepository getJobRepository() {
        return this.jobRepository;
    }

    @Override
    public PlatformTransactionManager getTransactionManager() {
        return this.transactionManager;
    }

    @Override
    public JobLauncher getJobLauncher() {
        return this.jobLauncher;
    }

    @Override
    public JobExplorer getJobExplorer() throws Exception {
        return this.jobExplorer;
    }

    private JobLauncher createJobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(taskExecutor);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @PostConstruct
    public void initialize()  throws Exception {
        try {
            // transactionManager:
            if( this.entityManagerFactory == null ){
                throw new Exception("Unable to initialize batch configurer : entityManagerFactory must not be null");
            }
            this.transactionManager = new JpaTransactionManager( this.entityManagerFactory );

            // jobRepository:
            MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean( this.transactionManager );
            jobRepositoryFactory.afterPropertiesSet();
            this.jobRepository = jobRepositoryFactory.getObject();

            // jobLauncher:
          /*   SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
            jobLauncher.setJobRepository(getJobRepository());
            jobLauncher.afterPropertiesSet();
            this.jobLauncher = jobLauncher;
*/
            // jobExplorer:
            MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
            jobExplorerFactory.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactory.getObject();
        }
        catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize Spring Batch", ex);
        }
        this.jobLauncher = createJobLauncher();
    }


    /*@PostConstruct
    public void initialize() throws Exception {
        if (dataSource == null) {
            logger.warn("No datasource was provided...using a Map based JobRepository");

            if (this.transactionManager == null) {
                this.transactionManager = new ResourcelessTransactionManager();
            }

            MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(this.transactionManager);
            jobRepositoryFactory.afterPropertiesSet();
            this.jobRepository = jobRepositoryFactory.getObject();

            MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean(jobRepositoryFactory);
            jobExplorerFactory.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactory.getObject();
        } else {
            this.jobRepository = createJobRepository();

            JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
            jobExplorerFactoryBean.setDataSource(this.dataSource);
            String tablePrefix = env.getProperty("batch.repository.tableprefix");
            if (tablePrefix != null) {
                jobExplorerFactoryBean.setTablePrefix(tablePrefix);
            }
            jobExplorerFactoryBean.afterPropertiesSet();
            this.jobExplorer = jobExplorerFactoryBean.getObject();
        }

        this.jobLauncher = createJobLauncher();
    }*/
}

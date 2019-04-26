package hello;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    //==============================================================================================
    
    // tag::readerwriterprocessor[]
    // Define the input, processor, and output
    /**
     * The 'READER' ( 'INPUT' ) : create an ItemReader. 
     * It looks for a file called sample-data.csv 
     * and parses each line item with enough information to turn it into a Person
     * 
     * @return
     */
    @Bean
    public FlatFileItemReader<Person> reader() {
        return new FlatFileItemReaderBuilder<Person>()
            .name("personItemReader")
            .resource(new ClassPathResource("sample-data.csv"))
            .delimited()
            .names(new String[]{"firstName", "lastName"})
            .fieldSetMapper(new BeanWrapperFieldSetMapper<Person>() {{
                setTargetType(Person.class);
            }})
            .build();
    }

    /**
     * The 'PROCESSOR' : creates an instance of 'PersonItemProcessor' 
     * defined earlier, meant to uppercase the data. 
     * 
     * @return
     */
    @Bean
    public PersonItemProcessor processor() {
        return new PersonItemProcessor();
    }

    /**
     * The 'WRITER' ( 'OUTPUT' ) creates an ItemWriter. 
     * This one is aimed at a JDBC destination and automatically gets a copy 
     * of the dataSource created by @EnableBatchProcessing. 
     * It includes the SQL statement needed to insert a single Person driven by Java bean properties
     * 
     * @param dataSource
     * @return
     */
    @Bean
    public JdbcBatchItemWriter<Person> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Person>()
            .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
            .sql("INSERT INTO people (first_name, last_name) VALUES (:firstName, :lastName)")
            .dataSource(dataSource)
            .build();
    }
    // end::readerwriterprocessor[]

    //==============================================================================================
    // JOB CONFIGURATION 

    // tag::jobstep[]
    /**
     * 'JOB' definition, we need an incrementer because jobs use a database to maintain execution state. 
     * We then list each step, of which this job has only one step. 
     * The job ends, and the Java API produces a perfectly configured job.
     * 
     * @param listener
     * @param step1
     * @return
     */
    @Bean
    public Job importUserJob(JobCompletionNotificationListener listener, Step step1) {
        return jobBuilderFactory.get("importUserJob")
            .incrementer(new RunIdIncrementer())
            .listener(listener)
            .flow(step1)
            .end()
            .build();
    }

    /**
     * 'STEP' definition, we define how much data to write at a time. 
     * In this case, it writes up to ten records at a time. 
     * Next, we configure the reader, processor, and writer using the injected bits from earlier.
     * 
     * @param writer
     * @return
     */
    @Bean
    public Step step1(JdbcBatchItemWriter<Person> writer) {
        return stepBuilderFactory.get("step1")
            .<Person, Person> chunk(10)  // the step processes items in chunks with size = 10 
            .reader(reader())
            .processor(processor())
            .writer(writer)
            .build();
    }
    // end::jobstep[]
    
}
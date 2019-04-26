package hello;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.batch.item.ItemProcessor;

/**
 * Simple transformer that converts the names to uppercase.
 * 
 * It implements Spring Batch’s ItemProcessor interface. 
 * This makes it easy to wire the code into a batch job. 
 * According to the interface, you receive an incoming Person object, 
 * after which you transform it to an upper-cased Person.
 * 
 * There is no requirement that the input and output types be the same. 
 * In fact, after one source of data is read, 
 * sometimes the application’s data flow needs a different data type.
 *
 */
public class PersonItemProcessor implements ItemProcessor<Person, Person> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public Person process(final Person person) throws Exception {
        final String firstName = person.getFirstName().toUpperCase();
        final String lastName = person.getLastName().toUpperCase();

        final Person transformedPerson = new Person(firstName, lastName);

        log.info("Converting (" + person + ") into (" + transformedPerson + ")");

        return transformedPerson;
    }

}
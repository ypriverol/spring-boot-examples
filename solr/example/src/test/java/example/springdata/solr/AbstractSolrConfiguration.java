package example.springdata.solr;

import example.springdata.solr.product.Product;
import example.springdata.solr.product.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.stream.IntStream;

/**
 * Some abstract classes to reuse in the configuration files.
 *
 * @author ypriverol
 * @version $Id$
 *
 */
public class AbstractSolrConfiguration {

    protected void doInitTestData(CrudRepository<Product, String> repository) {

        IntStream.range(0, 100)
                .forEach(index -> {
                    Product p = new Product();
                    p.setId("p-" + index);
                    p.setName("name-" + index);
                    repository.save(p);
                });
    }

    /**
     * This function helps to clean all.
     * @param repository
     */
    protected void deleteAllData(CrudRepository<Product, String> repository){
        repository.deleteAll();
    }
}

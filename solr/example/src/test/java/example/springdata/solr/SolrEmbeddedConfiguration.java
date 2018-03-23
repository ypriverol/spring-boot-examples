package example.springdata.solr;


import example.springdata.solr.product.Product;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.support.EmbeddedSolrServerFactory;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.stream.IntStream;

/**
 *
 * @author ypriverol
 * @version $Id$
 */
@SpringBootApplication
@EnableSolrRepositories(schemaCreationSupport = true)
public class SolrEmbeddedConfiguration extends AbstractSolrConfiguration{

    @Value("#{configuration['solr.Home']}")
    private String solrConfDir;

    @Autowired
    CrudRepository<Product, String> repo;

    @Bean
    public EmbeddedSolrServer solrServerEmbedded() throws IOException, SAXException, ParserConfigurationException {
        final EmbeddedSolrServerFactory embeddedSolrServerFactory = new EmbeddedSolrServerFactory(solrConfDir);
        return embeddedSolrServerFactory.getSolrClient();
    }

    @Bean
    public SolrTemplate solrTemplate() throws ParserConfigurationException, SAXException, IOException {
        return new SolrTemplate(solrServerEmbedded());
    }

    /**
     * Remove test data when context is shut down.
     */
    @PreDestroy
    public void deleteDocumentsOnShutdown() {
        repo.deleteAll();
    }

    /**
     * Initialize Solr instance with test data once context has started.
     */
    @PostConstruct
    public void initWithTestData() {
        repo.deleteAll(); // This needs to be added here to avoid
        doInitTestData(repo);
    }

}

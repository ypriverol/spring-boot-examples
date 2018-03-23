package example.springdata.solr;


import example.springdata.solr.product.Product;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;
import org.springframework.data.solr.server.support.EmbeddedSolrServerFactory;
import org.springframework.test.context.TestPropertySource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.IntStream;

/**
 *
 * @author ypriverol
 * @version $Id$
 */
@SpringBootApplication
@EnableSolrRepositories(schemaCreationSupport = true)
@TestPropertySource(locations = "classpath:application.properties")
public class SolrEmbeddedConfiguration extends AbstractSolrConfiguration{

    @Value("${solr.Home}")
    private String solrConfDir;

    @Autowired
    CrudRepository<Product, String> repo;

    @Bean
    public EmbeddedSolrServer solrServerEmbedded() throws IOException, SAXException, ParserConfigurationException, URISyntaxException {
        Path solrTempDirectory = Files.createTempDirectory("");
        String testURL = new File(SolrEmbeddedConfiguration.class.getClassLoader().getResource(solrConfDir).toURI()).getAbsolutePath();
        FileUtils.copyDirectory(new File(testURL), solrTempDirectory.toFile());
        final EmbeddedSolrServerFactory embeddedSolrServerFactory = new EmbeddedSolrServerFactory(solrTempDirectory.toString());
        return embeddedSolrServerFactory.getSolrClient();
    }

    @Bean
    public SolrTemplate solrTemplate() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
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


    @Test
    public void simpleCreation(){
        repo.findAll().forEach(System.out::println);
    }

}

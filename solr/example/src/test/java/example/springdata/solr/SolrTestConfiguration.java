/*
 * Copyright 2014-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package example.springdata.solr;

import example.springdata.solr.product.Product;

import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

/**
 * @author Christoph Strobl
 * @author Oliver Gierke
 */
@SpringBootApplication
@EnableSolrRepositories(schemaCreationSupport = true)
public class SolrTestConfiguration {

	@Autowired CrudRepository<Product, String> repo;

	public @Bean SolrTemplate solrTemplate() {
		return new SolrTemplate(new HttpSolrClient.Builder().withBaseSolrUrl("http://localhost:8983/solr").build());
	}

	/**
	 * Remove test data when context is shut down.
	 */
	public @PreDestroy void deleteDocumentsOnShutdown() {
		repo.deleteAll();
	}

	/**
	 * Initialize Solr instance with test data once context has started.
	 */
	public @PostConstruct void initWithTestData() {
		repo.deleteAll(); // This needs to be added here to avoid
		doInitTestData(repo);
	}

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

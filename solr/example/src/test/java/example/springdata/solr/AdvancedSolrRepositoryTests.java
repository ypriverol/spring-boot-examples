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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.springframework.data.solr.core.query.Criteria.*;
import static org.springframework.data.solr.core.query.ExistsFunction.*;

import example.springdata.solr.product.Product;
import example.springdata.solr.product.ProductRepository;
import example.springdata.solr.test.util.RequiresSolrServer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.query.Function;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Boost;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Christoph Strobl
 * @author Oliver Gierke
 * @author Mark Paluch
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AdvancedSolrRepositoryTests {

	public static @ClassRule RequiresSolrServer requiresRunningServer = RequiresSolrServer.onLocalhost();

	@Configuration
	static class Config extends SolrTestConfiguration {

		@Override
		protected void doInitTestData(CrudRepository<Product, String> repository) {

			Product playstation = new Product();
			playstation.setId("id-1");
			playstation.setName("Playstation");
			playstation.setDescription("The Sony playstation was the top selling gaming system in 1994.");
			playstation.setPopularity(5);

			Product playstation2 = new Product();
			playstation2.setId("id-2");
			playstation2.setName("Playstation Two");
			playstation2.setDescription("Playstation two is the successor of playstation in 2000.");

			Product superNES = new Product();
			superNES.setId("id-3");
			superNES.setName("Super Nintendo");
			superNES.setPopularity(3);

			Product nintendo64 = new Product();
			nintendo64.setId("id-4");
			nintendo64.setName("N64");
			nintendo64.setDescription("Nintendo 64");
			nintendo64.setPopularity(2);

			repository.saveAll(Arrays.asList(playstation, playstation2, superNES, nintendo64));
		}
	}

	@Autowired
	ProductRepository repository;

	@Autowired
	SolrOperations operations;

	/**
	 * {@link HighlightPage} holds next to the entities found also information about where a match was found within the
	 * document. This allows to fine grained display snipplets of data containing the matching term in context.
	 */
	@Test
	public void annotationBasedHighlighting() {

		HighlightPage<Product> products = repository.findByDescriptionStartingWith("play", new PageRequest(0, 10));

		products.getHighlighted().forEach(entry -> entry.getHighlights().forEach(highligh -> System.out
				.println(entry.getEntity().getId() + " | " + highligh.getField() + ":\t" + highligh.getSnipplets())));
	}

	/**
	 * Using {@link Boost} allows to influence scoring at query time. In this case we want hits in {@code Product#name} to
	 * count twice as much as such in {@code Product#description}.
	 */
	@Test
	public void annotationBasedBoosting() {
		repository.findTop10ByNameOrDescription("Nintendo", "Nintendo").forEach(System.out::println);
	}

	/**
	 * Using {@link Function} in queries has no influence on restricting results as all documents will match the function.
	 * Though it does influence document score. In this sample documents not having popularity assigned will be sorted to
	 * the end of the list.
	 */
	@Test
	public void influcenceScoreWithFunctions() {

		Query query = new SimpleQuery(where(exists("popularity"))).addProjectionOnField("score");

		operations.queryForPage("solr/techproducts", query, Product.class).forEach(System.out::println);
	}

	/**
	 * Using {@link SolrOperations#getById(java.io.Serializable, Class)} allows reading uncommitted documents from the
	 * update log.
	 */
	@Test
	public void useRealtimeGetToReadUncommitedDocuments() throws InterruptedException {

		Product xbox = new Product();
		xbox.setId("id-5");
		xbox.setName("XBox");
		xbox.setDescription("Microsift XBox");
		xbox.setPopularity(2);
		Query query = new SimpleQuery(where("id").is(xbox.getId()));

		// add document but delay commit for 3 seconds
		operations.saveBean("solr/techproducts", xbox, Duration.ofSeconds(3));

		// document will not be returned hence not yet committed to the index
		assertThat(operations.queryForObject("solr/techproducts", query, Product.class), is(Optional.empty()));

		// realtime-get fetches uncommitted document
		assertThat(operations.getById("solr/techproducts", xbox.getId(), Product.class), notNullValue());

		// wait a little so that changes get committed to the index - normal query will now be able to find the document.
		Thread.sleep(3010);
		assertThat(operations.queryForObject("solr/techproducts", query, Product.class).isPresent(), is(true));
	}
}

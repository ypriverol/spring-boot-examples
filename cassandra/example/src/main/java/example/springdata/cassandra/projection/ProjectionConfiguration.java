/*
 * Copyright 2016-2018 the original author or authors.
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
package example.springdata.cassandra.projection;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;
import org.springframework.data.cassandra.config.SchemaAction;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

/**
 * Basic {@link Configuration} to create the necessary schema for the {@link Customer} table.
 *
 * @author Mark Paluch
 */
@Configuration
@EnableAutoConfiguration
class ProjectionConfiguration {

	@Configuration
	@EnableCassandraRepositories
	static class CassandraConfig extends AbstractCassandraConfiguration {

		@Override
		public String getKeyspaceName() {
			return "example";
		}

		@Override
		public String[] getEntityBasePackages() {
			return new String[] { Customer.class.getPackage().getName() };
		}

		@Override
		public SchemaAction getSchemaAction() {
			return SchemaAction.RECREATE;
		}
	}
}

package com.mathianasj.spring.rsql;

import org.springframework.data.rest.webmvc.RepositorySearchesResource;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.stereotype.Component;

@Component
public class RsqlSearchResourcesProcessor implements ResourceProcessor<RepositorySearchesResource> {

	@Override
	public RepositorySearchesResource process(RepositorySearchesResource repositorySearchesResource) {
		final String search = repositorySearchesResource.getId().getHref();
		final Link rsqlSearch = new Link(search + "/rsql{?query,page,size}").withRel("rsql");
		repositorySearchesResource.add(rsqlSearch);

		return repositorySearchesResource;
	}

}

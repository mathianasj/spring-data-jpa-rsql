package com.mathianasj.spring.rsql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.repository.support.Repositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.ResourceMetadata;
import org.springframework.data.rest.core.mapping.ResourceType;
import org.springframework.data.rest.core.mapping.SearchResourceMappings;
import org.springframework.data.rest.webmvc.HttpHeadersPreparer;
import org.springframework.data.rest.webmvc.PersistentEntityResourceAssembler;
import org.springframework.data.rest.webmvc.ProfileController;
import org.springframework.data.rest.webmvc.ProfileResourceProcessor;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.data.rest.webmvc.RootResourceInformation;
import org.springframework.data.rest.webmvc.support.DefaultedPageable;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.core.EmbeddedWrappers;
import org.springframework.http.HttpMethod;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;

@RepositoryRestController
public class RsqlSearchController {
	private Map<Class<?>, RsqlRepositoryCustom<?, ?>> respositories;
	private static final EmbeddedWrappers WRAPPERS = new EmbeddedWrappers(false);
	private final PagedResourcesAssembler<Object> pagedResourcesAssembler;

	private final RepositoryEntityLinks entityLinks;
	private final RepositoryRestConfiguration config;

	@Autowired
	public RsqlSearchController(Repositories repositories, RepositoryRestConfiguration config,
			RepositoryEntityLinks entityLinks, PagedResourcesAssembler<Object> assembler,
			HttpHeadersPreparer headersPreparer, List<RsqlRepositoryCustom<?, ?>> inputRepos) {

		this.entityLinks = entityLinks;
		this.config = config;
		pagedResourcesAssembler = assembler;

		this.respositories = new HashMap<>();

		inputRepos.forEach(r -> {
			this.respositories.put(r.getEntityType(), r);
		});
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/{repository}/search/rsql")
	public Resources<?> findAll(@QuerydslPredicate RootResourceInformation resourceInformation,
			@RequestParam String query, DefaultedPageable pageable, Sort sort,
			PersistentEntityResourceAssembler assembler)
			throws ResourceNotFoundException, HttpRequestMethodNotSupportedException {
		resourceInformation.verifySupportedMethod(HttpMethod.GET, ResourceType.COLLECTION);

		RsqlRepositoryCustom<?, ?> repository = this.respositories.get(resourceInformation.getDomainType());

		JpaSpecificationExecutor<?> jpaRepo = (JpaSpecificationExecutor<?>) repository;

		ResourceMetadata metadata = resourceInformation.getResourceMetadata();
		Link baseLink = entityLinks.linkToPagedResource(resourceInformation.getDomainType(),
				pageable.isDefault() ? null : pageable.getPageable());

		// parse rsql
		Node rootNode = new RSQLParser().parse(query);
		@SuppressWarnings("rawtypes")
		Specification spec = rootNode.accept(CustomRsqlVisitorFactory.build(metadata.getDomainType()));

		@SuppressWarnings("unchecked")
		Iterable<?> results = jpaRepo.findAll(spec, pageable.getPageable());

		Resources<?> result = toResources(results, assembler, metadata.getDomainType(), baseLink);
		result.add(getCollectionResourceLinks(resourceInformation, pageable));
		return result;
	}

	protected Link resourceLink(RootResourceInformation resourceLink, @SuppressWarnings("rawtypes") Resource resource) {

		ResourceMetadata repoMapping = resourceLink.getResourceMetadata();

		Link selfLink = resource.getLink("self");
		String rel = repoMapping.getItemResourceRel();

		return new Link(selfLink.getHref(), rel);
	}

	@SuppressWarnings({ "unchecked" })
	protected Resources<?> toResources(Iterable<?> source, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Link baseLink) {

		if (source instanceof Page) {
			Page<Object> page = (Page<Object>) source;
			return entitiesToResources(page, assembler, domainType, baseLink);
		} else if (source instanceof Iterable) {
			return entitiesToResources((Iterable<Object>) source, assembler, domainType);
		} else {
			return entitiesToResources(Collections.EMPTY_LIST, assembler, domainType);
		}
	}

	protected Resources<?> entitiesToResources(Page<Object> page, PersistentEntityResourceAssembler assembler,
			Class<?> domainType, Link baseLink) {

		if (page.getContent().isEmpty()) {
			return pagedResourcesAssembler.toEmptyResource(page, domainType, baseLink);
		}

		return baseLink == null ? pagedResourcesAssembler.toResource(page, assembler)
				: pagedResourcesAssembler.toResource(page, assembler, baseLink);
	}

	protected Resources<?> entitiesToResources(Iterable<Object> entities, PersistentEntityResourceAssembler assembler,
			Class<?> domainType) {

		if (!entities.iterator().hasNext()) {

			List<Object> content = Arrays.<Object>asList(WRAPPERS.emptyCollectionOf(domainType));
			return new Resources<Object>(content, getDefaultSelfLink());
		}

		List<Resource<Object>> resources = new ArrayList<Resource<Object>>();

		for (Object obj : entities) {
			resources.add(obj == null ? null : assembler.toResource(obj));
		}

		return new Resources<Resource<Object>>(resources, getDefaultSelfLink());
	}

	protected Link getDefaultSelfLink() {
		return new Link(ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString());
	}

	private List<Link> getCollectionResourceLinks(RootResourceInformation resourceInformation,
			DefaultedPageable pageable) {

		ResourceMetadata metadata = resourceInformation.getResourceMetadata();
		SearchResourceMappings searchMappings = metadata.getSearchResourceMappings();

		List<Link> links = new ArrayList<Link>();
		links.add(new Link(ProfileController.getPath(this.config, metadata), ProfileResourceProcessor.PROFILE_REL));

		if (searchMappings.isExported()) {
			links.add(entityLinks.linkFor(metadata.getDomainType()).slash(searchMappings.getPath())
					.withRel(searchMappings.getRel()));
		}

		return links;
	}
}

package com.owl.thingsbot.dao;

import com.owl.thingsbot.entity.SiteInformation;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteInformationDao extends PagingAndSortingRepository<SiteInformation, Long> {
    List<SiteInformation> findAll();

    SiteInformation findByUriIsLike(String uri);

    List<SiteInformation> findByUriContainingIgnoreCase(final String url);
}

package com.owl.thingsbot.service;

import com.owl.thingsbot.entity.SiteInformation;

import java.util.List;


public interface SiteInformationService {
    /**
     * Get list of site full information like: site url - description
     *
     * @return list of sites
     */
    List<String> getInformationsList();

    List<SiteInformation> getAll();

    SiteInformation save(final SiteInformation information);

    void deleteById(final Long id);

    SiteInformation getSiteByURL(String url);

    List<SiteInformation> getSitesByUrl(String url);

    SiteInformation getById(final Long id);

    String getAllSitesURL();
}

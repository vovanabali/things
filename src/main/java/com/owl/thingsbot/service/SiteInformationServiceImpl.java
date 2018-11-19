package com.owl.thingsbot.service;

import com.owl.thingsbot.dao.SiteInformationDao;
import com.owl.thingsbot.entity.SiteInformation;
import com.owl.thingsbot.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SiteInformationServiceImpl implements SiteInformationService{
    private final SiteInformationDao siteInformationDao;
    @Override
    public List<String> getInformationsList() {
        return Utils.emptyIfNull(getAll()).stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }

    @Override
    public List<SiteInformation> getAll() {
        return siteInformationDao.findAll();
    }

    @Override
    public SiteInformation save(SiteInformation information) {
        return siteInformationDao.save(information);
    }

    @Override
    public void deleteById(Long id) {
        siteInformationDao.deleteById(id);
    }


    @Override
    public SiteInformation getSiteByURL(String url) {
        return siteInformationDao.findByUriIsLike(url);
    }

    @Override
    public List<SiteInformation> getSitesByUrl(String url) {
        return siteInformationDao.findByUriContainingIgnoreCase(url);
    }

    @Override
    public SiteInformation getById(Long id) {
        return siteInformationDao.findById(id).orElse(null);
    }

    @Override
    public String getAllSitesURL() {
        List<String> urls = Utils.emptyIfNull(getAll()).stream().map(SiteInformation::getUri).collect(Collectors.toList());
        if (urls.isEmpty()) {
            return "База в данный момент пуста";
        }
        final StringBuilder response = new StringBuilder();
        urls.forEach(s -> response.append(s.concat("\n\n")));
        return response.toString();
    }
}

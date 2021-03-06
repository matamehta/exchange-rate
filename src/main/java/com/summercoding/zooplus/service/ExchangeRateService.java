package com.summercoding.zooplus.service;

import com.google.gson.JsonParser;
import com.summercoding.zooplus.config.CacheConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;

/**
 * Service retrieving currency rates from the external source: https://currencylayer.com.
 * <p>
 * Uses caching for historical results.
 */
@Service
@Slf4j
public class ExchangeRateService {

    private static final String URL_LIVE = "http://apilayer.net/api/live";
    private static final String URL_HISTORICAL = "http://apilayer.net/api/historical";

    @Value("${apilayer.key}")
    private String key;

    @Autowired
    private RestTemplate restTemplate;

    public BigDecimal live(String currency) {
        log.info("Sending request to get live exchange rates for currency: {}", currency);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL_LIVE)
                .queryParam("access_key", key)
                .queryParam("currencies", currency);
        return requestExchangeRate(builder);
    }

    @Cacheable(CacheConfig.HISTORICAL_EXCHANGE_CACHE_NAME)
    public BigDecimal historical(String currency, String date) {
        log.info("Sending request to get historical exchange rates for currency: {} and date: {}", currency, date);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(URL_HISTORICAL)
                .queryParam("access_key", key)
                .queryParam("currencies", currency)
                .queryParam("date", date);
        return requestExchangeRate(builder);
    }

    private BigDecimal requestExchangeRate(UriComponentsBuilder builder) {
        String result = restTemplate.getForObject(builder.toUriString(), String.class);

        return new JsonParser()
                .parse(result).getAsJsonObject()
                .get("quotes").getAsJsonObject()
                .entrySet().iterator().next()
                .getValue().getAsBigDecimal();
    }
}

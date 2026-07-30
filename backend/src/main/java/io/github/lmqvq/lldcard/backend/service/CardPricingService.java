package io.github.lmqvq.lldcard.backend.service;

import org.springframework.stereotype.Service;
import io.github.lmqvq.lldcard.backend.entity.CardPricing;
import io.github.lmqvq.lldcard.backend.mapper.CardPricingMapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

@Service
public class CardPricingService {

    private final CardPricingMapper cardPricingMapper;

    public CardPricingService(CardPricingMapper cardPricingMapper) {
        this.cardPricingMapper = cardPricingMapper;
    }

    public Map<String, List<CardPricing>> getAllPricing() {
        List<CardPricing> all = cardPricingMapper.findAll();
        Map<String, List<CardPricing>> result = new HashMap<>();
        result.put("timeCards", all.stream().filter(p -> "time".equals(p.getType())).collect(Collectors.toList()));
        result.put("countCards", all.stream().filter(p -> "count".equals(p.getType())).collect(Collectors.toList()));
        return result;
    }

    public void addPricing(CardPricing pricing) {
        cardPricingMapper.insert(pricing);
    }

    public void updatePricing(CardPricing pricing) {
        cardPricingMapper.update(pricing);
    }

    public void deletePricing(Integer id) {
        cardPricingMapper.delete(id);
    }
}

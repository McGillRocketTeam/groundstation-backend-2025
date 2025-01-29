package ca.mrt.dashboard_persistence.services;

import ca.mrt.dashboard_persistence.models.Card;

import java.util.HashMap;
import java.util.Map;

public class CardService {

    public Card createCardFromMap(Map<String, Object> map) {
        Card card = new Card();
        card.setY(Integer.parseInt(map.get("y").toString()));
        card.setX(Integer.parseInt(map.get("x").toString()));
        card.setHeight(Integer.parseInt(map.get("h").toString()));
        card.setWidth(Integer.parseInt(map.get("w").toString()));
        card.setIndex((map.get("i").toString()));
        card.setConfig(map.get("config").toString());
        return card;
    }

    public Map<String, Object> getCardAsMap(Card card) {
        Map<String, Object> map = new HashMap<>();
        map.put("y", card.getY());
        map.put("x", card.getX());
        map.put("h", card.getHeight());
        map.put("w", card.getWidth());
        map.put("i", card.getIndex());
        map.put("config", card.getConfig());
        return map;
    }
}

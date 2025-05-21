package ca.mrt.dashboard_persistence.models;

import lombok.Data;


@Data
public class Card {
    private int x;
    private int y;
    private int width;
    private int height;
    private String index;
    private String config;
}

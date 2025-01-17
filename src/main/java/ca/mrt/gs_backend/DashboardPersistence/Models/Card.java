package ca.mrt.gs_backend.DashboardPersistence.Models;
import lombok.Data;

import java.util.HashMap;


@Data
public class Card {
    private int x;
    private int y;
    private int width;
    private int height;
    private String index;
    private HashMap<String, Object> config;
}

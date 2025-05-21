package ca.mrt.dashboard_persistence.models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Dashboard {
    private String iconName;
    private String name;
    private String path;
    private List<Card> layout = new ArrayList<Card>();
}

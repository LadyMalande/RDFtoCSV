package convertor;

import java.util.HashMap;
import java.util.Map;

public class Row {
    String id;
    Map<String, String> map;

    public Row(String id) {
        this.id = id;
        this.map = new HashMap<>();
    }
}

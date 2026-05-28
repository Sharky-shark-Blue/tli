package io.tlipoca.mod.oracle;

public class OracleDefinition {
    private final String id;
    private final String displayName;

    public OracleDefinition(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }
}

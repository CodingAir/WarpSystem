package de.codingair.warpsystem.spigot.features.playerwarps.imports;

public enum ImportType {
    ESSENTIALS(new EssentialsImportFilter());

    private final ImportFilter filter;

    ImportType(ImportFilter filter) {
        this.filter = filter;
    }

    public ImportFilter getFilter() {
        return filter;
    }
}

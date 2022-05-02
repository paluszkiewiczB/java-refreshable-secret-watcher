package pl.paluszkiewicz.fsisc;

public interface ByEventTypeMappingDelegate<S extends Secret> {
    char[] onDelete(S source);

    char[] onEdit(S source);

    char[] onCreate(S source);
}

package me.noramibu.dynamictrim.runtime.client.model.item;

public final class TrimModelPredicate {
    private TrimModelPredicate(float trimType) {
        this.trimType = trimType;
    }

    public float trimType;

    public static TrimModelPredicate of(float trimType) {
        return new TrimModelPredicate(trimType);
    }
}
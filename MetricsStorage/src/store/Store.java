package store;

import store.DynamoStore;

public class Store {
    private static DynamoStore store = new DynamoStore();
    public static synchronized DynamoStore getStore() { return store; }
}

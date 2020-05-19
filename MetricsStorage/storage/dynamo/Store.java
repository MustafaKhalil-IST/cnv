package storage.dynamo;

public class Store {
    private static DynamoStore store = new DynamoStore();
    public static synchronized DynamoStore getStore() { return store; }
}

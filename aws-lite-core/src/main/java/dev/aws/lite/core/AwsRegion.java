package dev.aws.lite.core;

public record AwsRegion(String name) {
    @Override
    public String toString() {
        return name;
    }
}

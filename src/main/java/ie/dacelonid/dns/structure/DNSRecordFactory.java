package ie.dacelonid.dns.structure;

@FunctionalInterface
public interface DNSRecordFactory<T> {
    T from(byte[] data, int index);
}
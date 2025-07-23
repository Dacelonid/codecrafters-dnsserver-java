package ie.dacelonid.dns.structure;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ie.dacelonid.dns.bitutils.DNSParserUtils.intToBytes;

public abstract class DNSPart {

    protected String name;
    protected int type;
    protected int classValue;

    public byte[] toBytes() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            String[] split = name.split("\\.");
            for (String s : split) {
                output.write(s.length());
                output.write(s.getBytes(StandardCharsets.UTF_8));
            }
            output.write(0x00);
            output.write(intToBytes(type, 2));
            output.write(intToBytes(classValue, 2));
            output.write(additionalInfo());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return output.toByteArray();
    }

    protected abstract byte[] additionalInfo();


}

package ie.dacelonid.dns.structure;

import ie.dacelonid.dns.bitutils.BitReader;
import ie.dacelonid.dns.bitutils.BitWriter;

import java.util.Arrays;
import java.util.Objects;

public class Header {
    private final int packetID;
    private final int queryResponseID;
    private final int opCode;
    private final int authAns;
    private final int trunc;
    private final int recurDesired;
    private final int recurAvail;
    private final int reserved;
    private final int respCode;
    private final int questionCount;
    private final int ansRecordCount;
    private final int authRecordCount;
    private final int additionalRecordCount;

    public int getQueryResponseID() {
        return queryResponseID;
    }

    public int getAuthAns() {
        return authAns;
    }

    public int getTrunc() {
        return trunc;
    }

    public int getRecurDesired() {
        return recurDesired;
    }

    public int getRecurAvail() {
        return recurAvail;
    }

    public int getReserved() {
        return reserved;
    }

    public int getRespCode() {
        return respCode;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public int getAnsRecordCount() {
        return ansRecordCount;
    }

    public int getAuthRecordCount() {
        return authRecordCount;
    }

    public int getAdditionalRecordCount() {
        return additionalRecordCount;
    }

    public Header(byte[] header) {
        BitReader reader = new BitReader(header);
        packetID = reader.readBits(16);
        queryResponseID = reader.readBits(1);
        opCode = reader.readBits(4);
        authAns = reader.readBits(1);
        trunc = reader.readBits(1);
        recurDesired = reader.readBits(1);
        recurAvail = reader.readBits(1);
        reserved = reader.readBits(3);
        respCode = reader.readBits(4);
        questionCount = reader.readBits(16);
        ansRecordCount = reader.readBits(16);
        authRecordCount = reader.readBits(16);
        additionalRecordCount = reader.readBits(16);
    }

    public byte[] tobytes() {
            BitWriter writer = new BitWriter(12);
            writer.writeBits(packetID, 16);
            writer.writeBits(queryResponseID, 1);
            writer.writeBits(opCode, 4);
            writer.writeBits(authAns, 1);
            writer.writeBits(trunc, 1);
            writer.writeBits(recurDesired, 1);
            writer.writeBits(recurAvail, 1);
            writer.writeBits(reserved, 3);
            writer.writeBits(respCode, 4);
            writer.writeBits(questionCount, 16);
            writer.writeBits(ansRecordCount, 16);
            writer.writeBits(authRecordCount, 16);
            writer.writeBits(additionalRecordCount, 16);
            return writer.getBytes();
    }

    private Header(HeaderBuilder builder) {
        packetID = builder.packetID;
        queryResponseID = builder.queryResponseID;
        opCode = builder.opCode;
        authAns = builder.authAns;
        trunc = builder.trunc;
        recurDesired = builder.recurDesired;
        recurAvail = builder.recurAvail;
        reserved = builder.reserved;
        respCode = builder.respCode;
        questionCount = builder.questionCount;
        ansRecordCount = builder.ansRecordCount;
        authRecordCount = builder.authRecordCount;
        additionalRecordCount = builder.additionalRecordCount;
    }

    @Override
    public String toString() {
        return "Header{" +
                "packetID=" + packetID +
                ", queryResponseID=" + queryResponseID +
                ", opCode=" + opCode +
                ", authAns=" + authAns +
                ", trunc=" + trunc +
                ", recurDesired=" + recurDesired +
                ", recurAvail=" + recurAvail +
                ", reserved=" + reserved +
                ", respCode=" + respCode +
                ", questionCount=" + questionCount +
                ", ansRecordCount=" + ansRecordCount +
                ", authRecordCount=" + authRecordCount +
                ", additionalRecordCount=" + additionalRecordCount +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Header header)) return false;
        return packetID == header.packetID && queryResponseID == header.queryResponseID && opCode == header.opCode && authAns == header.authAns && trunc == header.trunc && recurDesired == header.recurDesired && recurAvail == header.recurAvail && reserved == header.reserved && respCode == header.respCode && questionCount == header.questionCount && ansRecordCount == header.ansRecordCount && authRecordCount == header.authRecordCount && additionalRecordCount == header.additionalRecordCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packetID, queryResponseID, opCode, authAns, trunc, recurDesired, recurAvail, reserved, respCode, questionCount, ansRecordCount, authRecordCount, additionalRecordCount);
    }

    public int getPacketID() {
        return packetID;
    }

    public int getOpCode() {
        return opCode;
    }

    public static class HeaderBuilder {
        private int packetID = 0;
        private int queryResponseID = 0;
        private int opCode = 0;
        private int authAns = 0;
        private int trunc = 0;
        private int recurDesired = 0;
        private int recurAvail = 0;
        private int reserved = 0;
        private int respCode = 0;
        private int questionCount = 0;
        private int ansRecordCount = 0;
        private int authRecordCount = 0;
        private int additionalRecordCount = 0;

        public HeaderBuilder packetID(int packetID) {
            this.packetID = packetID;
            return this;
        }

        public HeaderBuilder queryResponseID(int queryResponseID) {
            this.queryResponseID = queryResponseID;
            return this;
        }

        public HeaderBuilder opCode(int opCode) {
            this.opCode = opCode;
            return this;
        }

        public HeaderBuilder authAns(int authAns) {
            this.authAns = authAns;
            return this;
        }

        public HeaderBuilder trunc(int trunc) {
            this.trunc = trunc;
            return this;
        }

        public HeaderBuilder recurDesired(int recurDesired) {
            this.recurDesired = recurDesired;
            return this;
        }

        public HeaderBuilder recurAvail(int recurAvail) {
            this.recurAvail = recurAvail;
            return this;
        }

        public HeaderBuilder reserved(int reserved) {
            this.reserved = reserved;
            return this;
        }

        public HeaderBuilder respCode(int respCode) {
            this.respCode = respCode;
            return this;
        }

        public HeaderBuilder questionCount(int questionCount) {
            this.questionCount = questionCount;
            return this;
        }

        public HeaderBuilder ansRecordCount(int ansRecordCount) {
            this.ansRecordCount = ansRecordCount;
            return this;
        }

        public HeaderBuilder authRecordCount(int authRecordCount) {
            this.authRecordCount = authRecordCount;
            return this;
        }

        public HeaderBuilder additionalRecordCount(int additionalRecordCount) {
            this.additionalRecordCount = additionalRecordCount;
            return this;
        }

        public Header build() {
            return new Header(this);
        }

        public Header from(byte[] data) {
            byte[] header = new byte[12];
            System.arraycopy(data, 0, header, 0, 12);
            return new Header(header);
        }

        public Header fromRequest(DNSMessage request) {
            Header requestHeader = request.getHeader();
            return new HeaderBuilder().packetID(requestHeader.getPacketID())
                    .queryResponseID(1).questionCount(requestHeader.questionCount)
                    .ansRecordCount(requestHeader.questionCount).opCode(requestHeader.getOpCode())
                    .recurDesired(requestHeader.getRecurDesired())
                    .respCode(requestHeader.getOpCode() == 0 ? 0 : 4)
                    .questionCount(requestHeader.getQuestionCount()).build();
        }
    }
}

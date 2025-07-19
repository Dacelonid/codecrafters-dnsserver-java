package ie.dacelonid.dns.structure;

import ie.dacelonid.dns.bitutils.BitWriter;

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
    private byte[] header;

    public byte[] tobytes() {
        if (header == null) {
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
            header = writer.getBytes();
        }
        return header;
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

    }
}

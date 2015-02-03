package es.bsc.tools.undef2prv;

class SyncInfo {

    public Long tsc;
    public Long timestamp;
    public Long start;
    public RecordNEvent ner;

    public SyncInfo(RecordNEvent ner) {
        this.tsc = new Long(ner.getTime());
        this.timestamp = new Long(ner.getTimestamp());
        this.start = (this.timestamp*1000L - this.tsc);
        this.ner = ner;
    }

    public String getApplication() {
        return this.ner.Application;
    }

}

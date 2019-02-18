package pl.sszwaczyk.security.threat;

public interface IThreatListener {

    void threatStarted(Threat threat);

    void threatEnded(Threat threat);
}

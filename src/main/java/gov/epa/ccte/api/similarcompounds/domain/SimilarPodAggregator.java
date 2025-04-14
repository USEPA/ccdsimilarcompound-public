package gov.epa.ccte.api.similarcompounds.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;


@Data
public class SimilarPodAggregator {

    @Column(name = "dtxsid")
    @JsonProperty("dtxsid")
    private String dtxsid;

    @Column(name = "iris_total")
    @JsonProperty("irisTotal")
    private Integer irisTotal;

    @Column(name = "pprtv_total")
    @JsonProperty("pprtvTotal")
    private Integer pprtvTotal;

    @Column(name = "atsdr_total")
    @JsonProperty("atsdrTotal")
    private Integer atsdrTotal;

    @Column(name = "opp_total")
    @JsonProperty("oppTotal")
    private Integer oppTotal;

    @Column(name = "other_total")
    @JsonProperty("otherTotal")
    private Integer otherTotal;

}

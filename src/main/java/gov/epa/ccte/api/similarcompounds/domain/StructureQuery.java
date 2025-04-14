package gov.epa.ccte.api.similarcompounds.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Data;

@Data
public class StructureQuery {
    @Column(name = "dtxsid")
    @JsonProperty("dtxsid")
    private String dtxsid;

    @Column(name = "dtxcid")
    @JsonProperty("dtxcid")
    private String dtxcid;

    @Column(name = "casrn")
    @JsonProperty("casrn")
    private String casrn;

    @Column(name = "preferred_name")
    @JsonProperty("preferredName")
    private String preferredName;
}

package gov.epa.ccte.api.similarcompounds.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimilarCompoundCount {
    @JsonProperty("similarCompounds")
    private Integer similarCompounds;
    @JsonProperty("tanimoto")
    private Double tanimoto;
}

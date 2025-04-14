package gov.epa.ccte.api.similarcompounds.web.rest.errors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoCompoundFoundException extends RuntimeException {
    public NoCompoundFoundException(String dtxcid, String smiles) {
        super("No similaer compounds are found for dtxcid =" + dtxcid + ", smiles =" + smiles);

        log.debug("No compound error found dtxcid=%1, smiles=%2", dtxcid, smiles);
    }
}

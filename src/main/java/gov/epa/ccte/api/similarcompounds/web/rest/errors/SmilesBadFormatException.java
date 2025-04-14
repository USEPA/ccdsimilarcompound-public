package gov.epa.ccte.api.similarcompounds.web.rest.errors;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SmilesBadFormatException extends RuntimeException {
    public SmilesBadFormatException(String smiles) {

        super("Smiles String ( " + smiles + ")  not meeting the expected basic format");

        log.debug("Smiles String (%1)  not meeting the expected basic format", smiles);
    }
}

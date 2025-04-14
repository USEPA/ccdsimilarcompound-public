package gov.epa.ccte.api.similarcompounds.web.rest;

import gov.epa.ccte.api.similarcompounds.domain.ResultCompound;
import gov.epa.ccte.api.similarcompounds.domain.SimilarCompoundCount;
import gov.epa.ccte.api.similarcompounds.domain.SimilarSubstance;
import gov.epa.ccte.api.similarcompounds.domain.StructureQuery;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
/* -- added a HTTP filter config/CorsFilter to avoid all cors errors
 @CrossOrigin(origins = {"http://localhost:3003", "http://comptox-int-edge.epa.gov", "http://ccte-ccd-stg.epa.gov", "http://ccte-ccd.epa.gov"})
*/
@RestController
public class SimilarCompoundController {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public SimilarCompoundController(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @RequestMapping(value = {"/no-known/{tanimoto}"}, method = RequestMethod.GET)
    @ResponseBody
    public List<ResultCompound> generic(@Parameter(description="Similarity cut off value.")  @PathVariable("tanimoto") Double tanimoto,
                                        @Parameter(description="Source Compound's smiles string") @RequestParam("smiles") String smiles,
                                        @Parameter(description="Toxval") @RequestParam(value = "toxval", required = false, defaultValue = "") String toxval,
                                        @Parameter(description = "Chemicals with atomics") @RequestParam(name = "includes", required = false)ArrayList<String> includes,
                                        @Parameter(description = "Chemicals without atomics") @RequestParam(name = "excludes", required = false)ArrayList<String> excludes
                                        ) throws Exception {

        log.debug("tanimoto= {}, smiles= {}, toxval = {} ", tanimoto, smiles,toxval);

        // Setting up default value
        if(tanimoto == null)
            tanimoto = 0.8;


        String sql = "select 'false' as selected, c.dtxsid, s.dsstox_compound_id as dtxcid, generic_substance_id, casrn, " +
                " preferred_name, c.compound_id, stereo, isotope, multicomponent, pubchem_count,\n" +
                "       pubmed_count, sources_count, cpdata_count, active_assays, total_assays, percent_assays, toxcast_select,\n" +
                "       monoisotopic_mass, mol_formula, qc_level, qc_level_desc, pubchem_cid, related_substance_count, \n" +
                "       related_structure_count, has_structure_image, iupac_name, smiles, inchi_string, average_mass, inchikey, toxval_data, " +
                "       bingo.getsimilarity(molfile,:smiles,'Tanimoto') as similarity\n" +
                "from similarity_search.dsstox_structures s join similarity_search.chemical_details c on s.cid = c.compound_id\n" +
                "where (molfile @ (:tanimoto,1,:smiles,'Tanimoto')::bingo.sim) AND dtxsid <> 'DTXSID00000000'" +
                " AND (c.inchikey is not null or c.mol_formula is not null) ";

        if(toxval.equalsIgnoreCase("Y")){
            log.debug("add toxval=Y");
            sql = sql.concat(" AND c.toxval_data = 'Y' ORDER BY similarity desc, monoisotopic_mass asc");
        }else{
            log.debug("no toxval=Y");
            sql = sql.concat("ORDER BY similarity desc, monoisotopic_mass asc");
        }


        List<ResultCompound> resultCompounds =  jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("smiles", smiles)
                .addValue("tanimoto", tanimoto), new BeanPropertyRowMapper(ResultCompound.class)
        );

        if(includes != null || excludes != null){
            return filterResult(resultCompounds, includes, excludes);
        }else{
            return resultCompounds;
        }
        //return resultCompounds;
    }

    @RequestMapping(value = {"/substructure/"}, method = RequestMethod.GET)
    @ResponseBody
    public List<ResultCompound> substructure(@Parameter(description="Source Compound's smiles string") @RequestParam("smiles") String smiles,
                                             @Parameter(description="Toxval") @RequestParam(value = "toxval", required = false, defaultValue = "") String toxval,
                                             @Parameter(description = "Chemicals with atomics") @RequestParam(name = "includes", required = false)ArrayList<String> includes,
                                             @Parameter(description = "Chemicals without atomics") @RequestParam(name = "excludes", required = false)ArrayList<String> excludes
                                              ) throws Exception {

        log.debug("substructure for = {}, toxval={}", smiles, toxval);

        String sql = "select 'false' as selected, c.dtxsid, s.dsstox_compound_id as dtxcid, generic_substance_id, casrn, " +
                " preferred_name, c.compound_id, stereo, isotope, multicomponent, pubchem_count,\n" +
                "       pubmed_count, sources_count, cpdata_count, active_assays, total_assays, percent_assays, toxcast_select,\n" +
                "       monoisotopic_mass, mol_formula, qc_level, qc_level_desc, pubchem_cid, related_substance_count, \n" +
                "       related_structure_count, has_structure_image, iupac_name, smiles, inchi_string, average_mass, inchikey, c.toxval_data," +
                "       bingo.getsimilarity(molfile,:smiles,'Tanimoto') as similarity\n" +
                "from similarity_search.dsstox_structures s join similarity_search.chemical_details c on s.cid = c.compound_id\n" +
                "where (molfile @(:smiles,'')::bingo.sub) AND dtxsid <> 'DTXSID00000000' AND (c.inchikey is not null or c.mol_formula is not null) ";

        if(toxval.equalsIgnoreCase("Y")) {
            log.debug("adding toxval=Y ");
            sql = "SELECT * FROM (" + sql.concat(" AND c.toxval_data = 'Y' LIMIT 10000) AS Base_SQL ORDER BY monoisotopic_mass ASC");
        }else {
            log.debug("without toxval=Y ");
            sql = "SELECT * from ("+ sql.concat(" LIMIT 10000) AS BASE_SQL ORDER BY monoisotopic_mass asc");

        }

        List<ResultCompound> resultCompounds =  jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("smiles", smiles),
                new BeanPropertyRowMapper(ResultCompound.class)
        );

        excludes = makeItNullIfNoElements(excludes);
        includes = makeItNullIfNoElements(includes);

        List<ResultCompound> returnValues;

        if(includes != null){
            returnValues = new ArrayList<>();

            log.debug("{} before element include filter", resultCompounds.size());
            for(ResultCompound compound : resultCompounds) {
                if(stringExist(compound.getMolFormula(), includes) == true)
                    returnValues.add(compound);
            }
            log.debug("{} after element include filter", returnValues.size());
            resultCompounds = returnValues;
        }

        if(excludes != null){
            returnValues = new ArrayList<>();

            log.debug("{} before element exclude filter", resultCompounds.size());
            for(ResultCompound compound : resultCompounds){
                if(stringExist(compound.getMolFormula(), excludes) == false)
                    returnValues.add(compound);
            }
            log.debug("{} after element exclude filter", returnValues.size());
            resultCompounds = returnValues;
        }

        log.debug("return records {} ", resultCompounds.size());

        return resultCompounds;
    }

    private ArrayList<String> makeItNullIfNoElements(ArrayList<String> array) {
        if(array == null)
            return null;
        else if(array.size() == 0)
            return null;
        else if(array.size() == 1 && array.get(0).equalsIgnoreCase("null"))
            return null;
        else
            return array;
    }

    private List<ResultCompound> filterResult(List<ResultCompound> resultCompounds, ArrayList<String> includes, ArrayList<String> excludes) {
        ArrayList<ResultCompound> returnValues = new ArrayList<>();

        log.debug("{} before element filter", resultCompounds.size());
        for(ResultCompound compound : resultCompounds){
            if((includes != null && stringExist(compound.getMolFormula(), includes) == true) &&
                    (excludes != null && stringExist(compound.getMolFormula(), excludes) == false))
                returnValues.add(compound);
            else if(excludes == null && stringExist(compound.getMolFormula(), includes) == true)
                returnValues.add(compound);
            else if(includes != null && stringExist(compound.getMolFormula(), excludes) == false)
                returnValues.add(compound);
        }

        log.debug("{} after element filter", returnValues.size());
        return returnValues;
    }

    // I have to create this when UI is sending null character - CE-3842
    private Boolean filtersExists(ArrayList<String> array) {
//        if(array == null)
//            return false;
//        if(array.size() == 0)
//            return false;
//        if(array.size() == 1 && array.get(0).equalsIgnoreCase("null"))
//            return false;
//        else
//            return true;
        if(array == null)
            return false;
        else
            return true;
    }

    private boolean stringExist(String formula, ArrayList<String> elements) {
        if(elements == null)
            return true;
        else
        // return elements.stream().anyMatch(formula::contains);
            return elements.stream().allMatch(formula::contains);
    }

    @RequestMapping(value = {"/structure/"}, method = RequestMethod.GET)
    @ResponseBody
    public List<StructureQuery> structure(@Parameter(description="Source Compound's smiles string") @RequestParam("smiles") String smiles) throws Exception {

        log.debug("structure for = {}", smiles);

        String sql = "select c.dtxsid, s.dsstox_compound_id as dtxcid, casrn, preferred_name " +
                "from similarity_search.dsstox_structures s join similarity_search.chemical_details c on s.cid = c.compound_id\n" +
                "where (molfile @(:smiles,'')::bingo.exact) AND dtxsid <> 'DTXSID00000000'";


        List<StructureQuery> structureQuery =  jdbcTemplate.query(sql, new MapSqlParameterSource()
                        .addValue("smiles", smiles),
                new BeanPropertyRowMapper(StructureQuery.class)
        );

        return structureQuery;
    }

    @RequestMapping(value = {"/by-dtxcid/{dtxcid}/{tanimoto}"}, method = RequestMethod.GET)
    @ResponseBody
    public List<ResultCompound> getSimilarCompoundsWithTanimoto(@Parameter(description="Source Compound's DTXCID") @PathVariable("dtxcid") String dtxcid,
                                                                @Parameter(description="Similarity cut off value.")  @PathVariable("tanimoto") Double tanimoto,
                                                                @Parameter(description="Source Compound's smiles string") @RequestParam("smiles") String smiles) throws Exception {

        log.debug("dtxcid= {}, smiles= {}, tanimoto= {}", dtxcid, smiles, tanimoto);

        // Setting up default value
        if(tanimoto == null)
            tanimoto = 0.8;

        // validating smile string
        // I need to get better regex for validating smiles string. For DTXSID4046076  it is returning wrong smiles where it is correct one.
/*        if(validate(smiles) == false)
            throw new SmilesBadFormatException(smiles);*/

        String sql = "select 'false' as selected, c.dtxsid, s.dsstox_compound_id as dtxcid, generic_substance_id, casrn, " +
                " preferred_name, c.compound_id, stereo, isotope, multicomponent, pubchem_count,\n" +
                "       pubmed_count, sources_count, cpdata_count, active_assays, total_assays, percent_assays, toxcast_select,\n" +
                "       monoisotopic_mass, mol_formula, qc_level, qc_level_desc, pubchem_cid, related_substance_count, \n" +
                "       related_structure_count, has_structure_image, iupac_name, smiles, inchi_string, average_mass, inchikey, " +
                "       bingo.getsimilarity(molfile,:smiles,'Tanimoto') as similarity\n" +
                "from similarity_search.dsstox_structures s join similarity_search.chemical_details c on s.cid = c.compound_id\n" +
                "where (molfile @ (:tanimoto,1,:smiles,'Tanimoto')::bingo.sim) AND (s.dsstox_compound_id != :dtxcid) AND dtxsid <> 'DTXSID00000000'" +
                " AND (c.inchikey is not null or c.mol_formula is not null) ORDER BY similarity desc, monoisotopic_mass asc";

        List<ResultCompound> resultCompounds =  jdbcTemplate.query(sql, new MapSqlParameterSource()
                .addValue("smiles", smiles)
                .addValue("tanimoto", tanimoto)
                .addValue("dtxcid", dtxcid), new BeanPropertyRowMapper(ResultCompound.class)
        );

        return resultCompounds;

/*        if(similarCompounds == null || similarCompounds.isEmpty()){
            throw new NoCompoundFoundException(dtxcid, smiles);
        }else
            return similarCompounds;*/
    }
    
    @RequestMapping(value = "/count-by-dtxcid/{dtxcid}/", method = RequestMethod.GET)
    @ResponseBody
    public SimilarCompoundCount getSimilarCompoundsCount(
            @Parameter(description="Source Compound's DTXCID") @PathVariable("dtxcid") String dtxcid,
            @Parameter(description="Source Compound's smiles string") @RequestParam("smiles") String smiles) throws Exception {

        log.debug("dtxcid= {}, smiles= {}, tanimoto= {}", dtxcid, smiles);

        Double tanimoto = 0.8;

        String sql = "select count(*)\n" +
                "from similarity_search.dsstox_structures s Join similarity_search.chemical_details d on s.dsstox_compound_id=d.dtxcid\n" +
                "where (molfile @ (:tanimoto,1,:smiles,'Tanimoto')::bingo.sim) AND (s.dsstox_compound_id != :dtxcid)AND dtxsid <> 'DTXSID00000000'" +
                "AND (d.inchikey is not null or d.mol_formula is not null)";

        Integer count =  jdbcTemplate.queryForObject(sql, new MapSqlParameterSource()
                .addValue("smiles", smiles)
                .addValue("tanimoto", tanimoto)
                .addValue("dtxcid", dtxcid), Integer.class
        );

        if(count == null || count == 0 ){
            return SimilarCompoundCount.builder().similarCompounds(0).tanimoto(tanimoto).build();
        }else{
            return SimilarCompoundCount.builder().similarCompounds(count).tanimoto(tanimoto).build();

        }
    }

    @RequestMapping(value = {"/by-dtxsid/{dtxsid}/{tanimoto}"}, method = RequestMethod.GET)
    @ResponseBody
    public List<SimilarSubstance> getSimilarSubstance(@Parameter(description="Source Compound's DTXSID") @PathVariable("dtxsid") String dtxsid,
                                                      @Parameter(description="Similarity cut off value.")  @PathVariable("tanimoto") Double tanimoto
                                                                ) throws Exception {

        log.debug("dtxsid= {}, tanimoto= {}", dtxsid, tanimoto);

        // Setting up default value
        if(tanimoto == null)
            tanimoto = 0.8;

        // Get the SMILEs for given DTXSID
        String smilesQuery = "select smiles from similarity_search.chemical_details where dtxsid = :dtxsid";

        String smiles = jdbcTemplate.queryForObject(smilesQuery, new MapSqlParameterSource()
                .addValue("dtxsid", dtxsid), String.class);

        String similarSubstanceQuery = "select dtxsid, dtxcid, casrn, preferred_name,\n" +
                "       bingo.getsimilarity(molfile, :smiles, 'Tanimoto') as similarity\n" +
                "from similarity_search.dsstox_structures s\n" +
                "         Join similarity_search.chemical_details d on s.dsstox_compound_id = d.dtxcid\n" +
                "where (molfile @ (:tanimoto, 1, :smiles, 'Tanimoto')::bingo.sim)\n" +
                " AND (d.inchikey is not null or d.mol_formula is not null)" +
                " AND dtxsid <> 'DTXSID00000000' AND dtxsid <> :dtxsid \n" +
                "order by bingo.getsimilarity(molfile, :smiles, 'Tanimoto') desc";

        List<SimilarSubstance> resultSubstances =  jdbcTemplate.query(similarSubstanceQuery, new MapSqlParameterSource()
                .addValue("tanimoto", tanimoto)
                .addValue("dtxsid", dtxsid)
                .addValue("smiles", smiles), new BeanPropertyRowMapper(SimilarSubstance.class)
        );

        return resultSubstances;

/*        if(similarCompounds == null || similarCompounds.isEmpty()){
            throw new NoCompoundFoundException(dtxcid, smiles);
        }else
            return similarCompounds;*/
    }


    // validating Smile String
    private boolean validate(String smiles){
        log.debug("valdating SMILES = {} ", smiles);

        boolean flag = smiles.matches("^([^J][A-Za-z0-9@+\\-\\[\\]\\(\\)\\\\\\/%=#$]+)$");

        log.debug("validate result = {} ", flag);

        return flag;
    }
}

package services.impl;

import exceptions.IncorrectCNFException;
import prism.Prism;
import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismLog;
import repositories.model.CNF.CNFModel;
import repositories.model.CNF_negation.CNFNegationModel;
import repositories.model.ModelCheckResult;
import services.CNFModelCheckerService;
import utils.CombinationsUtils;
import utils.ModelParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CNFModelCheckerServiceImpl implements CNFModelCheckerService {

    private CombinationsUtils combinationsUtils = new CombinationsUtils();

    private List<int[]> spec;

    private List<String> organizations;

    @Override
    public List<ModelCheckResult> getModelCheckingResult(String configPath, boolean computeBackwards) {
        List<ModelCheckResult> result = new ArrayList<>();
        ModelParser modelParser = new ModelParser();
        File configFile = new File(configPath);

        try {
            CNFNegationModel cnfModel = modelParser.parseCNFNegationModel(configFile);

            Map<String, Double> probabilitiesMap = modelParser.parseAcceptanceProbabilities(configFile);

            List<Double> probabilities = new ArrayList<>(cnfModel.getOrganizations().size());

            organizations = cnfModel.getOrganizations();

            for (String orgName :
                    cnfModel.getOrganizations()) {
                probabilities.add(probabilitiesMap.get(orgName));
            }

            List<Set<int[]>> backwardTransitionsCombinations = new ArrayList<>();

            if (computeBackwards) {
                backwardTransitionsCombinations = combinationsUtils
                        .getBackwardTransitionsCombinations(cnfModel.getOrganizations().size());
            }
            else{
                Set<int[]> zeroBackwards = new HashSet<>(1);
                zeroBackwards.add(new int[]{});
                backwardTransitionsCombinations.add(zeroBackwards);
            }

            spec = modelParser.getSortedSpecifications(cnfModel);

            for (Set<int[]> backwardTransitions :
                    backwardTransitionsCombinations) {
//                System.out.println("compute with backward transition: ");
//                backwardTransitions.forEach(backwards -> {
//                    System.out.println(Arrays.toString(backwards));
//                });
                CNFModelGeneratorServiceImpl modelGenerator = new CNFModelGeneratorServiceImpl(spec, probabilities,
                        backwardTransitions);
                ModelCheckResult modelCheckResult = new ModelCheckResult(cnfModel.getOrganizations());
                modelCheckResult.setBackwardTransitions(backwardTransitions);
                double probability = 0;
                double expectedMessages = 0;

                try {
                    // Create a log for PRISM output (hidden or stdout)
                    PrismLog mainLog = new PrismDevNullLog();
                    //PrismLog mainLog = new PrismFileLog("stdout");

                    // Initialise PRISM engine
                    Prism prism = new Prism(mainLog);
                    prism.initialise();

                    // Load the model generator into PRISM,
                    // export the model to a dot file (which triggers its construction)
                    prism.loadModelGenerator(modelGenerator);
                    prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("./resources/dtmc.dot"));

                    // Then do some model checking and print the result
                    String[] props = new String[]{
                            "P=?[F \"goal\"]",
                            "R=?[F \"end\"]"
                    };

                    probability = Double.parseDouble(prism.modelCheck(props[0]).getResult().toString());
                    expectedMessages = Double.parseDouble(prism.modelCheck(props[1]).getResult().toString());

                    // Close down PRISM
                    prism.closeDown();

                } catch (FileNotFoundException | PrismException e) {
                    System.out.println("Error: " + e.getMessage());
                    System.exit(1);
                }

                modelCheckResult.setProbability(probability);
                modelCheckResult.setExpectedMessages(expectedMessages);
                result.add(modelCheckResult);
            }
        } catch (IOException | IncorrectCNFException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }

    public List<int[]> getSpec() {
        return spec;
    }

    public List<String> getOrganizations() {
        return organizations;
    }
}

package services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import repositories.model.ModelCheckResult;
import repositories.model.ModelCheckerResultWrapper;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CNFModelChecker {
    static boolean computeBackwards;
    static String configPath;
    static String specPath = null;
    static String outputPath;

    public static void main(String[] args) throws Exception {
        int argsNum = args.length;
        configPath = args[0];
        if (argsNum == 3) {
            outputPath = args[1];
            computeBackwards = Boolean.parseBoolean(args[2]);
        } else if (argsNum == 4) {
            specPath = args[1];
            outputPath = args[2];
            computeBackwards = Boolean.parseBoolean(args[3]);
        } else {
            throw new Exception("Incorrect number of input arguments.");
        }

        new CNFModelChecker().run();
    }

    public void run() {
        CNFModelCheckerServiceImpl cnfModelCheckerService = new CNFModelCheckerServiceImpl();
        List<ModelCheckResult> results = cnfModelCheckerService.getModelCheckingResult(configPath, specPath, outputPath,
                computeBackwards);

        ModelCheckerResultWrapper modelCheckerResultWrapper = new ModelCheckerResultWrapper();
        modelCheckerResultWrapper.setModelCheckResultList(results);
        modelCheckerResultWrapper.setSpecification(cnfModelCheckerService.getSpec());
        modelCheckerResultWrapper.setOrganizations(cnfModelCheckerService.getOrganizations());

        outputPath = outputPath + "/results.json";

        System.out.printf("writing results to json in %s%n", outputPath);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File(outputPath), modelCheckerResultWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

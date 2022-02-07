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
    static String outputPath;

    public static void main(String[] args) {
        configPath = args[0];
        outputPath = args[1];
        computeBackwards = Boolean.parseBoolean(args[2]);

        new CNFModelChecker().run();
    }

    public void run() {
        CNFModelCheckerServiceImpl cnfModelCheckerService = new CNFModelCheckerServiceImpl();
        List<ModelCheckResult> results = cnfModelCheckerService.getModelCheckingResult(configPath, computeBackwards);

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

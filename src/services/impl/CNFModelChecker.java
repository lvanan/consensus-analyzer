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

    public static void main(String[] args) {
        configPath = args[0];
        computeBackwards = Boolean.parseBoolean(args[1]);

        new CNFModelChecker().run();
    }

    public void run() {
        CNFModelCheckerServiceImpl cnfModelCheckerService = new CNFModelCheckerServiceImpl();
        List<ModelCheckResult> results = cnfModelCheckerService.getModelCheckingResult(configPath, computeBackwards);

        ModelCheckerResultWrapper modelCheckerResultWrapper = new ModelCheckerResultWrapper();
        modelCheckerResultWrapper.setModelCheckResultList(results);
        modelCheckerResultWrapper.setSpecification(cnfModelCheckerService.getSpec());
        modelCheckerResultWrapper.setOrganizations(cnfModelCheckerService.getOrganizations());

        System.out.println("great, now writing results to json: ");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(new File("resources/results.json"), modelCheckerResultWrapper);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

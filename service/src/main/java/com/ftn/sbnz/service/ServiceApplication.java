package com.ftn.sbnz.service;

import org.drools.template.ObjectDataCompiler;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.runtime.conf.ClockTypeOption;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

@SpringBootApplication
public class ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceApplication.class, args);
    }

    @Bean
    public KieContainer kieContainer() {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        KieModuleModel kmodule = ks.newKieModuleModel();

        kmodule.newKieBaseModel("defaultKieBase")
                .setDefault(true)
                .setEventProcessingMode(EventProcessingOption.STREAM)
                .newKieSessionModel("nutritionKSession")
                .setDefault(true)
                .setClockType(ClockTypeOption.get("realtime"));

        kfs.writeKModuleXML(kmodule.toXML());

        // Load the main nutrition rules DRL from the kjar on the classpath
        InputStream staticRules = getClass().getResourceAsStream("/rules/nutrition-rules.drl");
        if (staticRules != null) {
            kfs.write("src/main/resources/rules/nutrition_rules.drl",
                    ks.getResources().newInputStreamResource(staticRules));
        } else {
            System.err.println("WARNING: /rules/nutrition-rules.drl not found on classpath — main rules will not be loaded!");
        }

        String calorieDrl = compileTemplate("/templates/calorie-balance.csv", "/templates/calorie-balance.drt");
        kfs.write("src/main/resources/rules/compiled_calorie_rules.drl", calorieDrl);

        String temporalDrl = compileTemplate("/templates/temporal-patterns.csv", "/templates/temporal-patterns.drt");
        if (temporalDrl != null && !temporalDrl.isBlank()) {
            kfs.write("src/main/resources/rules/compiled_temporal_rules.drl", temporalDrl);
        }

        KieBuilder kieBuilder = ks.newKieBuilder(kfs);
        kieBuilder.buildAll();

        KieModule kieModule = kieBuilder.getKieModule();
        return ks.newKieContainer(kieModule.getReleaseId());
    }
    
    private String compileTemplate(String csvPath, String drtPath) {
        List<Map<String, Object>> mappedData;
        try (InputStream csvStream = getClass().getResourceAsStream(csvPath)) {
            if (csvStream == null) {
                System.err.println("WARNING: Could not find CSV file: " + csvPath + " — skipping template.");
                return "";
            }
            mappedData = parseCsvToMap(csvStream);
        } catch (Exception e) {
            throw new RuntimeException("Error processing CSV data from " + csvPath, e);
        }

        // If no data rows, skip compilation — produces invalid DRL with empty template body
        if (mappedData.isEmpty()) {
            System.out.println("INFO: No data rows in " + csvPath + " — skipping template compilation.");
            return "";
        }

        String compiledDrl;
        try (InputStream drtStream = getClass().getResourceAsStream(drtPath)) {
            if (drtStream == null) {
                throw new IllegalArgumentException("Could not find DRT file: " + drtPath);
            }
            ObjectDataCompiler compiler = new ObjectDataCompiler();
            compiledDrl = compiler.compile(mappedData, drtStream);
        } catch (Exception e) {
            throw new RuntimeException("Error compiling template " + drtPath, e);
        }

        System.out.println("\n========================================================");
        System.out.println("SUCCESSFULLY GENERATED DRL FROM TEMPLATE: " + drtPath);
        System.out.println("========================================================");
        System.out.println(compiledDrl);
        System.out.println("========================================================\n");

        return compiledDrl;
    }


    private List<Map<String, Object>> parseCsvToMap(InputStream csvStream) {
        List<Map<String, Object>> data = new ArrayList<>();
        Scanner scanner = new Scanner(csvStream); 
        
        if (!scanner.hasNextLine()) return data;
        
        String[] headers = scanner.nextLine().split(",");
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            if (line.isEmpty() || line.startsWith(",,")) continue;
            
            String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
            Map<String, Object> row = new java.util.HashMap<>();
            for (int i = 0; i < headers.length; i++) {
                if (i < values.length) {
                    String val = values[i].trim();
                    
                    // ========================================================
                    // FIX: Strip Excel/CSV wrapper quotes if they exist
                    // ========================================================
                    if (val.startsWith("\"") && val.endsWith("\"") && val.length() > 1) {
                        val = val.substring(1, val.length() - 1).replace("\"\"", "\"").trim();
                    }
                    
                    row.put(headers[i].trim(), val);
                } else {
                    row.put(headers[i].trim(), "");
                }
            }
            data.add(row);
        }
        return data;
    }
}
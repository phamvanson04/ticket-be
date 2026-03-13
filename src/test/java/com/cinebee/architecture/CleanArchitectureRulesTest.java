package com.cinebee.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.core.importer.ImportOption;

@AnalyzeClasses(packages = "com.cinebee", importOptions = ImportOption.DoNotIncludeTests.class)
class CleanArchitectureRulesTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_outer_layers =
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..application..", "..presentation..", "..infrastructure..");

    @ArchTest
    static final ArchRule presentation_should_not_depend_on_infrastructure =
            noClasses()
                    .that().resideInAPackage("..presentation..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure..");

    @ArchTest
    static final ArchRule shared_should_not_depend_on_other_layers =
            noClasses()
                    .that().resideInAPackage("..shared..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..domain..", "..application..", "..presentation..", "..infrastructure..");
}

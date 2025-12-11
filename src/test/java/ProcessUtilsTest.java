import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.util.ProcessUtils;

public class ProcessUtilsTest {

    private Process creerProcessAvecAllocations() {
        // GIVEN : un processus simple
        Process p = new Process("P1", 0, 10, 4, 50, 1);

        // Et une exécution associée à un algo
        ExecutionInfo exec = new ExecutionInfo(0, 10, 4);
        p.addExecution("ALGO", exec);

        // Avec deux allocations CPU
        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 0, 5),
                new Allocation(p, "CPU2", 5, 10)
        ));
        return p;
    }

    @Test
    void testGetExecutions() {
        // GIVEN
        Process p = creerProcessAvecAllocations();

        // WHEN
        var executions = ProcessUtils.getExecutions(p);

        // THEN
        assertEquals(1, executions.size());
        assertTrue(executions.containsKey("ALGO"));
        assertEquals(0, executions.get("ALGO").getDateDebut());
    }

    @Test
    void testGetExecution() {
        // GIVEN
        Process p = creerProcessAvecAllocations();

        // WHEN
        ExecutionInfo exec = ProcessUtils.getExecution(p, "ALGO");

        // THEN
        assertNotNull(exec);
        assertEquals(10, exec.getDateFin());
    }

    @Test
    void testGetAllocations() {
        // GIVEN
        Process p = creerProcessAvecAllocations();

        // WHEN
        List<Allocation> allocations = ProcessUtils.getAllocations(p, "ALGO");

        // THEN
        assertEquals(2, allocations.size());
        assertEquals("CPU1", allocations.get(0).getProcessor());
        assertEquals("CPU2", allocations.get(1).getProcessor());
    }

    @Test
    void testGetNomAlgos() {
        // GIVEN
        Process p = creerProcessAvecAllocations();

        // WHEN
        List<String> algos = ProcessUtils.getNomAlgos(p);

        // THEN
        assertEquals(1, algos.size());
        assertEquals("ALGO", algos.get(0));
    }


    @Test
    void testGetAllCpus() {
        // GIVEN
        Process p1 = creerProcessAvecAllocations();
        Process p2 = new Process("P2", 0, 8, 2, 20, 1);
        p2.addExecution("ALGO", new ExecutionInfo(0, 8, 2));
        p2.setAllocations("ALGO", List.of(
                new Allocation(p2, "CPU3", 0, 4),
                new Allocation(p2, "CPU1", 4, 8)
        ));

        // WHEN
        List<String> cpus = ProcessUtils.getAllCpus(List.of(p1, p2), "ALGO");

        // THEN
        // Les CPU doivent être uniques et triés
        assertEquals(3, cpus.size());
        assertEquals(List.of("CPU1", "CPU2", "CPU3"), cpus);
    }

    @Test
    void testGetExecutionRenvoieMêmeObjet() {
        // GIVEN
        Process p = creerProcessAvecAllocations();
        ExecutionInfo exec = p.getExecutionInfo("ALGO");

        // WHEN
        ExecutionInfo result = ProcessUtils.getExecution(p, "ALGO");

        // THEN
        assertSame(exec, result, "La méthode doit renvoyer le même objet ExecutionInfo que celui stocké dans le Process");
    }

    @Test
    void testGetAllocationsRenvoieMêmeListe() {
        // GIVEN
        Process p = creerProcessAvecAllocations();
        List<Allocation> originalAllocations = p.getAllocations("ALGO");

        // WHEN
        List<Allocation> result = ProcessUtils.getAllocations(p, "ALGO");

        // THEN
        assertSame(originalAllocations, result, "La méthode doit renvoyer exactement la même liste d'allocations que celle du Process");
    }


    @Test
    void testGetExecutionsAvecExecution() {
        Process p = creerProcessAvecAllocations();

        // WHEN : récupération des exécutions
        var executions = ProcessUtils.getExecutions(p);

        // THEN : la map contient bien l'exécution
        assertEquals(1, executions.size(), "Doit contenir exactement 1 execution");
        assertTrue(executions.containsKey("ALGO"), "Doit contenir la clé ALGO");
        assertEquals(0, executions.get("ALGO").getDateDebut(), "Date de début correcte");
    }

    @Test
    void testGetExecutionsSansExecution() {
        Process p = new Process("P2", 0, 5, 2, 10, 1);

        // WHEN : récupération des exécutions
        var executions = ProcessUtils.getExecutions(p);

        // THEN : la map doit être vide
        assertNotNull(executions, "La map ne doit pas être null");
        assertTrue(executions.isEmpty(), "La map doit être vide pour un processus sans execution");
    }

    @Test
    void testGetExecutionPresent() {
        Process p = creerProcessAvecAllocations();

        // WHEN : récupération de l'exécution spécifique
        ExecutionInfo exec = ProcessUtils.getExecution(p, "ALGO");

        // THEN : l'objet doit être correct
        assertNotNull(exec, "Execution doit être présente");
        assertEquals(10, exec.getDateFin(), "Date de fin correcte");
    }

    @Test
    void testGetExecutionAbsent() {
        Process p = creerProcessAvecAllocations();

        // WHEN : récupération d'un algo inexistant
        ExecutionInfo exec = ProcessUtils.getExecution(p, "ALGO_INEXISTANT");

        // THEN : doit retourner null
        assertNull(exec, "Algo inexistant doit renvoyer null");
    }

    @Test
    void testGetAllocationsAvecAllocations() {
        Process p = creerProcessAvecAllocations();

        // WHEN : récupération des allocations
        List<Allocation> allocations = ProcessUtils.getAllocations(p, "ALGO");

        // THEN : vérification du contenu
        assertEquals(2, allocations.size(), "Doit contenir 2 allocations");
        assertEquals("CPU1", allocations.get(0).getProcessor(), "Premier CPU correct");
        assertEquals("CPU2", allocations.get(1).getProcessor(), "Second CPU correct");
    }

    @Test
    void testGetAllocationsSansAllocations() {
        Process p = new Process("P3", 0, 5, 2, 10, 1);

        // WHEN : récupération des allocations pour un algo inexistant
        List<Allocation> allocations = ProcessUtils.getAllocations(p, "ALGO");

        // THEN : liste vide mais non null
        assertNotNull(allocations, "Liste ne doit pas être null");
        assertTrue(allocations.isEmpty(), "Liste doit être vide si aucune allocation");
    }

    @Test
    void testGetNomAlgosAvecAlgo() {
        Process p = creerProcessAvecAllocations();

        // WHEN : récupération des noms d'algo
        List<String> algos = ProcessUtils.getNomAlgos(p);

        // THEN : vérification
        assertEquals(1, algos.size(), "Doit contenir 1 algo");
        assertEquals("ALGO", algos.get(0), "Nom de l'algo correct");
    }

    @Test
    void testGetNomAlgosSansAlgo() {
        Process p = new Process("P4", 0, 5, 2, 10, 1);

        // WHEN : récupération des noms d'algo
        List<String> algos = ProcessUtils.getNomAlgos(p);

        // THEN : liste vide
        assertNotNull(algos, "Liste ne doit pas être null");
        assertTrue(algos.isEmpty(), "Liste doit être vide si aucun algo");
    }

    @Test
    void testGetAllocationsPourPlusieursProcessus() {
        Process p1 = creerProcessAvecAllocations();
        Process p2 = new Process("P2", 5, 8, 2, 20, 1);
        p2.addExecution("ALGO", new ExecutionInfo(5, 13, 2));
        p2.setAllocations("ALGO", List.of(
                new Allocation(p2, "CPU1", 5, 9),
                new Allocation(p2, "CPU2", 9, 13)
        ));

        // WHEN : récupération des allocations pour plusieurs processus
        List<Allocation> allAllocations = ProcessUtils.getAllocations(List.of(p1, p2), "ALGO");

        // THEN : vérification
        assertEquals(4, allAllocations.size(), "Doit contenir toutes les allocations de tous les processus");
    }

    @Test
    void testGetAllocationsPourPlusieursProcessusContenu() {
        Process p1 = creerProcessAvecAllocations();
        Process p2 = new Process("P2", 5, 8, 2, 20, 1);
        p2.addExecution("ALGO", new ExecutionInfo(5, 13, 2));
        p2.setAllocations("ALGO", List.of(
                new Allocation(p2, "CPU1", 5, 9),
                new Allocation(p2, "CPU2", 9, 13)
        ));

        List<Allocation> allAllocations = ProcessUtils.getAllocations(List.of(p1, p2), "ALGO");

        assertEquals(4, allAllocations.size(), "Doit contenir toutes les allocations de tous les processus");

        // Vérifie le contenu exact
        assertEquals("CPU1", allAllocations.get(0).getProcessor());
        assertEquals(0, allAllocations.get(0).getDateDebutExecution());
        assertEquals(5, allAllocations.get(0).getDateFinExecution());

        assertEquals("CPU2", allAllocations.get(1).getProcessor());
        assertEquals(5, allAllocations.get(1).getDateDebutExecution());
        assertEquals(10, allAllocations.get(1).getDateFinExecution());

        assertEquals("CPU1", allAllocations.get(2).getProcessor());
        assertEquals(5, allAllocations.get(2).getDateDebutExecution());
        assertEquals(9, allAllocations.get(2).getDateFinExecution());

        assertEquals("CPU2", allAllocations.get(3).getProcessor());
        assertEquals(9, allAllocations.get(3).getDateDebutExecution());
        assertEquals(13, allAllocations.get(3).getDateFinExecution());
    }


    @Test
    void testGetAllocationsPourPlusieursProcessusSansAllocations() {
        Process p1 = new Process("P1", 0, 5, 2, 10, 1);
        Process p2 = new Process("P2", 0, 5, 2, 10, 1);

        // WHEN : récupération des allocations pour un algo inexistant
        List<Allocation> allAllocations = ProcessUtils.getAllocations(List.of(p1, p2), "ALGO");

        // THEN : liste vide
        assertNotNull(allAllocations, "Liste ne doit pas être null");
        assertTrue(allAllocations.isEmpty(), "Liste doit être vide si aucun processus n'a d'allocation");
    }

    @Test
    void testGetAllCpusAvecDoublons() {
        Process p1 = creerProcessAvecAllocations();
        Process p2 = new Process("P2", 0, 8, 2, 20, 1);
        p2.addExecution("ALGO", new ExecutionInfo(0, 8, 2));
        p2.setAllocations("ALGO", List.of(
                new Allocation(p2, "CPU3", 0, 4),
                new Allocation(p2, "CPU1", 4, 8)
        ));

        // WHEN : récupération des CPUs
        List<String> cpus = ProcessUtils.getAllCpus(List.of(p1, p2), "ALGO");

        // THEN : CPU uniques et triés
        assertEquals(3, cpus.size(), "Doit contenir 3 CPU uniques");
        assertEquals(List.of("CPU1", "CPU2", "CPU3"), cpus, "CPU triés correctement");
    }

    @Test
    void testGetAllCpusSansAllocations() {
        Process p1 = new Process("P1", 0, 5, 2, 10, 1);

        // WHEN : récupération des CPU pour algo inexistant
        List<String> cpus = ProcessUtils.getAllCpus(List.of(p1), "ALGO");

        // THEN : liste vide
        assertNotNull(cpus, "Liste ne doit pas être null");
        assertTrue(cpus.isEmpty(), "Liste doit être vide si aucun CPU n'est utilisé");
    }

    @Test
    void testGetAllCpusAvecAllocationsNulles() {
        // GIVEN : un processus avec un algo mais allocations null
        Process p = new Process("P1", 0, 10, 4, 50, 1);
        p.addExecution("ALGO", null); // exécution null
        p.setAllocations("ALGO", null); // allocations null

        // WHEN : récupération des CPU
        List<String> cpus = ProcessUtils.getAllCpus(List.of(p), "ALGO");

        // THEN : doit renvoyer liste vide et non null
        assertNotNull(cpus, "Liste ne doit pas être null");
        assertTrue(cpus.isEmpty(), "Liste doit être vide si les allocations sont null");
    }

    @Test
    void testGetNomAlgosAvecExecutionsNull() {
        // GIVEN : un processus sans exécution initialisée
        Process p = new Process("P2", 0, 5, 2, 20, 1);
    

        // WHEN : récupération des noms d'algo
        List<String> algos = ProcessUtils.getNomAlgos(p);

        // THEN : doit renvoyer liste vide mais non null
        assertNotNull(algos, "Liste ne doit pas être null même si aucune exécution");
        assertTrue(algos.isEmpty(), "Liste doit être vide si aucune exécution");
    }

    

}

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.ProcessValidator;

public class ProcessValidatorTest {

    private Process creerProcessSimple() {
        return new Process("P1", 0, 10, 4, 50, 1);
    }

    @Test
    void testProcessValide() {
        Process p = creerProcessSimple();
        p.addExecution("ALGO", new ExecutionInfo(0, 10, 4));
        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 0, 5),
                new Allocation(p, "CPU1", 5, 10)
        ));
        List<Process> liste = List.of(p);

        assertDoesNotThrow(() -> ProcessValidator.valider(liste));
    }

    @Test
    void testIdDoublon() {
        Process p1 = creerProcessSimple();
        Process p2 = creerProcessSimple(); // Même ID
        List<Process> liste = List.of(p1, p2);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testTempsExecutionNegatif() {
        Process p = new Process("P2", 0, 0, 4, 50, 1);
        List<Process> liste = List.of(p);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testRamNegative() {
        Process p = new Process("P3", 0, 10, 0, 50, 1);
        List<Process> liste = List.of(p);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testDeadlineAvantSoumission() {
        Process p = new Process("P4", 10, 10, 4, 5, 1);
        List<Process> liste = List.of(p);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testPriorityNegatif() {
        Process p = new Process("P5", 0, 10, 4, 50, -1);
        List<Process> liste = List.of(p);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testExecutionValidatorException() {
        Process p = creerProcessSimple();
        // Execution invalide : dateDebut > dateFin
        p.addExecution("ALGO", new ExecutionInfo(10, 5, 4));
        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 10, 5)
        ));
        List<Process> liste = List.of(p);

        assertThrows(FileParsingException.class, () -> ProcessValidator.valider(liste));
    }

    @Test
    void testListeVide() {
        List<Process> liste = new ArrayList<>();
        // Une liste vide ne doit rien lancer
        assertDoesNotThrow(() -> ProcessValidator.valider(liste));
    }
}

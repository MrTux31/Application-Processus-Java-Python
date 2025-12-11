

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

import com.ordonnancement.model.Allocation;
import com.ordonnancement.model.ExecutionInfo;
import com.ordonnancement.model.Process;
import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.ExecutionValidator;

public class ExecutionValidatorTest {

    private Process creerProcessSimple() {
        return new Process("P1", 0, 10, 4, 50, 1);
    }

    @Test
    void testExecutionValide() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 10, p.getRequiredRam()));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 0, 5),
                new Allocation(p, "CPU1", 5, 10)
        ));

        assertDoesNotThrow(() -> ExecutionValidator.valider(p));
    }


    @Test
    void testDateDebutSuperieureDateFin() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(10, 5, 0)); // Date début supérieure date fin

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testTempsExecutionInsuffisant() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 8, 0)); // manque 2 unités (8 != tps total : 10)

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testAllocationDebutAvantExecutionGlobale() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(5, 15, 0));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 3, 7) // ❌ 3 < 5
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testAllocationFinApresExecutionGlobale() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 10, 0));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 2, 15) // ❌ 15 > 10
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testAllocationDebutApresFinGlobale() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 10, 0));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 12, 14) // ❌ 12 > 10
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testAllocationFinAvantDebutGlobale() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(5, 15, 0));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 0, 3) // ❌ 3 < 5
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testAllocationDatesNegatives() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 10, 0));

        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", -1, 4) // ❌
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    @Test
    void testTempsTotalExecutionIncorrect() {
        Process p = creerProcessSimple();

        p.addExecution("ALGO", new ExecutionInfo(0, 10, 0));

        // Total = 5 + 1 = 6 (au lieu de 10)
        p.setAllocations("ALGO", List.of(
                new Allocation(p, "CPU1", 0, 5),
                new Allocation(p, "CPU1", 9, 10)
        ));

        assertThrows(FileParsingException.class, () -> ExecutionValidator.valider(p));
    }

    

    @Test
    void testAucunAlgoAucuneErreur() {
        Process p = creerProcessSimple();

        assertDoesNotThrow(() -> ExecutionValidator.valider(p));
    }

}

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ordonnancement.service.parser.FileParsingException;
import com.ordonnancement.service.validation.FileValidator;

public class FileValidatorTest {

    @TempDir
    Path tempDir; // Dossier temporaire fourni par JUnit

    @Test
    void testCheminNull() {
        assertThrows(FileParsingException.class, () -> FileValidator.verifierCheminFichier(null));
    }

    @Test
    void testCheminVide() {
        assertThrows(FileParsingException.class, () -> FileValidator.verifierCheminFichier(""));
        assertThrows(FileParsingException.class, () -> FileValidator.verifierCheminFichier("   "));
    }

    @Test
    void testFichierInexistant() {
        String chemin = tempDir.resolve("inexistant.txt").toString();
        assertThrows(FileParsingException.class, () -> FileValidator.verifierCheminFichier(chemin));
    }

    @Test
    void testCheminVersDossier() {
        String cheminDossier = tempDir.toString();
        assertThrows(FileParsingException.class, () -> FileValidator.verifierCheminFichier(cheminDossier));
    }

    @Test
    void testCheminValideFichier() throws IOException {
        Path fichier = Files.createFile(tempDir.resolve("fichier.txt"));
        assertDoesNotThrow(() -> FileValidator.verifierCheminFichier(fichier.toString()));
    }
}

package pi.stagepfesotetel.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pi.stagepfesotetel.entities.ONT;
import pi.stagepfesotetel.entities.Splitter;
import pi.stagepfesotetel.exceptions.ResourceNotFoundException;
import pi.stagepfesotetel.repositories.ONTRepository;
import pi.stagepfesotetel.repositories.SplitterRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @InjectMocks private InventoryService inventoryService;
    @Mock private ONTRepository ontRepository;
    @Mock private SplitterRepository splitterRepository;

    @Test
    void getOntById_existingId_returnsOptionalContainingOnt() {
        ONT ont = new ONT(); ont.setId(1L);
        when(ontRepository.findById(1L)).thenReturn(Optional.of(ont));
        Optional<ONT> result = inventoryService.getOntById(1L);
        assertTrue(result.isPresent(), "An existing ONT must be returned in an Optional");
        assertEquals(1L, result.orElseThrow().getId(), "The returned ONT ID must match the request");
    }

    @Test
    void createOnt_validOntWithExistingSplitter_savesOnt() {
        Splitter splitter = new Splitter(); splitter.setId(10L);
        ONT ont = new ONT(); ont.setSplitter(splitter); ont.setSerial("ONT123"); ont.setRxPower(-20.0); ont.setTxPower(1.5);
        when(splitterRepository.existsById(10L)).thenReturn(true);
        when(ontRepository.save(ont)).thenReturn(ont);
        assertEquals(ont, inventoryService.createOnt(ont), "A valid ONT must be saved and returned");
        verify(ontRepository).save(ont);
    }

    @Test
    void createOnt_missingSplitter_rejectsRequest() {
        ONT ont = new ONT(); ont.setSerial("ONT123"); ont.setRxPower(-20.0); ont.setTxPower(1.5);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> inventoryService.createOnt(ont),
                "Creating an ONT without a splitter must fail");
        assertEquals("Le splitter est obligatoire", exception.getMessage(), "The validation error must explain the missing splitter");
    }

    @Test
    void deleteOnt_missingId_throwsResourceNotFound() {
        when(ontRepository.existsById(99L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> inventoryService.deleteOnt(99L),
                "Deleting an unknown ONT must report a missing resource");
    }
}

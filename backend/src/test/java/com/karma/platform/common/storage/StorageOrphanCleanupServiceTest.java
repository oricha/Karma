package com.karma.platform.common.storage;

import com.karma.platform.config.KarmaStorageProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StorageOrphanCleanupServiceTest {

    @Mock
    private KarmaStorageProperties storageProperties;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private StorageReferenceService storageReferenceService;

    @InjectMocks
    private StorageOrphanCleanupService cleanupService;

    @Test
    void deletesOnlyUnreferencedUploadKeys() {
        when(storageProperties.enabled()).thenReturn(true);
        when(fileStorageService.listKeys("uploads/")).thenReturn(List.of("uploads/u1/a.jpg", "uploads/u1/b.jpg"));
        when(storageReferenceService.isKeyReferenced("uploads/u1/a.jpg")).thenReturn(true);
        when(storageReferenceService.isKeyReferenced("uploads/u1/b.jpg")).thenReturn(false);

        int deleted = cleanupService.deleteOrphanedUploads();

        assertEquals(1, deleted);
        verify(fileStorageService).delete("uploads/u1/b.jpg");
        verify(fileStorageService, never()).delete("uploads/u1/a.jpg");
    }

    @Test
    void skipsWhenStorageDisabled() {
        when(storageProperties.enabled()).thenReturn(false);

        assertEquals(0, cleanupService.deleteOrphanedUploads());

        verify(fileStorageService, never()).listKeys("uploads/");
    }
}

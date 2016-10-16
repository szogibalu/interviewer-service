package com.szogibalu.interviewer.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

public class FileSystemStorageServiceTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private StorageService systemUnderTest;

    private StorageProperties configuration = new StorageProperties();

    @Before
    public void init() {
        configuration.setLocation(temporaryFolder.getRoot().getPath());
        systemUnderTest = new FileSystemStorageService(configuration);
    }

    @Test
    public void shouldStoreFile() {
        String fileName = "test.txt";
        MultipartFile file = new MockMultipartFile("file", fileName, "text/plain", "Test Exercise".getBytes());

        systemUnderTest.store(file);

        File result = Paths.get(configuration.getLocation())
                .resolve(fileName)
                .toFile();
        assertThat(result.getName()).isEqualTo(fileName);
    }

    @Test(expected = StorageException.class)
    public void shouldNotStoreEmptyFile() {
        String fileName = "test.txt";
        byte[] content = {};
        MultipartFile file = new MockMultipartFile(fileName, content);

        systemUnderTest.store(file);

        fail();
    }

    @Test
    public void shouldLoadAllFileFromTheGivenFolder() throws IOException {
        temporaryFolder.newFile("test1.txt");
        temporaryFolder.newFile("test2.txt");

        Stream<Path> result = systemUnderTest.loadAll();

        assertThat(result.toArray()).hasSize(2);
    }

}

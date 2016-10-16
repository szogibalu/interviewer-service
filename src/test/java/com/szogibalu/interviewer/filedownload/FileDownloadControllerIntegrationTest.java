package com.szogibalu.interviewer.filedownload;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import com.szogibalu.interviewer.storage.StorageFileNotFoundException;
import com.szogibalu.interviewer.storage.StorageService;

@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileDownloadControllerIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private StorageService storageService;

    @Test
    @WithMockUser
    public void shouldListAllFiles() throws Exception {
        given(this.storageService.loadAll())
                .willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

        this.mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("files",
                        contains("http://localhost/files/first.txt", "http://localhost/files/second.txt")));
    }

    @Test
    @WithMockUser
    public void shouldGetBackErrorWhenMissingFile() throws Exception {
        given(this.storageService.loadAsResource("test.txt"))
                .willThrow(StorageFileNotFoundException.class);

        this.mvc.perform(get("/files/test.txt"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", is("File not found")));
    }

}

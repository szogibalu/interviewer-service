package com.szogibalu.interviewer.filedownload;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.szogibalu.interviewer.storage.StorageFileNotFoundException;
import com.szogibalu.interviewer.storage.StorageService;

@Controller()
public class FileDownloadController implements HandlerExceptionResolver {

    private final StorageService storageService;

    @Autowired
    public FileDownloadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping({ "/", "/files" })
    public String listUploadedFiles(Model model) throws IOException {
        model.addAttribute("files",
                storageService.loadAll()
                        .map(path -> MvcUriComponentsBuilder
                                .fromMethodName(FileDownloadController.class, "serveFile",
                                        path.getFileName().toString())
                                .build().toString())
                        .collect(toList()));
        return "files";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);

        return ResponseEntity.ok()
                .header(CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception exception) {
        ModelAndView modelAndView = new ModelAndView("files");
        if (exception instanceof StorageFileNotFoundException) {
            modelAndView.addObject("error", "File not found");
        } else {
            modelAndView.addObject("error", "Unexpected error");
        }
        return modelAndView;
    }

}

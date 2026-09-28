package com.logic.project.api;
import com.logic.project.domain.*; import com.logic.project.service.FileBoardService; import jakarta.servlet.http.HttpSession; import lombok.RequiredArgsConstructor; import org.springframework.core.io.Resource; import org.springframework.http.*; import org.springframework.web.bind.annotation.*; import org.springframework.web.multipart.MultipartFile; import java.nio.charset.StandardCharsets; import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
@CrossOrigin(origins={
        "http://localhost:5173",
        "http://127.0.0.1:5173",
        "http://localhost:80",
        "http://127.0.0.1:80"},
        allowCredentials="true")
public class FileBoardRestController {
    private final FileBoardService fs;

    @GetMapping
    ResponseEntity<?> list(HttpSession s){
        login(s);
        return ResponseEntity.ok(fs.findAll().stream().map(this::dto).toList());
    }

    @PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<?> upload(@RequestParam("title") String title,@RequestParam("file") MultipartFile file,HttpSession s){
        Member u=login(s);
        admin(u);
        return ResponseEntity.ok(dto(fs.upload(u.getId(),title,file)));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<?> delete(@PathVariable("id") Long id,HttpSession s){
        Member u=login(s);
        admin(u);
        fs.delete(u.getId(),id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    ResponseEntity<Resource> download(@PathVariable("id") Long id,HttpSession s){
        login(s);
        var d=fs.download(id);
        String ct=d.contentType()==null?MediaType.APPLICATION_OCTET_STREAM_VALUE:d.contentType();
        var cd=ContentDisposition.attachment().filename(d.originalFileName(),StandardCharsets.UTF_8).build();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).header(HttpHeaders.CONTENT_DISPOSITION,cd.toString()).body(d.resource());
    }

    private Map<String,Object> dto(FileBoard f){
        return Map.of("id",f.getId(),"title",f.getTitle(),"originalFileName",f.getOriginalFileName(),"contentType",f.getContentType()==null?"":f.getContentType(),"fileSize",f.getFileSize(),"downloadCount",f.getDownloadCount(),"createdAt",f.getCreatedAt(),"uploaderName",f.getUploader().getName());
    }

    private Member login(HttpSession s){
        Member m=(Member)s.getAttribute("loginMember");
        if(m==null)throw new IllegalStateException("로그인이 필요합니다.");return m;
    }

    private void admin(Member m){
        if(m.getRole()!=MemberRole.ADMIN) throw new SecurityException("관리자 권한이 필요합니다.");
    }
}

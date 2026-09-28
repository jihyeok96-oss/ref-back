package com.logic.project.service;

import com.logic.project.domain.*;
import com.logic.project.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FileBoardService {
    private final FileBoardRepository fr;
    private final MemberRepository mr;

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;   //static/uploads

    //자료실 목록
    public List<FileBoard> findAll(){
        return fr.findAllByOrderByCreatedAtDesc();
    }

    //자료실 상세보기
    public FileBoard findById(Long id){
        return fr.findById(id).orElseThrow(()->new IllegalArgumentException("자료를 찾을 수 없습니다."));
    }

    //자료 등록
    @Transactional
    public FileBoard upload(Long mid,String title,MultipartFile file) {
        Member m=get(mid);
        admin(m);
        if(title==null||title.isBlank()) throw new IllegalArgumentException("자료 제목은 필수입니다.");
        if(file==null||file.isEmpty()) throw new IllegalArgumentException("파일을 선택해주세요.");

        try{
            Path dir=Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String original=StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            if(original.isBlank()) throw new IllegalArgumentException("파일명이 올바르지 않습니다.");
            String stored=UUID.randomUUID()+"_"+original;Path target=dir.resolve(stored).normalize();
            if(!target.startsWith(dir)) throw new IllegalArgumentException("허용되지 않는 경로입니다.");
            Files.copy(file.getInputStream(),target,StandardCopyOption.REPLACE_EXISTING);
            return fr.save(FileBoard.builder().title(title.trim()).originalFileName(original).storedFileName(stored).contentType(file.getContentType()).fileSize(file.getSize()).uploader(m).build());
        } catch(IOException e) {
            throw new IllegalStateException("파일 저장 중 오류가 발생했습니다.",e);
        }
    }

    //자료 수정
    @Transactional
    public FileBoard update(Long mid, Long id, String title, MultipartFile file) {
        Member m = get(mid);
        admin(m); // 관리자 권한 확인

        FileBoard f = findById(id);

        // 1. 제목 수정 (입력값이 있을 때만 필수 체크 후 반영)
        if (title == null || title.isBlank()) throw new IllegalArgumentException("자료 제목은 필수입니다.");
        f.updateTitle(title.trim()); // domain 객체에 변경 감지(Dirty Checking) 메서드가 정의되어 있어야 합니다.

        // 2. 새 파일이 첨부된 경우에만 기존 파일 삭제 후 새 파일 업로드 프로세스 진행
        if (file != null && !file.isEmpty()) {
            try {
                Path dir = Paths.get(uploadDir).toAbsolutePath().normalize();

                // 2-1. 기존에 저장되어 있던 물리 파일 삭제
                Path oldFile = dir.resolve(f.getStoredFileName()).normalize();
                if (oldFile.startsWith(dir)) {
                    Files.deleteIfExists(oldFile);
                }

                // 2-2. 새 파일 물리 저장소에 복사
                String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
                if (original.isBlank()) throw new IllegalArgumentException("파일명이 올바르지 않습니다.");

                String stored = UUID.randomUUID() + "_" + original;
                Path target = dir.resolve(stored).normalize();

                if (!target.startsWith(dir)) throw new IllegalArgumentException("허용되지 않는 경로입니다.");
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                // 2-3. domain 엔티티 정보 갱신 (변경 감지 활용)
                f.updateFileInfo(original, stored, file.getContentType(), file.getSize());

            } catch (IOException e) {
                throw new IllegalStateException("파일 수정 중 오류가 발생했습니다.", e);
            }
        }

        return f; // 트랜잭션 종료 시 영속성 컨텍스트에 의해 DB에 자동 반영(Flush)됨
    }

    //자료 삭제
    @Transactional
    public void delete(Long mid,Long id){
        Member m=get(mid);
        admin(m);
        FileBoard f=findById(id);
        try{
            Path dir= Paths.get(uploadDir).toAbsolutePath().normalize();
            Path p=dir.resolve(f.getStoredFileName()).normalize();
            if(p.startsWith(dir)) Files.deleteIfExists(p);
            fr.delete(f);
        } catch(IOException e) {
            throw new IllegalStateException("파일 삭제 중 오류가 발생했습니다.",e);
        }
    }

    //추가 메서드
    //파일 다운로드
    @Transactional
    public DownloadFile download(Long id){
        FileBoard f=findById(id);
        try{
            Path dir=Paths.get(uploadDir).toAbsolutePath().normalize();
            Path p=dir.resolve(f.getStoredFileName()).normalize();
            if(!p.startsWith(dir)||!Files.exists(p)) throw new IllegalArgumentException("파일이 존재하지 않습니다.");
            f.increaseDownloadCount();
            return new DownloadFile(new UrlResource(p.toUri()),f.getOriginalFileName(),f.getContentType());
        } catch(MalformedURLException e) {
            throw new IllegalStateException("파일을 읽을 수 없습니다.",e);
        }
    }

    //회원 정보 불러오기
    private Member get(Long id){
        return mr.findById(id).orElseThrow(()->new IllegalArgumentException("회원을 찾을 수 없습니다."));
    }

    //관리자인지 확인
    private void admin(Member m){
        if(m.getRole() != MemberRole.ADMIN) throw new SecurityException("관리자 권한이 필요합니다.");
    }

    //파일 다운로드 레코드
    public record DownloadFile(Resource resource, String originalFileName, String contentType){ }
}

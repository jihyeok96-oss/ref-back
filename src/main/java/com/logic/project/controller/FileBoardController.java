package com.logic.project.controller;

import com.logic.project.domain.*; 
import com.logic.project.service.FileBoardService; 
import jakarta.servlet.http.HttpSession; 
import lombok.RequiredArgsConstructor; 
import org.springframework.core.io.Resource; 
import org.springframework.http.*; 
import org.springframework.stereotype.Controller; 
import org.springframework.ui.Model; 
import org.springframework.web.bind.annotation.*; 
import org.springframework.web.multipart.MultipartFile; 
import java.nio.charset.StandardCharsets;
@Controller 
@RequiredArgsConstructor 
@RequestMapping("/files") 
public class FileBoardController { 
	private final FileBoardService fs;

	@GetMapping 
	String list(HttpSession s,Model m){
		Member u=login(s);
		m.addAttribute("files",fs.findAll());
		m.addAttribute("isAdmin",u.getRole()==MemberRole.ADMIN);
		return "files/list";
	}

	@GetMapping("/new") 
	String form(HttpSession s){
		admin(login(s));return "files/form";
	}

	@PostMapping 
	String upload(@RequestParam String title,@RequestParam MultipartFile file,HttpSession s,Model m){
		Member u=login(s);
		try{
			admin(u);
			fs.upload(u.getId(),title,file);
			return "redirect:/files";
		} catch(RuntimeException e) {
			m.addAttribute("error",e.getMessage());return "files/form";
		}
	}

	@GetMapping("/{id}/download") 
	ResponseEntity<Resource> download(@PathVariable Long id,HttpSession s){
		login(s);
		var d=fs.download(id);
		String ct=d.contentType()==null?MediaType.APPLICATION_OCTET_STREAM_VALUE:d.contentType();
		var cd=ContentDisposition.attachment().filename(d.originalFileName(),StandardCharsets.UTF_8).build();
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(ct)).header(HttpHeaders.CONTENT_DISPOSITION,cd.toString()).body(d.resource());
	}

	@PostMapping("/{id}/delete") 
	String delete(@PathVariable Long id,HttpSession s){
		Member u=login(s);
		admin(u);
		fs.delete(u.getId(),id);
		return "redirect:/files";
	}

	private Member login(HttpSession s){
		Member m=(Member)s.getAttribute("loginMember");
		if(m==null)throw new IllegalStateException("로그인이 필요합니다.");
		return m;
	} 

	private void admin(Member m){
		if(m.getRole()!=MemberRole.ADMIN) throw new SecurityException("관리자 권한이 필요합니다.");
	}
}

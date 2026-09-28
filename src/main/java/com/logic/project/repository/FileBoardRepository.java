package com.logic.project.repository;

import com.logic.project.domain.FileBoard; 
import org.springframework.data.jpa.repository.JpaRepository; 
import java.util.List;

public interface FileBoardRepository extends JpaRepository<FileBoard,Long>{ 
	List<FileBoard> findAllByOrderByCreatedAtDesc(); 
}

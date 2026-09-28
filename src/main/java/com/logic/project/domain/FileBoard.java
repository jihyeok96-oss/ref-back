package com.logic.project.domain;

import jakarta.persistence.*; 
import lombok.*; 
import java.time.LocalDateTime;

@Entity
@Table(name="file_board") 
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class FileBoard {

	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY) 
	private Long id;

	@Column(length=200,nullable=false) 
	private String title;

	@Column(name="original_file_name",length=255,nullable=false) 
	private String originalFileName;
 
	@Column(name="stored_file_name",length=255,nullable=false,unique=true) 
	private String storedFileName;

	@Column(name="content_type",length=150) 
	private String contentType;

	@Column(name="file_size",nullable=false)
	private Long fileSize;

	@ManyToOne(fetch=FetchType.LAZY,optional=false) 
	@JoinColumn(name="uploader_id",nullable=false) 
	private Member uploader;

	@Column(name="download_count",nullable=false) 
	@Builder.Default 
	private Long downloadCount=0L;

	@Column(name="created_at",nullable=false) 
	@Builder.Default 
	private LocalDateTime createdAt=LocalDateTime.now();

	public void increaseDownloadCount(){
		downloadCount++;
	}

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateFileInfo(String originalFileName, String storedFileName, String contentType, long fileSize) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
    }
}

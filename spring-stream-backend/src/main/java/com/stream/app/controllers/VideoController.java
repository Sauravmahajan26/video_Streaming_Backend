package com.stream.app.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.aspectj.apache.bcel.classfile.Module.Require;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.stream.app.entities.Video;
import com.stream.app.payload.CustomMessage;
import com.stream.app.services.VideoService;

@RestController
@RequestMapping("/api/v1/videos")
@CrossOrigin("*")
public class VideoController {

	@Autowired
	private VideoService videoService;
	

	public VideoController(VideoService videoService) {
		
		this.videoService = videoService;
	}


//-----------------------------------------------------------------------------------------------
	//create video
	@PostMapping
	public ResponseEntity<?> create(
			@RequestParam("file")MultipartFile file,
			@RequestParam("title")String title,
			@RequestParam("description") String description 
			){
		
		Video video = new Video();
		video.setTitle(title);
		video.setDescription(description);
		video.setVideoId(UUID.randomUUID().toString());
		
		Video savedvideo = videoService.save(video,file);
		
		if(savedvideo != null) {
			return ResponseEntity.status(HttpStatus.OK).body(video);
		}else {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomMessage.builder()
																							.message("Video not uplaoded")
																							.success(false)
																							.build());
		}
		
	}
//-----------------------------------------------------------------------------------------------
	//get all videos
		@GetMapping
		public List<Video> getAll(){
			return videoService.getAll();
		}
	
//----------------------------------------------------------------------------------------------
	// stream video
	@GetMapping("/stream/{videoId}")
	public ResponseEntity<Resource> stream(
			@PathVariable String videoId){
		
		Video video = videoService.get(videoId);
		String contentType=video.getContentType();
		
		String filePath=video.getFilePath();
		Resource resourse  = new FileSystemResource(filePath);
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		
		
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resourse);
		
	}
	//-----------------------------------------------------------------------------------------------
	//stream video in chunks
	@GetMapping("/stream/range/{videoId}")
	public ResponseEntity<Resource> StreamVideoRange(
			@PathVariable String videoId,
			@RequestHeader(value="Range",required = false) String range
			){
		System.out.println(range);
		
		Video video = videoService.get(videoId);
		Path path = Paths.get(video.getFilePath());
		
		Resource resource = new FileSystemResource(path);
		
		String contentType = video.getContentType();
		
		if(contentType == null) {
			contentType = "application/octet-stream";
		}
		long filelength = path.toFile().length();
		 
		if(range == null) {
			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
		}
		
		long rangeStart;
		long rangeEnd;
		long chunkSize = 1 * 1024 * 1024; // 1 MB chunk size

		String[] ranges = range.replace("bytes=", "").split("-");

		rangeStart = Long.parseLong(ranges[0]);
		
		 if (ranges.length > 1) {
		        rangeEnd = Long.parseLong(ranges[1]);
		    } else {
		        rangeEnd = rangeStart + chunkSize - 1;
		    }

		    if (rangeEnd > filelength - 1) {
		        rangeEnd = filelength - 1;
		    }

		
		//kitna data bhejna hai
		InputStream inputStream;
		
		try {
			
			inputStream = Files.newInputStream(path);
			inputStream.skip(rangeStart);
			long contentLength = rangeEnd - rangeStart + 1;
			
			System.out.println("range start : "+rangeStart);
			System.out.println("range end : "+rangeEnd);
			
			byte[] data = new byte[(int)contentLength];
			int read = inputStream.read(data,0,data.length);
			System.out.println("read data : "+read);
	
		HttpHeaders httpHeader = new HttpHeaders();
		httpHeader.add("Content-Range", "bytes "+rangeStart+"-"+rangeEnd+"/"+filelength);
		httpHeader.add("Cache-Control","no-cache,no-store,must-revalidate");
		httpHeader.add("Pragma", "no-cache");
		httpHeader.add("Expires", "0");
		httpHeader.add("X-Content-Type-Optional", "nosniff");
		
		httpHeader.setContentLength(contentLength);
		
		return ResponseEntity
					.status(HttpStatus.PARTIAL_CONTENT)
					.headers(httpHeader)
					.contentType(MediaType.parseMediaType(contentType))
					.body(new ByteArrayResource(data));
		
		} catch (IOException ex) {
			// TODO: handle exception
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	
		}
	
	
}

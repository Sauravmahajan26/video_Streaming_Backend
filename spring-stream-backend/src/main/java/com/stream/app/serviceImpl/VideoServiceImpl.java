package com.stream.app.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.stream.app.entities.Video;
import com.stream.app.repositories.VideoRepository;
import com.stream.app.services.VideoService;

import jakarta.annotation.PostConstruct;

@Service
public class VideoServiceImpl implements VideoService{

	@Value("${files.video}")
	String DIR;
	
	@Value("${file.video.hsl}")
	String HSL_DIR;
	
	@Autowired
	private VideoRepository videorepo;
	
	public VideoServiceImpl(VideoRepository videorepo) {
		this.videorepo = videorepo;
	}

//------------------------------------------------------------------------------------------
	
	@PostConstruct
	public void init() {
	File file = new File(DIR);
	
	File file1 = new File(HSL_DIR);
	
	if(!file1.exists()) {
		file1.mkdir();
		System.out.println("Folder created");
	}else {
		System.out.println("Folder already created");
	}
	
	if(!file.exists()) {
		file.mkdir();
		System.out.println("Folder created");
	}else {
		System.out.println("Folder already created");
	}
	
	}
//----------------------------------------------------------------------------------------------
	
	@Override
	public Video save(Video video, MultipartFile file) {
		
		try {
		//original filename
		String filename = file.getOriginalFilename();
		System.out.println("filename"+filename);
		String contentType = file.getContentType();
		System.out.println("contentType"+contentType);
		InputStream inputStream = file.getInputStream();
		
		//folder path : create
		String cleanFileName =StringUtils.cleanPath(filename);
		System.out.println("cleanFileName"+cleanFileName);
		String cleanFolder = StringUtils.cleanPath(DIR);
		System.out.println("cleanFolder" +cleanFolder);
		
		//folder path with filename
		Path path = Paths.get(cleanFolder,cleanFileName);
		System.out.println(path);
		
		String normalizedPath = path.toString().replace("\\", "/");
		System.out.println("normalizedPath: " + normalizedPath);
		
		//copy file to the folder
		Files.copy(inputStream, path,StandardCopyOption.REPLACE_EXISTING);
		
		//video meta data 
		video.setContentType(contentType);
		video.setFilePath(normalizedPath);	
	
		processVideo(video.getVideoId());
		
		//delete video if any error occure while processing and delete database entry
		
		
		//meta data save to db
		return videorepo.save(video);
		
		}catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}
//-----------------------------------------------------------------------------------------------

	@Override
	public Video get(String videoId) {
		// TODO Auto-generated method stub
		
		Video video = videorepo.findById(videoId).orElseThrow(() -> new RuntimeException("Video not found"));
		
		return video;
	}
//-------------------------------------------------------------------------------------------------
	@Override
	public Video getByTitle(String title) {
		// TODO Auto-generated method stub
		return null;
	}
//-------------------------------------------------------------------------------------------------
	@Override
	public List<Video> getAll() {
		// TODO Auto-generated method stub]
		
		return videorepo.findAll();
	}
//-------------------------------------------------------------------------------------------------
	
//	public String processVideo(String videoId) {
//	    // Retrieve the video object
//	    Video video = this.get(videoId);
//	    if (video == null) {
//	        throw new IllegalArgumentException("Video with ID " + videoId + " not found");
//	    }
//
//	    // Retrieve the file path of the video
//	    String filePath = video.getFilePath();
//	    if (filePath == null || filePath.isEmpty()) {
//	        throw new IllegalArgumentException("Invalid file path for video ID " + videoId);
//	    }
//
//	    // Construct paths for different resolutions
//	    Path videoPath = Paths.get(filePath);
////	   
//	    try {
////	        
//	        Path outPutPath = Paths.get(HSL_DIR,videoId);
//	        
//	        Files.createDirectories(outPutPath);
//	        
//	        String ffmpegCmd = String.format("ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filname \"%s/segment_%%3d.ts\" \"%s/master.m3u8\" ",videoPath,outPutPath,outPutPath);
//	        
//	        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash","-c",ffmpegCmd);
//	        processBuilder.inheritIO();
//	        try {
//				Process process = ProcessBuilder.start();
//				int exit = process.waitFor();
//				if(exit != 0 ) {
//					throw new RuntimeException("Video process failed");
//				}
//			return videoId;
//	        
//	        
//	        
//	        
//	    } catch (IOException ex) {
//	        throw new RuntimeException("Failed to create directories for video processing: " + ex.getMessage(), ex);
//	    }
//	    } catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			new RuntimeException(e);
//		}
//
//
//	    return HSL_DIR + videoId;
//	}
	public String processVideo(String videoId) {
	    // Retrieve the video object
	    Video video = this.get(videoId);
	    if (video == null) {
	        throw new IllegalArgumentException("Video with ID " + videoId + " not found");
	    }

	    // Retrieve the file path of the video
	    String filePath = video.getFilePath();
	    if (filePath == null || filePath.isEmpty()) {
	        throw new IllegalArgumentException("Invalid file path for video ID " + videoId);
	    }

	    // Construct paths for different resolutions
	    Path videoPath = Paths.get(filePath);
	   
	    try {
	        Path outPutPath = Paths.get(HSL_DIR, videoId);
	        Files.createDirectories(outPutPath);

	        // Construct the ffmpeg command
	        String ffmpegCmd = String.format("ffmpeg -i \"%s\" -c:v libx264 -c:a aac -strict -2 -f hls -hls_time 10 -hls_list_size 0 -hls_segment_filename \"%s/segment_%%3d.ts\" \"%s/master.m3u8\"",
	                                         videoPath, outPutPath, outPutPath);

	        String[] command;
	        if (System.getProperty("os.name").toLowerCase().contains("win")) {
	            command = new String[]{"cmd.exe", "/c", ffmpegCmd};
	        } else {
	            command = new String[]{"/bin/bash", "-c", ffmpegCmd};
	        }
	        
	        // Initialize and start the process
	        ProcessBuilder processBuilder = new ProcessBuilder(command);
	        processBuilder.inheritIO();
	        Process process = processBuilder.start();
	        int exitCode = process.waitFor();
	        
	        if (exitCode != 0) {
	            throw new RuntimeException("Video processing failed with exit code " + exitCode);
	        }

	        return videoId;

	    } catch (IOException ex) {
	        throw new RuntimeException("Failed to create directories or start the video processing process: " + ex.getMessage(), ex);
	    } catch (InterruptedException ex) {
	        Thread.currentThread().interrupt(); // Restore interrupted state
	        throw new RuntimeException("Video processing was interrupted: " + ex.getMessage(), ex);
	    }
	}



}

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
	    String output360p = Paths.get(HSL_DIR, videoId, "360p").toString();
	    String output720p = Paths.get(HSL_DIR, videoId, "720p").toString();
	    String output1080p = Paths.get(HSL_DIR, videoId, "1080p").toString();

	    try {
	        Files.createDirectories(Paths.get(output360p));
	        Files.createDirectories(Paths.get(output720p));
	        Files.createDirectories(Paths.get(output1080p));
	    } catch (IOException ex) {
	        throw new RuntimeException("Failed to create directories for video processing: " + ex.getMessage(), ex);
	    }

	    // TODO: Continue processing the video, e.g., transcoding, splitting, etc.

	    // Return a meaningful result, e.g., the base path where the video was processed
	    return HSL_DIR + videoId;
	}


}

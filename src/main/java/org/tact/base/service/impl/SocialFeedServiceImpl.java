package org.tact.base.service.impl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.tact.base.domain.YoutubeFeed;
import org.tact.base.service.YoutubeService;



@Service
public class SocialFeedServiceImpl implements YoutubeService {
	
	/**
	 * 
	 * 	app_live.properties
	 	
	 	#youtube.playlistid=
		youtube.playlistid=
		youtube.apikey=
		youtube.feedurl=
	  
	 * 
	 */
	
	private static Logger _log = LoggerFactory.getLogger(YoutubeFeed.class);
	
	//@Value("${youtube.playlistid}")
	private String ytPlayListId = "PLqFAz7TQamvsQjY4Peo5nwm-f2HjL1jvc";

	//@Value("${youtube.feedurl}")
	private String ytFeedURL = "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=30&playlistId={playlistid}&key={apikey}";

	//@Value("${youtube.apikey}")
	private String ytApiKey = "AIzaSyBGNUsqZHibBgtzy9Brq7Y4FGbj0YH9Ilc";
	
	private static String YOUTUBE = "Youtube";
	
	private static String VIDEO_TYPE = "VIDEO";
	
	@Autowired 
	RestTemplate restTemplate;

	@Override
	public List<YoutubeFeed> getYoutubePosts() {
		
		List<YoutubeFeed> socialList = new LinkedList<YoutubeFeed>();
		
		try {

			DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("playlistid", ytPlayListId);
			map.put("apikey", ytApiKey);

			String strResponse = restTemplate.getForObject(ytFeedURL, String.class, map);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(strResponse, JsonNode.class);


			JsonNode itemsNode = rootNode.path("items");
			Iterator<JsonNode> ite = itemsNode.getElements();

			while (ite.hasNext()) {
				JsonNode item = ite.next();
				JsonNode snippet = item.path("snippet");


				if(snippet.path("resourceId").path("kind").getTextValue().equals("youtube#video")){

					String strVideoId = snippet.path("resourceId").path("videoId").getTextValue();

					YoutubeFeed sf = new YoutubeFeed();

					sf.setTitle(snippet.path("title").getTextValue());
					sf.setFeedId(strVideoId);
					sf.setLink("https://www.youtube.com/watch?v=" + strVideoId);
					sf.setSource(SocialFeedServiceImpl.YOUTUBE);
					sf.setMessage(snippet.path("description").getTextValue());
					sf.setSync_date(new Date());
					sf.setType(SocialFeedServiceImpl.VIDEO_TYPE);
					sf.setUploaded_date(dtf.parse(snippet.path("publishedAt").getTextValue()));
					
					String thumbnaiStandard = snippet.path("thumbnails").path("standard").path("url").getTextValue();
					if(thumbnaiStandard == null){
						thumbnaiStandard = snippet.path("thumbnails").path("high").path("url").getTextValue();
					}					
					sf.setThumbnail_link(thumbnaiStandard);					
					
					sf.setUser_id(snippet.path("channelTitle").getTextValue());
					
					socialList.add(sf);
				}				
			}
			_log.debug("{insertYoutubeFeeds} Completed insertYoutubeFeeds");
			
			return socialList;

		} catch (Exception e) {
			e.printStackTrace();
			_log.warn("{insertYoutubeFeeds} "+e.toString());
			
			return null;
		}		
	}
}

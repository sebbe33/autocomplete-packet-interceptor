package com.alexsebbe;

import java.util.List;

public interface WebFlowVectorReceivedListener {
	void onFailure(WebFlowVectorFetcher source);
	void onEmptyResultResponse(WebFlowVectorFetcher source, String searchString);
	void onNonEmptyResultResponse(WebFlowVectorFetcher source, List<WebFlow> webFlowVector, String searchString);
}

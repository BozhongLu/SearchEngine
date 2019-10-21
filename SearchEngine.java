import java.util.*;
import java.io.*;
//Bozhong Lu  260683363
// This class implements a google-like search engine
public class SearchEngine {

	public HashMap<String, LinkedList<String>> wordIndex; // this will contain a
															// set of pairs
															// (String,
															// LinkedList of
															// Strings)
	public DirectedGraph internet; // this is our internet graph

	// Constructor initializes everything to empty data structures
	// It also sets the location of the internet files
	SearchEngine() {
		// Below is the directory that contains all the internet files
		HtmlParsing.internetFilesLocation = "internetFiles";
		wordIndex = new HashMap<String, LinkedList<String>>();
		internet = new DirectedGraph();
	} // end of constructor//2017

	// Returns a String description of a searchEngine
	public String toString() {
		return "wordIndex:\n" + wordIndex + "\ninternet:\n" + internet;
	}

	// This does a graph traversal of the internet, starting at the given url.
	// For each new vertex seen, it updates the wordIndex, the internet graph,
	// and the set of visited vertices.

	void traverseInternet(String url) throws Exception {
		// do Breadth First Search in order to build the graph of the internet and list of words
		LinkedList<String> queue = new LinkedList<String>();
		//add the temporary url to the queue 
		queue.add(url);
		//add the temporary url as vertexies to the internet graph 
		internet.addVertex(url);

		while (!queue.isEmpty()) {
			//remove a url from the bottom of the queue and then serve it 
			String tempURL = queue.remove();
			LinkedList<String> wordList;
			LinkedList<String> neighborList;
			//create a LinkedList of String in order to store all the words in the temporary url
			wordList = HtmlParsing.getContent(tempURL);
			//create a LinkedList of String in order to store all the neighbors of the temporary url
			neighborList = HtmlParsing.getLinks(tempURL);

			// add the words found at tempURL to the word index
			for (String keyWords : wordList) {
				LinkedList<String> urlList = wordIndex.get(keyWords);
				//if this word never appeared before , then create a new urlList in order to store 
				//all the urls that contains it , in a LinkedList
				//after adding the temporary url to the urlList , put the keyWords and urlList in the wordIndex HashMap
				if (urlList == null) {
					urlList = new LinkedList<String>();
					urlList.add(tempURL);
					wordIndex.put(keyWords, urlList);
				} else if (!urlList.contains(tempURL)) {
					urlList.add(tempURL);
				}
				//if the urlList of this word doesn't contain tempURL , then add tempURL to the urlList 
			}
            //loop through all the links in the neighborList 
			for (String link : neighborList) {
				LinkedList<String> vertexList = internet.getVertices();
				//if the link is not contained in the internet graph
				//add the link as a new vertex in the internet graph 
				//then add the link to the queue in order to be served later
				if (!vertexList.contains(link)) {
					internet.addVertex(link);
					queue.add(link);
				}
				//if the link already exist in the internet graph as vertex
				//then add an edge between tempURL and the link
				internet.addEdge(tempURL, link);
			}
		}

		/*
		 * Hints 0) This should take about 50-70 lines of code (or less) 1) To
		 * parse the content of the url, call htmlParsing.getContent(url), which
		 * returns a LinkedList of Strings containing all the words at the given
		 * url. Also call htmlParsing.getLinks(url). and assign their results to
		 * a LinkedList of Strings. 2) To iterate over all elements of a
		 * LinkedList, use an Iterator, as described in the text of the
		 * assignment 3) Refer to the description of the LinkedList methods at
		 * http://docs.oracle.com/javase/6/docs/api/ . You will most likely need
		 * to use the methods contains(String s), addLast(String s), iterator()
		 * 4) Refer to the description of the HashMap methods at
		 * http://docs.oracle.com/javase/6/docs/api/ . You will most likely need
		 * to use the methods containsKey(String s), get(String s), put(String
		 * s, LinkedList l).
		 */

	} // end of traverseInternet

	/*
	 * This computes the pageRanks for every vertex in the internet graph. It
	 * will only be called after the internet graph has been constructed using
	 * traverseInternet. Use the iterative procedure described in the text of
	 * the assignment to compute the pageRanks for every vertices in the graph.
	 * 
	 * This method will probably fit in about 30 lines.
	 */
	void computePageRanks() {
		//set the maximum number of iterations to 100
		//set the threshold to 0.0001
		final int ITERATION = 100;
		final double THRESHOLD = 0.0001;
		
        //Firstly , set all the pageRanks to 1
		for (String page : internet.getVertices()) {
			internet.setPageRank(page, 1);
		}

		int iterations = 0;
		boolean keepGoing = true;

		// while the THRESHOLD and the maximum number of iterations haven't been reached 
		// keep running the loop 
		while (keepGoing == true && iterations < ITERATION) {
			HashMap<String, Double> newRanks = new HashMap<String, Double>();
			// loop through all the vertexies in the internet graph 
			for (String page : internet.getVertices()) {
				//double variable used to store the new pageRanks of the page
				double rankSum = 0;
				//loop through all the neighbors that has edges point into the current page 
				for (String neighbor : internet.getEdgesInto(page)) {
					//calculate the new pageRank based on the pageRank of the pages that points into the current page
					rankSum = rankSum + internet.getPageRank(neighbor) / internet.getOutDegree(neighbor);
				}
				//put the page with its new rank in the newRanks map
				newRanks.put(page, 0.5 + 0.5 * rankSum);

			}

			keepGoing = false;
			//loop through all the pages in the internet graph
			for (String page : internet.getVertices()) {
				//get both their oldRanks and newRanks 
				//if the difference between the oldRank and the newRank is greater than the THRESHOLD 
				//the the loop should keep going 
				double oldValue = internet.getPageRank(page);
				double newValue = newRanks.get(page);

				if (Math.abs(newValue - oldValue) >= THRESHOLD) {
					keepGoing = true;
				}
				
                // set the pages in the internet graph with the new ranks
				internet.setPageRank(page, newValue);

			}
			iterations++;
		}
	} // end of computePageRanks

	/*
	 * Returns the URL of the page with the high page-rank containing the query
	 * word Returns the String "" if no web site contains the query. This method
	 * can only be called after the computePageRanks method has been executed.
	 * Start by obtaining the list of URLs containing the query word. Then
	 * return the URL with the highest pageRank. This method should take about
	 * 25 lines of code.
	 */
	String getBestURL(String query) {
		String bestURL = "";
		Double bestRank = null;
		//get all the URLs that contains the query word , store them in a LinkedList 
		LinkedList<String> URLlist = wordIndex.get(query);
		if (URLlist != null) {
			//loop through all the URLs that contains the query word 
			//get the pageRank of them , and store the URL with the best pageRank 
			for (String url : URLlist) {
				Double rank = internet.getPageRank(url);
				if (bestRank == null || rank > bestRank) {
					bestRank = rank;
					bestURL = url;
				}

			}
		}
		//return the URL with the best pageRank
		return bestURL; // remove this
	} // end of getBestURL

	public static void main(String args[]) throws Exception {
		SearchEngine mySearchEngine = new SearchEngine();
		// to debug your program, start with.
		mySearchEngine.traverseInternet("http://www.cs.mcgill.ca/~blanchem/250/a.html");

		// When your program is working on the small example, move on to
		//mySearchEngine.traverseInternet("http://www.cs.mcgill.ca");

		// this is just for debugging purposes. REMOVE THIS BEFORE SUBMITTING
		System.out.println(mySearchEngine);

		mySearchEngine.computePageRanks();

		BufferedReader stndin = new BufferedReader(new InputStreamReader(System.in));
		String query;
		do {
			System.out.print("Enter query: ");
			query = stndin.readLine();
			if (query != null && query.length() > 0) {
				System.out.println("Best site = " + mySearchEngine.getBestURL(query));
			}
		} while (query != null && query.length() > 0);
	} // end of main
}

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ProjectTesterImp implements ProjectTester {
    SearchEngine searchEngine;

    @Override
    public void initialize() {
        searchEngine = new SearchEngine();
        searchEngine.clearDir("crawlData");
    }

    @Override
    public void crawl(String seedURL) {
        searchEngine.crawl(seedURL);
    }

    @Override
    public List<String> getOutgoingLinks(String url) {
        try {
            ArrayList<String> outgoingLinks = new ArrayList<>();
            int index = searchEngine.getIndexFromURL(url);
            File outgoingLinksFile = new File("crawlData/" + index + "/outgoingLinks.txt");
            Scanner scanner = new Scanner(outgoingLinksFile);
            while (scanner.hasNextLine()) {
                outgoingLinks.add(scanner.nextLine());
            }
            scanner.close();
            if (outgoingLinks.isEmpty()) {
                return null;
            }
            return outgoingLinks;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public List<String> getIncomingLinks(String url) {
        try {
            ArrayList<String> incomingLinks = new ArrayList<>();
            int index = searchEngine.getIndexFromURL(url);
            File incomingLinksFile = new File("crawlData/" + index + "/incomingLinks.txt");
            Scanner scanner = new Scanner(incomingLinksFile);
            while (scanner.hasNextLine()) {
                incomingLinks.add(scanner.nextLine());
            }
            scanner.close();
            if (incomingLinks.isEmpty()) {
                return null;
            }
            return incomingLinks;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    @Override
    public double getPageRank(String url) {
        File urlMappingFile = new File("crawlData/urlPagerankMapping.txt");
        try {
            Scanner scanner = new Scanner(urlMappingFile);
            while (scanner.hasNextLine()) {
                String[] urlMapping = scanner.nextLine().split(" ");
                if (urlMapping[2].equals(url)) {
                    scanner.close();
                    return Double.parseDouble(urlMapping[1]);
                }
            }
            scanner.close();
            return -1;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    @Override
    public double getIDF(String word) {
        File idfFile = new File("crawlData/idf/" + word + ".txt");
        try {
            Scanner scanner = new Scanner(idfFile);
            double idf = Double.parseDouble(scanner.nextLine());
            scanner.close();
            return idf;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }

    @Override
    public double getTF(String url, String word) {
        try {
            int index = searchEngine.getIndexFromURL(url);
            File tfFile = new File("crawlData/" + index + "/tf/" + word + ".txt");
            Scanner scanner = new Scanner(tfFile);
            double tf = Double.parseDouble(scanner.nextLine());
            scanner.close();
            return tf;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }

    @Override
    public double getTFIDF(String url, String word) {
        try {
            int index = searchEngine.getIndexFromURL(url);
            File tfidfFile = new File("crawlData/" + index + "/tfidf/" + word + ".txt");
            Scanner scanner = new Scanner(tfidfFile);
            double tfidf = Double.parseDouble(scanner.nextLine());
            scanner.close();
            return tfidf;
        } catch (FileNotFoundException e) {
            return 0;
        }
    }

    @Override
    public List<SearchResult> search(String query, boolean boost, int X) {
        ArrayList<String> urls = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        File urlMappingFile = new File("crawlData/urlPagerankMapping.txt");
        try {
            Scanner scanner = new Scanner(urlMappingFile);
            while (scanner.hasNextLine()) {
                String[] urlMapping = scanner.nextLine().split(" ", 4);
                urls.add(urlMapping[2]);
                titles.add(urlMapping[3]);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
        ArrayList<String> queryWords = new ArrayList<>();
        for (String word : query.split(" ")) {
            queryWords.add(word.toLowerCase());
        }
        HashMap<String, Integer> wordFreqs = new HashMap<>();
        for (String word : queryWords) {
            if (wordFreqs.containsKey(word)) {
                wordFreqs.put(word, wordFreqs.get(word) + 1);
            } else {
                wordFreqs.put(word, 1);
            }
        }
        ArrayList<Double> queryVector = new ArrayList<>();
        HashMap<String, ArrayList<Double>> crawlVectors = new HashMap<>();
        for (String word : wordFreqs.keySet()) {
            double tfQuery = (double) wordFreqs.get(word) / queryWords.size();
            double idfQuery = getIDF(word);
            if (idfQuery == 0) {
                continue;
            }
            double tfidfQuery = Math.log(1 + tfQuery) / Math.log(2) * idfQuery;
            queryVector.add(tfidfQuery);
            for (String url : urls) {
                double tfidfCrawl = getTFIDF(url, word);
                if (!crawlVectors.containsKey(url)) {
                    crawlVectors.put(url, new ArrayList<>());
                }
                crawlVectors.get(url).add(tfidfCrawl);
            }
        }
        LinkedHashMap<String, Double> scores = new LinkedHashMap<>();
        for (String url : crawlVectors.keySet()) {
            double numerator = 0;
            for (int i = 0; i < queryVector.size(); i++) {
                numerator += queryVector.get(i) * crawlVectors.get(url).get(i);
            }
            double leftDenominator = 0;
            for (double queryVectorElement : queryVector) {
                leftDenominator += Math.pow(queryVectorElement, 2);
            }
            double rightDenominator = 0;
            for (double crawlVectorElement : crawlVectors.get(url)) {
                rightDenominator += Math.pow(crawlVectorElement, 2);
            }
            double denominator = Math.sqrt(leftDenominator) * Math.sqrt(rightDenominator);
            double cosineSimilarity = 0;
            if (denominator != 0) {
                cosineSimilarity = numerator / denominator;
            }
            if (boost) {
                scores.put(url, cosineSimilarity * getPageRank(url));
            } else {
                scores.put(url, cosineSimilarity);
            }
        }
        ArrayList<Result> searchResults = new ArrayList<>();
        for (int i = 0; i < scores.keySet().size(); i++) {
            searchResults.add(new Result(titles.get(i), scores.get(urls.get(i))));
        }
        Collections.sort(searchResults);
        ArrayList<SearchResult> topXSearchResults = new ArrayList<>();
        if (searchResults.isEmpty()) {
            return topXSearchResults;
        }
        for (int i = 0; i < X; i++) {
            topXSearchResults.add(searchResults.get(i));
        }
        return topXSearchResults;
    }
}

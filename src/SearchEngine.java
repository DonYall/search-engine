import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class SearchEngine {
    public int getIndexFromURL(String url) {
        File urlMappingFile = new File("crawlData/urlPagerankMapping.txt");
        try {
            Scanner scanner = new Scanner(urlMappingFile);
            while (scanner.hasNextLine()) {
                String[] urlMapping = scanner.nextLine().split(" ");
                if (urlMapping[2].equals(url)) {
                    scanner.close();
                    return Integer.parseInt(urlMapping[0]);
                }
            }
            return -1;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public void clearDir(String directory) {
       File dir = new File(directory);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    clearDir(file.getAbsolutePath());
                }
            }
            dir.delete();
        }
    }

    public String getTitleFromHTML(String html) {
        int start = html.indexOf("<title>") + 7;
        int end = html.indexOf("</title>");
        return html.substring(start, end);
    }

    public HashSet<String> getURLsFromHTML(String baseURL, String html) {
        HashSet<String> urls = new HashSet<String>();
        int href = html.indexOf("href=\"");
        String startURL = baseURL.substring(0, baseURL.lastIndexOf("/") + 1);
        while (href != -1) {
            int start = href + 6;
            int end = html.indexOf("\"", start);
            String url = html.substring(start, end);
            if (url.startsWith("./")) {
                url = startURL + url.substring(2);
            }
            href = html.indexOf("href=\"", end);
            urls.add(url);
        }
        return urls;
    }

    private ArrayList<String> getWordsFromHTML(String html) {
        ArrayList<String> words = new ArrayList<>();
        int start = html.indexOf("<p>");
        int end = html.indexOf("</p>");
        while (start != -1 && end != -1) {
            words.addAll(List.of(html.substring(start + 3, end).trim().split("\\s+")));
            start = html.indexOf("<p>", end);
            end = html.indexOf("</p>", start);
        }
        return words;
    }

    private void addURLToQueue(String url, ArrayList<String> queue, LinkedHashSet<String> urlMap) {
        if (!urlMap.contains(url)) {
            queue.add(url);
            urlMap.add(url);
        }
    }

    private void storeTFs(ArrayList<String> words, ArrayList<HashMap<String, Integer>> wordFreqs, int index) {
        File tfDir = new File("crawlData/" + index + "/" + "tf");
        tfDir.mkdirs();
        HashMap<String, Integer> wordCounts = new HashMap<>();
        for (String word : words) {
            File tfFile = new File("crawlData/" + index + "/tf/" + word + ".txt");
            if (!tfFile.exists()) {
                try {
                    tfFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (wordCounts.containsKey(word)) {
                wordCounts.put(word, wordCounts.get(word) + 1);
            } else {
                wordCounts.put(word, 1);
            }
        }
        wordFreqs.add(wordCounts);

        for (String word : wordCounts.keySet()) {
            try {
                FileWriter writer = new FileWriter("crawlData/" + index + "/tf/" + word + ".txt");
                double tf = (double) wordCounts.get(word) / words.size();
                writer.write(tf + "\n");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double getIDF(String word, ArrayList<HashMap<String, Integer>> wordFreqs, int n) {
        int count = 0;
        for (HashMap<String, Integer> wordFreq : wordFreqs) {
            if (wordFreq.containsKey(word)) {
                count++;
            }
        }
        return Math.log((double) n / (1 + count)) / Math.log(2);
    }

    private void storeTFIDF(String word, double idf, int index) {
        File tfFile = new File("crawlData/" + index + "/tf/" + word + ".txt");
        try {
            Scanner scanner = new Scanner(tfFile);
            double tf = Double.parseDouble(scanner.nextLine());
            scanner.close();
            double tfidf = Math.log(tf + 1) * idf / Math.log(2);
            File tfidfFile = new File("crawlData/" + index + "/tfidf/" + word + ".txt");
            tfidfFile.createNewFile();
            FileWriter writer = new FileWriter("crawlData/" + index + "/tfidf/" + word + ".txt");
            writer.write(tfidf + "\n");
            writer.close();
            File idfFile = new File("crawlData/idf/" + word + ".txt");
            idfFile.createNewFile();
            writer = new FileWriter("crawlData/idf/" + word + ".txt");
            writer.write(idf + "\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int countOccurrences(double[] arr, double value) {
        int count = 0;
        for (double i : arr) {
            if (i == value) {
                count++;
            }
        }
        return count;
    }

    private List<Double> getPageRanks(HashSet<String> urlMap, int n) {
        double[][] a = new double[n][n];
        for (int i = 0; i < n; i++) {
            a[i][i] = 0;
        }

        for (int i = 0; i < n; i++) {
            File outgoingLinks = new File("crawlData/" + i + "/outgoingLinks.txt");
            try {
                Scanner scanner = new Scanner(outgoingLinks);
                while (scanner.hasNextLine()) {
                    int j = List.of(urlMap.toArray()).indexOf(scanner.nextLine());
                    a[i][j] = 1;
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < n; i++) {
            int count = countOccurrences(a[i], 1.0);
            for (int j = 0; j < n; j++) {
                if (count != 0) {
                    if (a[i][j] == 1) {
                        a[i][j] = (double) 1 / count;
                    }
                } else {
                    a[i][j] = (double) 1 / n;
                }
            }
        }
        a = Matmult.multScalar(a, 0.9);

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                a[i][j] += 0.1 / n;
            }
        }

        List<List<Double>> b = new ArrayList<>();
        ArrayList<Double> bb = new ArrayList<>();
        bb.add(1.0);
        for (int i = 0; i < n-1; i++) {
            bb.add(0.0);
        }
        b.add(bb);

        while (true) {
            List<List<Double>> c = Matmult.multMatrix(b, a);
            double dist = Matmult.euclideanDistance(b, c);
            b = c;
            if (dist <= 0.0001) {
                break;
            }
        }
        return b.get(0);
    }

    public int crawl(String seed) {
        try {
            File idfDir = new File("crawlData/idf");
            idfDir.mkdirs();
            LinkedHashSet<String> urlMap = new LinkedHashSet<>();
            LinkedHashSet<String> titleMap = new LinkedHashSet<>();
            ArrayList<String> urlQueue = new ArrayList<>();
            HashMap<String, HashSet<String>> incomingLinksMap = new HashMap<>();
            ArrayList<HashMap<String, Integer>> wordFreqs = new ArrayList<>();
            addURLToQueue(seed, urlQueue, urlMap);
            int i = 0;
            while (!urlQueue.isEmpty()) {
                File dir = new File("crawlData/" + i);
                File tfDir = new File("crawlData/" + i + "/tf");
                File tfidfDir = new File("crawlData/" + i + "/tfidf");
                dir.mkdir();
                tfDir.mkdir();
                tfidfDir.mkdir();
                String url = urlQueue.remove(0);
                String html = WebRequester.readURL(url);
                String title = getTitleFromHTML(html);
                titleMap.add(title);
                HashSet<String> outgoingUrls = getURLsFromHTML(url, html);
                ArrayList<String> words = getWordsFromHTML(html);
                FileWriter writer = new FileWriter("crawlData/" + i + "/outgoingLinks.txt", true);
                for (String outgoingUrl : outgoingUrls) {
                    writer.write(outgoingUrl + "\n");
                    addURLToQueue(outgoingUrl, urlQueue, urlMap);
                    if (incomingLinksMap.containsKey(outgoingUrl)) {
                        incomingLinksMap.get(outgoingUrl).add(url);
                    } else {
                        HashSet<String> incomingLinks = new HashSet<>();
                        incomingLinks.add(url);
                        incomingLinksMap.put(outgoingUrl, incomingLinks);
                    }
                }
                writer.close();
                writer = new FileWriter("crawlData/" + i + "/title.txt");
                writer.write(title + "\n");
                writer.close();
                storeTFs(words, wordFreqs, i);
                i++;
            }
            List<Double> pageRanks = getPageRanks(urlMap, urlMap.size());
            ArrayList<String> titles = new ArrayList<>(titleMap);

            i = 0;
            for (String url : urlMap) {
                for (String word : wordFreqs.get(i).keySet()) {
                    double idf = getIDF(word, wordFreqs, urlMap.size());
                    storeTFIDF(word, idf, i);
                }

                FileWriter writer = new FileWriter("crawlData/urlPagerankMapping.txt", true);
                writer.write(i + " " + pageRanks.get(i) + " " + url + " " + titles.get(i) + "\n");
                writer.close();

                if (incomingLinksMap.containsKey(url)) {
                    writer = new FileWriter("crawlData/" + i + "/incomingLinks.txt", true);
                    for (String incomingLink : incomingLinksMap.get(url)) {
                        writer.write(incomingLink + "\n");
                    }
                    writer.close();
                }
                i++;
            }
            return urlMap.size();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static void main(String[] args) throws IOException {
        SearchEngine engine = new SearchEngine();
        String url = "https://people.scs.carleton.ca/~davidmckenney/tinyfruits/N-0.html";
        engine.clearDir("crawlData");
        int n = engine.crawl(url);
        System.out.println(n);
    }
}